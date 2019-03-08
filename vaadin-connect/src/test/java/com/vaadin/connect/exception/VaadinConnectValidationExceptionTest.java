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

package com.vaadin.connect.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.connect.exception.VaadinConnectValidationException.ValidationErrorData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VaadinConnectValidationExceptionTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void should_ThrowException_WhenNullMessageProvided_1() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Message cannot be null or empty");
    new ValidationErrorData(null);
  }

  @Test
  public void should_ThrowException_WhenEmptyMessageProvided_1() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Message cannot be null or empty");
    new ValidationErrorData("");
  }

  @Test
  public void should_ThrowException_WhenEmptyMessageProvided_2() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Message cannot be null or empty");
    new ValidationErrorData("", "whatever");
  }

  @Test
  public void should_ThrowException_WhenNullMessageProvided_2() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Message cannot be null or empty");
    new ValidationErrorData(null, "whatever");
  }

  @Test
  public void should_BeFine_WhenNullParameterNameProvided() {
    String message = "one";
    String parameterName = null;

    ValidationErrorData data = new ValidationErrorData(message, parameterName);

    assertEquals(parameterName, data.getParameterName());
    assertEquals(message, data.getMessage());
  }

  @Test
  public void should_BeFine_WhenEmptyParameterNameProvided() {
    String message = "one";
    String parameterName = "";

    ValidationErrorData data = new ValidationErrorData(message, parameterName);

    assertEquals(parameterName, data.getParameterName());
    assertEquals(message, data.getMessage());
  }

  @Test
  public void should_BeFine_WhenParameterNameProvided() {
    String message = "one";
    String parameterName = "two";

    ValidationErrorData data = new ValidationErrorData(message, parameterName);

    assertEquals(parameterName, data.getParameterName());
    assertEquals(message, data.getMessage());
  }

  @Test
  public void should_ThrowException_WhenNoErrorDataProvided() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("At least one 'validation error' is required");
    VaadinConnectValidationException.ValidationErrorData errorData = null;
    new VaadinConnectValidationException(errorData);
  }

  @Test
  public void should_ThrowException_WhenErrorDataProvidedIsEmpty() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("At least one 'validation error' is required");
    new VaadinConnectValidationException(Collections.emptyList());
  }

  /**
   * In this and the next test, {@link ValidationErrorData} are asserted in
   * equals and toString comparisons. Note that there are no overloads for those
   * methods in the data class since we don't want to expose it as a part of a
   * public API yet. The comparisons work only because the same object
   * references are used in the resulting object.
   */
  @Test
  public void should_BeFine_WhenSingleErrorDataProvided() {
    ValidationErrorData errorData = new ValidationErrorData("test", "tset");
    VaadinConnectValidationException exception = new VaadinConnectValidationException(
        errorData);

    assertNotNull(exception.getMessage());
    assertNull(exception.getDetail());
    List<ValidationErrorData> actualData = exception.getValidationErrorData();
    assertEquals(1, actualData.size());
    assertEquals(Collections.singletonList(errorData), actualData);

    Map<String, Object> data = exception.getSerializationData();
    assertEquals(3, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(exception.getMessage()));
    assertTrue(values.contains(exception.getClass().getName()));
    assertTrue(
        values.contains(Collections.singletonList(errorData).toString()));
  }

  @Test
  public void should_BeFine_WhenMultipleErrorDataProvided() {
    ValidationErrorData errorData1 = new ValidationErrorData("test1", "tset1");
    ValidationErrorData errorData2 = new ValidationErrorData("test2", "tset2");
    VaadinConnectValidationException exception = new VaadinConnectValidationException(
        Arrays.asList(errorData1, errorData2));

    assertNotNull(exception.getMessage());
    assertNull(exception.getDetail());
    List<ValidationErrorData> actualData = exception.getValidationErrorData();
    assertEquals(2, actualData.size());
    assertEquals(Arrays.asList(errorData1, errorData2), actualData);

    Map<String, Object> data = exception.getSerializationData();
    assertEquals(3, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(exception.getMessage()));
    assertTrue(values.contains(exception.getClass().getName()));
    assertTrue(
        values.contains(Arrays.asList(errorData1, errorData2).toString()));
  }
}
