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

public class ShortConversion extends BaseTypeConversion {
  @Test
  public void should_ConvertToShort_WhenReceiveANumber() throws Exception {
    assertCallMethodWithExpectedValue("addOneShort", "1", "2");
    assertCallMethodWithExpectedValue("addOneShort", "0", "1");
    assertCallMethodWithExpectedValue("addOneShort", "-1", "0");

    assertCallMethodWithExpectedValue("addOneShortBoxed", "1", "2");
    assertCallMethodWithExpectedValue("addOneShortBoxed", "0", "1");
    assertCallMethodWithExpectedValue("addOneShortBoxed", "-1", "0");
  }

  @Test
  public void should_ConvertToShort_WhenReceiveANumberAsString()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneShort", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneShort", "\"0\"", "1");
    assertCallMethodWithExpectedValue("addOneShort", "\"-1\"", "0");

    assertCallMethodWithExpectedValue("addOneShortBoxed", "\"1\"", "2");
    assertCallMethodWithExpectedValue("addOneShortBoxed", "\"0\"", "1");
    assertCallMethodWithExpectedValue("addOneShortBoxed", "\"-1\"", "0");
  }

  @Test
  public void should_ConvertToShort_WhenReceiveDecimalAsNumber()
      throws Exception {
    assertCallMethodWithExpectedValue("addOneShort", "1.1", "2");
    assertCallMethodWithExpectedValue("addOneShort", "0.0", "1");
    assertCallMethodWithExpectedValue("addOneShort", "-1.9", "0");

    assertCallMethodWithExpectedValue("addOneShortBoxed", "1.1", "2");
    assertCallMethodWithExpectedValue("addOneShortBoxed", "0.0", "1");
    assertCallMethodWithExpectedValue("addOneShortBoxed", "-1.9", "0");
  }

  @Test
  public void should_FailToConvertToShort_WhenReceiveDecimalAsString()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("addOneShort",
        "\"1.1\"");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneShortBoxed", "\"1.1\"");
    assertResponseCode(400, stringResponseEntity);
  }

  @Test
  public void should_FailToConvertToShort_WhenReceiveANumberOverflowOrUnderflow()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("addOneShort",
        "32768");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneShort", "-32769");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneShortBoxed", "32768");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("addOneShortBoxed", "-32769");
    assertResponseCode(400, stringResponseEntity);
  }
}
