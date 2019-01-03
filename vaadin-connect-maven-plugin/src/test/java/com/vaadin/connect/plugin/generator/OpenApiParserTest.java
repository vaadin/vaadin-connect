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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.connect.plugin.TestUtils;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenApiParserTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void Should_GenerateCorrectOpenApiModel_When_AProperPathAndConfigurationAreSet() {
    OpenApiParser generator = getGenerator("service");

    OpenAPI openAPI = generator.getOpenApi();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-openapi.json");
    Assert.assertEquals(Json.pretty(openAPI),
        Json.pretty(generator.generateOpenApi()));
    Assert.assertEquals(expectedJson, Json.pretty(openAPI));
  }

  @Test
  public void Should_Fail_When_UsingReservedWordInMethod() {
    expected.expect(IllegalStateException.class);
    getGenerator("reservedwordmethod").generateOpenApi();
  }

  @Test
  public void Should_Fail_When_UsingReservedWordInClass() {
    expected.expect(IllegalStateException.class);
    getGenerator("reservedwordclass").generateOpenApi();
  }

  private OpenApiParser getGenerator(String path) {
    OpenApiParser generator = new OpenApiParser();

    Path javaSourcePath = Paths
        .get("src/test/java/com/vaadin/connect/plugin/generator/" + path)
        .toAbsolutePath();
    generator.addSourcePath(javaSourcePath);

    generator.setOpenApiConfiguration(new OpenApiConfiguration("Test title",
        "0.0.1", "https://server.test", "Test description"));

    return generator;
  }
}
