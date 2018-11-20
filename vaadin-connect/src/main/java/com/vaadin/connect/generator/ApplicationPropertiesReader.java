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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a helper to read the application.properties file.
 */
public class ApplicationPropertiesReader {

  static final String DEFAULT_APPLICATION_PROPERTIES_PATH = "src/main/resources/application.properties";
  static final String APPLICATION_TITLE = "vaadin.connect.application.title";
  static final String APPLICATION_API_VERSION = "vaadin.connect.api.version";
  static final String ENDPOINT = "vaadin.connect.endpoint";
  static final String SERVER = "vaadin.connect.server";
  static final String SERVER_DESCRIPTION = "vaadin.connect.server.description";

  private static final Pattern PROPERTIES_REGEX = Pattern.compile("^(.*)=(.*)$",
      Pattern.MULTILINE);

  private ApplicationPropertiesReader() {
    // noop
  }

  /**
   * Read the application.properties file and return a map with propertyName as
   * key and propertyValue as value
   * 
   * @param inputPath
   *          input path of the application.properties
   * @return a map of properties or empty map if the file doesn't exist
   */
  public static Map<String, String> readProperties(Path inputPath) {
    if (!inputPath.toFile().exists()) {
      getLogger().warn("There is no application.properties in {}", inputPath);
      return Collections.emptyMap();
    }
    Map<String, String> applicationProperties = new HashMap<>();
    try {
      for (String line : Files.readAllLines(inputPath)) {
        Matcher matcher = PROPERTIES_REGEX.matcher(line);
        if (!matcher.matches()) {
          continue;
        }
        applicationProperties.put(matcher.group(1), matcher.group(2));
      }
    } catch (IOException e) {
      getLogger().error("Can't read the application.properties file", e);
    }
    return applicationProperties;
  }

  private static Logger getLogger() {
    return LoggerFactory.getLogger(ApplicationPropertiesReader.class);
  }
}
