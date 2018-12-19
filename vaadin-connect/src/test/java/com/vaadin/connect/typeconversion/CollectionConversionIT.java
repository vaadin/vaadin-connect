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
    String expectedArray = "[2,4,3,4]";
    assertEqualExpectedValueWhenCallingMethod("addOneIntegerCollection",
        inputArray, expectedArray);
  }

  @Test
  public void should_ConvertToIntegerSet_When_ReceiveNumberArray() {
    String inputArray = "[1,3,2,3]";
    String expectedArray = "[2,4,3,4]";
    assertEqualExpectedValueWhenCallingMethod("addOneIntegerCollection",
        inputArray, expectedArray);
  }

  @Test
  public void should_ConvertToIntegerCollection_When_ReceiveMixedDecimalNumberArray() {
    String inputArray = "[1,2.1]";
    assertEqualExpectedValueWhenCallingMethod("addOneIntegerCollection",
        inputArray, "[2,3]");
  }

  @Test
  public void should_FailToConvertToIntegerCollection_When_ReceiveMixedDecimalStringNumberArray() {
    String inputArray = "[1,\"3.0\",2,3]";
    assert400ResponseWhenCallingMethod("addOneIntegerCollection", inputArray);
  }

  @Test
  public void should_FailToConvertToIntegerCollection_When_ReceiveAString() {
    String inputArray = "\"[1]\"";
    assert400ResponseWhenCallingMethod("addOneIntegerCollection", inputArray);
  }

  @Test
  public void should_ConvertToStringCollection_When_ReceiveStringArray() {
    String inputArray = "[\"first\",\"2.0\",\"-3\",\"4\"]";
    String expectedArray = "[\"firstfoo\",\"2.0foo\",\"-3foo\",\"4foo\"]";
    assertEqualExpectedValueWhenCallingMethod("addFooStringCollection", inputArray,
        expectedArray);
  }

  @Test
  public void should_ConvertToStringCollection_When_ReceiveNumberArray() {
    String inputArray = "[1,2,3,4]";
    assertEqualExpectedValueWhenCallingMethod("addFooStringCollection", inputArray,
        "[\"1foo\",\"2foo\",\"3foo\",\"4foo\"]");
  }

  @Test
  public void should_ConvertToDoubleCollection_When_ReceiveNumberArray() {
    String inputArray = "[1.9,3.2,-2.0,0.3]";
    String expectedArray = "[2.9,4.2,-1.0,1.3]";
    assertEqualExpectedValueWhenCallingMethod("getPlusOneDouble", inputArray,
        expectedArray);
  }

  @Test
  public void should_FailToConvertToDoubleCollection_When_ReceiveArrayContainInteger() {
    String inputArray = "[1]";
    assertEqualExpectedValueWhenCallingMethod("getPlusOneDouble", inputArray,
        "[2.0]");
  }

  @Test
  public void should_ConvertToDoubleCollection_When_ReceiveArrayContainString() {
    String inputArray = "[\"1.0\"]";
    assertEqualExpectedValueWhenCallingMethod("getPlusOneDouble", inputArray,
        "[2.0]");
  }

  @Test
  public void should_ConvertToObjectCollection_When_ReceiveArrayObject() {
    String inputArray = "[\"1.0\", 1, 0.0, -99, {\"property\": \"value\"}]";
    String expectedArray = "[\"1.0\",1,0.0,-99,{\"property\":\"value\"}]";
    assertEqualExpectedValueWhenCallingMethod("getObjectCollection", inputArray,
        expectedArray);
  }
}
