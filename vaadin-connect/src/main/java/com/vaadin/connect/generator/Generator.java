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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PropertyResourceBundle;

import com.google.common.base.Charsets;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to generate OpenAPI document from a given java project.
 */
public class Generator {

  static final String DEFAULT_APPLICATION_PROPERTIES_PATH = "src/main/resources/application.properties";
  static final String ENDPOINT = "vaadin.connect.endpoint";
  static final String DEFAULT_ENDPOINT = "/connect";

  private static final String APPLICATION_TITLE = "vaadin.connect.application.title";
  private static final String APPLICATION_API_VERSION = "vaadin.connect.api.version";
  private static final String SERVER = "vaadin.connect.server";
  private static final String SERVER_DESCRIPTION = "vaadin.connect.server.description";
  private static final String DEFAULT_SERVER = "http://localhost:8080";
  private static final String DEFAULT_SERVER_DESCRIPTION = "Vaadin Connect backend";
  private static final String DEFAULT_APPLICATION_TITLE = "Vaadin Connect Application";
  private static final String DEFAULT_APPLICATION_API_VERSION = "0.0.1";

  private static final String DEFAULT_JAVA_SOURCE_PATH = "src/main/java";
  private static final String DEFAULT_OUTPUT_PATH = "target/generated-resources/openapi.json";

  /**
   * This main method will take arguments:
   * <ul>
   * <li>
   * <code>input="the java source path that will be parsed to OpenApi spec"</code>
   * <br>
   * Default value: "/current-directory/src/main/java"</li>
   * <li><code>output="the output path of the generated OpenApi json"</code>
   * <br>
   * Default value:
   * "/current-directory/target/generated-resources/openapi.json"</li>
   * <li>
   * <code>applicationProperties="the spring application.properties path"</code>
   * which has configurations for {@link Generator#SERVER},
   * {@link Generator#SERVER_DESCRIPTION}, {@link Generator#APPLICATION_TITLE},
   * {@link Generator#APPLICATION_API_VERSION}. <br>
   * Default value:
   * "/current-directory/src/main/resources/application.properties"</li>
   * </ul>
   *
   * <pre>
   * Example usage:
   * <code>java -cp vaadin-connect.jar com.vaadin.connect.generator.Generator
   * input=/home/user/my-input-source/ output=/home/user/output/openapi.json
   * applicationProperties=/home/user/myapp/src/main/resources/application.properties</code>
   *
   * </pre>
   *
   * @param args
   *          arguments list
   */
  public static void main(String[] args) {

    Path inputPath = getJavaSourcePath(args);
    Path outputPath = getOutputPath(args);
    OpenApiConfiguration configuration = readApplicationProperties(args);

    OpenApiGenerator generator = new OpenApiJavaParserImpl();
    generator.setSourcePath(inputPath);
    generator.setOpenApiConfiguration(configuration);
    getLogger().info("Parsing java files from {}", inputPath);
    OpenAPI openAPI = generator.generateOpenApi();

    getLogger().info("Writing output to {}", outputPath);
    writeToFile(outputPath, Json.pretty(openAPI));
  }

  private static Path getOutputPath(String[] args) {
    String input = getArgument(args, "output", DEFAULT_OUTPUT_PATH);
    return Paths.get(input).toAbsolutePath();
  }

  private static Path getJavaSourcePath(String[] args) {
    String input = getArgument(args, "input", DEFAULT_JAVA_SOURCE_PATH);
    return Paths.get(input).toAbsolutePath();
  }

  static Path getApplicationPropertiesPath(String[] args) {
    String input = getArgument(args, "applicationProperties",
        DEFAULT_APPLICATION_PROPERTIES_PATH);
    return Paths.get(input).toAbsolutePath();
  }

  static String getArgument(String[] args, String argumentName,
      String defaultValue) {
    for (String input : args) {
      String[] parts = input.split("=");
      if (argumentName.equalsIgnoreCase(parts[0])) {
        return parts[1];
      }
    }
    return defaultValue;
  }

  private static OpenApiConfiguration readApplicationProperties(String[] args) {
    Path applicationProperties = getApplicationPropertiesPath(args);
    try {
      PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(
          Files.newBufferedReader(applicationProperties));
      String endpoint = getProperties(propertyResourceBundle, ENDPOINT,
          DEFAULT_ENDPOINT);
      String server = StringUtils.removeEnd(
          getProperties(propertyResourceBundle, SERVER, DEFAULT_SERVER), "/");
      String serverDescription = getProperties(propertyResourceBundle,
          SERVER_DESCRIPTION, DEFAULT_SERVER_DESCRIPTION);
      String applicationTitle = getProperties(propertyResourceBundle,
          APPLICATION_TITLE, DEFAULT_APPLICATION_TITLE);
      String applicationApiVersion = getProperties(propertyResourceBundle,
          APPLICATION_API_VERSION, DEFAULT_APPLICATION_API_VERSION);
      return new OpenApiConfiguration(applicationTitle, applicationApiVersion,
          server + endpoint, serverDescription);
    } catch (IOException e) {
      getLogger().info("Can't read the application.properties file from "
          + applicationProperties, e);
    }
    return new OpenApiConfiguration(DEFAULT_APPLICATION_TITLE,
        DEFAULT_APPLICATION_API_VERSION, DEFAULT_SERVER + DEFAULT_ENDPOINT,
        DEFAULT_SERVER_DESCRIPTION);
  }

  /**
   * Write to the output path a string content.
   *
   * @param outputPath
   *          output path
   * @param content
   *          content to write
   */
  public static void writeToFile(Path outputPath, String content) {
    try {
      File parentFolder = outputPath.toFile().getParentFile();
      if (!parentFolder.exists()) {
        parentFolder.mkdirs();
      }
      if (!outputPath.toFile().exists()) {
        Files.createFile(outputPath);
      }
      try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputPath,
          Charsets.UTF_8)) {
        bufferedWriter.write(content);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Logger getLogger() {
    return LoggerFactory.getLogger(Generator.class);
  }

  private static String getProperties(
      PropertyResourceBundle propertyResourceBundle, String propertyName,
      String defaultValue) {
    String propertyValue = (String) propertyResourceBundle
        .handleGetObject(propertyName);
    return propertyValue != null ? propertyValue : defaultValue;
  }
}
