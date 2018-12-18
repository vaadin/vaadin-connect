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
package com.vaadin.connect.typeconversion;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;

import com.vaadin.connect.VaadinConnectController;
import com.vaadin.connect.VaadinService;
import com.vaadin.connect.oauth.VaadinConnectOAuthAclChecker;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseTypeConversion {
  private VaadinConnectController vaadinConnectController;
  private ObjectMapper objectMapper = new ObjectMapper()
      .configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

  @Before
  public void setUp() {
    String beanName = VaadinConnectTypeConversionServices.class.getName();

    ApplicationContext contextMock = mock(ApplicationContext.class);
    VaadinConnectOAuthAclChecker oAuthAclCheckerMock = mock(
        VaadinConnectOAuthAclChecker.class);
    when(contextMock.getBeansWithAnnotation(VaadinService.class))
        .thenReturn(Collections.singletonMap(beanName,
            new VaadinConnectTypeConversionServices()));
    when(contextMock.getType(beanName))
        .thenReturn((Class) VaadinConnectTypeConversionServices.class);

    when(oAuthAclCheckerMock.check(notNull())).thenReturn(null);
    vaadinConnectController = new VaadinConnectController(null,
        oAuthAclCheckerMock, contextMock);
  }

  protected void assertResponseCode(int expectedResponseCode,
      ResponseEntity<String> stringResponseEntity) {
    Assert.assertEquals(expectedResponseCode,
        stringResponseEntity.getStatusCodeValue());
  }

  protected ObjectNode readJson(String s) throws IOException {
    return objectMapper.readValue(s, ObjectNode.class);
  }

  protected ResponseEntity<String> callMethod(String methodName,
      String parameterValue) throws Exception {
    ObjectNode parameters = readJson("{\"value\": " + parameterValue + "}");
    return vaadinConnectController.serveVaadinService(
        "VaadinConnectTypeConversionServices", methodName, parameters);
  }

  protected void assertCallMethodWithExpectedValue(String methodName,
      String parameterValue, String expectedValue) throws Exception {
    ResponseEntity<String> responseEntity = callMethod(methodName,
        parameterValue);
    Assert.assertEquals(expectedValue, responseEntity.getBody());
  }
}
