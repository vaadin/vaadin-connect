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

public class FloatConversion extends BaseTypeConversion {
  @Test
  public void should_ConvertToFloat_WhenReceiveANumber() throws Exception {
    assertCallMethodWithExpectedValue("addOneFloat", "1", "2.0");
    assertCallMethodWithExpectedValue("addOneFloat", "-1", "0.0");
    assertCallMethodWithExpectedValue("addOneFloat", "0", "1.0");

    assertCallMethodWithExpectedValue("addOneFloatBoxed", "1", "2.0");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "-1", "0.0");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "0", "1.0");
  }

  @Test
  public void should_ConvertToFloat_WhenReceiveANumberAsString()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneFloat", "\"1\"", "2.0");
    assertCallMethodWithExpectedValue("addOneFloat", "\"-1\"", "0.0");
    assertCallMethodWithExpectedValue("addOneFloat", "\"0\"", "1.0");

    assertCallMethodWithExpectedValue("addOneFloatBoxed", "\"1\"", "2.0");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "\"-1\"", "0.0");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "\"0\"", "1.0");
  }

  @Test
  public void should_ConvertToFloat_WhenReceiveDecimalAsNumber()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneFloat", "1.1", "2.1");
    assertCallMethodWithExpectedValue("addOneFloat", "1.9", "2.9");
    assertCallMethodWithExpectedValue("addOneFloat", "-1.9", "-0.9");

    assertCallMethodWithExpectedValue("addOneFloatBoxed", "1.1", "2.1");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "1.9", "2.9");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "-1.9", "-0.9");
  }

  @Test
  public void should_ConvertToFloat_WhenReceiveDecimalAsString()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneFloat", "\"1.1\"", "2.1");
    assertCallMethodWithExpectedValue("addOneFloat", "\"1.9\"", "2.9");
    assertCallMethodWithExpectedValue("addOneFloat", "\"-1.9\"", "-0.9");

    assertCallMethodWithExpectedValue("addOneFloatBoxed", "\"1.1\"", "2.1");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "\"1.9\"", "2.9");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "\"-1.9\"", "-0.9");
  }

  @Test
  public void should_HandleOverflowFloat_WhenReceiveANumberOverflowOrUnderflow()
      throws Exception {
    String overflowFloat = "3.4028236E38";
    String underflowFloat = "-3.4028235E39";
    assertCallMethodWithExpectedValue("addOneFloat", overflowFloat,
        "\"" + String.valueOf(Float.POSITIVE_INFINITY + "\""));
    assertCallMethodWithExpectedValue("addOneFloat", underflowFloat,
        "\"" + String.valueOf(Float.NEGATIVE_INFINITY + "\""));

    assertCallMethodWithExpectedValue("addOneFloatBoxed", overflowFloat,
        "\"" + String.valueOf(Float.POSITIVE_INFINITY + "\""));
    assertCallMethodWithExpectedValue("addOneFloatBoxed", underflowFloat,
        "\"" + String.valueOf(Float.NEGATIVE_INFINITY + "\""));
  }

  @Test
  public void should_HandleSpecialCaseForFloat_WhenReceiveSpecialInput()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneFloat", "NaN", "\"NaN\"");
    assertCallMethodWithExpectedValue("addOneFloat", "null", "1.0");
    assertCallMethodWithExpectedValue("addOneFloat", "Infinity", "\"Infinity\"");
    assertCallMethodWithExpectedValue("addOneFloat", "-Infinity", "\"-Infinity\"");

    assertCallMethodWithExpectedValue("addOneFloatBoxed", "NaN", "\"NaN\"");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "null", "null");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "Infinity",
        "\"Infinity\"");
    assertCallMethodWithExpectedValue("addOneFloatBoxed", "-Infinity",
        "\"-Infinity\"");
  }
}
