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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

public final class TestUtils {
  private TestUtils() {
  }

  public static Properties readProperties(String filePath) {
    Properties properties = new Properties();
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
      properties.load(reader);
    } catch (IOException e) {
      throw new AssertionError(
          String.format("Failed to read the properties file '%s", filePath));
    }
    return properties;
  }

  public static String getExpectedJson(Class<?> resourceOwner, String name) {
    try (BufferedReader input = new BufferedReader(
        new InputStreamReader(resourceOwner.getResourceAsStream(name)))) {
      return input.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      return "";
    }
  }
}
