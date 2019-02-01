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

import javax.annotation.security.DenyAll;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.vaadin.connect.VaadinService;
import com.vaadin.connect.oauth.VaadinConnectOAuthAclChecker;
import com.vaadin.connect.plugin.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

// TODO kb naming, texts in asserts.
public final class GenericOpenApiTest {
  private static final List<Class<?>> JSON_NUMBER_CLASSES = Arrays.asList(
      Number.class, byte.class, char.class, short.class, int.class, long.class,
      float.class, double.class);

  private static final VaadinConnectOAuthAclChecker securityChecker = new VaadinConnectOAuthAclChecker();

  private GenericOpenApiTest() {
  }

  public static void verifyJson(Package testPackage, Path actualJsonOutput,
      URL customApplicationProperties, URL expectedJsonResourceUrl) {
    new OpenApiSpecGenerator(
        customApplicationProperties == null ? new PropertiesConfiguration()
            : TestUtils.readProperties(customApplicationProperties.getPath()))
                .generateOpenApiSpec(
                    Collections.singletonList(
                        java.nio.file.Paths.get("src/test/java", testPackage
                            .getName().replace('.', File.separatorChar))),
                    actualJsonOutput);

    Assert.assertTrue("No generated json found",
        actualJsonOutput.toFile().exists());
    String actualJson;
    try {
      actualJson = StringUtils.toEncodedString(
          Files.readAllBytes(actualJsonOutput), Charset.defaultCharset());
    } catch (IOException e) {
      throw new AssertionError(String
          .format("Failed to read generated json at '%s'", actualJsonOutput));
    }

    assertEquals(TestUtils.readResource(expectedJsonResourceUrl), actualJson);
  }

  public static void verifyOpenApi(Package testPackage,
      Class<?>... testServiceClasses) {
    OpenAPI actualOpenAPI = getOpenApiObject(testPackage);

    List<Class<?>> serviceClasses = new ArrayList<>();
    List<Class<?>> nonServiceClasses = new ArrayList<>();
    collectServiceClasses(serviceClasses, nonServiceClasses,
        testServiceClasses);

    assertPaths(actualOpenAPI.getPaths(), serviceClasses);

    Optional.ofNullable(actualOpenAPI.getComponents())
        .map(Components::getSchemas).ifPresent(
            schemas -> assertComponentSchemas(schemas, nonServiceClasses));
  }

  private static OpenAPI getOpenApiObject(Package testPackage) {
    OpenApiObjectGenerator generator = new OpenApiObjectGenerator();

    Path javaSourcePath = java.nio.file.Paths.get("src/test/java/",
        testPackage.getName().replace('.', File.separatorChar));
    generator.addSourcePath(javaSourcePath);

    generator.setOpenApiConfiguration(new OpenApiConfiguration("Test title",
        "0.0.1", "https://server.test", "Test description"));

    return generator.getOpenApi();
  }

  private static void collectServiceClasses(List<Class<?>> serviceClasses,
      List<Class<?>> nonServiceClasses, Class<?>... inputClasses) {
    for (Class<?> testServiceClass : inputClasses) {
      if (testServiceClass.isAnnotationPresent(VaadinService.class)) {
        serviceClasses.add(testServiceClass);
      } else {
        nonServiceClasses.add(testServiceClass);
      }
      collectServiceClasses(serviceClasses, nonServiceClasses,
          testServiceClass.getDeclaredClasses());
    }
  }

  private static void assertPaths(Paths actualPaths,
      List<Class<?>> testServiceClasses) {
    int pathCount = 0;
    for (Class<?> testServiceClass : testServiceClasses) {
      for (Method expectedServiceMethod : testServiceClass
          .getDeclaredMethods()) {
        if (!Modifier.isPublic(expectedServiceMethod.getModifiers())
            || securityChecker.getSecurityTarget(expectedServiceMethod)
                .isAnnotationPresent(DenyAll.class)) {
          continue;
        }
        pathCount++;
        String expectedServiceUrl = String.format("/%s/%s",
            getServiceName(testServiceClass), expectedServiceMethod.getName());
        PathItem actualPath = actualPaths.get(expectedServiceUrl);
        assertNotNull(actualPath);
        assertPath(testServiceClass, expectedServiceMethod, actualPath);
      }
    }
    assertEquals(pathCount, actualPaths.size());
  }

  private static String getServiceName(Class<?> testServiceClass) {
    String customName = testServiceClass.getAnnotation(VaadinService.class)
        .value();
    return customName.isEmpty() ? testServiceClass.getSimpleName() : customName;
  }

  private static void assertPath(Class<?> testServiceClass,
      Method expectedServiceMethod, PathItem actualPath) {
    Operation actualOperation = actualPath.getPost();
    assertEquals(actualOperation.getTags(),
        Collections.singletonList(testServiceClass.getSimpleName()));
    assertTrue(actualOperation.getOperationId()
        .contains(getServiceName(testServiceClass)));
    assertTrue(actualOperation.getOperationId()
        .contains(expectedServiceMethod.getName()));

    if (expectedServiceMethod.getParameterCount() > 0) {
      Schema requestSchema = extractSchema(
          actualOperation.getRequestBody().getContent());
      assertRequestSchema(requestSchema,
          expectedServiceMethod.getParameterTypes());
    }

    ApiResponses responses = actualOperation.getResponses();
    assertEquals(1, responses.size());
    ApiResponse apiResponse = responses.get("200");
    assertNotNull(apiResponse);

    if (expectedServiceMethod.getReturnType() != void.class) {
      assertSchema(extractSchema(apiResponse.getContent()),
          expectedServiceMethod.getReturnType());
    }

    if (securityChecker.requiresAuthentication(expectedServiceMethod)) {
      assertNotNull(actualOperation.getSecurity());
    } else {
      assertNull(actualOperation.getSecurity());
    }
  }

  private static void assertRequestSchema(Schema requestSchema,
      Class<?>... parameterTypes) {
    Map<String, Schema> properties = requestSchema.getProperties();
    assertEquals(parameterTypes.length, properties.size());
    int index = 0;
    for (Schema propertySchema : properties.values()) {
      assertSchema(propertySchema, parameterTypes[index]);
      index++;
    }
  }

  private static Schema extractSchema(Content content) {
    assertEquals(1, content.size());
    return content.get("application/json").getSchema();
  }

  private static void assertComponentSchemas(Map<String, Schema> actualSchemas,
      List<Class<?>> testServiceClasses) {
    int schemasCount = 0;
    for (Class<?> expectedSchemaClass : testServiceClasses) {
      schemasCount++;
      Schema actualSchema = actualSchemas
          .get(expectedSchemaClass.getSimpleName());
      assertNotNull(actualSchema);
      assertSchema(actualSchema, expectedSchemaClass);
    }
    assertEquals(schemasCount, actualSchemas.size());
  }

  private static void assertSchema(Schema actualSchema,
      Class<?> expectedSchemaClass) {
    if (expectedSchemaClass.isArray()) {
      assertEquals("array", actualSchema.getType());
      assertTrue(actualSchema instanceof ArraySchema);
      assertSchema(((ArraySchema) actualSchema).getItems(),
          expectedSchemaClass.getComponentType());
    } else if (String.class.isAssignableFrom(expectedSchemaClass)) {
      assertTrue(actualSchema instanceof StringSchema);
    } else if (boolean.class.isAssignableFrom(expectedSchemaClass)
        || Boolean.class.isAssignableFrom(expectedSchemaClass)) {
      assertTrue(actualSchema instanceof BooleanSchema);
    } else if (JSON_NUMBER_CLASSES.stream()
        .anyMatch(jsonNumberClass -> jsonNumberClass
            .isAssignableFrom(expectedSchemaClass))) {
      assertTrue(actualSchema instanceof NumberSchema);
    } else if (Collection.class.isAssignableFrom(expectedSchemaClass)) {
      assertTrue(actualSchema instanceof ArraySchema);
    } else if (Map.class.isAssignableFrom(expectedSchemaClass)) {
      assertTrue(actualSchema instanceof MapSchema);
    } else {
      assertTrue(actualSchema instanceof ObjectSchema);
      if (actualSchema.get$ref() == null) {
        Map<String, Schema> properties = actualSchema.getProperties();
        assertNotNull(properties);
        assertTrue(properties.size() > 0);

        int expectedFieldsCount = 0;
        for (Field expectedSchemaField : expectedSchemaClass
            .getDeclaredFields()) {
          if (Modifier.isTransient(expectedSchemaField.getModifiers())) {
            continue;
          }

          expectedFieldsCount++;
          Schema propertySchema = properties.get(expectedSchemaField.getName());
          assertSchema(propertySchema, expectedSchemaField.getType());
        }
        assertEquals(expectedFieldsCount, properties.size());
      } else {
        assertNull(actualSchema.getProperties());
      }
    }
  }
}
