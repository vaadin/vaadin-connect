package com.vaadin.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VaadinServiceController {
  private static final String VAADIN_SERVICE_MAPPER_BEAN_QUALIFIER = "vaadinServiceMapper";

  private final ObjectMapper vaadinServiceMapper;
  private final Map<String, VaadinServiceData> vaadinServices = new HashMap<>();

  private static class VaadinServiceData {
    private final Object vaadinServiceObject;
    private final Map<String, Method> methods = new HashMap<>();

    private VaadinServiceData(Object vaadinServiceObject,
        Method... serviceMethods) {
      this.vaadinServiceObject = vaadinServiceObject;
      Stream.of(serviceMethods)
          .filter(method -> method.getDeclaringClass() != Object.class)
          .forEach(method -> methods
              .put(method.getName().toLowerCase(Locale.ENGLISH), method));
    }

    private Optional<Method> getMethod(String methodName) {
      return Optional.ofNullable(methods.get(methodName));
    }

    private Object getServiceObject() {
      return vaadinServiceObject;
    }
  }

  public VaadinServiceController(
      @Autowired(required = false) @Qualifier(VAADIN_SERVICE_MAPPER_BEAN_QUALIFIER) ObjectMapper vaadinServiceMapper,
      ApplicationContext context) {
    this.vaadinServiceMapper = vaadinServiceMapper != null ? vaadinServiceMapper
        : Jackson2ObjectMapperBuilder.json()
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            .build();

    context.getBeansWithAnnotation(VaadinService.class)
        .forEach((name, serviceBean) -> {
          // Check the bean type instead of the implementation type in
          // case of e.g. proxies
          Class<?> beanType = context.getType(name);
          if (beanType == null) {
            throw new IllegalStateException(String.format(
                "Unable to determine a type for the bean with name '%s', double check your bean configuration",
                name));
          }

          String serviceName = beanType.getSimpleName();
          if (serviceName.isEmpty()) {
            throw new IllegalStateException(String.format(
                "A bean with name '%s' and type '%s' is annotated with '%s' annotation but is an anonymous class. Modify the bean declaration so that it is not an anonymous class",
                name, beanType, VaadinService.class));
          }

          vaadinServices.put(serviceName.toLowerCase(Locale.ENGLISH),
              new VaadinServiceData(serviceBean, beanType.getMethods()));
        });
  }

  // @formatter:off
  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/ser/var
  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/testservice/ss
  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/testservice/complexTest
  // curl -i -H "Content-Type: application/json" -d '{"arg1": [1], "arg2": 2, "arg3": 3.0}' http://localhost:8080/testservice/testMultipleParameters

  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/testservice/test
  // curl -i -H "Content-Type: application/json" -d '{"count": 3}' http://localhost:8080/testservice/complexTest
  // curl -i -H "Content-Type: application/json" -d '{"arg1": "1", "arg2": 2, "arg3": 3.0}' http://localhost:8080/testservice/testMultipleParameters
  // curl -i -H "Content-Type: application/json" -d '{"testParam": "test"}' http://localhost:8080/testservice/noReturnValue
  // @formatter:on
  @PostMapping(path = "/{service}/{method}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<String> serveVaadinService(
      @PathVariable("service") String serviceName,
      @PathVariable("method") String methodName,
      @RequestBody(required = false) ObjectNode body)
  // TODO kb process this exception also
  // TODO kb do not show stacktraces by default in response messages
      throws InvocationTargetException {
    VaadinServiceData vaadinServiceData = vaadinServices
        .get(serviceName.toLowerCase(Locale.ENGLISH));
    Method methodToInvoke = vaadinServiceData == null ? null
        : vaadinServiceData.getMethod(methodName.toLowerCase(Locale.ENGLISH))
            .orElse(null);
    if (methodToInvoke == null) {
      return ResponseEntity.notFound().build();
    }

    List<JsonNode> requestParameters = getRequestParameters(body);
    Parameter[] javaParameters = methodToInvoke.getParameters();
    if (javaParameters.length != requestParameters.size()) {
      return ResponseEntity.badRequest().body(String.format(
          "Incorrect number of parameters for service '%s' method '%s', expected: '%s', got: '%s'",
          serviceName, methodName, javaParameters.length,
          requestParameters.size()));
    }

    Object[] vaadinServiceParameters;
    try {
      vaadinServiceParameters = getVaadinServiceParameters(requestParameters,
          javaParameters);
    } catch (IOException e) {
      return ResponseEntity.badRequest().body(String.format(
          "Unable to deserialize parameters for service '%s' method '%s'. Expected parameter types (and their order) are: '[%s]'",
          serviceName, methodName,
          Stream.of(javaParameters).map(Parameter::getType).map(Class::getName)
              .collect(Collectors.joining(", "))));
    }

    Object returnValue;
    try {
      returnValue = methodToInvoke
          .invoke(vaadinServiceData.getServiceObject(), vaadinServiceParameters);
    } catch (IllegalAccessException e) {
      // TODO kb return a proper error code instead
      throw new IllegalStateException(e);
    }

    try {
      return ResponseEntity
          .ok(vaadinServiceMapper.writeValueAsString(returnValue));
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(String.format(
              "Failed to serialize service '%s' method '%s' response. Double check method's return type or specify a custom mapper bean with qualifier '%s'",
              serviceName, methodName, VAADIN_SERVICE_MAPPER_BEAN_QUALIFIER));
    }
  }

  private Object[] getVaadinServiceParameters(List<JsonNode> requestParameters,
      Parameter[] javaParameters) throws IOException {
    Object[] serviceParameters = new Object[javaParameters.length];
    for (int i = 0; i < javaParameters.length; i++) {
      serviceParameters[i] = vaadinServiceMapper
          .readerFor(javaParameters[i].getType())
          .readValue(requestParameters.get(i));
    }
    return serviceParameters;
  }

  private List<JsonNode> getRequestParameters(ObjectNode body) {
    List<JsonNode> parametersData = new ArrayList<>();
    if (body != null) {
      body.fields()
          .forEachRemaining(entry -> parametersData.add(entry.getValue()));
    }
    return parametersData;
  }
}
