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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;

/**
 * Vaadin connect javascript generator implementation for swagger-codegen. Some
 * parts of the implementation are copied from
 * {@link io.swagger.codegen.languages.JavascriptClientCodegen}
 */
public class VaadinConnectJsGenerator extends DefaultCodegenConfig {

  public static final String GENERATOR_NAME = "javascript-vaadin-connect";
  private static final String NUMBER_TYPE = "number";
  private static final String BOOLEAN_TYPE = "boolean";
  private static final String STRING_TYPE = "string";
  private static final String OBJECT_TYPE = "object";
  private static final String ARRAY_TYPE = "array";
  private static final String BOXED_ARRAY_TYPE = "Array";
  private static final String EXTENSION_VAADIN_CONNECT_PARAMETERS = "x-vaadin-connect-parameters";
  private static final String EXTENSION_VAADIN_CONNECT_METHOD_NAME = "x-vaadin-connect-method-name";
  private static final String VAADIN_CONNECT_CLASS_DESCRIPTION = "vaadinConnectClassDescription";

  /**
   * Create vaadin connect js codegen instance.
   */
  public VaadinConnectJsGenerator() {
    super();

    // set the output folder here
    outputFolder = "target/generated-resources/js";

    /*
     * Api classes. You can write classes for each Api file with the
     * apiTemplateFiles map. as with models, add multiple entries with different
     * extensions for multiple files per class
     */
    apiTemplateFiles.put("ESModuleApiTemplate.mustache", // the template to use
        ".js"); // the extension for each file to write

    /*
     * Template Location. This is the location which templates will be read
     * from. The generator will use the resource stream to attempt to read the
     * templates.
     */
    templateDir = "com/vaadin/connect/generator";

    /*
     * Reserved words copied from https://www.w3schools.com/js/js_reserved.asp
     */
    reservedWords = new HashSet<>(Arrays.asList("abstract", "arguments",
        "await", BOOLEAN_TYPE, "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "debugger", "default", "delete", "do",
        "double", "else", "enum", "eval", "export", "extends", "false", "final",
        "finally", "float", "for", "function", "goto", "if", "implements",
        "import", "in", "instanceof", "int", "interface", "let", "long",
        "native", "new", "null", "package", "private", "protected", "public",
        "return", "short", "static", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "true", "try", "typeof", "var", "void",
        "volatile", "while", "with", "yield"));

    /*
     * Language Specific Primitives. These types will not trigger imports by the
     * client generator
     */
    languageSpecificPrimitives = new HashSet<>(
        Arrays.asList("String", "Boolean", "Number", BOXED_ARRAY_TYPE, "Object",
            "Date", "File", "Blob"));

    instantiationTypes.put(ARRAY_TYPE, BOXED_ARRAY_TYPE);
    instantiationTypes.put("list", BOXED_ARRAY_TYPE);
    instantiationTypes.put("map", "Object");
    typeMapping.clear();
    typeMapping.put(ARRAY_TYPE, ARRAY_TYPE);
    typeMapping.put("map", OBJECT_TYPE);
    typeMapping.put("List", ARRAY_TYPE);
    typeMapping.put(BOOLEAN_TYPE, BOOLEAN_TYPE);
    typeMapping.put(STRING_TYPE, STRING_TYPE);
    typeMapping.put("int", NUMBER_TYPE);
    typeMapping.put("float", NUMBER_TYPE);
    typeMapping.put(NUMBER_TYPE, NUMBER_TYPE);
    typeMapping.put("DateTime", "date");
    typeMapping.put("date", "date");
    typeMapping.put("long", NUMBER_TYPE);
    typeMapping.put("short", NUMBER_TYPE);
    typeMapping.put("char", STRING_TYPE);
    typeMapping.put("double", NUMBER_TYPE);
    typeMapping.put(OBJECT_TYPE, OBJECT_TYPE);
    typeMapping.put("integer", NUMBER_TYPE);
    typeMapping.put("ByteArray", "blob");
    typeMapping.put("binary", "blob");
    typeMapping.put("UUID", STRING_TYPE);
    typeMapping.put("BigDecimal", NUMBER_TYPE);
  }

  /**
   * Configures the type of generator.
   *
   * @return the CodegenType for this generator
   * @see io.swagger.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  /**
   * Configures a friendly name for the generator. This will be used by the
   * generator to select the library with the -l flag.
   *
   * @return the friendly name for the generator
   */
  public String getName() {
    return GENERATOR_NAME;
  }

  /**
   * Returns human-friendly help for the generator. Provide the consumer with
   * help tips, parameters here
   *
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a Vaadin Connect service wrappers.";
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle
   * escaping those terms here. This logic is only called if a variable matches
   * the reserved words
   *
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    if (this.reservedWordsMappings().containsKey(name)) {
      return this.reservedWordsMappings().get(name);
    }
    return "_" + name; // add an underscore to the name
  }

  /**
   * Location to write model files. You can use the modelPackage() as defined
   * when the class is instantiated
   */
  @Override
  public String modelFileFolder() {
    return outputFolder;
  }

  /**
   * Location to write api files. You can use the apiPackage() as defined when
   * the class is instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder;
  }

  @Override
  public String escapeUnsafeCharacters(String input) {
    return input.replace("*/", "*_/").replace("/*", "/_*");
  }

  @Override
  public String escapeQuotationMark(String input) {
    // remove ', " to avoid code injection
    return input.replace("\"", "").replace("'", "");
  }

  @Override
  public CodegenOperation fromOperation(String path, String httpMethod,
      Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
    if (!"POST".equalsIgnoreCase(httpMethod)) {
      throw new GeneratorException(
          "Code generator only supports POST requests.");
    }
    if (!path.matches("^/([^/]+)/([^/]+)$")) {
      throw new GeneratorException(
        "Path must be in form of \"/<ServiceName>/<MethodName>\".");
    }
    CodegenOperation codegenOperation = super.fromOperation(path, httpMethod,
        operation, schemas, openAPI);
    int i = path.lastIndexOf('/') + 1;
    String methodName = path.substring(i);
    codegenOperation.getVendorExtensions()
        .put(EXTENSION_VAADIN_CONNECT_METHOD_NAME, methodName);
    return codegenOperation;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
    Map<String, Object> operations = (Map<String, Object>) objs
        .get("operations");
    Object vaadinServicesExtension = vendorExtensions()
        .get(OpenApiJavaParserImpl.VAADIN_SERVICES_EXTENSION_NAME);
    if (vaadinServicesExtension instanceof Map) {
      String classname = (String) operations.get("classname");
      Object classDescription = ((Map<String, OpenAPiVaadinServicesExtension>) vaadinServicesExtension)
          .get(classname);
      objs.put(VAADIN_CONNECT_CLASS_DESCRIPTION, classDescription);
    }
    return super.postProcessOperations(objs);
  }

  @Override
  @SuppressWarnings("unchecked")
  public CodegenParameter fromRequestBody(RequestBody body,
      Map<String, Schema> schemas, Set<String> imports) {
    CodegenParameter codegenParameter = super.fromRequestBody(body, schemas,
        imports);
    Schema requestBodySchema = getRequestBodySchema(body);
    if (requestBodySchema != null
        && StringUtils.isNotBlank(requestBodySchema.get$ref())) {
      Schema requestSchema = schemas
          .get(getSimpleRef(requestBodySchema.get$ref()));
      List<ParameterInformation> paramsList = getParamsList(
          requestSchema.getProperties());
      codegenParameter.getVendorExtensions()
          .put(EXTENSION_VAADIN_CONNECT_PARAMETERS, paramsList);
    }

    return codegenParameter;
  }

  private List<ParameterInformation> getParamsList(
      Map<String, Schema> properties) {
    List<ParameterInformation> paramsList = new ArrayList<>();
    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
      String name = entry.getKey();
      String type = getTypeFromSchema(entry.getValue());
      ParameterInformation parameterInformation = new ParameterInformation(name,
          type, entry.getValue().getDescription());
      paramsList.add(parameterInformation);
    }
    return paramsList;
  }

  private String getTypeFromSchema(Schema schema) {
    if (StringUtils.isNotBlank(schema.getType())) {
      return schema.getType();
    }
    if (StringUtils.isNotBlank(schema.get$ref())) {
      return getSimpleRef(schema.get$ref());
    }
    return OBJECT_TYPE;
  }

  private Schema getRequestBodySchema(RequestBody body) {
    Content content = body.getContent();
    if (content == null) {
      return null;
    }
    MediaType mediaType = content.get(DEFAULT_CONTENT_TYPE);
    if (mediaType != null && mediaType.getSchema() != null) {
      return mediaType.getSchema();
    }
    return null;
  }

  @Override
  public String toApiName(String name) {
    if (name.length() == 0) {
      return "DefaultService";
    }
    return initialCaps(name);
  }

  @Override
  public String toApiFilename(String name) {
    return name;
  }

  @Override
  public String getArgumentsLocation() {
    return null;
  }

  @Override
  protected String getTemplateDir() {
    return templateDir;
  }

  @Override
  public String getDefaultTemplateDir() {
    return templateDir;
  }

  /**
   * Parameter information object which is used to store body parameters in a
   * convenient way to process in the template.
   */
  private static class ParameterInformation {
    private final String name;
    private final String type;
    private final String description;

    ParameterInformation(String name, String type, String description) {
      this.name = name;
      this.type = type;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String getDescription() {
      return description;
    }
  }
}
