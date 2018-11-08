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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.javaparser.utils.CodeGenerationUtils;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * This class is used to generate OpenAPI document from a given java project.
 */
public class Generator {

  /**
   * Executable method of the class
   *
   * @param args
   *         arguments list
   */
  public static void main(String[] args) {
    OpenApiGenerator generator = new OpenApiJavaParserImpl();

    Path inputPath = CodeGenerationUtils.mavenModuleRoot(Generator.class)
      .resolveSibling(
        "vaadin-connect-demo/src/main/java/com/vaadin/connect/demo");
    generator.setSourcePath(inputPath);

    generator.setOpenApiConfiguration(
      new OpenApiConfiguration("Demo Application", "0.0.1",
        "http://localhost:8080", "Demo server"));
    OpenAPI openAPI = generator.generateOpenApi();

    writeToFile(openAPI);
  }

  private static void writeToFile(OpenAPI openAPI) {
    Path outputPath = CodeGenerationUtils.mavenModuleRoot(Generator.class)
      .resolve("target/generated-resources/openapi.json");
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
      Logger.getLogger(Generator.class.getName())
        .log(Level.WARNING, "Can't write to file", e);
    }
  }
}
