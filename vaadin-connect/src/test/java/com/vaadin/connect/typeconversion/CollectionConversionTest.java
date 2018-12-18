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
import org.springframework.http.ResponseEntity;

public class CollectionConversionTest extends BaseTypeConversion {

  @Test
  public void should_ConvertToIntegerCollection_WhenReceiveNumberArray()
      throws Exception {
    String inputArray = "[1,3,2,3]";
    String expectedArray = "[1,3,2,3]";
    assertCallMethodWithExpectedValue("getIntegerCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToIntegerSet_WhenReceiveNumberArray()
    throws Exception {
    String inputArray = "[1,3,2,3]";
    String expectedArray = "[1,3,2,3]";
    assertCallMethodWithExpectedValue("getIntegerCollection", inputArray,
      expectedArray);
  }

  @Test
  public void should_ConvertToIntegerCollection_WhenReceiveMixedDecimalNumberArray()
      throws Exception {
    String inputArray = "[1,2.1]";
    assertCallMethodWithExpectedValue("getIntegerCollection", inputArray,
        "[1,2]");
  }

  @Test
  public void should_FailToConvertToIntegerCollection_WhenReceiveMixedDecimalStringNumberArray()
      throws Exception {
    String inputArray = "[1,\"3.0\",2,3]";
    ResponseEntity<String> responseEntity = callMethod("getIntegerCollection",
        inputArray);
    assertResponseCode(400, responseEntity);
  }

  @Test
  public void should_FailToConvertToIntegerCollection_WhenReceiveAString()
      throws Exception {
    String inputArray = "\"[1]\"";
    ResponseEntity<String> responseEntity = callMethod("getIntegerCollection",
        inputArray);
    assertResponseCode(400, responseEntity);
  }

  @Test
  public void should_ConvertToStringCollection_WhenReceiveStringArray()
      throws Exception {
    String inputArray = "[\"first\",\"2.0\",\"-3\",\"4\"]";
    String expectedArray = "[\"first\",\"2.0\",\"-3\",\"4\"]";
    assertCallMethodWithExpectedValue("getStringCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToStringCollection_WhenReceiveNumberArray()
      throws Exception {
    String inputArray = "[1,2,3,4]";
    assertCallMethodWithExpectedValue("getStringCollection", inputArray,
        "[\"1\",\"2\",\"3\",\"4\"]");
  }

  @Test
  public void should_ConvertToDoubleCollection_WhenReceiveNumberArray()
      throws Exception {
    String inputArray = "[1.9,3.2,-2.0,0.3]";
    String expectedArray = "[1.9,3.2,-2.0,0.3]";
    assertCallMethodWithExpectedValue("getDoubleCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_FailToConvertToDoubleCollection_WhenReceiveArrayContainInteger()
      throws Exception {
    String inputArray = "[1]";
    assertCallMethodWithExpectedValue("getDoubleCollection", inputArray,
        "[1.0]");
  }

  @Test
  public void should_ConvertToDoubleCollection_WhenReceiveArrayContainString()
      throws Exception {
    String inputArray = "[\"1.0\"]";
    assertCallMethodWithExpectedValue("getDoubleCollection", inputArray,
        "[1.0]");
  }

  @Test
  public void should_ConvertToObjectCollection_WhenReceiveArrayObject()
      throws Exception {
    String inputArray = "[\"1.0\", 1, 0.0, -99, {\"property\": \"value\"}]";
    String expectedArray = "[\"1.0\",1,0.0,-99,{\"property\":\"value\"}]";
    assertCallMethodWithExpectedValue("getObjectCollection", inputArray,
        expectedArray);
  }
}
