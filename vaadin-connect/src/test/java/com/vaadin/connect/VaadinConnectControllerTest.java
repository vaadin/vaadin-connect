package com.vaadin.connect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vaadin.connect.oauth.VaadinConnectOAuthAclChecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VaadinConnectControllerTest {
  private static final TestClass TEST_SERVICE = new TestClass();
  private static final String TEST_SERVICE_NAME = TEST_SERVICE.getClass()
      .getSimpleName();
  private static final Method TEST_METHOD;

  static {
    TEST_METHOD = Stream.of(TEST_SERVICE.getClass().getDeclaredMethods())
        .filter(method -> "testMethod".equals(method.getName())).findFirst()
        .orElseThrow(
            () -> new AssertionError("Failed to find a service method"));
  }

  @VaadinService
  public static class TestClass {
    public String testMethod(int parameter) {
      return parameter + "-test";
    }
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void should_ThrowException_When_ContextHasNoBeanData() {
    String beanName = "test";

    ApplicationContext contextMock = mock(ApplicationContext.class);
    when(contextMock.getType(beanName)).thenReturn(null);
    when(contextMock.getBeansWithAnnotation(VaadinService.class))
        .thenReturn(Collections.singletonMap(beanName, null));

    exception.expect(IllegalStateException.class);
    exception.expectMessage(beanName);
    new VaadinConnectController(null, null, contextMock);
  }

  @Test
  public void should_ThrowException_When_NoServiceNameCanBeReceived() {
    TestClass anonymousClass = new TestClass() {
    };
    assertEquals("Service to test should have no name",
        anonymousClass.getClass().getSimpleName(), "");

    exception.expect(IllegalStateException.class);
    exception.expectMessage("anonymous");
    exception.expectMessage(anonymousClass.getClass().getName());
    createVaadinController(anonymousClass);
  }

  @Test
  public void should_Return404_When_ServiceNotFound() {
    String missingServiceName = "whatever";
    assertNotEquals(missingServiceName, TEST_SERVICE_NAME);

    ResponseEntity<String> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(missingServiceName, null, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void should_Return404_When_MethodNotFound() {
    String missingServiceMethod = "whatever";
    assertNotEquals(TEST_METHOD.getName(), missingServiceMethod);

    ResponseEntity<String> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(TEST_SERVICE_NAME, missingServiceMethod, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void should_Return404_When_IllegalAccessToMethodIsPerformed() {
    String accessErrorMessage = "Access error";

    VaadinConnectOAuthAclChecker restrictingCheckerMock = mock(
        VaadinConnectOAuthAclChecker.class);
    when(restrictingCheckerMock.check(TEST_METHOD))
        .thenReturn(accessErrorMessage);

    ResponseEntity<String> response = createVaadinController(TEST_SERVICE, null,
        restrictingCheckerMock).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(), null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains(accessErrorMessage));

    verify(restrictingCheckerMock, only()).check(TEST_METHOD);
    verify(restrictingCheckerMock, times(1)).check(TEST_METHOD);
  }

  @Test
  public void should_Return400_When_LessParametersSpecified1() {
    ResponseEntity<String> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(), null);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains("0"));
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody
            .contains(Integer.toString(TEST_METHOD.getParameterCount())));
  }

  @Test
  public void should_Return400_When_MoreParametersSpecified() {
    ResponseEntity<String> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters("{\"value1\": 222, \"value2\": 333}"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains("2"));
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody
            .contains(Integer.toString(TEST_METHOD.getParameterCount())));
  }

  @Test
  public void should_Return400_When_IncorrectParameterTypesAreProvided() {
    ResponseEntity<String> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters("{\"value\": [222]}"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody
            .contains(TEST_METHOD.getParameterTypes()[0].getSimpleName()));
  }

  @Test
  public void should_Return400_When_ServiceMethodThrowsIllegalArgumentException()
      throws Exception {
    int inputValue = 222;

    Method serviceMethodMock = mock(Method.class);
    when(serviceMethodMock.invoke(TEST_SERVICE, inputValue))
        .thenThrow(new IllegalArgumentException("OOPS"));
    when(serviceMethodMock.getParameters())
        .thenReturn(TEST_METHOD.getParameters());

    VaadinConnectController controller = createVaadinController(TEST_SERVICE);
    controller.vaadinServices.get(TEST_SERVICE_NAME.toLowerCase()).methods
        .put(TEST_METHOD.getName().toLowerCase(), serviceMethodMock);

    ResponseEntity<String> response = controller.serveVaadinService(
        TEST_SERVICE_NAME, TEST_METHOD.getName(),
        createRequestParameters(String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody
            .contains(TEST_METHOD.getParameterTypes()[0].getSimpleName()));

    verify(serviceMethodMock, times(1)).invoke(TEST_SERVICE, inputValue);
    verify(serviceMethodMock, times(1)).getParameters();
  }

  @Test
  public void should_Return500_When_ServiceMethodThrowsIllegalAccessException()
      throws Exception {
    int inputValue = 222;

    Method serviceMethodMock = mock(Method.class);
    when(serviceMethodMock.invoke(TEST_SERVICE, inputValue))
        .thenThrow(new IllegalAccessException("OOPS"));
    when(serviceMethodMock.getParameters())
        .thenReturn(TEST_METHOD.getParameters());

    VaadinConnectController controller = createVaadinController(TEST_SERVICE);
    controller.vaadinServices.get(TEST_SERVICE_NAME.toLowerCase()).methods
        .put(TEST_METHOD.getName().toLowerCase(), serviceMethodMock);

    ResponseEntity<String> response = controller.serveVaadinService(
        TEST_SERVICE_NAME, TEST_METHOD.getName(),
        createRequestParameters(String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains("access failure"));

    verify(serviceMethodMock, times(1)).invoke(TEST_SERVICE, inputValue);
    verify(serviceMethodMock, times(1)).getParameters();
  }

  @Test
  public void should_Return500_When_ServiceMethodThrowsInvocationTargetException()
      throws Exception {
    int inputValue = 222;

    Method serviceMethodMock = mock(Method.class);
    when(serviceMethodMock.invoke(TEST_SERVICE, inputValue)).thenThrow(
        new InvocationTargetException(new IllegalStateException("OOPS")));
    when(serviceMethodMock.getParameters())
        .thenReturn(TEST_METHOD.getParameters());

    VaadinConnectController controller = createVaadinController(TEST_SERVICE);
    controller.vaadinServices.get(TEST_SERVICE_NAME.toLowerCase()).methods
        .put(TEST_METHOD.getName().toLowerCase(), serviceMethodMock);

    ResponseEntity<String> response = controller.serveVaadinService(
        TEST_SERVICE_NAME, TEST_METHOD.getName(),
        createRequestParameters(String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains("execution failure"));

    verify(serviceMethodMock, times(1)).invoke(TEST_SERVICE, inputValue);
    verify(serviceMethodMock, times(1)).getParameters();
  }

  @Test
  public void should_Return500_When_MapperFailsToSerializeResponse()
      throws Exception {
    ObjectMapper mapperMock = mock(ObjectMapper.class);
    when(mapperMock.readerFor(int.class))
        .thenReturn(new ObjectMapper().readerFor(int.class));
    when(mapperMock.writeValueAsString(notNull()))
        .thenThrow(new JsonMappingException(null, "sss"));

    ResponseEntity<String> response = createVaadinController(TEST_SERVICE,
        mapperMock).serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters("{\"value\": 222}"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    String responseBody = response.getBody();
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains(
            VaadinConnectController.VAADIN_SERVICE_MAPPER_BEAN_QUALIFIER));

    verify(mapperMock, times(1)).readerFor(int.class);
    verify(mapperMock, times(1)).writeValueAsString(notNull());
  }

  @Test
  public void should_ReturnCorrectResponse_When_EverythingIsCorrect() {
    int inputValue = 222;
    String expectedOutput = TEST_SERVICE.testMethod(inputValue);

    ResponseEntity<String> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters(
                String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(String.format("\"%s\"", expectedOutput), response.getBody());
  }

  private void assertServiceInfoPresent(String responseBody) {
    assertTrue(String.format(
        "Response body '%s' should have service information in it",
        responseBody), responseBody.contains(TEST_SERVICE_NAME));
    assertTrue(String.format(
        "Response body '%s' should have service information in it",
        responseBody), responseBody.contains(TEST_METHOD.getName()));
  }

  private ObjectNode createRequestParameters(String jsonBody) {
    try {
      return new ObjectMapper().readValue(jsonBody, ObjectNode.class);
    } catch (IOException e) {
      throw new AssertionError(
          String.format("Failed to deserialize the json: %s", jsonBody), e);
    }
  }

  private <T> VaadinConnectController createVaadinController(T service) {
    VaadinConnectOAuthAclChecker oAuthAclCheckerMock = mock(
        VaadinConnectOAuthAclChecker.class);
    when(oAuthAclCheckerMock.check(TEST_METHOD)).thenReturn(null);

    return createVaadinController(service, null, oAuthAclCheckerMock);
  }

  private <T> VaadinConnectController createVaadinController(T service,
      ObjectMapper vaadinServiceMapper) {
    VaadinConnectOAuthAclChecker oAuthAclCheckerMock = mock(
        VaadinConnectOAuthAclChecker.class);
    when(oAuthAclCheckerMock.check(TEST_METHOD)).thenReturn(null);
    return createVaadinController(service, vaadinServiceMapper,
        oAuthAclCheckerMock);
  }

  private <T> VaadinConnectController createVaadinController(T service,
      ObjectMapper vaadinServiceMapper, VaadinConnectOAuthAclChecker checker) {
    Class<?> serviceClass = service.getClass();
    ApplicationContext contextMock = mock(ApplicationContext.class);
    when(contextMock.getBeansWithAnnotation(VaadinService.class))
        .thenReturn(Collections.singletonMap(serviceClass.getName(), service));
    when(contextMock.getType(serviceClass.getName()))
        .thenReturn((Class) serviceClass);
    return new VaadinConnectController(vaadinServiceMapper, checker,
        contextMock);
  }
}
