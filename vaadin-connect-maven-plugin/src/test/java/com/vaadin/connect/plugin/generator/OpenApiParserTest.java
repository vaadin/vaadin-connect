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

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.connect.plugin.TestUtils;
import com.vaadin.connect.plugin.generator.collectionservice.CollectionTestService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OpenApiParserTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void Should_GenerateCorrectOpenApiModel_When_AProperPathAndConfigurationAreSet() {
    OpenApiParser generator = getGenerator("service");

    OpenAPI openAPI = generator.getOpenApi();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-openapi.json");
    assertEquals(Json.pretty(openAPI),
        Json.pretty(generator.generateOpenApi()));
    assertEquals(expectedJson, Json.pretty(openAPI));
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

  private Schema extractSchema(Content content) {
    assertEquals(1, content.size());
    return content.get("application/json").getSchema();
  }

  @Test
  public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName() {
    OpenAPI openAPI = getGenerator("collectionservice").generateOpenApi();
    Class<CollectionTestService> testServiceClass = CollectionTestService.class;

    io.swagger.v3.oas.models.Paths actualPaths = openAPI.getPaths();
    Method[] expectedServiceMethods = testServiceClass.getDeclaredMethods();
    assertEquals(expectedServiceMethods.length, actualPaths.size());
    Stream.of(expectedServiceMethods).forEach(expectedServiceMethod -> {
      String expectedServiceUrl = String.format("/%s/%s",
          testServiceClass.getSimpleName(), expectedServiceMethod.getName());
      PathItem actualPath = actualPaths.get(expectedServiceUrl);
      assertNotNull(actualPath);
      Operation actualOperation = actualPath.getPost();
      assertEquals(actualOperation.getTags(),
          Collections.singletonList(testServiceClass.getSimpleName()));
      assertNotNull(actualOperation.getOperationId());
      assertNotNull(actualOperation.getDescription());

      if (expectedServiceMethod.getParameterCount() > 0) {
        // TODO kb
        Schema requestSchema = extractSchema(
            actualOperation.getRequestBody().getContent());
      }

      ApiResponses responses = actualOperation.getResponses();
      assertEquals(1, responses.size());
      ApiResponse apiResponse = responses.get("200");
      assertNotNull(apiResponse);

      // TODO kb
      Schema responseSchema = extractSchema(apiResponse.getContent());

      // TODO kb should we assert it at all?
      actualOperation.getSecurity();
    });

    Map<String, Schema> actualSchemas = openAPI.getComponents().getSchemas();
    Stream.of(testServiceClass.getDeclaredClasses())
        .forEach(expectedSchemaClass -> {
          Schema actualSchema = actualSchemas
              .get(expectedSchemaClass.getSimpleName());
          assertNotNull(actualSchema);
          Stream.of(expectedSchemaClass.getDeclaredFields())
              .forEach(expectedSchemaField -> {
                Object schemaType = actualSchema.getProperties()
                    .get(expectedSchemaField.getName());
                Assert.assertTrue(
                    String.format("Unknown type '%s', expected '%s'",
                        schemaType, expectedSchemaField.getType()),
                    schemaType.getClass().getSimpleName()
                        .toLowerCase(Locale.ENGLISH)
                        .contains(expectedSchemaField.getType().getSimpleName()
                            .toLowerCase(Locale.ENGLISH)));
              });
        });
  }

  @Test
  public void should_notGenerateServiceMethodsWithoutSecurityAnnotations_When_DenyAllOnClass() {
    OpenAPI openAPI = getGenerator("denyall").generateOpenApi();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-denyall-service.json");

    String actualJson = Json.pretty(openAPI);
    assertEquals(expectedJson, actualJson);
    Assert.assertFalse(actualJson.contains("shouldBeDenied"));
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
