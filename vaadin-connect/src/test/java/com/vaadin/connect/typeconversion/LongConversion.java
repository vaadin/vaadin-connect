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

public class LongConversion extends BaseTypeConversion {

  @Test
  public void should_ConvertToLong_WhenReceiveANumber() throws Exception {
    assertCallMethodWithExpectedValue("addOneLong", "1", "2");
    assertCallMethodWithExpectedValue("addOneLong", "-1", "0");
    assertCallMethodWithExpectedValue("addOneLong", "0", "1");

    assertCallMethodWithExpectedValue("addOneLongBoxed", "1", "2");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "-1", "0");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "0", "1");
  }

  @Test
  public void should_ConvertToLong_WhenReceiveANumberAsString()
      throws Exception {

    assertCallMethodWithExpectedValue("addOneLong", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneLong", "\"-1\"", "0");
    assertCallMethodWithExpectedValue("addOneLong", "\"0\"", "1");

    assertCallMethodWithExpectedValue("addOneLongBoxed", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "\"-1\"", "0");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "\"0\"", "1");
  }

  @Test
  public void should_ConvertToLong_WhenReceiveDecimalAsNumber()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneLong", "1.9", "2");
    assertCallMethodWithExpectedValue("addOneLong", "-1.0", "0");
    assertCallMethodWithExpectedValue("addOneLong", "0.0", "1");

    assertCallMethodWithExpectedValue("addOneLongBoxed", "1.9", "2");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "-1.0", "0");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "0.0", "1");
  }

  @Test
  public void should_FailToConvertToLong_WhenReceiveDecimalAsString()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("addOneLong",
        "\"1.1\"");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneLongBoxed", "\"1.1\"");
    assertResponseCode(400, stringResponseEntity);
  }

  @Test
  public void should_HandleOverflowLong_WhenReceiveANumberOverflowOrUnderflow()
      throws Exception {
    String overflowLong = "9223372036854775808"; // 2^63
    assertCallMethodWithExpectedValue("addOneLong", overflowLong,
        String.valueOf(Long.MIN_VALUE + 1));
    assertCallMethodWithExpectedValue("addOneLongBoxed", overflowLong,
        String.valueOf(Long.MIN_VALUE + 1));

    String underflowLong = "-9223372036854775809"; // -2^63-1
    assertCallMethodWithExpectedValue("addOneLong", underflowLong,
        String.valueOf(Long.MIN_VALUE));
    assertCallMethodWithExpectedValue("addOneLongBoxed", underflowLong,
        String.valueOf(Long.MIN_VALUE));
  }

  @Test
  public void should_HandleSpecialInputForLong_WhenReceiveASpecialInput()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneLong", "null", "1");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "null", "null");

    assertCallMethodWithExpectedValue("addOneLong", "NaN", "1");
    assertCallMethodWithExpectedValue("addOneLongBoxed", "NaN", "1");
  }
}
