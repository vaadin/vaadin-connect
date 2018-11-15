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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * This class is used to generate OpenAPI document from a given java project.
 */
public class Generator {
  private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

  private static final String APPLICATION_TITLE = "vaadin.connect.application.title";
  private static final String APPLICATION_API_VERSION = "vaadin.connect.api.version";
  private static final String ENDPOINT = "vaadin.connect.endpoint";
  private static final String SERVER = "vaadin.connect.server";
  private static final String SERVER_DESCRIPTION = "vaadin.connect.server.description";
  private static final String DEFAULT_JAVA_SOURCE_PATH = "src/main/java";
  private static final String DEFAULT_OUTPUT_PATH = "target/generated-resources/openapi.json";
  private static final String DEFAULT_APPLICATION_PROPERTIES_PATH = "src/main/resources/application.properties";

  /**
   * This main method will take:
   * <ul>
   * <li>The first program argument as the java source path
   * that will be parsed to OpenApi spec. Default value:
   * "/<current-directory/src/main/java"
   * </li>
   * <li>
   * The second program argument as the output path of the generated OpenApi
   * json. Default value: "/<current-directory/target/generated-resources/openapi.json"
   * </li>
   * <li>The third program argument as the spring application.properties path
   * which has configurations for {@link Generator#SERVER}, {@link
   * Generator#SERVER_DESCRIPTION}, {@link Generator#APPLICATION_TITLE}, {@link
   * Generator#APPLICATION_API_VERSION}. Default value: "/<current-directory/src/main/resources/application.properties"
   * </li>
   * </ul>
   *
   * <pre>
   * Example usage:
   * <code>java -cp vaadin-connect.jar com.vaadin.connect.generator.Generator
   * "/home/user/my-input-source/" "/home/user/output/openapi.json"
   * "/home/user/myapp/src/main/resources/application.properties"</code>
   *
   * </pre>
   *
   * @param args
   *         arguments list
   */
  public static void main(String[] args) {

    Path inputPath = getJavaSourcePath(args);
    Path outputPath = getOutputPath(args);
    OpenApiConfiguration configuration = readApplicationProperties(args);

    OpenApiGenerator generator = new OpenApiJavaParserImpl();
    generator.setSourcePath(inputPath);
    generator.setOpenApiConfiguration(configuration);
    LOGGER.info("Parsing java files from {}", inputPath);
    OpenAPI openAPI = generator.generateOpenApi();

    LOGGER.info("Writing output to {}", outputPath);
    writeToFile(openAPI, outputPath);
  }

  private static Path getOutputPath(String[] args) {
    Path outputPath;
    if (args.length >= 2) {
      outputPath = Paths.get(args[1]);
    } else {
      outputPath = Paths.get(DEFAULT_OUTPUT_PATH).toAbsolutePath();
    }
    return outputPath;
  }

  private static Path getJavaSourcePath(String[] args) {
    Path inputPath;
    if (args.length >= 1) {
      inputPath = Paths.get(args[0]);
    } else {
      inputPath = Paths.get(DEFAULT_JAVA_SOURCE_PATH).toAbsolutePath();
    }
    return inputPath;
  }

  private static OpenApiConfiguration readApplicationProperties(String[] args) {
    Path applicationProperties;
    if (args.length >= 3) {
      applicationProperties = Paths.get(args[2]);
    } else {
      applicationProperties = Paths.get(DEFAULT_APPLICATION_PROPERTIES_PATH);
    }
    String endpoint = "connect";
    String server = "https://localhost:8080/";
    String serverDescription = "Vaadin Connect backend";
    String applicationTitle = "Vaadin Connect Application";
    String applicationApiVersion = "0.0.1";
    if (!applicationProperties.toFile().exists()) {
      LOGGER.warn("There is no application.properties in {}",
        applicationProperties);
      return new OpenApiConfiguration(applicationTitle, applicationApiVersion,
        server, serverDescription);
    }
    try {
      Pattern regex = Pattern.compile("^(.*)=(.*)$", Pattern.MULTILINE);
      for (String line : Files.readAllLines(applicationProperties)) {
        Matcher matcher = regex.matcher(line);
        if (!matcher.matches()) {
          continue;
        }
        String propertyName = matcher.group(1);
        String propertyValue = matcher.group(2);
        switch (propertyName) {
        case ENDPOINT:
          endpoint = propertyValue;
          break;
        case SERVER:
          server = StringUtils.appendIfMissing(propertyValue, "/");
          break;
        case SERVER_DESCRIPTION:
          serverDescription = propertyValue;
          break;
        case APPLICATION_TITLE:
          applicationTitle = propertyValue;
          break;
        case APPLICATION_API_VERSION:
          applicationApiVersion = propertyValue;
          break;
        default:
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.error("Can't read the application.properties file", e);
    }
    return new OpenApiConfiguration(applicationTitle, applicationApiVersion,
      server + endpoint, serverDescription);

  }

  private static void writeToFile(OpenAPI openAPI, Path outputPath) {

    try {
      File parentFolder = outputPath.toFile().getParentFile();
      if (!parentFolder.exists()) {
        parentFolder.mkdirs();
      }
      if (!outputPath.toFile().exists()) {
        Files.createFile(outputPath);
      }
      Files.write(outputPath, Json.pretty(openAPI).getBytes());
    } catch (IOException e) {
      LOGGER.error("Can't write to file", e);
    }
  }
}
