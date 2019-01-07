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

package com.vaadin.connect.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Vaadin Connect mojo base class, defining common Maven properties that are
 * used by all mojo classes.
 */
abstract class VaadinConnectMojoBase extends AbstractMojo {
  private static final Logger log = LoggerFactory
      .getLogger(VaadinConnectMojoBase.class);

  @Parameter(defaultValue = "${project.basedir}/src/main/resources/application.properties")
  private File applicationProperties;

  @Parameter(defaultValue = "${project.build.directory}/generated-resources/openapi.json", required = true)
  protected File openApiJsonFile;

  @Parameter(defaultValue = "${project.basedir}/frontend/generated/", required = true)
  protected File generatedFrontendDirectory;

  /**
   * Reads application properties from the
   * {@link VaadinConnectMojoBase#applicationProperties} path. If there are no
   * file or the file read fails, an empty properties are returned.
   *
   * @return application properties, if any, empty properties otherwise
   */
  protected Properties readApplicationProperties() {
    Properties properties = new Properties();
    if (applicationProperties != null && applicationProperties.exists()) {
      try (BufferedReader reader = Files
          .newBufferedReader(applicationProperties.toPath())) {
        properties.load(reader);
      } catch (IOException e) {
        log.info("Can't read the application.properties file from {}",
            applicationProperties, e);
      }
    } else {
      log.debug(
          "Found no application properties at '{}', using default values.",
          applicationProperties);
    }
    return properties;
  }
}
