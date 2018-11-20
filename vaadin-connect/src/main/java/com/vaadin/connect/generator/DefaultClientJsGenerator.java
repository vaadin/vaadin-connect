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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PropertyResourceBundle;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads the application.properties and generates
 * connect-client.default.js file
 */
public class DefaultClientJsGenerator {

  private static final String DEFAULT_OUTPUT_PATH = "frontend/src/generated/connect-client.default.js";
  private static final String CONNECT_CLIENT_DEFAULT_JS_TEMPLATE_PATH = "/com/vaadin/connect/generator/connect-client.default.js.template";

  /**
   * This main method will take arguments:
   * <ul>
   * <li>
   * <code>output="Output path of the generated connect-client.default.js"</code><br>
   * Default value is {@link DefaultClientJsGenerator#DEFAULT_OUTPUT_PATH}.</li>
   * <li><code>applicationProperties="application.properties file path"</code><br>
   * Default value is {@link Generator#DEFAULT_APPLICATION_PROPERTIES_PATH}.
   * </li>
   * </ul>
   * If the {@link Generator#ENDPOINT} is not defined in application.properties,
   * default value {@link Generator#DEFAULT_ENDPOINT} will be used.
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
    try {
      PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(
          Files.newBufferedReader(applicationPropertiesPath));
      String endpoint = (String) propertyResourceBundle
          .handleGetObject(Generator.ENDPOINT);
      if (endpoint != null) {
        return endpoint;
      }
    } catch (IOException e) {
      getLogger().info("Can't read the application.properties file from "
          + applicationPropertiesPath, e);
    }
    return Generator.DEFAULT_ENDPOINT;
  }

  private static Logger getLogger() {
    return LoggerFactory.getLogger(DefaultClientJsGenerator.class.getName());
  }
}
