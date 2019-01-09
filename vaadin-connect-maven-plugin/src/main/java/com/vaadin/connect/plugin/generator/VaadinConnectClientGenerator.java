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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.vaadin.connect.plugin.generator.GeneratorUtils.DEFAULT_ENDPOINT;
import static com.vaadin.connect.plugin.generator.GeneratorUtils.ENDPOINT;

/**
 * Generates the Vaadin Connect Client file, based on the application
 * properties, if provided.
 */
public class VaadinConnectClientGenerator {
  public static final String DEFAULT_GENERATED_CONNECT_CLIENT_NAME = "connect-client.default.js";
  private final String endpoint;

  /**
   * Creates the generator, getting the data needed for the generation out of
   * the application properties.
   *
   * @param applicationProperties
   *          the properties with the data required for the generation
   */
  public VaadinConnectClientGenerator(Properties applicationProperties) {
    this.endpoint = applicationProperties.getProperty(ENDPOINT,
        DEFAULT_ENDPOINT);
  }

  /**
   * Generates the client file in the file specified.
   *
   * @param outputFilePath
   *          the file to generate the OpenAPI v3 specification of the client
   *          code into
   *
   * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
   *      specification</a>
   */
  public void generateVaadinConnectClientFile(Path outputFilePath) {
    String generatedDefaultClientJs = getDefaultClientJsTemplate()
        .replace("{{ENDPOINT}}", endpoint);
    GeneratorUtils.writeToFile(outputFilePath, generatedDefaultClientJs);
  }

  private String getDefaultClientJsTemplate() {
    try (BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(
            getClass().getClassLoader()
                .getResourceAsStream("connect-client.default.js.template"),
            StandardCharsets.UTF_8))) {
      return bufferedReader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new IllegalStateException(
          "Unable to read connect-client.default.js.template", e);
    }
  }
}
