package com.vaadin.connect.generator;

import com.google.common.base.Charsets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class reads the application.properties and generates
 * connect-client.default.js file
 */
public class DefaultClientJsGenerator {
  static final String DEFAULT_ENDPOINT = "/connect";

  private static final String DEFAULT_OUTPUT_PATH = "frontend/src/generated/connect-client.default.js";
  private static final String CONNECT_CLIENT_DEFAULT_JS_TEMPLATE_PATH = "/com/vaadin/connect/generator/connect-client.default.js.template";

  /**
   * This main method will take arguments:
   * <ul>
   * <li>output="Output path of the generated connect-client.default.js".
   * Default value is {@link DefaultClientJsGenerator#DEFAULT_OUTPUT_PATH}.</li>
   * <li>applicationProperties="application.properties file path". Default value
   * is
   * {@link ApplicationPropertiesReader#DEFAULT_APPLICATION_PROPERTIES_PATH}.</li>
   * </ul>
   * If the {@link ApplicationPropertiesReader#ENDPOINT} is not defined in
   * application.properties, default value
   * {@link DefaultClientJsGenerator#DEFAULT_ENDPOINT} will be used.
   * 
   * <pre>
   * Example:
   * <code>java -cp vaadin-connect.jar com.vaadin.connect.generator.DefaultClientJsGenerator
   *     output=/home/user/output/openapi.json
   *     applicationProperties=/home/user/myapp/src/main/resources/application.properties</code>
   * 
   * </pre>
   * 
   * @param args
   *          program arguments
   */
  public static void main(String[] args) {
    Path outputPath = getOutputPath(args);
    String endpoint = getEndpointConfiguration(args);
    String generatedDefaultClientJs = getDefaultClientJsTemplate()
        .replace("{{ENDPOINT}}", endpoint);
    Generator.writeToFile(outputPath, generatedDefaultClientJs);
  }

  private static Path getOutputPath(String[] args) {
    String input = Generator.getArgument(args, "output", DEFAULT_OUTPUT_PATH);
    return Paths.get(input).toAbsolutePath();
  }

  private static String getDefaultClientJsTemplate() {
    try {
      URL url = DefaultClientJsGenerator.class
          .getResource(CONNECT_CLIENT_DEFAULT_JS_TEMPLATE_PATH);
      return new String(Files.readAllBytes(Paths.get(url.toURI())),
          Charsets.UTF_8);
    } catch (URISyntaxException | IOException e) {
      throw new IllegalStateException(
          "Unable to read connect-client.default.js.template", e);
    }
  }

  private static String getEndpointConfiguration(String[] args) {
    Path applicationPropertiesPath = Generator
        .getApplicationPropertiesPath(args);
    Map<String, String> propertiesMap = ApplicationPropertiesReader
        .readProperties(applicationPropertiesPath);
    return propertiesMap.getOrDefault(ApplicationPropertiesReader.ENDPOINT,
        DEFAULT_ENDPOINT);
  }
}
