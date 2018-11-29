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

package com.vaadin.connect.plugin.generator;

import java.nio.file.Path;
import java.util.Properties;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.connect.plugin.generator.GeneratorUtils.DEFAULT_ENDPOINT;
import static com.vaadin.connect.plugin.generator.GeneratorUtils.ENDPOINT;

/**
 * A generator class that creates the open api specification file from the
 * sources provided.
 */
public class OpenApiSpecGenerator {
  private static final String APPLICATION_TITLE = "vaadin.connect.application.title";
  private static final String APPLICATION_API_VERSION = "vaadin.connect.api.version";
  private static final String SERVER = "vaadin.connect.server";
  private static final String SERVER_DESCRIPTION = "vaadin.connect.server.description";
  private static final String DEFAULT_SERVER = "http://localhost:8080";
  private static final String DEFAULT_SERVER_DESCRIPTION = "Vaadin Connect backend";
  private static final String DEFAULT_APPLICATION_TITLE = "Vaadin Connect Application";
  private static final String DEFAULT_APPLICATION_API_VERSION = "0.0.1";

  private static final Logger log = LoggerFactory
      .getLogger(OpenApiSpecGenerator.class);
  private final OpenApiParser generator;

  /**
   * Creates the generator, getting the data needed for the generation out of
   * the application properties.
   *
   * @param applicationProperties
   *          the properties with the data required for the generation
   */
  public OpenApiSpecGenerator(Properties applicationProperties) {
    generator = new OpenApiParser();
    generator.setOpenApiConfiguration(
        extractOpenApiConfiguration(applicationProperties));
  }

  /**
   * Generates the open api spec file based on the sources provided.
   *
   * @param sourcesPath
   *          the source root to be analyzed
   * @param specOutputFile
   *          the target file to write the generation output to
   */
  public void generateOpenApiSpec(Path sourcesPath, Path specOutputFile) {
    generator.setSourcePath(sourcesPath);
    log.info("Parsing java files from {}", sourcesPath);
    OpenAPI openAPI = generator.generateOpenApi();

    log.info("Writing output to {}", specOutputFile);
    GeneratorUtils.writeToFile(specOutputFile, Json.pretty(openAPI));
  }

  private OpenApiConfiguration extractOpenApiConfiguration(
      Properties applicationProperties) {
    String endpoint = applicationProperties.getProperty(ENDPOINT,
        DEFAULT_ENDPOINT);
    String server = StringUtils.removeEnd(
        applicationProperties.getProperty(SERVER, DEFAULT_SERVER), "/");
    String serverDescription = applicationProperties
        .getProperty(SERVER_DESCRIPTION, DEFAULT_SERVER_DESCRIPTION);
    String applicationTitle = applicationProperties
        .getProperty(APPLICATION_TITLE, DEFAULT_APPLICATION_TITLE);
    String applicationApiVersion = applicationProperties
        .getProperty(APPLICATION_API_VERSION, DEFAULT_APPLICATION_API_VERSION);
    return new OpenApiConfiguration(applicationTitle, applicationApiVersion,
        server + endpoint, serverDescription);
  }
}
