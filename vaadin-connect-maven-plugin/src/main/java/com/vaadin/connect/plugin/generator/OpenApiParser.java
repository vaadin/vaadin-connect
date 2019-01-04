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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
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
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.vaadin.connect.VaadinService;
import com.vaadin.connect.VaadinServiceNameChecker;
import com.vaadin.connect.oauth.AnonymousAllowed;

/**
 * Java parser class which scans for all {@link VaadinService} classes and
 * produces OpenApi json.
 */
class OpenApiParser {
  private static final String VAADIN_CONNECT_JWT_SECURITY_SCHEME = "vaadin-connect-jwt";

  private List<Path> javaSourcePaths = new ArrayList<>();
  private OpenApiConfiguration configuration;
  private Set<String> usedSchemas;
  private Map<String, String> servicesJavadoc;
  private Map<String, Schema> nonServiceSchemas;
  private Map<String, PathItem> pathItems;
  private OpenAPI openApiModel;
  private final VaadinServiceNameChecker serviceNameChecker = new VaadinServiceNameChecker();

  void addSourcePath(Path sourcePath) {
    if (sourcePath == null) {
      throw new IllegalArgumentException(
          "Java source path must be a valid directory");
    }
    if (!sourcePath.toFile().exists()) {
      throw new IllegalArgumentException("Java source path doesn't exist");
    }
    this.javaSourcePaths.add(sourcePath);
  }

  void setOpenApiConfiguration(OpenApiConfiguration configuration) {
    this.configuration = configuration;
  }

  OpenAPI getOpenApi() {
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
    usedSchemas = new HashSet<>();
    servicesJavadoc = new HashMap<>();

    ParserConfiguration parserConfiguration = new ParserConfiguration()
        .setSymbolResolver(new JavaSymbolSolver(
            new CombinedTypeSolver(new ReflectionTypeSolver(false))));
    javaSourcePaths.stream()
        .map(path -> new SourceRoot(path, parserConfiguration))
        .forEach(this::parseSourceRoot);

    for (String s : usedSchemas) {
      Schema schema = nonServiceSchemas.get(s);
      if (schema != null) {
        openApiModel.getComponents().addSchemas(s, schema);
      }
    }
    addTagsInformation();
  }

  private void parseSourceRoot(SourceRoot sourceRoot) {
    try {
      sourceRoot.parse("", this::process);
    } catch (Exception e) {
      LoggerFactory.getLogger(OpenApiParser.class).error(e.getMessage(), e);
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
    SecurityScheme vaadinConnectJwtScheme = new SecurityScheme();
    vaadinConnectJwtScheme.type(SecurityScheme.Type.HTTP);
    vaadinConnectJwtScheme.scheme("bearer");
    vaadinConnectJwtScheme.bearerFormat("JWT");
    components.addSecuritySchemes(VAADIN_CONNECT_JWT_SECURITY_SCHEME,
        vaadinConnectJwtScheme);
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
      nonServiceSchemas.put(classDeclaration.getNameAsString(),
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
      if (field.isTransient()) {
        continue;
      }
      field.getVariables().forEach(variableDeclarator -> schema.addProperties(
          variableDeclarator.getNameAsString(),
          parseResolvedTypeToSchema(variableDeclarator.getType().resolve())));
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
      if (!methodDeclaration.isPublic()
          || methodDeclaration.isAnnotationPresent(DenyAll.class)) {
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

  private boolean requiresAuthentication(
      ClassOrInterfaceDeclaration typeDeclaration,
      MethodDeclaration methodDeclaration) {
    if (hasSecurityAnnotation(methodDeclaration)) {
      return !methodDeclaration.isAnnotationPresent(AnonymousAllowed.class)
          || methodDeclaration.isAnnotationPresent(DenyAll.class);
    } else if (hasSecurityAnnotation(typeDeclaration)) {
      return !typeDeclaration.isAnnotationPresent(AnonymousAllowed.class);
    }
    return true;
  }

  private boolean hasSecurityAnnotation(NodeWithAnnotations<?> node) {
    return node.isAnnotationPresent(AnonymousAllowed.class)
        || node.isAnnotationPresent(PermitAll.class)
        || node.isAnnotationPresent(DenyAll.class)
        || node.isAnnotationPresent(RolesAllowed.class);
  }

  private Operation createPostOperation(MethodDeclaration methodDeclaration,
      boolean requiresAuthentication) {
    Operation post = new Operation();
    if (requiresAuthentication) {
      SecurityRequirement securityItem = new SecurityRequirement();
      securityItem.addList(VAADIN_CONNECT_JWT_SECURITY_SCHEME);
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
    mediaItem.schema(parseResolvedTypeToSchema(methodReturnType.resolve()));
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
      Schema paramSchema = parseResolvedTypeToSchema(
          parameter.getType().resolve());
      paramSchema
          .description(paramsDescription.get(parameter.getNameAsString()));

      String name = (isReservedWord(parameter.getNameAsString()) ? "_" : "")
          .concat(parameter.getNameAsString());
      requestSchema.addProperties(name, paramSchema);
    });
    return requestBody;
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
    }
    return createUserBeanSchema(resolvedType);
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
      String name = resolvedType.asReferenceType().getTypeDeclaration()
          .getName();
      usedSchemas.add(name);
      return new ObjectSchema().$ref(name);
    }
    return new ObjectSchema();
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
