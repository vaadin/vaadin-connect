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

import org.junit.Test;

public class MapConversionTest extends BaseTypeConversion {

  @Test
  public void should_ConvertToMapOfString_WhenReceiveMapOfString()
      throws Exception {
    String inputValue = "{\"key\": \"value\", \"second_key\":\"2\"}";
    String expectedValue = "{\"key\":\"value-foo\",\"second_key\":\"2-foo\"}";
    assertCallMethodWithExpectedValue("getFooMapStringString", inputValue,
        expectedValue);
  }

  @Test
  public void should_ConvertToMapOfString_WhenReceiveMapWithNonStringValue()
      throws Exception {
    String inputValue = "{\"key\": 1, \"second_key\": 2.0}";
    String expectedValue = "{\"key\":\"1-foo\",\"second_key\":\"2.0-foo\"}";
    assertCallMethodWithExpectedValue("getFooMapStringString", inputValue,
        expectedValue);
  }

  @Test
  public void should_ConvertToMapOfInteger_WhenReceiveMapOfInteger()
      throws Exception {
    String inputValue = "{\"key\":2,\"second_key\":3}";
    String expectedValue = "{\"key\":3,\"second_key\":4}";
    assertCallMethodWithExpectedValue("getAddOneMapStringInteger", inputValue,
        expectedValue);
  }

  @Test
  public void should_ConvertToMapOfInteger_WhenReceiveMapOfNonInteger()
      throws Exception {
    String inputValue = "{\"key\":\"2\"}";
    String expectedValue = "{\"key\":3}";
    assertCallMethodWithExpectedValue("getAddOneMapStringInteger", inputValue,
        expectedValue);
  }

  @Test
  public void should_ConvertToMapOfInteger_WhenReceiveMapOfDecimal()
      throws Exception {
    String inputValue = "{\"key\": 2.0}";
    String expectedValue = "{\"key\":3}";
    assertCallMethodWithExpectedValue("getAddOneMapStringInteger", inputValue,
        expectedValue);

    inputValue = "{\"key\":2.9}";
    assertCallMethodWithExpectedValue("getAddOneMapStringInteger", inputValue,
        expectedValue);
  }

  @Test
  public void should_ConvertToMapOfDouble_WhenReceiveMapOfDecimal()
      throws Exception {
    String inputValue = "{\"key\": 2.0}";
    String expectedValue = "{\"key\":3.0}";
    assertCallMethodWithExpectedValue("getAddOneMapStringDouble", inputValue,
        expectedValue);
  }

  @Test
  public void should_FailToConvertToMapOfDouble_WhenReceiveMapOfInteger()
      throws Exception {
    String inputValue = "{\"key\": 2}";
    String expectedValue = "{\"key\":3.0}";
    assertCallMethodWithExpectedValue("getAddOneMapStringDouble", inputValue,
        expectedValue);
  }

  @Test
  public void should_FailToConvertToMapOfStringEnum_WhenReceiveMapOfStringEnum()
      throws Exception {
    String inputValue = "{\"first_key\": \"FIRST\", \"second_key\": \"SECOND\"}";
    String expectedValue = "{\"first_key\":\"SECOND\",\"second_key\":\"THIRD\"}";
    assertCallMethodWithExpectedValue("getNextEnumMapStringEnum", inputValue,
        expectedValue);
  }

  @Test
  public void should_ConvertToMapOfEnumInteger_WhenReceiveMapOfEnumInteger()
      throws Exception {
    String inputValue = "{\"FIRST\": \"1\"}";
    String expectedValue = "{\"FIRST\":2}";
    assertCallMethodWithExpectedValue("getAddOneMapEnumInteger", inputValue,
        expectedValue);
  }
}
