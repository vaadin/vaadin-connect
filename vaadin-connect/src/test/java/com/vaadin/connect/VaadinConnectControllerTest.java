package com.vaadin.connect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vaadin.connect.oauth.VaadinConnectOAuthAclChecker;
import com.vaadin.connect.testservice.BridgeMethodTestService;

import static com.vaadin.connect.VaadinConnectController.ERROR_MESSAGE_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
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

  @VaadinService("CustomService")
  public static class TestClassWithCustomServiceName {
    public String testMethod(int parameter) {
      return parameter + "-test";
    }
  }

  @VaadinService("my service")
  public static class TestClassWithIllegalServiceName {
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
    new VaadinConnectController(mock(ObjectMapper.class), null, null,
        contextMock);
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
  public void should_ThrowException_When_IncorrectServiceNameProvided() {
    TestClassWithIllegalServiceName serviceWithIllegalName = new TestClassWithIllegalServiceName();
    String incorrectName = serviceWithIllegalName.getClass()
        .getAnnotation(VaadinService.class).value();
    VaadinServiceNameChecker nameChecker = new VaadinServiceNameChecker();
    String expectedCheckerMessage = nameChecker.check(incorrectName);
    assertNotNull(expectedCheckerMessage);

    exception.expect(IllegalStateException.class);
    exception.expectMessage(incorrectName);
    exception.expectMessage(expectedCheckerMessage);

    createVaadinController(serviceWithIllegalName, mock(ObjectMapper.class),
        null, nameChecker);
  }

  @Test
  public void should_Return404_When_ServiceNotFound() {
    String missingServiceName = "whatever";
    assertNotEquals(missingServiceName, TEST_SERVICE_NAME);

    ResponseEntity<?> response = createVaadinController(TEST_SERVICE)
        .serveVaadinService(missingServiceName, null, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void should_Return404_When_MethodNotFound() {
    String missingServiceMethod = "whatever";
    assertNotEquals(TEST_METHOD.getName(), missingServiceMethod);

    ResponseEntity<?> response = createVaadinController(TEST_SERVICE)
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

    VaadinServiceNameChecker nameCheckerMock = mock(
        VaadinServiceNameChecker.class);
    when(nameCheckerMock.check(TEST_SERVICE_NAME)).thenReturn(null);

    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) createVaadinController(
        TEST_SERVICE, new ObjectMapper(), restrictingCheckerMock,
        nameCheckerMock).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(), null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains(accessErrorMessage));

    verify(restrictingCheckerMock, only()).check(TEST_METHOD);
    verify(restrictingCheckerMock, times(1)).check(TEST_METHOD);
  }

  @Test
  public void should_Return400_When_LessParametersSpecified1() {
    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) createVaadinController(
        TEST_SERVICE).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(), null);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains("0"));
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody
            .contains(Integer.toString(TEST_METHOD.getParameterCount())));
  }

  @Test
  public void should_Return400_When_MoreParametersSpecified() {
    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) createVaadinController(
        TEST_SERVICE).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(),
            createRequestParameters("{\"value1\": 222, \"value2\": 333}"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains("2"));
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody
            .contains(Integer.toString(TEST_METHOD.getParameterCount())));
  }

  @Test
  public void should_Return400_When_IncorrectParameterTypesAreProvided() {
    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) createVaadinController(
        TEST_SERVICE).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(),
            createRequestParameters("{\"value\": [222]}"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
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

    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) controller
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters(
                String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
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

    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) controller
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters(
                String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
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

    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) controller
        .serveVaadinService(TEST_SERVICE_NAME, TEST_METHOD.getName(),
            createRequestParameters(
                String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
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
    TypeFactory typeFactory = mock(TypeFactory.class);
    when(mapperMock.getTypeFactory()).thenReturn(typeFactory);
    when(typeFactory.constructType(int.class))
        .thenReturn(SimpleType.constructUnsafe(int.class));
    when(mapperMock.readerFor(SimpleType.constructUnsafe(int.class)))
        .thenReturn(new ObjectMapper()
            .readerFor(SimpleType.constructUnsafe(int.class)));
    when(mapperMock.writeValueAsString(notNull()))
        .thenThrow(new JsonMappingException(null, "sss"));

    ResponseEntity<Map<String, String>> response = (ResponseEntity<Map<String, String>>) createVaadinController(
        TEST_SERVICE, mapperMock).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(), createRequestParameters("{\"value\": 222}"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    String responseBody = response.getBody().get(ERROR_MESSAGE_FIELD);
    assertServiceInfoPresent(responseBody);
    assertTrue(String.format("Invalid response body: '%s'", responseBody),
        responseBody.contains(
            VaadinConnectController.VAADIN_SERVICE_MAPPER_BEAN_QUALIFIER));

    verify(mapperMock, times(1))
        .readerFor(SimpleType.constructUnsafe(int.class));
    verify(mapperMock, times(1)).writeValueAsString(notNull());
  }

  @Test
  public void should_ReturnCorrectResponse_When_EverythingIsCorrect() {
    int inputValue = 222;
    String expectedOutput = TEST_SERVICE.testMethod(inputValue);

    ResponseEntity<String> response = (ResponseEntity<String>) createVaadinController(
        TEST_SERVICE).serveVaadinService(TEST_SERVICE_NAME,
            TEST_METHOD.getName(), createRequestParameters(
                String.format("{\"value\": %s}", inputValue)));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(String.format("\"%s\"", expectedOutput), response.getBody());
  }

  @Test
  public void should_NotUseBridgeMethod_When_ServiceHasBridgeMethodFromInterface() {
    String inputId = "2222";
    String expectedResult = String.format("{\"id\":\"%s\"}", inputId);
    BridgeMethodTestService.InheritedClass testService = new BridgeMethodTestService.InheritedClass();
    String testMethodName = "testMethodFromInterface";
    ResponseEntity<String> response = (ResponseEntity<String>) createVaadinController(
        testService).serveVaadinService(testService.getClass().getSimpleName(),
            testMethodName, createRequestParameters(
                String.format("{\"value\": {\"id\": \"%s\"}}", inputId)));
    assertEquals(expectedResult, response.getBody());
  }

  @Test
  public void should_NotUseBridgeMethod_When_ServiceHasBridgeMethodFromParentClass() {
    String inputId = "2222";
    BridgeMethodTestService.InheritedClass testService = new BridgeMethodTestService.InheritedClass();
    String testMethodName = "testMethodFromClass";

    ResponseEntity<String> response = (ResponseEntity<String>) createVaadinController(
        testService).serveVaadinService(testService.getClass().getSimpleName(),
            testMethodName,
            createRequestParameters(String.format("{\"value\": %s}", inputId)));
    assertEquals(inputId, response.getBody());
  }

  @Test
  public void should_ReturnCorrectResponse_When_CallingNormalOverriddenMethod() {
    String inputId = "2222";
    BridgeMethodTestService.InheritedClass testService = new BridgeMethodTestService.InheritedClass();
    String testMethodName = "testNormalMethod";

    ResponseEntity<String> response = (ResponseEntity<String>) createVaadinController(
        testService).serveVaadinService(testService.getClass().getSimpleName(),
            testMethodName,
            createRequestParameters(String.format("{\"value\": %s}", inputId)));
    assertEquals(inputId, response.getBody());
  }

  @Test
  public void should_UseCustomServiceName_When_ItIsDefined() {
    int input = 111;
    String expectedOutput = new TestClassWithCustomServiceName()
        .testMethod(input);
    String beanName = TestClassWithCustomServiceName.class.getSimpleName();

    ApplicationContext contextMock = mock(ApplicationContext.class);
    when(contextMock.getType(beanName))
        .thenReturn((Class) TestClassWithCustomServiceName.class);
    when(contextMock.getBeansWithAnnotation(VaadinService.class))
        .thenReturn(Collections.singletonMap(beanName,
            new TestClassWithCustomServiceName()));

    VaadinConnectController vaadinConnectController = new VaadinConnectController(
        new ObjectMapper(), mock(VaadinConnectOAuthAclChecker.class),
        mock(VaadinServiceNameChecker.class), contextMock);
    ResponseEntity<String> response = (ResponseEntity<String>) vaadinConnectController
        .serveVaadinService("CustomService", "testMethod",
            createRequestParameters(String.format("{\"value\": %s}", input)));
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(String.format("\"%s\"", expectedOutput), response.getBody());
  }

  @Test
  public void should_UseDefaultObjectMapper_When_NoneIsProvided() {
    ApplicationContext contextMock = mock(ApplicationContext.class);
    ObjectMapper mockDefaultObjectMapper = mock(ObjectMapper.class);
    JacksonProperties mockJacksonProperties = mock(JacksonProperties.class);
    when(contextMock.getBean(ObjectMapper.class))
        .thenReturn(mockDefaultObjectMapper);
    when(contextMock.getBean(JacksonProperties.class))
        .thenReturn(mockJacksonProperties);
    when(mockJacksonProperties.getVisibility())
        .thenReturn(Collections.emptyMap());
    new VaadinConnectController(null, mock(VaadinConnectOAuthAclChecker.class),
        mock(VaadinServiceNameChecker.class), contextMock);

    verify(contextMock, times(1)).getBean(ObjectMapper.class);
    verify(mockDefaultObjectMapper, times(1))
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    verify(contextMock, times(1)).getBean(JacksonProperties.class);
  }

  @Test
  public void should_NotOverrideVisibility_When_JacksonPropertiesProvideVisibility() {
    ApplicationContext contextMock = mock(ApplicationContext.class);
    ObjectMapper mockDefaultObjectMapper = mock(ObjectMapper.class);
    JacksonProperties mockJacksonProperties = mock(JacksonProperties.class);
    when(contextMock.getBean(ObjectMapper.class))
        .thenReturn(mockDefaultObjectMapper);
    when(contextMock.getBean(JacksonProperties.class))
        .thenReturn(mockJacksonProperties);
    when(mockJacksonProperties.getVisibility())
        .thenReturn(Collections.singletonMap(PropertyAccessor.ALL,
            JsonAutoDetect.Visibility.PUBLIC_ONLY));
    new VaadinConnectController(null, mock(VaadinConnectOAuthAclChecker.class),
        mock(VaadinServiceNameChecker.class), contextMock);

    verify(contextMock, times(1)).getBean(ObjectMapper.class);
    verify(mockDefaultObjectMapper, times(0))
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    verify(contextMock, times(1)).getBean(JacksonProperties.class);
  }

  @Test
  public void should_ThrowError_When_DefaultObjectMapperIsNotFound() {
    ApplicationContext contextMock = mock(ApplicationContext.class);
    when(contextMock.getBean(ObjectMapper.class))
        .thenThrow(new NoSuchBeanDefinitionException("Bean not found"));

    exception.expect(IllegalStateException.class);
    exception.expectMessage("object mapper");

    new VaadinConnectController(null, mock(VaadinConnectOAuthAclChecker.class),
        mock(VaadinServiceNameChecker.class), contextMock);
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

    VaadinServiceNameChecker nameCheckerMock = mock(
        VaadinServiceNameChecker.class);
    when(nameCheckerMock.check(TEST_SERVICE_NAME)).thenReturn(null);

    return createVaadinController(service, new ObjectMapper(),
        oAuthAclCheckerMock, nameCheckerMock);
  }

  private <T> VaadinConnectController createVaadinController(T service,
      ObjectMapper vaadinServiceMapper) {
    VaadinConnectOAuthAclChecker oAuthAclCheckerMock = mock(
        VaadinConnectOAuthAclChecker.class);
    when(oAuthAclCheckerMock.check(TEST_METHOD)).thenReturn(null);

    VaadinServiceNameChecker nameCheckerMock = mock(
        VaadinServiceNameChecker.class);
    when(nameCheckerMock.check(TEST_SERVICE_NAME)).thenReturn(null);

    return createVaadinController(service, vaadinServiceMapper,
        oAuthAclCheckerMock, nameCheckerMock);
  }

  private <T> VaadinConnectController createVaadinController(T service,
      ObjectMapper vaadinServiceMapper,
      VaadinConnectOAuthAclChecker oAuthAclChecker,
      VaadinServiceNameChecker serviceNameChecker) {
    Class<?> serviceClass = service.getClass();
    ApplicationContext contextMock = mock(ApplicationContext.class);
    when(contextMock.getBeansWithAnnotation(VaadinService.class))
        .thenReturn(Collections.singletonMap(serviceClass.getName(), service));
    when(contextMock.getType(serviceClass.getName()))
        .thenReturn((Class) serviceClass);
    return new VaadinConnectController(vaadinServiceMapper, oAuthAclChecker,
        serviceNameChecker, contextMock);
  }
}
