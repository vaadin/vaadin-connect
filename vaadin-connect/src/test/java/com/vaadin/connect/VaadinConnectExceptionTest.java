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

package com.vaadin.connect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VaadinConnectExceptionTest {
  private static final String TEST_MESSAGE = "test message";
  private static final IllegalStateException TEST_EXCEPTION = new IllegalStateException(
      "test exception");
  private static final Set<String> TEST_DETAIL = Collections
      .singleton("detail");

  @Test
  public void when_ExceptionWithMessageOnlyCreated_VaadinConnectExceptionTypeAndMessageInTheData() {
    Map<String, Object> data = new VaadinConnectException(TEST_MESSAGE)
        .getSerializationData();

    assertEquals(2, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(TEST_MESSAGE));
    assertTrue(values.contains(VaadinConnectException.class.getName()));
  }

  @Test
  public void when_ExceptionWithCauseOnlyCreated_CauseTypeAndCauseMessageInTheData() {
    VaadinConnectException exception = new VaadinConnectException(
        TEST_EXCEPTION);
    Map<String, Object> data = exception.getSerializationData();

    assertEquals(2, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(exception.getMessage()));
    assertTrue(values.contains(TEST_EXCEPTION.getClass().getName()));
  }

  @Test
  public void when_ExceptionWithCauseOnlyCreated_AndCauseIsWithoutMessage_CauseTypeInTheData() {
    Map<String, Object> data = new VaadinConnectException(new Exception())
        .getSerializationData();

    assertEquals(2, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    // See corresponding Throwable constructor
    assertEquals(values,
        Arrays.asList(Exception.class.getName(), Exception.class.getName()));
  }

  @Test
  public void when_ExceptionWithMessageAndDetailCreated_BothAreInTheDataAlongWithConnectExceptionType() {
    Map<String, Object> data = new VaadinConnectException(TEST_MESSAGE,
        TEST_DETAIL).getSerializationData();

    assertEquals(3, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(TEST_MESSAGE));
    assertTrue(values.contains(VaadinConnectException.class.getName()));
    assertTrue(values.contains(TEST_DETAIL.toString()));
  }

  @Test
  public void when_ExceptionWithMessageAndNullDetailCreated_BothAreInTheDataAlongWithConnectExceptionType() {
    Map<String, Object> data = new VaadinConnectException(TEST_MESSAGE,
        (Object) null).getSerializationData();

    assertEquals(2, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(TEST_MESSAGE));
    assertTrue(values.contains(VaadinConnectException.class.getName()));
  }

  @Test
  public void when_ExceptionWithMessageAndCauseCreated_BothAreInTheData() {
    Map<String, Object> data = new VaadinConnectException(TEST_MESSAGE,
        TEST_EXCEPTION).getSerializationData();

    assertEquals(2, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(TEST_MESSAGE));
    assertTrue(values.contains(TEST_EXCEPTION.getClass().getName()));
  }

  @Test
  public void when_ExceptionWithNullMessageAndCauseCreated_BothAreInTheData() {
    Map<String, Object> data = new VaadinConnectException(null, TEST_EXCEPTION)
        .getSerializationData();

    assertEquals(2, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(TEST_EXCEPTION.getMessage()));
    assertTrue(values.contains(TEST_EXCEPTION.getClass().getName()));
  }

  @Test
  public void when_ExceptionWithAllArgsCreated_AllAreInTheData() {
    Map<String, Object> data = new VaadinConnectException(TEST_MESSAGE,
        TEST_EXCEPTION, TEST_DETAIL).getSerializationData();

    assertEquals(3, data.size());
    List<String> values = data.values().stream().map(Objects::toString)
        .collect(Collectors.toList());
    assertTrue(values.contains(TEST_MESSAGE));
    assertTrue(values.contains(TEST_EXCEPTION.getClass().getName()));
    assertTrue(values.contains(TEST_DETAIL.toString()));
  }
}
