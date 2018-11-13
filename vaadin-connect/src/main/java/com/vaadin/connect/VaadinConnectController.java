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
package com.vaadin.connect;

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

/**
 * The controller that is responsible for processing Vaadin Connect requests.
 * Each class that is annotated with {@link VaadinService} gets its public
 * methods exposed so that those can be triggered by a correct POST request,
 * including the methods inherited from the other classes, excluding
 * {@link Object} class ones. Other methods (non-public) are not considered by
 * the controller.
 * <p>
 * For example, if a class with name {@code TestClass} that has the only public
 * method {@code testMethod} was annotated with the annotation, it can be called
 * via {@literal http://${base_url}/testclass/testmethod} POST call, where
 * {@literal ${base_url}} is the application base url, configured by the user.
 * Class name and method name case in the request URL does not matter, but if
 * the method has parameters, the request body should contain a valid JSON with
 * all parameters in the same order as they are declared in the method. The
 * parameter types should also correspond for the request to be successful.
 */
@RestController
public class VaadinConnectController {
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

  /**
   * A constructor used to initialize the controller.
   *
   * @param vaadinServiceMapper
   *          optional bean to override the default {@link ObjectMapper} that is
   *          used for serializing and deserializing request and response bodies
   *          Use
   *          {@link VaadinConnectController#VAADIN_SERVICE_MAPPER_BEAN_QUALIFIER}
   *          qualifier to override the mapper.
   * @param context
   *          Spring context to extract beans annotated with
   *          {@link VaadinService} from
   */
  public VaadinConnectController(
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

          String serviceName = Optional
              .ofNullable(beanType.getAnnotation(VaadinService.class))
              .map(VaadinService::value).filter(value -> !value.isEmpty())
              .orElse(beanType.getSimpleName());
          if (serviceName.isEmpty()) {
            throw new IllegalStateException(String.format(
                "A bean with name '%s' and type '%s' is annotated with '%s' annotation but is an anonymous class hence has no name. ",
                name, beanType, VaadinService.class)
                + String.format(
                    "Either modify the bean declaration so that it is not an anonymous class or specify a service name in the '%s' annotation",
                    VaadinService.class));
          }

          vaadinServices.put(serviceName.toLowerCase(Locale.ENGLISH),
              new VaadinServiceData(serviceBean, beanType.getMethods()));
        });
  }

  /**
   * Captures and processes the Vaadin Connect requests.
   * <p>
   * Matches the service name and a method name with the corresponding Java
   * class and a public method in the class. Extracts parameters from a request
   * body if the Java method requires any and applies in the same order. After
   * the method call, serializes the Java method execution result and sends it
   * back.
   * <p>
   * If an issue occurs during the request processing, an error response is
   * returned instead of the serialized Java method return value.
   *
   * @param serviceName
   *          the name of a service to address the calls to, not case sensitive
   * @param methodName
   *          the method name to execute on a service, not case sensitive
   * @param body
   *          optional request body, that should be specified if the method
   *          called has parameters
   * @return execution result as a JSON string or an error message string
   */
  @PostMapping(path = "/{service}/{method}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<String> serveVaadinService(
      @PathVariable("service") String serviceName,
      @PathVariable("method") String methodName,
      @RequestBody(required = false) ObjectNode body) {
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
          "Incorrect number of parameters for service '%s' method '%s', expected: %s, got: %s",
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
          serviceName, methodName, listMethodParameterTypes(javaParameters)));
    }

    Object returnValue;
    try {
      returnValue = methodToInvoke.invoke(vaadinServiceData.getServiceObject(),
          vaadinServiceParameters);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(String.format(
          "Received incorrect arguments for service '%s' method '%s'. Expected parameter types (and their order) are: '[%s]'",
          serviceName, methodName, listMethodParameterTypes(javaParameters)));
    } catch (IllegalAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(String.format("Service '%s' method '%s' access failure",
              serviceName, methodName));
    } catch (InvocationTargetException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(String.format("Service '%s' method '%s' execution failure",
              serviceName, methodName));
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

  private String listMethodParameterTypes(Parameter[] javaParameters) {
    return Stream.of(javaParameters).map(Parameter::getType).map(Class::getName)
        .collect(Collectors.joining(", "));
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
