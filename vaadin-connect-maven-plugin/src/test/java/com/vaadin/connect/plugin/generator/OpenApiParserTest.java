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

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.connect.plugin.GenericOpenApiTest;
import com.vaadin.connect.plugin.generator.collectionservice.CollectionTestService;
import com.vaadin.connect.plugin.generator.denyall.DenyAllClass;
import com.vaadin.connect.plugin.generator.service.GeneratorTestClass;

public class OpenApiParserTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void Should_GenerateCorrectOpenApiModel_When_AProperPathAndConfigurationAreSet() {
    OpenApiParser generator = getGenerator("service");

    OpenAPI openAPI = generator.getOpenApi();
    GenericOpenApiTest.verify(openAPI, GeneratorTestClass.class);
    GenericOpenApiTest.verifyJson(openAPI,
        getClass().getResource("expected-openapi.json"));
  }

  @Test
  public void Should_Fail_When_UsingReservedWordInMethod() {
    expected.expect(IllegalStateException.class);
    expected.expectMessage("reserved");
    getGenerator("reservedwordmethod").generateOpenApi();
  }

  @Test
  public void Should_Fail_When_UsingReservedWordInClass() {
    expected.expect(IllegalStateException.class);
    expected.expectMessage("reserved");
    getGenerator("reservedwordclass").generateOpenApi();
  }

  @Test
  public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName() {
    OpenAPI openAPI = getGenerator("collectionservice").generateOpenApi();
    GenericOpenApiTest.verify(openAPI, CollectionTestService.class);
  }

  @Test
  public void should_notGenerateServiceMethodsWithoutSecurityAnnotations_When_DenyAllOnClass() {
    OpenAPI openAPI = getGenerator("denyall").generateOpenApi();
    GenericOpenApiTest.verify(openAPI, DenyAllClass.class);
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
