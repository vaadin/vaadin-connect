/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.connect.plugin.generator;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.connect.VaadinService;
import com.vaadin.connect.VaadinServiceNameChecker;
import com.vaadin.connect.auth.AnonymousAllowed;

/**
 * Java parser class which scans for all {@link VaadinService} classes and
 * produces OpenApi json.
 */
public class OpenApiObjectGenerator {
  public static final String EXTENSION_VAADIN_CONNECT_PARAMETERS_DESCRIPTION = "x-vaadin-parameters-description";

  private static final String VAADIN_CONNECT_OAUTH2_SECURITY_SCHEME = "vaadin-connect-oauth2";
  private static final String VAADIN_CONNECT_OAUTH2_TOKEN_URL = "/oauth/token";

  private List<Path> javaSourcePaths = new ArrayList<>();
  private OpenApiConfiguration configuration;
  private Map<String, ResolvedReferenceType> usedSchemas;
  private Map<String, String> servicesJavadoc;
  private Map<String, Schema> nonServiceSchemas;
  private Map<String, PathItem> pathItems;
  private OpenAPI openApiModel;
  private final VaadinServiceNameChecker serviceNameChecker = new VaadinServiceNameChecker();
  private ClassLoader typeResolverClassLoader;

  /**
   * Adds the source path to the generator to process.
   *
   * @param sourcePath
   *          the source path to generate the medatata from
   */
  public void addSourcePath(Path sourcePath) {
    if (sourcePath == null) {
      throw new IllegalArgumentException(
          "Java source path must be a valid directory");
    }
    if (!sourcePath.toFile().exists()) {
      throw new IllegalArgumentException(
          String.format("Java source path '%s' doesn't exist", sourcePath));
    }
    this.javaSourcePaths.add(sourcePath);
  }

  /**
   * Set project's class loader which is used for resolving types from that
   * project.
   *
   * @param typeResolverClassLoader
   *          the project's class loader for type resolving
   */
  void setTypeResolverClassLoader(ClassLoader typeResolverClassLoader) {
    this.typeResolverClassLoader = typeResolverClassLoader;
  }

  /**
   * Sets the configuration to be used when generating an Open API spec.
   *
   * @param configuration
   *          the generator configuration
   */
  public void setOpenApiConfiguration(OpenApiConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Gets the Open API, generates it if necessary.
   *
   * @return the Open API data
   */
  public OpenAPI getOpenApi() {
    if (openApiModel == null) {
      init();
    }
    return openApiModel;
  }

  OpenAPI generateOpenApi() {
    init();
    return openApiModel;
  }

  private void init() {
    if (javaSourcePaths == null || configuration == null) {
      throw new IllegalStateException(
          "Java source path and configuration should not be null");
    }
    openApiModel = createBasicModel();
    nonServiceSchemas = new HashMap<>();
    pathItems = new TreeMap<>();
    usedSchemas = new HashMap<>();
    servicesJavadoc = new HashMap<>();

    ParserConfiguration parserConfiguration = createParserConfiguration();

    javaSourcePaths.stream()
        .map(path -> new SourceRoot(path, parserConfiguration))
        .forEach(this::parseSourceRoot);

    for (Map.Entry<String, ResolvedReferenceType> entry : usedSchemas
        .entrySet()) {
      Schema schema = nonServiceSchemas.get(entry.getKey());
      if (schema == null) {
        schema = parseReferencedTypeAsSchema(entry.getValue());
      }
      openApiModel.getComponents().addSchemas(entry.getKey(), schema);
    }

    addTagsInformation();
  }

  private ParserConfiguration createParserConfiguration() {
    CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(
        new ReflectionTypeSolver(false));
    if (typeResolverClassLoader != null) {
      combinedTypeSolver
          .add(new ClassLoaderTypeSolver(typeResolverClassLoader));
    }
    return new ParserConfiguration()
        .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
  }

  private void parseSourceRoot(SourceRoot sourceRoot) {
    try {
      sourceRoot.parse("", this::process);
    } catch (Exception e) {
      getLogger().error(e.getMessage(), e);
      throw new IllegalStateException(String.format(
          "Can't parse the java files in the source root '%s'", sourceRoot), e);
    }
  }

  private void addTagsInformation() {
    for (Map.Entry<String, String> serviceJavadoc : servicesJavadoc
        .entrySet()) {
      Tag tag = new Tag();
      tag.name(serviceJavadoc.getKey());
      tag.description(serviceJavadoc.getValue());
      openApiModel.addTagsItem(tag);
    }
  }

  private OpenAPI createBasicModel() {
    OpenAPI openAPI = new OpenAPI();

    Info info = new Info();
    info.setTitle(configuration.getApplicationTitle());
    info.setVersion(configuration.getApplicationApiVersion());
    openAPI.setInfo(info);

    Paths paths = new Paths();
    openAPI.setPaths(paths);

    Server server = new Server();
    server.setUrl(configuration.getServerUrl());
    server.setDescription(configuration.getServerDescription());
    openAPI.setServers(Collections.singletonList(server));
    Components components = new Components();
    SecurityScheme vaadinConnectOAuth2Scheme = new SecurityScheme()
        .type(SecurityScheme.Type.OAUTH2)
        .flows(new OAuthFlows().password(new OAuthFlow()
            .tokenUrl(VAADIN_CONNECT_OAUTH2_TOKEN_URL).scopes(new Scopes())));
    components.addSecuritySchemes(VAADIN_CONNECT_OAUTH2_SECURITY_SCHEME,
        vaadinConnectOAuth2Scheme);
    openAPI.components(components);
    return openAPI;
  }

  @SuppressWarnings("squid:S1172")
  private SourceRoot.Callback.Result process(Path localPath, Path absolutePath,
      ParseResult<CompilationUnit> result) {
    result.ifSuccessful(compilationUnit -> compilationUnit.getPrimaryType()
        .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
        .map(BodyDeclaration::asClassOrInterfaceDeclaration)
        .map(this::appendNestedClasses).orElse(Collections.emptyList())
        .forEach(this::parseClass));
    pathItems.forEach((pathName, pathItem) -> openApiModel.getPaths()
        .addPathItem(pathName, pathItem));
    return SourceRoot.Callback.Result.DONT_SAVE;
  }

  private Collection<ClassOrInterfaceDeclaration> appendNestedClasses(
      ClassOrInterfaceDeclaration topLevelClass) {
    Set<ClassOrInterfaceDeclaration> nestedClasses = topLevelClass.getMembers()
        .stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration)
        .map(BodyDeclaration::asClassOrInterfaceDeclaration)
        .collect(Collectors.toCollection(() -> new TreeSet<>(
            Comparator.comparing(NodeWithSimpleName::getNameAsString))));
    nestedClasses.add(topLevelClass);
    return nestedClasses;
  }

  private void parseClass(ClassOrInterfaceDeclaration classDeclaration) {
    Optional<AnnotationExpr> serviceAnnotation = classDeclaration
        .getAnnotationByClass(VaadinService.class);
    if (!serviceAnnotation.isPresent()) {
      nonServiceSchemas.put(classDeclaration.resolve().getQualifiedName(),
          parseClassAsSchema(classDeclaration));
    } else {
      classDeclaration.getJavadoc().ifPresent(
          javadoc -> servicesJavadoc.put(classDeclaration.getNameAsString(),
              javadoc.getDescription().toText()));

      pathItems.putAll(createPathItems(
          getServiceName(classDeclaration, serviceAnnotation.get()),
          classDeclaration));
    }
  }

  private String getServiceName(ClassOrInterfaceDeclaration classDeclaration,
      AnnotationExpr serviceAnnotation) {
    String serviceName = Optional.ofNullable(serviceAnnotation)
        .filter(Expression::isSingleMemberAnnotationExpr)
        .map(Expression::asSingleMemberAnnotationExpr)
        .map(SingleMemberAnnotationExpr::getMemberValue)
        .map(Expression::asStringLiteralExpr)
        .map(LiteralStringValueExpr::getValue).filter(StringUtils::isNotBlank)
        .orElse(classDeclaration.getNameAsString());

    String validationError = serviceNameChecker.check(serviceName);
    if (validationError != null) {
      throw new IllegalStateException(
          String.format("Service name '%s' is invalid, reason: '%s'",
              serviceName, validationError));
    }
    return serviceName;
  }

  private Schema parseClassAsSchema(
      TypeDeclaration<ClassOrInterfaceDeclaration> typeDeclaration) {
    Schema schema = new ObjectSchema();
    typeDeclaration.getJavadoc().ifPresent(
        javadoc -> schema.description(javadoc.getDescription().toText()));
    for (FieldDeclaration field : typeDeclaration.getFields()) {
      if (field.isTransient() || field.isStatic()
          || field.isAnnotationPresent(JsonIgnore.class)) {
        continue;
      }
      field.getVariables()
          .forEach(variableDeclarator -> schema.addProperties(
              variableDeclarator.getNameAsString(),
              parseTypeToSchema(variableDeclarator.getType())));
    }
    return schema;
  }

  private boolean isReservedWord(String word) {
    return word != null && VaadinServiceNameChecker.ECMA_SCRIPT_RESERVED_WORDS
        .contains(word.toLowerCase());
  }

  private Map<String, PathItem> createPathItems(String serviceName,
      ClassOrInterfaceDeclaration typeDeclaration) {
    Map<String, PathItem> newPathItems = new HashMap<>();
    for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
      if (isAccessForbidden(typeDeclaration, methodDeclaration)) {
        continue;
      }
      String methodName = methodDeclaration.getNameAsString();

      if (isReservedWord(methodName)) {
        throw new IllegalStateException("The method name '" + methodName
            + "' in the service class '" + typeDeclaration.getNameAsString()
            + "' is a JavaScript reserved word");
      }

      Operation post = createPostOperation(methodDeclaration,
          requiresAuthentication(typeDeclaration, methodDeclaration));

      if (methodDeclaration.getParameters().isNonEmpty()) {
        post.setRequestBody(createRequestBody(methodDeclaration));
      }

      ApiResponses responses = createApiResponses(methodDeclaration);
      post.setResponses(responses);
      post.tags(Collections.singletonList(typeDeclaration.getNameAsString()));
      PathItem pathItem = new PathItem().post(post);

      String pathName = "/" + serviceName + "/" + methodName;
      pathItem.readOperationsMap()
          .forEach((httpMethod, operation) -> operation.setOperationId(
              String.join("_", serviceName, methodName, httpMethod.name())));
      newPathItems.put(pathName, pathItem);
    }
    return newPathItems;
  }

  private boolean isAccessForbidden(ClassOrInterfaceDeclaration typeDeclaration,
      MethodDeclaration methodDeclaration) {
    return !methodDeclaration.isPublic()
        || (hasSecurityAnnotation(methodDeclaration)
            ? methodDeclaration.isAnnotationPresent(DenyAll.class)
            : typeDeclaration.isAnnotationPresent(DenyAll.class));
  }

  private boolean requiresAuthentication(
      ClassOrInterfaceDeclaration typeDeclaration,
      MethodDeclaration methodDeclaration) {
    if (hasSecurityAnnotation(methodDeclaration)) {
      return !methodDeclaration.isAnnotationPresent(AnonymousAllowed.class);
    } else {
      return !typeDeclaration.isAnnotationPresent(AnonymousAllowed.class);
    }
  }

  private boolean hasSecurityAnnotation(MethodDeclaration method) {
    return method.isAnnotationPresent(AnonymousAllowed.class)
        || method.isAnnotationPresent(PermitAll.class)
        || method.isAnnotationPresent(DenyAll.class)
        || method.isAnnotationPresent(RolesAllowed.class);
  }

  private Operation createPostOperation(MethodDeclaration methodDeclaration,
      boolean requiresAuthentication) {
    Operation post = new Operation();
    if (requiresAuthentication) {
      SecurityRequirement securityItem = new SecurityRequirement();
      securityItem.addList(VAADIN_CONNECT_OAUTH2_SECURITY_SCHEME);
      post.addSecurityItem(securityItem);
    }

    methodDeclaration.getJavadoc().ifPresent(
        javadoc -> post.setDescription(javadoc.getDescription().toText()));
    return post;
  }

  private ApiResponses createApiResponses(MethodDeclaration methodDeclaration) {
    ApiResponse successfulResponse = createApiSuccessfulResponse(
        methodDeclaration);
    ApiResponses responses = new ApiResponses();
    responses.addApiResponse("200", successfulResponse);
    return responses;
  }

  private ApiResponse createApiSuccessfulResponse(
      MethodDeclaration methodDeclaration) {
    Content successfulContent = new Content();
    // "description" is a REQUIRED property of Response
    ApiResponse successfulResponse = new ApiResponse().description("");
    methodDeclaration.getJavadoc().ifPresent(javadoc -> {
      for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
        if (blockTag.getType() == JavadocBlockTag.Type.RETURN) {
          successfulResponse
              .setDescription("Return " + blockTag.getContent().toText());
        }
      }
    });
    if (!methodDeclaration.getType().isVoidType()) {
      MediaType mediaItem = createReturnMediaType(methodDeclaration);
      successfulContent.addMediaType("application/json", mediaItem);
      successfulResponse.content(successfulContent);
    }
    return successfulResponse;
  }

  private MediaType createReturnMediaType(MethodDeclaration methodDeclaration) {
    MediaType mediaItem = new MediaType();
    Type methodReturnType = methodDeclaration.getType();
    mediaItem.schema(parseTypeToSchema(methodReturnType));
    return mediaItem;
  }

  private RequestBody createRequestBody(MethodDeclaration methodDeclaration) {
    Map<String, String> paramsDescription = new HashMap<>();
    methodDeclaration.getJavadoc().ifPresent(javadoc -> {
      for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
        if (blockTag.getType() == JavadocBlockTag.Type.PARAM) {
          paramsDescription.put(blockTag.getName().orElse(""),
              blockTag.getContent().toText());
        }
      }
    });

    RequestBody requestBody = new RequestBody();
    Content requestBodyContent = new Content();
    requestBody.content(requestBodyContent);
    MediaType requestBodyObject = new MediaType();
    requestBodyContent.addMediaType("application/json", requestBodyObject);
    Schema requestSchema = new ObjectSchema();
    requestBodyObject.schema(requestSchema);
    methodDeclaration.getParameters().forEach(parameter -> {
      Schema paramSchema = parseTypeToSchema(parameter.getType());

      String name = (isReservedWord(parameter.getNameAsString()) ? "_" : "")
          .concat(parameter.getNameAsString());
      if (StringUtils.isBlank(paramSchema.get$ref())) {
        paramSchema
            .description(paramsDescription.remove(parameter.getNameAsString()));
      }
      requestSchema.addProperties(name, paramSchema);
    });
    if (!paramsDescription.isEmpty()) {
      requestSchema.addExtension(
          EXTENSION_VAADIN_CONNECT_PARAMETERS_DESCRIPTION,
          new LinkedHashMap<>(paramsDescription));
    }
    return requestBody;
  }

  private Schema parseTypeToSchema(Type javaType) {
    try {
      return parseResolvedTypeToSchema(javaType.resolve());
    } catch (Exception e) {
      getLogger().info(String.format(
          "Can't resolve type '%s' for creating custom OpenAPI Schema. Using the default ObjectSchema instead.",
          javaType.asString()), e);
    }
    return new ObjectSchema();
  }

  private static Logger getLogger() {
    return LoggerFactory.getLogger(OpenApiObjectGenerator.class);
  }

  private Schema parseResolvedTypeToSchema(ResolvedType resolvedType) {
    if (resolvedType.isArray()) {
      return createArraySchema(resolvedType);
    }
    if (isNumberType(resolvedType)) {
      return new NumberSchema();
    } else if (isStringType(resolvedType)) {
      return new StringSchema();
    } else if (isCollectionType(resolvedType)) {
      return createCollectionSchema(resolvedType.asReferenceType());
    } else if (isBooleanType(resolvedType)) {
      return new BooleanSchema();
    } else if (isMapType(resolvedType)) {
      return createMapSchema(resolvedType);
    } else if (isDateType(resolvedType)) {
      return new DateSchema();
    } else if (isDateTimeType(resolvedType)) {
      return new DateTimeSchema();
    } else if (isUnhandledJavaType(resolvedType)) {
      return new ObjectSchema();
    }
    return createUserBeanSchema(resolvedType);
  }

  private boolean isUnhandledJavaType(ResolvedType resolvedType) {
    return resolvedType.isReferenceType() && resolvedType.asReferenceType()
        .getQualifiedName().startsWith("java.");
  }

  private boolean isDateTimeType(ResolvedType resolvedType) {
    return resolvedType.isReferenceType() && isTypeOf(
        resolvedType.asReferenceType(), LocalDate.class, Instant.class);
  }

  private boolean isDateType(ResolvedType resolvedType) {
    return resolvedType.isReferenceType() && isTypeOf(
        resolvedType.asReferenceType(), Date.class, LocalDate.class);
  }

  private boolean isNumberType(ResolvedType type) {
    if (type.isPrimitive()) {
      ResolvedPrimitiveType resolvedPrimitiveType = type.asPrimitive();
      return resolvedPrimitiveType != ResolvedPrimitiveType.BOOLEAN
          && resolvedPrimitiveType != ResolvedPrimitiveType.CHAR;
    } else {
      return isTypeOf(type.asReferenceType(), Number.class);
    }
  }

  private boolean isCollectionType(ResolvedType type) {
    return !type.isPrimitive()
        && isTypeOf(type.asReferenceType(), Collection.class);
  }

  private boolean isMapType(ResolvedType type) {
    return !type.isPrimitive() && isTypeOf(type.asReferenceType(), Map.class);
  }

  private boolean isBooleanType(ResolvedType type) {
    if (type.isPrimitive()) {
      return type == ResolvedPrimitiveType.BOOLEAN;
    } else {
      return isTypeOf(type.asReferenceType(), Boolean.class);
    }
  }

  private boolean isStringType(ResolvedType type) {
    if (type.isPrimitive()) {
      return type.asPrimitive() == ResolvedPrimitiveType.CHAR;
    } else {
      return isTypeOf(type.asReferenceType(), String.class, Character.class);
    }
  }

  private boolean isTypeOf(ResolvedReferenceType type, Class... clazz) {
    List<String> classes = Arrays.stream(clazz).map(Class::getName)
        .collect(Collectors.toList());
    return classes.contains(type.getQualifiedName()) || type.getAllAncestors()
        .stream().map(ResolvedReferenceType::getQualifiedName)
        .anyMatch(classes::contains);
  }

  private Schema createUserBeanSchema(ResolvedType resolvedType) {
    if (resolvedType.isReferenceType()) {
      String qualifiedName = resolvedType.asReferenceType().getQualifiedName();
      usedSchemas.put(qualifiedName, resolvedType.asReferenceType());

      String ref = "#/components/schemas/" + qualifiedName;
      return new ObjectSchema().$ref(ref);
    }
    return new ObjectSchema();
  }

  private Schema parseReferencedTypeAsSchema(
      ResolvedReferenceType resolvedType) {
    Schema schema = new ObjectSchema();
    Set<String> validFields = getValidFields(resolvedType);
    Set<ResolvedFieldDeclaration> declaredFields = resolvedType
        .getDeclaredFields().stream()
        .filter(resolvedFieldDeclaration -> validFields
            .contains(resolvedFieldDeclaration.getName()))
        .collect(Collectors.toSet());
    // Make sure the order is consistent in properties map
    schema.setProperties(new TreeMap<>());
    for (ResolvedFieldDeclaration resolvedFieldDeclaration : declaredFields) {
      ResolvedFieldDeclaration fieldDeclaration = resolvedFieldDeclaration
          .asField();
      String name = fieldDeclaration.getName();
      Schema type = parseResolvedTypeToSchema(fieldDeclaration.getType());
      schema.addProperties(name, type);
    }
    return schema;
  }

  private Set<String> getValidFields(ResolvedReferenceType resolvedType) {
    Set<String> fields;
    try {
      Class<?> aClass = Class.forName(resolvedType.getQualifiedName());
      fields = Arrays.stream(aClass.getDeclaredFields()).filter(field -> {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)
            && !field.isAnnotationPresent(JsonIgnore.class);
      }).map(Field::getName).collect(Collectors.toSet());
    } catch (ClassNotFoundException e) {
      getLogger().info(String.format("Can't get list of field from class %s",
          resolvedType.getQualifiedName()), e);
      fields = Collections.emptySet();
    }
    return fields;
  }

  private Schema createMapSchema(ResolvedType type) {
    Schema mapSchema = new MapSchema();
    List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = type
        .asReferenceType().getTypeParametersMap();
    if (typeParametersMap.size() == 2) {
      // Assumed that Map always has the first type parameter as `String` and
      // the second is for its value type
      ResolvedType mapValueType = typeParametersMap.get(1).b;
      mapSchema.additionalProperties(parseResolvedTypeToSchema(mapValueType));
    }
    return mapSchema;
  }

  private Schema createArraySchema(ResolvedType type) {
    ArraySchema array = new ArraySchema();
    array.items(
        parseResolvedTypeToSchema(type.asArrayType().getComponentType()));
    return array;
  }

  private Schema createCollectionSchema(ResolvedReferenceType type) {
    ArraySchema array = new ArraySchema();
    List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = type
        .getTypeParametersMap();
    if (!typeParametersMap.isEmpty()) {
      ResolvedType collectionParameterType = typeParametersMap.get(0).b;
      array.items(parseResolvedTypeToSchema(collectionParameterType));
    }
    return array;
  }
}
