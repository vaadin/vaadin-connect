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

public class CharacterConversion extends BaseTypeConversion {
  @Test
  public void should_ConvertToChar_WhenReceiveASingleCharOrNumber()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("getChar",
        "\"a\"");
    Assert.assertEquals("\"a\"", stringResponseEntity.getBody());
    callMethod("getCharBoxed", "\"a\"");
    Assert.assertEquals("\"a\"", stringResponseEntity.getBody());

    int maxValueCanBeCastToChar = 0xFFFF;
    stringResponseEntity = callMethod("getChar",
        String.valueOf(maxValueCanBeCastToChar));
    Assert.assertEquals((char) maxValueCanBeCastToChar,
        getCharFromResponse(stringResponseEntity.getBody()));
    stringResponseEntity = callMethod("getCharBoxed",
        String.valueOf(maxValueCanBeCastToChar));
    Assert.assertEquals((char) maxValueCanBeCastToChar,
        getCharFromResponse(stringResponseEntity.getBody()));

  }

  @Test
  public void should_FailToConvertToChar_WhenReceiveInvalidNumber()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("getChar", "1.1");
    assertResponseCode(400, stringResponseEntity);
    stringResponseEntity = callMethod("getCharBoxed", "1.1");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("getChar", "-1");
    assertResponseCode(400, stringResponseEntity);
    stringResponseEntity = callMethod("getCharBoxed", "-1");
    assertResponseCode(400, stringResponseEntity);

    int overMax = 0xFFFF + 1;
    stringResponseEntity = callMethod("getChar", String.valueOf(overMax));
    assertResponseCode(400, stringResponseEntity);
    stringResponseEntity = callMethod("getCharBoxed", String.valueOf(overMax));
    assertResponseCode(400, stringResponseEntity);
  }

  @Test
  public void should_FailToConvertToChar_WhenReceiveLongString()
      throws Exception {
    ResponseEntity<String> stringResponseEntity = callMethod("getChar",
        "\"aa\"");
    assertResponseCode(400, stringResponseEntity);

    stringResponseEntity = callMethod("getCharBoxed", "\"aa\"");
    assertResponseCode(400, stringResponseEntity);
  }

  private char getCharFromResponse(String response) {
    if (response.length() > 3) {
      return (char) Integer
          .parseInt(response.substring(3, response.length() - 1));
    } else {
      return response.charAt(1);
    }
  }
}
