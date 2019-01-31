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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import com.vaadin.connect.VaadinService;
import com.vaadin.connect.oauth.VaadinConnectOAuthAclChecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

// TODO kb naming, tests in asserts. Make it as a base abstract class or a static method.
public class GenericOpenApiTest {

  public void verify(OpenAPI actualOpenAPI, Class<?> testServiceClass) {
    assertTrue(testServiceClass.isAnnotationPresent(VaadinService.class));
    assertPaths(actualOpenAPI.getPaths(), testServiceClass);
    assertComponentSchemas(actualOpenAPI.getComponents().getSchemas(),
        testServiceClass);
  }

  private void assertPaths(Paths actualPaths, Class<?> testServiceClass) {
    Method[] expectedServiceMethods = testServiceClass.getDeclaredMethods();
    assertEquals(expectedServiceMethods.length, actualPaths.size());

    for (Method expectedServiceMethod : expectedServiceMethods) {
      String expectedServiceUrl = String.format("/%s/%s",
          testServiceClass.getSimpleName(), expectedServiceMethod.getName());
      PathItem actualPath = actualPaths.get(expectedServiceUrl);
      assertNotNull(actualPath);
      assertPath(testServiceClass, expectedServiceMethod, actualPath);
    }
  }

  private void assertPath(Class<?> testServiceClass,
      Method expectedServiceMethod, PathItem actualPath) {
    Operation actualOperation = actualPath.getPost();
    assertEquals(actualOperation.getTags(),
        Collections.singletonList(testServiceClass.getSimpleName()));
    assertTrue(actualOperation.getOperationId()
        .contains(testServiceClass.getSimpleName()));
    assertTrue(actualOperation.getOperationId()
        .contains(expectedServiceMethod.getName()));
    assertNotNull(actualOperation.getDescription());

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

    assertSchema(extractSchema(apiResponse.getContent()),
        expectedServiceMethod.getReturnType());

    if (new VaadinConnectOAuthAclChecker()
        .requiresAuthentication(expectedServiceMethod)) {
      assertNotNull(actualOperation.getSecurity());
    } else {
      assertNull(actualOperation.getSecurity());
    }
  }

  private void assertRequestSchema(Schema requestSchema,
      Class<?>... parameterTypes) {
    Map<String, Schema> properties = requestSchema.getProperties();
    assertEquals(parameterTypes.length, properties.size());
    int index = 0;
    for (Schema propertySchema : properties.values()) {
      assertSchema(propertySchema, parameterTypes[index]);
      index++;
    }
  }

  private Schema extractSchema(Content content) {
    assertEquals(1, content.size());
    return content.get("application/json").getSchema();
  }

  private void assertComponentSchemas(Map<String, Schema> actualSchemas,
      Class<?> testServiceClass) {
    Class<?>[] declaredClasses = testServiceClass.getDeclaredClasses();
    assertEquals(declaredClasses.length, actualSchemas.size());

    for (Class<?> expectedSchemaClass : declaredClasses) {
      Schema actualSchema = actualSchemas
          .get(expectedSchemaClass.getSimpleName());
      assertNotNull(actualSchema);
      assertSchema(actualSchema, expectedSchemaClass);
    }
  }

  private void assertSchema(Schema actualSchema, Class<?> expectedSchemaClass) {
    if (expectedSchemaClass.isArray()) {
      assertEquals("array", actualSchema.getType());
      assertTrue(actualSchema instanceof ArraySchema);
      assertSchema(((ArraySchema) actualSchema).getItems(),
          expectedSchemaClass.getComponentType());
    } else if (String.class.isAssignableFrom(expectedSchemaClass)) {
      assertEquals("string", actualSchema.getType());
    } else if (Number.class.isAssignableFrom(expectedSchemaClass)) {
      assertEquals("number", actualSchema.getType());
    } else if (Collection.class.isAssignableFrom(expectedSchemaClass)) {
      assertEquals("array", actualSchema.getType());
    } else {
      assertEquals("object", actualSchema.getType());
      if (actualSchema.get$ref() == null) {
        Map<String, Schema> properties = actualSchema.getProperties();
        assertNotNull(properties);
        assertTrue(properties.size() > 0);
        assertEquals(expectedSchemaClass.getDeclaredFields().length,
            properties.size());

        for (Field expectedSchemaField : expectedSchemaClass
            .getDeclaredFields()) {
          Schema propertySchema = properties.get(expectedSchemaField.getName());
          assertSchema(propertySchema, expectedSchemaField.getType());
        }
      } else {
        assertNull(actualSchema.getProperties());
      }
    }
  }
}
