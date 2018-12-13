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

public class ArrayConversionTest extends BaseTypeConversion {

  @Test
  public void should_ConvertToArrayInt_WhenReceiveArrayInt() throws Exception {
    String inputArray = "[1,2,3]";
    String expectedArray = "[2,3,4]";
    assertCallMethodWithExpectedValue("getAddOneArray", inputArray,
        expectedArray);
  }

  @Test
  public void should_FailToConvertToArrayInt_WhenReceiveMixedIntStringArray()
      throws Exception {
    String inputArray = "[1,\"string-value\",2,3]";
    ResponseEntity<String> responseEntity = callMethod("getAddOneArray",
        inputArray);
    assertResponseCode(400, responseEntity);
  }

  @Test
  public void should_ConvertToArrayInt_WhenReceiveMixedNumberArray()
      throws Exception {
    String inputArray = "[1,2.0,-3.75,NaN]";
    String expectedArray = "[2,3,-2,1]";
    assertCallMethodWithExpectedValue("getAddOneArray", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToArrayObject_WhenReceiveMixedArray()
      throws Exception {
    String inputArray = "[1,2.0,-3.75,\"MyString\",[1,2,3]]";
    String expectedArray = "[1,2.0,-3.75,\"MyString\",[1,2,3]]";
    assertCallMethodWithExpectedValue("getObjectArray", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToArrayString_WhenReceiveMixedStringNumberArray()
      throws Exception {
    String inputArray = "[1,\"string-value\",2.0,3]";
    String expectedArray = "[\"1-foo\",\"string-value-foo\",\"2.0-foo\",\"3-foo\"]";
    assertCallMethodWithExpectedValue("getFooStringArray", inputArray,
        expectedArray);
  }
}
