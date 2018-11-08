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
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VaadinServiceController {
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
      @Autowired(required = false) @Qualifier("vaadinServiceMapper") ObjectMapper vaadinServiceMapper,
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
            throw new IllegalStateException("WTF?");
          }

          String serviceName = beanType.getSimpleName();
          if (serviceName.isEmpty()) {
            throw new IllegalStateException(
                "Anonymous class as a VaadinService declared, wtf?");
          }

          vaadinServices.put(serviceName.toLowerCase(Locale.ENGLISH),
              new VaadinServiceData(serviceBean, beanType.getMethods()));
        });
  }

  // @formatter:off
  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/ser/var
  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/testservice/ss

  // curl -i -H "Content-Type: application/json" -d '' http://localhost:8080/testservice/test
  // curl -i -H "Content-Type: application/json" -d '{"count":3}' http://localhost:8080/testservice/complexTest
  // @formatter:on
  @PostMapping(path = "/{service}/{method}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<String> serveVaadinService(
      @PathVariable("service") String serviceName,
      @PathVariable("method") String methodName,
      @RequestBody(required = false) ObjectNode body)
      throws IOException, InvocationTargetException, IllegalAccessException {
    VaadinServiceData vaadinServiceData = vaadinServices
        .get(serviceName.toLowerCase(Locale.ENGLISH));
    Method methodToInvoke = vaadinServiceData == null ? null
        : vaadinServiceData.getMethod(methodName.toLowerCase(Locale.ENGLISH))
            .orElse(null);
    if (methodToInvoke == null) {
      return ResponseEntity.notFound().build();
    }

    Object returnValue = methodToInvoke.invoke(
        vaadinServiceData.getServiceObject(),
        getVaadinServiceParameters(body, methodToInvoke.getParameters()));
    return ResponseEntity
        .ok(vaadinServiceMapper.writeValueAsString(returnValue));
  }

  private Object[] getVaadinServiceParameters(
      @RequestBody(required = false) ObjectNode body,
      Parameter[] javaParameters) throws IOException {
    List<JsonNode> requestParameters = getRequestParameters(body);
    Object[] javaArguments = new Object[javaParameters.length];
    int nextJsonParamIndex = 0;
    for (int i = 0; i < javaParameters.length; i++) {
      Parameter parameter = javaParameters[i];
      Object value;

      JsonNode jsonValue = requestParameters.get(nextJsonParamIndex++);
      value = vaadinServiceMapper.readerFor(parameter.getType())
          .readValue(jsonValue);
      javaArguments[i] = value;
    }
    return javaArguments;
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
