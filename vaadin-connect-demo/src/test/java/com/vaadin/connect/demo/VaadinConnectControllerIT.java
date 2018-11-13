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
package com.vaadin.connect.demo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VaadinConnectControllerIT {
  private static final String TEST_SERVICE_NAME = TestService.class
      .getSimpleName();

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate template;

  @Test
  public void simpleMethodExecutedSuccessfully() {
    int argument = 3;
    String methodName = "addOne";
    ResponseEntity<Integer> response = sendVaadinServiceRequest(methodName,
        Collections.singletonMap("number", argument), Integer.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(String.format(
        "Method '%s' was expected to increment its argument by 1 and return it, but that did not happen",
        methodName), Integer.valueOf(argument + 1), response.getBody());
  }

  @Test
  public void wrongNumberOfArgumentsCallFails() {
    Map<String, Integer> requestObject = new HashMap<>();
    requestObject.put("number", 3);
    requestObject.put("bad_number", 33);

    String methodName = "addOne";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        requestObject, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void noArgumentCallFails() {
    String methodName = "addOne";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void wrongArgumentTypeFails() {
    String methodName = "addOne";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        Collections.singletonMap("number",
            Arrays.asList("this", "is", "wrong")),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void wrongServiceNameReturns404() {
    String serviceMethod = "addOne";
    checkMethodPresenceInService(serviceMethod, true);

    ResponseEntity<String> response = template.postForEntity(
        getRequestUrl("thisServiceNameIsWrong", serviceMethod),
        Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void wrongMethodNameReturns404() {
    String absentMethodName = "absentMethod";
    checkMethodPresenceInService(absentMethodName, false);

    ResponseEntity<String> response = sendVaadinServiceRequest(absentMethodName,
        Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void privateMethodReturns404() {
    String methodToCall = "privateMethod";
    checkMethodPresenceInService(methodToCall, true);

    ResponseEntity<String> response = sendVaadinServiceRequest(methodToCall,
        Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void noReturnNoArgumentsMethodExecutedSuccessfully() {
    ResponseEntity<String> response = sendVaadinServiceRequest(
        "noReturnNoArguments", Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("null", response.getBody());
  }

  @Test
  public void methodAndServiceCaseDoesNotMatter() {
    String serviceName1 = TEST_SERVICE_NAME.toUpperCase(Locale.ENGLISH);
    String serviceName2 = TEST_SERVICE_NAME.toLowerCase(Locale.ENGLISH);
    assertNotEquals(serviceName1, serviceName2);
    assertTrue(serviceName1.equalsIgnoreCase(serviceName2));

    String methodName = "noReturnNoArguments";
    String serviceMethod1 = methodName.toUpperCase(Locale.ENGLISH);
    String serviceMethod2 = methodName.toLowerCase(Locale.ENGLISH);
    assertNotEquals(serviceMethod1, serviceMethod2);
    assertTrue(serviceMethod1.equalsIgnoreCase(serviceMethod2));

    ResponseEntity<String> response1 = template.postForEntity(
        getRequestUrl(serviceName1, serviceMethod1), Collections.emptyMap(),
        String.class);
    ResponseEntity<String> response2 = template.postForEntity(
        getRequestUrl(serviceName2, serviceMethod2), Collections.emptyMap(),
        String.class);

    assertEquals(HttpStatus.OK, response1.getStatusCode());
    assertEquals("null", response1.getBody());
    assertEquals(
        "Expected to have identical responses for requests that differ in method name case and service name case only",
        response1, response2);
  }

  @Test
  public void complexRequestAndResponseAreSupported() {
    String name = "test";
    Map<String, Object> complexRequestPart = new HashMap<>();
    complexRequestPart.put("name", name);
    complexRequestPart.put("count", 3);

    ResponseEntity<TestService.ComplexResponse> response = sendVaadinServiceRequest(
        "complexEntitiesTest",
        Collections.singletonMap("request", complexRequestPart),
        TestService.ComplexResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    Map<Integer, List<String>> expectedResult = new HashMap<>();
    expectedResult.put(0, Collections.emptyList());
    expectedResult.put(1, Collections.singletonList("0"));
    expectedResult.put(2, Arrays.asList("0", "1"));
    assertEquals(new TestService.ComplexResponse(name, expectedResult),
        response.getBody());
  }

  @Test
  public void exceptionTest() {
    String methodName = "throwsException";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  private <T> ResponseEntity<T> sendVaadinServiceRequest(String methodName,
      Object requestData, Class<T> responseType) {
    return template.postForEntity(getRequestUrl(TEST_SERVICE_NAME, methodName),
        requestData, responseType);
  }

  private String getRequestUrl(String serviceName, String methodName) {
    return String.format("http://localhost:%d/%s/%s", port, serviceName,
        methodName);
  }

  private void checkMethodPresenceInService(String method,
      boolean shouldPresent) {
    Class<TestService> serviceClass = TestService.class;
    boolean isPresent = Stream.of(serviceClass.getDeclaredMethods())
        .map(Method::getName).anyMatch(method::equalsIgnoreCase);
    if (shouldPresent) {
      assertTrue(
          String.format("Class '%s' has no method '%s' as it was expected to",
              serviceClass, method),
          isPresent);
    } else {
      assertFalse(String.format(
          "Class '%s' has method '%s' but it was not expected to have it",
          serviceClass, method), isPresent);
    }
  }

  private void assertVaadinErrorResponse(String body, String methodName) {
    assertNotNull(body);
    assertNotNull(methodName);
    assertTrue(
        "Vaadin service error response was expected to contain service name in it",
        body.contains(TEST_SERVICE_NAME));
    assertTrue(
        "Vaadin service error response was expected to contain called method name in it",
        body.contains(methodName));
  }
}
