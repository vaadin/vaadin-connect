package com.vaadin.connect.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;

public class VaddinConnectJsGenerator extends DefaultCodegenConfig {

  // source folder where to write the files
  protected String sourceFolder = "src";
  protected String apiVersion = "1.0.0";

  public VaddinConnectJsGenerator() {
    super();

    // set the output folder here
    outputFolder = "target/generated-resources/js";

    /**
     * Api classes. You can write classes for each Api file with the
     * apiTemplateFiles map. as with models, add multiple entries with different
     * extensions for multiple files per class
     */
    apiTemplateFiles.put("esmoduleApiTemplate.mustache", // the template to use
        ".js"); // the extension for each file to write

    /**
     * Template Location. This is the location which templates will be read
     * from. The generator will use the resource stream to attempt to read the
     * templates.
     */
    templateDir = "com/vaadin/connect/generator";

    /**
     * Api Package. Optional, if needed, this can be used in templates
     */
    apiPackage = "io.swagger.client.api";

    /**
     * Model Package. Optional, if needed, this can be used in templates
     */
    modelPackage = "io.swagger.client.model";

    /**
     * Reserved words. Override this with reserved words specific to your
     * language
     */
    reservedWords = new HashSet<String>(Arrays.asList("abstract", "arguments",
        "boolean", "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "debugger", "default", "delete", "do", "double", "else",
        "enum", "eval", "export", "extends", "false", "final", "finally",
        "float", "for", "function", "goto", "if", "implements", "import", "in",
        "instanceof", "int", "interface", "let", "long", "native", "new",
        "null", "package", "private", "protected", "public", "return", "short",
        "static", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "true", "try", "typeof", "var", "void", "volatile",
        "while", "with", "yield", "Array", "Date", "eval", "function",
        "hasOwnProperty", "Infinity", "isFinite", "isNaN", "isPrototypeOf",
        "Math", "NaN", "Number", "Object", "prototype", "String", "toString",
        "undefined", "valueOf"));

    /**
     * Additional Properties. These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);

    /**
     * Supporting Files. You can write single files for the generator with the
     * entire object tree available. If the input file has a suffix of
     * `.mustache it will be processed by the template engine. Otherwise, it
     * will be copied
     */
//    supportingFiles.add(new SupportingFile("myFile.mustache", // the input
//                                                              // template or
//                                                              // file
//        "", // the destination folder, relative `outputFolder`
//        "myFile.sample") // the output file
//    );

    /**
     * Language Specific Primitives. These types will not trigger imports by the
     * client generator
     */
    languageSpecificPrimitives = new HashSet<String>(Arrays.asList("String",
        "Boolean", "Number", "Array", "Object", "Date", "File", "Blob"));

    instantiationTypes.put("array", "Array");
    instantiationTypes.put("list", "Array");
    instantiationTypes.put("map", "Object");
    typeMapping.clear();
    typeMapping.put("array", "array");
    typeMapping.put("map", "object");
    typeMapping.put("List", "array");
    typeMapping.put("boolean", "boolean");
    typeMapping.put("string", "string");
    typeMapping.put("int", "number");
    typeMapping.put("float", "number");
    typeMapping.put("number", "Number");
    typeMapping.put("DateTime", "date");
    typeMapping.put("date", "date");
    typeMapping.put("long", "number");
    typeMapping.put("short", "number");
    typeMapping.put("char", "string");
    typeMapping.put("double", "number");
    typeMapping.put("object", "object");
    typeMapping.put("integer", "number");
    // binary not supported in JavaScript client right now, using String as a
    // workaround
    typeMapping.put("ByteArray", "blob"); // I don't see ByteArray defined in
                                          // the Swagger docs.
    typeMapping.put("binary", "blob");
    typeMapping.put("UUID", "string");
    typeMapping.put("BigDecimal", "number");
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
    return "javascript-vaadin-connect";
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
  public String toEnumVarName(String value, String datatype) {
    if (value.length() == 0) {
      return "empty";
    }

    // for symbol, e.g. $, #
    if (getSymbolName(value) != null) {
      return (getSymbolName(value)).toUpperCase();
    }

    return value;
  }

  @Override
  public String toEnumValue(String value, String datatype) {
    if ("Integer".equals(datatype) || "Number".equals(datatype)) {
      return value;
    } else {
      return "\"" + escapeText(value) + "\"";
    }
  }

  @Override
  public String escapeQuotationMark(String input) {
    // remove ', " to avoid code injection
    return input.replace("\"", "").replace("'", "");
  }

  @Override
  public CodegenParameter fromRequestBody(RequestBody body,
      Map<String, Schema> schemas, Set<String> imports) {
    CodegenParameter codegenParameter = super.fromRequestBody(body, schemas,
        imports);
    Schema requestBodySchema = getRequestBodySchema(body);
    if (requestBodySchema != null
        && StringUtils.isNotBlank(requestBodySchema.get$ref())) {
      Schema o = schemas.get(getSimpleRef(requestBodySchema.get$ref()));
      List<ParameterInformation> paramsList = getParamsList(o.getProperties());
      codegenParameter.getVendorExtensions().put("x-vaadin-parameters",
          paramsList);
    }

    return codegenParameter;
  }

  private List<ParameterInformation> getParamsList(
      Map<String, Schema> properties) {
    List<ParameterInformation> paramsList = new ArrayList<>();
    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
      String name = entry.getKey();
      String type = StringUtils.defaultIfBlank(entry.getValue().getType(),
          "object");
      ParameterInformation parameterInformation = new ParameterInformation(name,
          type, entry.getValue().getDescription());
      paramsList.add(parameterInformation);
    }
    return paramsList;
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
