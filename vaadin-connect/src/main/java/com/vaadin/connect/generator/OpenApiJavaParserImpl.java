/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.connect.generator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.JavadocBlockTag;
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
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.vaadin.connect.VaadinService;

/**
 * Java parser class which scans for all {@link VaadinService} classes and
 * produces OpenApi json.
 */
public class OpenApiJavaParserImpl implements OpenApiGenerator {

  public static final String VAADIN_SERVICES_EXTENSION_NAME = "x-vaadin-services";
  private static final List<String> NUMBER_TYPES = Arrays.asList("int",
      "integer", "short", "long", "double", "float");
  private static final List<String> STRING_TYPES = Arrays.asList("string",
      "char");
  private static final List<String> BOOLEAN_TYPES = Collections
      .singletonList("boolean");
  private static final List<String> MAP_TYPES = Arrays.asList("map", "hashmap",
      "hashtable", "treemap", "sortedmap");
  private static final List<String> COLLECTION_TYPES = Arrays.asList(
      "collection", "list", "arraylist", "linkedlist", "set", "hashset",
      "sortedset", "treeset");
  private static final List<String> PREDEFINED_TYPES = Stream
      .of(NUMBER_TYPES, STRING_TYPES, BOOLEAN_TYPES, MAP_TYPES,
          COLLECTION_TYPES)
      .flatMap(Collection::stream).collect(Collectors.toList());
  private Path javaSourcePath;
  private OpenApiConfiguration configuration;
  private Set<String> usedSchemas;
  private Map<String, OpenAPiVaadinServicesExtension> vaadinServicesExtensionMap;
  private Map<String, Schema> nonServiceSchemas;
  private OpenAPI openApiModel;

  @Override
  public void setSourcePath(Path sourcePath) {
    if (sourcePath == null) {
      throw new IllegalArgumentException(
          "Java source path must be a valid directory");
    }
    if (!sourcePath.toFile().exists()) {
      throw new IllegalArgumentException("Java source path doesn't exist");
    }
    this.javaSourcePath = sourcePath;
  }

  @Override
  public void setOpenApiConfiguration(OpenApiConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public OpenAPI getOpenApi() {
    if (openApiModel == null) {
      init();
    }
    return openApiModel;
  }

  @Override
  public OpenAPI generateOpenApi() {
    init();
    return openApiModel;
  }

  private void init() {
    if (javaSourcePath == null || configuration == null) {
      throw new IllegalStateException(
          "Java source path and configuration should not be null");
    }
    SourceRoot sourceRoot = new SourceRoot(javaSourcePath);
    openApiModel = createBasicModel();
    nonServiceSchemas = new HashMap<>();
    usedSchemas = new HashSet<>();
    vaadinServicesExtensionMap = new HashMap<>();
    try {
      sourceRoot.parse("", this::process);
    } catch (Exception e) {
      LoggerFactory.getLogger(OpenApiJavaParserImpl.class).error(e.getMessage(),
          e);
      throw new IllegalStateException("Can't parse the java files", e);
    }

    for (String s : usedSchemas) {
      Schema schema = nonServiceSchemas.get(s);
      if (schema != null) {
        openApiModel.getComponents().addSchemas(s, schema);
      }
    }

    openApiModel.addExtension(VAADIN_SERVICES_EXTENSION_NAME,
        vaadinServicesExtensionMap);
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
    openAPI.components(new Components());
    return openAPI;
  }

  @SuppressWarnings("squid:S1172")
  private SourceRoot.Callback.Result process(Path localPath, Path absolutePath,
      ParseResult<CompilationUnit> result) {
    result.ifSuccessful(compilationUnit -> compilationUnit.getPrimaryType()
        .ifPresent(typeDeclaration -> {
          if (typeDeclaration.isClassOrInterfaceDeclaration()) {
            parseClass(typeDeclaration.asClassOrInterfaceDeclaration());
          }
        }));
    return SourceRoot.Callback.Result.DONT_SAVE;
  }

  private void parseClass(ClassOrInterfaceDeclaration classDeclaration) {
    String className = classDeclaration.getNameAsString();
    if (!classDeclaration
        .isAnnotationPresent(VaadinService.class.getSimpleName())) {
      nonServiceSchemas.put(className, parseClassAsSchema(classDeclaration));
      return;
    }
    OpenAPiVaadinServicesExtension openAPiVaadinServicesExtension = new OpenAPiVaadinServicesExtension()
        .description("");
    classDeclaration.getJavadoc()
        .ifPresent(javadoc -> openAPiVaadinServicesExtension
            .description(javadoc.getDescription().toText()));

    vaadinServicesExtensionMap.put(className, openAPiVaadinServicesExtension);

    Map<String, PathItem> pathItems = createPathItems(classDeclaration);

    for (Map.Entry<String, PathItem> entry : pathItems.entrySet()) {
      String methodName = entry.getKey();
      PathItem pathItem = entry.getValue();
      String pathName = "/" + className + "/" + methodName;
      openApiModel.getPaths().addPathItem(pathName, pathItem);
    }
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
      field.getVariables().forEach(variableDeclarator -> {
        if (isBeanType(variableDeclarator.getType())) {
          Schema propertyItem = new Schema()
              .$ref(variableDeclarator.getTypeAsString());
          schema.addProperties(variableDeclarator.getNameAsString(),
              propertyItem);
          usedSchemas.add(variableDeclarator.getTypeAsString());
        } else if (isMapType(variableDeclarator.getType())) {
          schema.addProperties(variableDeclarator.getNameAsString(),
              parseTypeToSchema(variableDeclarator.getType()));
        } else {
          Schema propertyItem = parseTypeToSchema(variableDeclarator.getType());
          schema.addProperties(variableDeclarator.getNameAsString(),
              propertyItem);
        }
      });
    }
    parseInnerClasses(typeDeclaration.asClassOrInterfaceDeclaration());
    return schema;
  }

  private boolean isMapType(Type type) {
    return MAP_TYPES.contains(getTypeName(type));
  }

  private Map<String, PathItem> createPathItems(
      ClassOrInterfaceDeclaration typeDeclaration) {
    Map<String, PathItem> pathItems = new HashMap<>();
    for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
      if (!methodDeclaration.isPublic()) {
        continue;
      }
      String methodName = methodDeclaration.getNameAsString();

      Operation post = createPostOperation(methodDeclaration);

      if (methodDeclaration.getParameters().isNonEmpty()) {
        post.setRequestBody(createRequestBody(methodDeclaration));
      }

      ApiResponses responses = createApiResponses(methodDeclaration);
      post.setResponses(responses);
      post.tags(Collections.singletonList(typeDeclaration.getNameAsString()));
      PathItem pathItem = new PathItem().post(post);
      pathItems.put(methodName, pathItem);
    }
    parseInnerClasses(typeDeclaration);
    return pathItems;
  }

  private void parseInnerClasses(ClassOrInterfaceDeclaration typeDeclaration) {
    for (BodyDeclaration member : typeDeclaration.getMembers()) {
      if (member.isClassOrInterfaceDeclaration()) {
        ClassOrInterfaceDeclaration classDeclaration = member
            .asClassOrInterfaceDeclaration();
        nonServiceSchemas.put(classDeclaration.getNameAsString(),
            parseClassAsSchema(classDeclaration));
      }
    }
  }

  private Operation createPostOperation(MethodDeclaration methodDeclaration) {
    Operation post = new Operation();
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
    ApiResponse successfulResponse = new ApiResponse();
    methodDeclaration.getJavadoc().ifPresent(javadoc -> {
      for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
        if (blockTag.getType() == JavadocBlockTag.Type.RETURN) {
          successfulResponse
              .setDescription("Return " + blockTag.getContent().toText());
        }
      }
    });
    if (StringUtils.isBlank(successfulResponse.getDescription())) {
      successfulResponse.setDescription(
          "Request has been processed without any return result");
    }
    if (!methodDeclaration.getType().isVoidType()) {
      MediaType mediaItem = createReturnMediaType(methodDeclaration);
      successfulContent.addMediaType("application/json", mediaItem);
      successfulResponse.content(successfulContent);
    }
    return successfulResponse;
  }

  private MediaType createReturnMediaType(MethodDeclaration methodDeclaration) {
    MediaType mediaItem = new MediaType();
    Schema schema;
    if (isBeanType(methodDeclaration.getType())) {
      String type = methodDeclaration.getType().asString();
      schema = new Schema().$ref(type);
      usedSchemas.add(type);
    } else {
      schema = parseTypeToSchema(methodDeclaration.getType());
    }
    mediaItem.schema(schema);
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
      Schema paramSchema;
      if (isBeanType(parameter.getType())) {
        paramSchema = new Schema().$ref(parameter.getTypeAsString());
        usedSchemas.add(parameter.getTypeAsString());
      } else {
        parameter.getType().getElementType();
        paramSchema = parseTypeToSchema(parameter.getType());
      }
      paramSchema
          .description(paramsDescription.get(parameter.getNameAsString()));
      requestSchema.addProperties(parameter.getNameAsString(), paramSchema);
    });
    return requestBody;
  }

  private boolean isBeanType(Type type) {
    if (type.isPrimitiveType()) {
      return false;
    }
    String typeName = getTypeName(type);
    boolean isPredefinedType = PREDEFINED_TYPES.contains(typeName);
    return !type.isArrayType() && type.isReferenceType() && !isPredefinedType;
  }

  private Schema parseTypeToSchema(Type javaType) {
    if (javaType.isArrayType()) {
      return createArraySchema(javaType);
    }
    String typeName = getTypeName(javaType);
    if (NUMBER_TYPES.contains(typeName)) {
      return new NumberSchema();
    } else if (STRING_TYPES.contains(typeName)) {
      return new StringSchema();
    } else if (COLLECTION_TYPES.contains(typeName)) {
      return createArraySchema(javaType);
    } else if (BOOLEAN_TYPES.contains(typeName)) {
      return new BooleanSchema();
    } else if (MAP_TYPES.contains(typeName)) {
      return createMapSchema(javaType);
    }
    return createUserBeanSchema(javaType);
  }

  private Schema createUserBeanSchema(Type javaType) {
    String userType = javaType.asString();
    usedSchemas.add(userType);
    return new ObjectSchema().$ref(userType);
  }

  private String getTypeName(Type javaType) {
    String typeName;
    if (javaType.isClassOrInterfaceType()) {
      typeName = javaType.asClassOrInterfaceType().getNameAsString()
          .toLowerCase(Locale.ENGLISH);
    } else {
      typeName = javaType.asString().toLowerCase(Locale.ENGLISH);
    }
    return typeName;
  }

  private Schema createMapSchema(Type javaType) {
    Schema mapSchema = new MapSchema();
    Type mapValueType = (Type) javaType.getChildNodes().get(2);
    mapSchema.additionalProperties(parseTypeToSchema(mapValueType));
    return mapSchema;
  }

  private Schema createArraySchema(Type type) {
    ArraySchema array = new ArraySchema();
    if (type.isArrayType()) {
      // The first child of type "int[]" is its actual type
      Type arrayType = (Type) type.getChildNodes().get(0);
      array.items(parseTypeToSchema(arrayType));
    } else {
      // Child nodes of List<String> are: List and String
      // So we need the second one
      Type arrayType = (Type) type.getChildNodes().get(1);
      array.items(parseTypeToSchema(arrayType));
    }
    return array;
  }
}
