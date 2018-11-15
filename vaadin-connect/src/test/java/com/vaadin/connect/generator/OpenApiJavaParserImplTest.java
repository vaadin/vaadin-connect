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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenApiJavaParserImplTest {

  private OpenApiGenerator generator;
  private OpenApiConfiguration configuration;
  private Path javaSourcePath;

  @Before
  public void setUp() {
    generator = new OpenApiJavaParserImpl();
    configuration = new OpenApiConfiguration("Test title", "0.0.1",
      "https://server.test", "Test description");
    javaSourcePath = Paths.get("src/test/java/com/vaadin/connect/generator")
      .toAbsolutePath();
    generator.setSourcePath(javaSourcePath);
    generator.setOpenApiConfiguration(configuration);
  }

  @Test
  public void Should_GenerateCorrectOpenApiModel_When_AProperPathAndConfigurationAreSet() {
    OpenAPI openAPI = generator.getOpenApi();
    String expectedJson = OpenApiJavaParserImplTest.getExpectedJson("expected-openapi.json");
    Assert.assertEquals(Json.pretty(openAPI),
      Json.pretty(generator.generateOpenApi()));
    Assert.assertEquals(expectedJson, Json.pretty(openAPI));
  }

  static String getExpectedJson(String name) {
    try (InputStream input = OpenApiJavaParserImplTest.class
      .getResourceAsStream(name)) {
      return new BufferedReader(new InputStreamReader(input)).lines()
        .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      return "";
    }
  }

}
