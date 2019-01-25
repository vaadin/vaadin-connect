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
package com.vaadin.connect.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.vaadin.connect.VaadinConnectProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VaadinConnectControllerIT {
  private static final String TEST_SERVICE_NAME = DemoVaadinService.class
      .getSimpleName();
  private static boolean tokenInjected = false;

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate template;

  @Autowired
  private VaadinConnectProperties vaadinConnectProperties;

  @Before
  public void authenticate() {
    if (!tokenInjected) {
      String accessToken = getAccessToken();
      List<ClientHttpRequestInterceptor> interceptors = template
          .getRestTemplate().getInterceptors();
      interceptors.clear();
      interceptors.add((request, body, execution) -> {
        request.getHeaders().setBearerAuth(accessToken);
        return execution.execute(request, body);
      });
      tokenInjected = true;
    }
  }

  private String getAccessToken() {
    List<ClientHttpRequestInterceptor> interceptors = template.getRestTemplate()
        .getInterceptors();
    ClientHttpRequestInterceptor getAccessTokenInterceptor = (request, body,
        execution) -> {
      HttpHeaders headers = request.getHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      return execution.execute(request, body);
    };

    interceptors.add(getAccessTokenInterceptor);
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> response = template.postForEntity(
        String.format("http://localhost:%d/oauth/token", port),
        getTokenRequest(), Map.class);
    interceptors.remove(getAccessTokenInterceptor);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    return Optional.ofNullable(response.getBody())
        .map(body -> body.get("access_token")).map(Object::toString)
        .orElseThrow(() -> new AssertionError(
            "Did not get an access token from access token request"));
  }

  private MultiValueMap<String, String> getTokenRequest() {
    MultiValueMap<String, String> getTokenRequest = new LinkedMultiValueMap<>();
    getTokenRequest.put("username",
        Collections.singletonList(DemoVaadinOAuthConfiguration.TEST_LOGIN));
    getTokenRequest.put("password",
        Collections.singletonList(DemoVaadinOAuthConfiguration.TEST_PASSWORD));
    getTokenRequest.put("grant_type", Collections.singletonList("password"));
    return getTokenRequest;
  }

  @Test
  public void should_ReceiveCorrectResponse_When_CorrectRequestIsProvided() {
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
  public void should_ReceiveBadRequest_When_IncorrectNumberOfParametersProvided() {
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
  public void should_ReceiveBadRequest_When_NoParametersProvided() {
    String methodName = "addOne";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void should_ReceiveBadRequest_When_IncorrectParameterTypesProvided() {
    String methodName = "addOne";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        Collections.singletonMap("number",
            Arrays.asList("this", "is", "wrong")),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void should_ReceiveNotFound_When_WrongServiceNameProvided() {
    String serviceMethod = "addOne";
    checkMethodPresenceInService(serviceMethod, true);

    ResponseEntity<String> response = template.postForEntity(
        getRequestUrl("thisServiceNameIsWrong", serviceMethod),
        Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void should_ReceiveNotFound_When_WrongMethodNameProvided() {
    String absentMethodName = "absentMethod";
    checkMethodPresenceInService(absentMethodName, false);

    ResponseEntity<String> response = sendVaadinServiceRequest(absentMethodName,
        Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void should_ReceiveNotFound_When_PrivateMethodNameProvided() {
    String methodToCall = "privateMethod";
    checkMethodPresenceInService(methodToCall, true);

    ResponseEntity<String> response = sendVaadinServiceRequest(methodToCall,
        Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void should_ReceiveCorrectResponse_When_MethodWithNoReturnCalled() {
    ResponseEntity<String> response = sendVaadinServiceRequest(
        "noReturnNoArguments", Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("null", response.getBody());
  }

  @Test
  public void should_ReceiveSameResponses_When_DifferentNameCaseUsed() {
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
  public void should_ReceiveCorrectResponse_When_ComplexObjectIsReturnedByServiceMethod() {
    String name = "test";
    Map<String, Object> complexRequestPart = new HashMap<>();
    complexRequestPart.put("name", name);
    complexRequestPart.put("count", 3);

    ResponseEntity<DemoVaadinService.ComplexResponse> response = sendVaadinServiceRequest(
        "complexEntitiesTest",
        Collections.singletonMap("request", complexRequestPart),
        DemoVaadinService.ComplexResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    Map<Integer, List<String>> expectedResult = new HashMap<>();
    expectedResult.put(0, Collections.emptyList());
    expectedResult.put(1, Collections.singletonList("0"));
    expectedResult.put(2, Arrays.asList("0", "1"));
    assertEquals(new DemoVaadinService.ComplexResponse(name, expectedResult),
        response.getBody());
  }

  @Test
  public void should_ReceiveInternalError_When_ServiceMethodThrowsException() {
    String methodName = "throwsException";
    ResponseEntity<String> response = sendVaadinServiceRequest(methodName,
        Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void should_RequestFail_When_InvalidRoleUsedInRequest() {
    String methodName = "permitRoleAdmin";

    ResponseEntity<String> response = sendVaadinServiceRequest(methodName, Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void should_RequestFail_When_MethodCallProhibitedByClassAnnotation() {
    String methodName = "deniedByClass";

    ResponseEntity<String> response = sendVaadinServiceRequest(methodName, Collections.emptyMap(), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertVaadinErrorResponse(response.getBody(), methodName);
  }

  @Test
  public void should_AllowAnonymousAccess_When_AnonymousAllowed() {
    template.getRestTemplate().getInterceptors().clear();
    tokenInjected = false;

    ResponseEntity<String> response = sendVaadinServiceRequest(
        "hasAnonymousAccess", Collections.emptyMap(), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"anonymous success\"", response.getBody());
  }

  // TestRestTemplate always injects custom headers that I was not able to
  // remove with the interceptors hence the apache http client is used to make a
  // simple POST request
  @Test
  public void should_AllowAnonymousAccess_When_NoHeadersSpecified() {
    String url = getRequestUrl("DemoVaadinService", "hasAnonymousAccess");
    CloseableHttpClient client = HttpClients.createDefault();

    List<String> response;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        client.execute(new HttpPost(url)).getEntity().getContent()))) {
      response = br.lines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new AssertionError(
          String.format("Failed to send a request to url '%s'", url), e);
    }

    assertEquals(1, response.size());
    assertEquals("\"anonymous success\"", response.get(0));
  }

  private <T> ResponseEntity<T> sendVaadinServiceRequest(String methodName,
      Object requestData, Class<T> responseType) {
    return template.postForEntity(getRequestUrl(TEST_SERVICE_NAME, methodName),
        requestData, responseType);
  }

  private String getRequestUrl(String serviceName, String methodName) {
    return String.format("http://localhost:%d%s/%s/%s", port,
        vaadinConnectProperties.getVaadinConnectEndpoint(), serviceName,
        methodName);
  }

  private void checkMethodPresenceInService(String method,
      boolean shouldPresent) {
    Class<DemoVaadinService> serviceClass = DemoVaadinService.class;
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
