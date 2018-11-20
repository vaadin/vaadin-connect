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

import com.google.common.base.Charsets;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class is used to generate OpenAPI document from a given java project.
 */
public class Generator {

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
   * which has configurations for {@link ApplicationPropertiesReader#SERVER},
   * {@link ApplicationPropertiesReader#SERVER_DESCRIPTION},
   * {@link ApplicationPropertiesReader#APPLICATION_TITLE},
   * {@link ApplicationPropertiesReader#APPLICATION_API_VERSION}. <br>
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
        ApplicationPropertiesReader.DEFAULT_APPLICATION_PROPERTIES_PATH);
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
    Map<String, String> propertiesMap = ApplicationPropertiesReader
        .readProperties(applicationProperties);
    String endpoint = propertiesMap.getOrDefault(
        ApplicationPropertiesReader.ENDPOINT,
        DefaultClientJsGenerator.DEFAULT_ENDPOINT);
    String server = StringUtils.removeEnd(propertiesMap.getOrDefault(
        ApplicationPropertiesReader.SERVER, "https://localhost:8080/"), "/");
    String serverDescription = propertiesMap.getOrDefault(
        ApplicationPropertiesReader.SERVER_DESCRIPTION,
        "Vaadin Connect backend");
    String applicationTitle = propertiesMap.getOrDefault(
        ApplicationPropertiesReader.APPLICATION_TITLE,
        "Vaadin Connect Application");
    String applicationApiVersion = propertiesMap.getOrDefault(
        ApplicationPropertiesReader.APPLICATION_API_VERSION, "0.0.1");
    return new OpenApiConfiguration(applicationTitle, applicationApiVersion,
        server + endpoint, serverDescription);
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
      getLogger().error("Can't write to file", e);
    }
  }

  private static Logger getLogger() {
    return LoggerFactory.getLogger(Generator.class);
  }
}
