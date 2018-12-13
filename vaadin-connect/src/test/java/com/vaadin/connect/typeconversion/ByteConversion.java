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

public class ByteConversion extends BaseTypeConversion {
  @Test
  public void should_ConvertNumberToByte_WhenReceiveNumber() throws Exception {
    assertCallMethodWithExpectedValue("addOneByte", "1", "2");
    assertCallMethodWithExpectedValue("addOneByte", "0", "1");
    assertCallMethodWithExpectedValue("addOneByte", "-1", "0");

    assertCallMethodWithExpectedValue("addOneByteBoxed", "1", "2");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "0", "1");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "-1", "0");
  }

  @Test
  public void should_ConvertNumberToByte_WhenReceiveNumberAsString()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneByte", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneByte", "\"0\"", "1");
    assertCallMethodWithExpectedValue("addOneByte", "\"-1\"", "0");

    assertCallMethodWithExpectedValue("addOneByteBoxed", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "\"0\"", "1");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "\"-1\"", "0");
  }

  @Test
  public void should_ConvertNumberToByte_WhenReceiveDecimalAsNumber()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneByte", "1.1", "2");
    assertCallMethodWithExpectedValue("addOneByte", "0.0", "1");
    assertCallMethodWithExpectedValue("addOneByte", "-1.9", "0");

    assertCallMethodWithExpectedValue("addOneByteBoxed", "1.1", "2");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "0.0", "1");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "-1.9", "0");
  }

  @Test
  public void should_FailToConvertNumberToByte_WhenReceiveDecimalAsString()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("addOneByte",
        "\"1.1\"");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneByteBoxed", "\"1.1\"");
    assertResponseCode(400, stringResponseEntity);
  }

  @Test
  public void should_HandleOverflowByte_WhenReceiveOverflowNumber()
      throws Exception {
    String overflowInputByte = "128";
    assertCallMethodWithExpectedValue("addOneByte", overflowInputByte,
        String.valueOf(Byte.MIN_VALUE + 1));
    assertCallMethodWithExpectedValue("addOneByteBoxed", overflowInputByte,
        String.valueOf(Byte.MIN_VALUE + 1));
  }

  @Test
  public void should_FailToHandleUnderflowByte_WhenReceiveUnderflowNumber()
      throws Exception {
    String underflowInputByte = "-129";
    ResponseEntity<String> stringResponseEntity = callMethod("addOneByte",
        underflowInputByte);
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneByteBoxed", underflowInputByte);
    assertResponseCode(400, stringResponseEntity);
  }

  @Test
  public void should_HandleSpecialInputForByte_WhenSpecialInput()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneByte", "null", "1");
    assertCallMethodWithExpectedValue("addOneByte", "NaN", "1");

    assertCallMethodWithExpectedValue("addOneByteBoxed", "null", "null");
    assertCallMethodWithExpectedValue("addOneByteBoxed", "NaN", "1");
  }
}
