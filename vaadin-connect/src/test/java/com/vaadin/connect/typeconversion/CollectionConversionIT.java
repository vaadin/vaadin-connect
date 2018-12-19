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

public class CollectionConversionIT extends BaseTypeConversion {

  @Test
  public void should_ConvertToIntegerCollection_When_ReceiveNumberArray() {
    String inputArray = "[1,3,2,3]";
    String expectedArray = "[1,3,2,3]";
    assertEqualExpectedValueWhenCallingMethod("getIntegerCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToIntegerSet_When_ReceiveNumberArray() {
    String inputArray = "[1,3,2,3]";
    String expectedArray = "[1,3,2,3]";
    assertEqualExpectedValueWhenCallingMethod("getIntegerCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToIntegerCollection_When_ReceiveMixedDecimalNumberArray() {
    String inputArray = "[1,2.1]";
    assertEqualExpectedValueWhenCallingMethod("getIntegerCollection", inputArray,
        "[1,2]");
  }

  @Test
  public void should_FailToConvertToIntegerCollection_When_ReceiveMixedDecimalStringNumberArray() {
    String inputArray = "[1,\"3.0\",2,3]";
    assert400ResponseWhenCallingMethod("getIntegerCollection", inputArray);
  }

  @Test
  public void should_FailToConvertToIntegerCollection_When_ReceiveAString() {
    String inputArray = "\"[1]\"";
    assert400ResponseWhenCallingMethod("getIntegerCollection", inputArray);
  }

  @Test
  public void should_ConvertToStringCollection_When_ReceiveStringArray() {
    String inputArray = "[\"first\",\"2.0\",\"-3\",\"4\"]";
    String expectedArray = "[\"first\",\"2.0\",\"-3\",\"4\"]";
    assertEqualExpectedValueWhenCallingMethod("getStringCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToStringCollection_When_ReceiveNumberArray() {
    String inputArray = "[1,2,3,4]";
    assertEqualExpectedValueWhenCallingMethod("getStringCollection", inputArray,
        "[\"1\",\"2\",\"3\",\"4\"]");
  }

  @Test
  public void should_ConvertToDoubleCollection_When_ReceiveNumberArray() {
    String inputArray = "[1.9,3.2,-2.0,0.3]";
    String expectedArray = "[1.9,3.2,-2.0,0.3]";
    assertEqualExpectedValueWhenCallingMethod("getDoubleCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_FailToConvertToDoubleCollection_When_ReceiveArrayContainInteger() {
    String inputArray = "[1]";
    assertEqualExpectedValueWhenCallingMethod("getDoubleCollection", inputArray,
        "[1.0]");
  }

  @Test
  public void should_ConvertToDoubleCollection_When_ReceiveArrayContainString() {
    String inputArray = "[\"1.0\"]";
    assertEqualExpectedValueWhenCallingMethod("getDoubleCollection", inputArray,
        "[1.0]");
  }

  @Test
  public void should_ConvertToObjectCollection_When_ReceiveArrayObject() {
    String inputArray = "[\"1.0\", 1, 0.0, -99, {\"property\": \"value\"}]";
    String expectedArray = "[\"1.0\",1,0.0,-99,{\"property\":\"value\"}]";
    assertEqualExpectedValueWhenCallingMethod("getObjectCollection", inputArray,
        expectedArray);
  }
}
