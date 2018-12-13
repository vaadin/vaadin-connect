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

public class BooleanConversion extends BaseTypeConversion {
  @Test
  public void should_ConvertToBoolean_WhenReceiveTrueOrFalse()
      throws Exception {
    assertCallMethodWithExpectedValue("revertBoolean", "true", "false");
    assertCallMethodWithExpectedValue("revertBoolean", "false", "true");

    assertCallMethodWithExpectedValue("revertBooleanBoxed", "true", "false");
    assertCallMethodWithExpectedValue("revertBooleanBoxed", "false", "true");
  }

  @Test
  public void should_ConvertToBoolean_WhenReceiveTrueOrFalseAsString()
      throws Exception {
    assertCallMethodWithExpectedValue("revertBoolean", "\"true\"", "false");
    assertCallMethodWithExpectedValue("revertBoolean", "\"false\"", "true");

    assertCallMethodWithExpectedValue("revertBooleanBoxed", "\"true\"", "false");
    assertCallMethodWithExpectedValue("revertBooleanBoxed", "\"false\"", "true");
  }

  @Test
  public void should_ConvertToBoolean_WhenReceiveANumber() throws Exception {
    assertCallMethodWithExpectedValue("revertBoolean", "1", "false");
    assertCallMethodWithExpectedValue("revertBoolean", "-1", "false");
    assertCallMethodWithExpectedValue("revertBoolean", "0", "true");

    assertCallMethodWithExpectedValue("revertBooleanBoxed", "1", "false");
    assertCallMethodWithExpectedValue("revertBooleanBoxed", "-1", "false");
    assertCallMethodWithExpectedValue("revertBooleanBoxed", "0", "true");
  }

  @Test
  public void should_FailToConvertToBoolean_WhenReceiveARandomString()
      throws Exception {
    ResponseEntity<String> responseEntity = callMethod("revertBoolean", "\"foo\"");
    assertResponseCode(400, responseEntity);

    responseEntity = callMethod("revertBooleanBoxed", "\"foo\"");
    assertResponseCode(400, responseEntity);
  }

  @Test
  public void should_FailToConvertToBoolean_WhenReceiveADecimal()
      throws Exception {
    ResponseEntity<String> responseEntity = callMethod("revertBoolean", "1.1");
    assertResponseCode(400, responseEntity);

    responseEntity = callMethod("revertBooleanBoxed", "1.1");
    assertResponseCode(400, responseEntity);
  }

  @Test
  public void should_HandleSpecialInputForBoolean_WhenReceiveSpecialInput()
      throws Exception {
    assertCallMethodWithExpectedValue("revertBoolean", "null", "true");

    assertCallMethodWithExpectedValue("revertBooleanBoxed", "null", "null");
  }
}
