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
import java.nio.file.Paths;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.connect.plugin.TestUtils;

public class OpenApiParserTest {

  private OpenApiParser generator;

  @Before
  public void setUp() {
    generator = new OpenApiParser();
    OpenApiConfiguration configuration = new OpenApiConfiguration("Test title",
        "0.0.1", "https://server.test", "Test description");
    Path javaSourcePath = Paths
        .get("src/test/java/com/vaadin/connect/plugin/generator")
        .toAbsolutePath();
    generator.addSourcePath(javaSourcePath);
    generator.setOpenApiConfiguration(configuration);
  }

  @Test
  public void Should_GenerateCorrectOpenApiModel_When_AProperPathAndConfigurationAreSet() {
    OpenAPI openAPI = generator.getOpenApi();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-openapi.json");
    Assert.assertEquals(Json.pretty(openAPI),
        Json.pretty(generator.generateOpenApi()));
    Assert.assertEquals(expectedJson, Json.pretty(openAPI));
  }
}
