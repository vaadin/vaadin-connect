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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.connect.VaadinServiceNameChecker;

import static com.vaadin.connect.plugin.VaadinClientGeneratorMojo.DEFAULT_GENERATED_CONNECT_CLIENT_IMPORT_PATH;
import static com.vaadin.connect.plugin.VaadinClientGeneratorMojo.DEFAULT_GENERATED_CONNECT_CLIENT_NAME;

/**
 * Vaadin connect JavaScript generator implementation for swagger-codegen. Some
 * parts of the implementation are copied from
 * {@link io.swagger.codegen.languages.JavascriptClientCodegen}
 */
public class VaadinConnectJsGenerator extends DefaultCodegenConfig {

  private static final String GENERATOR_NAME = "javascript-vaadin-connect";
  private static final String NUMBER_TYPE = "number";
  private static final String BOOLEAN_TYPE = "boolean";
  private static final String STRING_TYPE = "string";
  private static final String OBJECT_TYPE = "object";
  private static final String ARRAY_TYPE = "array";
  private static final String BOXED_ARRAY_TYPE = "Array";
  private static final String EXTENSION_VAADIN_CONNECT_PARAMETERS = "x-vaadin-connect-parameters";
  private static final String EXTENSION_VAADIN_CONNECT_SHOW_JSDOC = "x-vaadin-connect-show-jsdoc";
  private static final String EXTENSION_VAADIN_CONNECT_METHOD_NAME = "x-vaadin-connect-method-name";
  private static final String EXTENSION_VAADIN_CONNECT_SERVICE_NAME = "x-vaadin-connect-service-name";
  private static final String VAADIN_CONNECT_USER_TYPE_DESCRIPTIONS = "vaadinConnectUserTypes";
  private static final String VAADIN_CONNECT_CLASS_DESCRIPTION = "vaadinConnectClassDescription";
  private static final String CLIENT_PATH_TEMPLATE_PROPERTY = "vaadinConnectDefaultClientPath";
  private static final Pattern PATH_REGEX = Pattern
      .compile("^/([^/{}\n\t]+)/([^/{}\n\t]+)$");
  private static final String OPERATION = "operation";

  private List<Tag> tags;
  private Map<String, Set<TypeInformation>> userTypes = new HashMap<>();
  private String currentTag;

  private static class VaadinConnectJSOnlyGenerator extends DefaultGenerator {
    @Override
    public File writeToFile(String filename, String contents)
        throws IOException {
      if (filename.endsWith(".js")) {
        return super.writeToFile(filename, contents);
      }
      return null;
    }
  }

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
    templateDir = "com/vaadin/connect/plugin/generator";

    /*
     * Reserved words copied from https://www.w3schools.com/js/js_reserved.asp
     */
    reservedWords = VaadinServiceNameChecker.ECMA_SCRIPT_RESERVED_WORDS;

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
    typeMapping.put("DateTime", "Date");
    typeMapping.put("date", "Date");
    typeMapping.put("long", NUMBER_TYPE);
    typeMapping.put("short", NUMBER_TYPE);
    typeMapping.put("char", STRING_TYPE);
    typeMapping.put("double", NUMBER_TYPE);
    typeMapping.put(OBJECT_TYPE, OBJECT_TYPE);
    typeMapping.put("integer", NUMBER_TYPE);
    typeMapping.put("ByteArray", "blob");
    typeMapping.put("binary", "blob");
    typeMapping.put("file", "blob");
    typeMapping.put("UUID", STRING_TYPE);
    typeMapping.put("BigDecimal", NUMBER_TYPE);
  }

  /**
   * Runs the code generation based on the data from the OpenAPI json. Generates
   * the target files in the directory specified, overwriting the files and
   * creating the target directory, if necessary.
   *
   * @param openApiJsonFile
   *          the api spec file to analyze
   * @param generatedFrontendDirectory
   *          the directory to generateOpenApiSpec the files into
   *
   * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
   *      specification</a>
   */
  public static void launch(File openApiJsonFile,
      File generatedFrontendDirectory) {
    launch(openApiJsonFile, generatedFrontendDirectory, null);
  }

  /**
   * Runs the code generation based on the data from the OpenAPI json. Generates
   * the target files in the directory specified, overwriting the files and
   * creating the target directory, if necessary.
   *
   * @param openApiJsonFile
   *          the api spec file to analyze
   * @param generatedFrontendDirectory
   *          the directory to generateOpenApiSpec the files into
   * @param defaultClientPath
   *          the default client path which is imported in the generated files.
   *          If it is {@code null}, the default generate client path is used.
   * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
   *      specification</a>
   */
  public static void launch(File openApiJsonFile,
      File generatedFrontendDirectory, String defaultClientPath) {
    CodegenConfigurator configurator = new CodegenConfigurator();
    configurator.setLang(VaadinConnectJsGenerator.class.getName());
    configurator.setInputSpecURL(openApiJsonFile.toString());
    configurator.setOutputDir(generatedFrontendDirectory.toString());
    configurator.addAdditionalProperty(CLIENT_PATH_TEMPLATE_PROPERTY,
        getDefaultClientPath(defaultClientPath));
    generate(configurator);
  }

  private static String getDefaultClientPath(String path) {
    return ObjectUtils.defaultIfNull(path,
        DEFAULT_GENERATED_CONNECT_CLIENT_IMPORT_PATH);
  }

  private static void generate(CodegenConfigurator configurator) {
    SwaggerParseResult parseResult = getParseResult(configurator);
    if (parseResult != null && parseResult.getMessages().isEmpty()) {
      Set<File> generatedFiles = new VaadinConnectJSOnlyGenerator()
          .opts(configurator.toClientOptInput()).generate().stream()
          .filter(Objects::nonNull).collect(Collectors.toSet());

      cleanGeneratedFolder(configurator.getOutputDir(), generatedFiles);
    } else {
      String error = parseResult == null ? ""
          : StringUtils.join(parseResult.getMessages().toArray());
      cleanGeneratedFolder(configurator.getOutputDir(), Collections.emptySet());
      throw getUnexpectedOpenAPIException(configurator.getInputSpecURL(),
          error);
    }
  }

  private static void cleanGeneratedFolder(String outputDir,
      Set<File> generatedFiles) {
    File outputDirFile = new File(outputDir);
    if (!outputDirFile.exists()) {
      return;
    }
    File[] filesToDelete = outputDirFile
        .listFiles(pathname -> shouldDelete(generatedFiles, pathname));
    if (filesToDelete != null) {
      for (File file : filesToDelete) {
        getLogger().info("Removing stale generated file '{}'.",
            file.getAbsolutePath());
        deleteFile(file);
      }
    }
  }

  private static void deleteFile(File file) {
    try {
      FileUtils.forceDelete(file);
    } catch (IOException e) {
      getLogger().info(String.format(
          "Failed to remove '%s' while cleaning the generated folder.",
          file.getAbsolutePath()), e);
    }
  }

  private static boolean shouldDelete(Set<File> generatedFiles, File file) {
    return !generatedFiles.contains(file)
        && !DEFAULT_GENERATED_CONNECT_CLIENT_NAME.equals(file.getName());
  }

  private static IllegalStateException getUnexpectedOpenAPIException(
      String inputFile, String errorMessage) {
    return new IllegalStateException(
        "Unexpected error while generating vaadin-connect JavaScript service wrappers."
            + " The input file " + inputFile
            + " might be corrupted, please try running the generating tasks again. "
            + errorMessage);
  }

  private static SwaggerParseResult getParseResult(
      CodegenConfigurator configurator) {
    try {
      String inputSpec = configurator.loadSpecContent(
          configurator.getInputSpecURL(), Collections.emptyList());
      ParseOptions options = new ParseOptions();
      options.setResolve(true);
      options.setFlatten(true);
      return new OpenAPIParser().readContents(inputSpec,
          Collections.emptyList(), options);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Unexpected error while generating vaadin-connect JavaScript service wrappers. "
              + String.format("Can't read file '%s'",
                  configurator.getInputSpecURL()),
          e);
    }
  }

  private static Logger getLogger() {
    return LoggerFactory.getLogger(VaadinConnectJsGenerator.class);
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
    return this.reservedWordsMappings().getOrDefault(name, "_" + name);
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
    // Escape opening/closing block comment to avoid code injection
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
      throw getGeneratorException(
          "Code generator only supports POST requests.");
    }
    Matcher matcher = PATH_REGEX.matcher(path);
    if (!matcher.matches()) {
      throw getGeneratorException(
          "Path must be in form of \"/<ServiceName>/<MethodName>\".");
    }
    CodegenOperation codegenOperation = super.fromOperation(path, httpMethod,
        operation, schemas, openAPI);
    String serviceName = matcher.group(1);
    String methodName = matcher.group(2);
    codegenOperation.getVendorExtensions()
        .put(EXTENSION_VAADIN_CONNECT_METHOD_NAME, methodName);
    codegenOperation.getVendorExtensions()
        .put(EXTENSION_VAADIN_CONNECT_SERVICE_NAME, serviceName);
    validateOperationTags(path, httpMethod, operation);
    return codegenOperation;
  }

  private void validateOperationTags(String path, String httpMethod,
      Operation operation) {
    List<String> operationTags = operation.getTags();
    if (operationTags == null || operationTags.isEmpty()) {
      getLogger().warn(
          "The {} operation with path \"{}\" does not have any tag. The generated method will be included in Default class.",
          httpMethod, path);
    } else if (operationTags.size() > 1) {
      String fileList = String.join(", ", operationTags);
      getLogger().warn(
          "The {} operation with path \"{}\" contains multiple tags. The generated method will be included in classes: \"{}\".",
          httpMethod, path, fileList);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
    Map<String, Object> operations = (Map<String, Object>) objs
        .get("operations");
    String classname = (String) operations.get("classname");
    for (Tag tag : tags) {
      if (tag.getName().equals(classname)) {
        objs.put(VAADIN_CONNECT_CLASS_DESCRIPTION, tag.getDescription());
        break;
      }
    }
    if (objs.get(VAADIN_CONNECT_CLASS_DESCRIPTION) == null) {
      warnNoClassInformation(classname);
    }

    if ((operations.get(OPERATION) instanceof List)) {
      List<CodegenOperation> codegenOperations = (List<CodegenOperation>) operations
          .get(OPERATION);
      setShouldShowJsDoc(codegenOperations);
      objs.put(VAADIN_CONNECT_USER_TYPE_DESCRIPTIONS,
          codegenOperations.stream().map(CodegenOperation::getTags)
              .flatMap(Collection::stream).map(Tag::getName).map(userTypes::get)
              .filter(Objects::nonNull).flatMap(Collection::stream)
              .collect(Collectors.toSet()));
    }
    return super.postProcessOperations(objs);
  }

  private void setShouldShowJsDoc(List<CodegenOperation> operations) {
    for (CodegenOperation coop : operations) {
      boolean hasDescription = StringUtils.isNotBlank(coop.getNotes());
      boolean hasParameter = hasParameter(coop);
      boolean hasReturnType = StringUtils.isNotBlank(coop.getReturnType());
      boolean hasResponseDescription = hasResponseDescription(coop);
      if (hasDescription || hasParameter || hasReturnType
          || hasResponseDescription) {
        coop.getVendorExtensions().put(EXTENSION_VAADIN_CONNECT_SHOW_JSDOC,
            true);
      }
    }
  }

  private boolean hasResponseDescription(CodegenOperation coop) {
    for (CodegenResponse response : coop.getResponses()) {
      if (StringUtils.isNotBlank(response.getMessage())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasParameter(CodegenOperation coop) {
    for (CodegenParameter bodyParam : coop.getBodyParams()) {
      if (bodyParam.getVendorExtensions()
          .get(EXTENSION_VAADIN_CONNECT_PARAMETERS) != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void preprocessOpenAPI(OpenAPI openAPI) {
    super.processOpenAPI(openAPI);
    List<Tag> openAPITags = openAPI.getTags();
    userTypes.clear();
    currentTag = null;
    this.tags = openAPITags != null ? openAPITags : Collections.emptyList();
  }

  private void warnNoClassInformation(String classname) {
    // Link should be replace later
    getLogger().warn("The operations with tag {} doesn't have description."
        + "For more information, please visit https://vaadin.com/vaadin-connect#vaadin-services-extension-in-open-api.",
        classname);
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
      List<ParameterInformation> paramsList = getParamsList(requestSchema);
      codegenParameter.getVendorExtensions()
          .put(EXTENSION_VAADIN_CONNECT_PARAMETERS, paramsList);
    }

    for (String schemaName : imports) {
      Schema schema = schemas.get(schemaName);
      if (schema != null) {
        addUserTypesFromSchema(schemas, currentTag, schemaName, schema);
      }
    }

    return codegenParameter;
  }

  @Override
  public String sanitizeTag(String tag) {
    this.currentTag = tag;
    return super.sanitizeTag(tag);
  }

  private void addUserTypesFromSchema(Map<String, Schema> schemas, String tag,
      String schemaName, Schema schema) {
    Set<TypeInformation> types = userTypes.computeIfAbsent(tag,
        key -> new HashSet<>());

    List<ParameterInformation> schemaParams = getParamsList(schema);
    if (!schemaParams.isEmpty() || !StringUtils.isAllBlank(schemaName,
        schema.getType(), schema.getDescription())) {
      types.add(new TypeInformation(schemaName, schema.getType(),
          schema.getDescription(), schemaParams));
    }

    ((Map<String, Schema>) schema.getProperties()).values().stream()
        .map(Schema::getAdditionalProperties)
        .filter(property -> property instanceof Schema)
        .map(property -> ((Schema) property)).map(Schema::get$ref)
        .filter(Objects::nonNull).map(this::getSimpleRef)
        .forEach(nestedSchemaName -> {
          Schema nestedSchema = schemas.get(nestedSchemaName);
          if (nestedSchema != null) {
            addUserTypesFromSchema(schemas, tag, nestedSchemaName,
                nestedSchema);
          }
        });
  }

  private List<ParameterInformation> getParamsList(Schema requestSchema) {
    Map<String, Schema> properties = requestSchema.getProperties();
    List<ParameterInformation> paramsList = new ArrayList<>();
    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
      String name = entry.getKey();
      name = isReservedWord(name) ? escapeReservedWord(name) : name;
      String type = getTypeFromSchema(entry.getValue());
      String description = entry.getValue().getDescription();
      if (StringUtils.isBlank(description)) {
        description = getDescriptionFromParameterExtension(name, requestSchema);
      }
      ParameterInformation parameterInformation = new ParameterInformation(name,
          type, description);
      paramsList.add(parameterInformation);
    }
    return paramsList;
  }

  private String getDescriptionFromParameterExtension(String paramName,
      Schema requestSchema) {
    if (requestSchema.getExtensions() == null) {
      return "";
    }
    Map<String, String> paramDescription = (Map<String, String>) requestSchema
        .getExtensions().get(
            OpenApiObjectGenerator.EXTENSION_VAADIN_CONNECT_PARAMETERS_DESCRIPTION);
    return paramDescription.getOrDefault(paramName, "");
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
    return initialCaps(name);
  }

  @Override
  protected String getTemplateDir() {
    return templateDir;
  }

  @Override
  public String getDefaultTemplateDir() {
    return templateDir;
  }

  private RuntimeException getGeneratorException(String message) {
    // TODO Link should be replaced later
    return new RuntimeException(message
        + " For more information, please checkout the Vaadin Connect Generator "
        + "documentation page at https://vaadin.com/vaadin-connect.");
  }

  @Override
  public void addHandlebarHelpers(Handlebars handlebars) {
    super.addHandlebarHelpers(handlebars);
    handlebars.registerHelper("multiplelines", getMultipleLinesHelper());
  }

  private Helper<String> getMultipleLinesHelper() {
    return (context, options) -> {
      Options.Buffer buffer = options.buffer();
      String[] lines = context.split("\n");
      Context parent = options.context;
      Template fn = options.fn;
      for (String line : lines) {
        buffer.append(options.apply(fn, parent.combine("@line", line)));
      }
      return buffer;
    };
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

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ParameterInformation that = (ParameterInformation) o;
      return Objects.equals(name, that.name) && Objects.equals(type, that.type)
          && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, type, description);
    }
  }

  private static class TypeInformation {
    private final String name;
    private final String type;
    private final String description;
    private final List<ParameterInformation> parameterInformation;

    TypeInformation(String name, String type, String description,
        List<ParameterInformation> parameterInformation) {
      this.name = name;
      this.type = type;
      this.description = description;
      this.parameterInformation = parameterInformation;
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

    public List<ParameterInformation> getParameterInformation() {
      return parameterInformation;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TypeInformation that = (TypeInformation) o;
      return Objects.equals(name, that.name) && Objects.equals(type, that.type)
          && Objects.equals(description, that.description)
          && Objects.equals(parameterInformation, that.parameterInformation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, type, description, parameterInformation);
    }
  }
}
