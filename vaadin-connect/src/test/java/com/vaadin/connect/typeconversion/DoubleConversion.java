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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class DoubleConversion extends BaseTypeConversion {
  @Test
  public void should_ConvertToDouble_WhenReceiveANumber() throws Exception {
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "1", "2.0");
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "-1", "0.0");
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "0", "1.0");

    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "1", "2.0");
    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "-1", "0.0");
    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "0", "1.0");
  }

  @Test
  public void should_ConvertToDouble_WhenReceiveANumberAsString()
      throws Exception {
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"1\"", "2.0");
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"-1\"", "0.0");
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"0\"", "1.0");

    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"1\"",
        "2.0");
    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"-1\"",
        "0.0");
    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"0\"",
        "1.0");
  }

  @Test
  public void should_ConvertToDouble_WhenReceiveDecimalAsNumber()
      throws Exception {
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "1.1", "2.1");
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "-1.9", "-0.9");

    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "1.1", "2.1");
    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "-1.9",
        "-0.9");
  }

  @Test
  public void should_ConvertToDouble_WhenReceiveDecimalAsNumberAsString()
      throws Exception {
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"1.1\"", "2.1");
    assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"-1.9\"", "-0.9");

    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"1.1\"",
        "2.1");
    assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"-1.9\"",
        "-0.9");
  }

  @Test
  public void should_HandleOverflowDouble_WhenReceiveANumberOverflowOrUnderflow()
      throws Exception {
    String overflowDouble = "2.7976931348623158E308";
    String underflowDouble = "-2.7976931348623157E308";
    assertCallMethodWithExpectedValue("addOneDouble", overflowDouble,
        "\"" + String.valueOf(Float.POSITIVE_INFINITY + "\""));
    assertCallMethodWithExpectedValue("addOneDouble", underflowDouble,
        "\"" + String.valueOf(Double.NEGATIVE_INFINITY) + "\"");

    assertCallMethodWithExpectedValue("addOneDoubleBoxed", overflowDouble,
        "\"" + String.valueOf(Float.POSITIVE_INFINITY + "\""));
    assertCallMethodWithExpectedValue("addOneDoubleBoxed", underflowDouble,
        "\"" + String.valueOf(Double.NEGATIVE_INFINITY) + "\"");
  }

  @Test
  public void should_ShouldHandleSpecialInputForDouble_WhenReceiveSpecialInput()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneDouble", "null", "1.0");
    assertCallMethodWithExpectedValue("addOneDouble", "NaN", "\"NaN\"");
    assertCallMethodWithExpectedValue("addOneDouble", "Infinity",
        "\"Infinity\"");
    assertCallMethodWithExpectedValue("addOneDouble", "-Infinity",
        "\"-Infinity\"");

    assertCallMethodWithExpectedValue("addOneDoubleBoxed", "null", "null");
    assertCallMethodWithExpectedValue("addOneDoubleBoxed", "NaN", "\"NaN\"");
    assertCallMethodWithExpectedValue("addOneDoubleBoxed", "Infinity",
        "\"Infinity\"");
    assertCallMethodWithExpectedValue("addOneDoubleBoxed", "-Infinity",
        "\"-Infinity\"");
  }

  private void assertCallMethodWithExpectedDoubleValue(String methodName,
      String parameterValue, String expectedValue) throws Exception {
    ResponseEntity<String> responseEntity = callMethod(methodName,
        parameterValue);
    Assert.assertEquals(Double.valueOf(expectedValue),
        Double.valueOf(responseEntity.getBody()), 0.1);
  }
}
