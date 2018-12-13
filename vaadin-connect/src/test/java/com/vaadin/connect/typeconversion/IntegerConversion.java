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

public class IntegerConversion extends BaseTypeConversion {

  @Test
  public void should_ConvertNumberToInt_WhenReceiveNumberAsNumber()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneInt", "1", "2");
    assertCallMethodWithExpectedValue("addOneInt", "0", "1");
    assertCallMethodWithExpectedValue("addOneInt", "-1", "0");

    assertCallMethodWithExpectedValue("addOneIntBoxed", "1", "2");
    assertCallMethodWithExpectedValue("addOneIntBoxed", "0", "1");
    assertCallMethodWithExpectedValue("addOneIntBoxed", "-1", "0");
  }

  @Test
  public void should_ConvertNumberToInt_WhenReceiveNumberAsString()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneInt", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneInt", "\"0\"", "1");
    assertCallMethodWithExpectedValue("addOneInt", "\"-1\"", "0");

    assertCallMethodWithExpectedValue("addOneIntBoxed", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneIntBoxed", "\"0\"", "1");
    assertCallMethodWithExpectedValue("addOneIntBoxed", "\"-1\"", "0");
  }

  @Test
  public void should_HandleOverflowInteger_WhenReceiveOverflowNumber()
      throws Exception {
    String overflowInputInteger = "2147483648";
    assertCallMethodWithExpectedValue("addOneInt", overflowInputInteger,
        String.valueOf(Integer.MIN_VALUE + 1));
    assertCallMethodWithExpectedValue("addOneIntBoxed", overflowInputInteger,
        String.valueOf(Integer.MIN_VALUE + 1));

    String underflowInputInteger = "-2147483649";
    // underflow will become MAX, then +1 in the method => MIN
    assertCallMethodWithExpectedValue("addOneInt", underflowInputInteger,
        String.valueOf(Integer.MIN_VALUE));
    assertCallMethodWithExpectedValue("addOneIntBoxed", underflowInputInteger,
        String.valueOf(Integer.MIN_VALUE));
  }

  @Test
  public void should_ConvertDecimalToInt_WhenReceiveADecimalAsNumber()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneInt", "1.1", "2");
    assertCallMethodWithExpectedValue("addOneInt", "-1.9", "0");

    assertCallMethodWithExpectedValue("addOneIntBoxed", "1.1", "2");
    assertCallMethodWithExpectedValue("addOneIntBoxed", "-1.9", "0");
  }

  @Test
  public void should_FailToConvertDecimalToInt_WhenReceiveADecimalAsString()
      throws Exception {
    ResponseEntity<String> responseEntity = callMethod("addOneInt", "\"1.1\"");
    assertResponseCode(400, responseEntity);

    responseEntity = callMethod("addOneIntBoxed", "\"1.1\"");
    assertResponseCode(400, responseEntity);
  }

  @Test
  public void should_HandleSpecialInputForInt_WhenReceiveSpecialInput()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneInt", "null", "1");
    assertCallMethodWithExpectedValue("addOneInt", "NaN", "1");

    assertCallMethodWithExpectedValue("addOneIntBoxed", "null", "null");
    assertCallMethodWithExpectedValue("addOneIntBoxed", "NaN", "1");
  }
}
