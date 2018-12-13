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

public class DateTimeConversion extends BaseTypeConversion {

  // region date tests
  @Test
  public void should_ConvertToDate_WhenReceiveATimeStampAsNumber()
      throws Exception {
    String timeStamp = "1546300800";
    assertCallMethodWithExpectedValue("getDate", timeStamp, timeStamp);
  }

  @Test
  public void should_ConvertToDate_WhenReceiveATimeStampAsString()
      throws Exception {
    String timeStamp = "\"1546300800\"";
    String expected = "1546300800";
    assertCallMethodWithExpectedValue("getDate", timeStamp, expected);
  }

  @Test
  public void should_ConvertToDate_WhenReceiveADate() throws Exception {
    String inputDate = "\"2019-01-01\"";
    String expectedTimestamp = "1546300800000";
    assertCallMethodWithExpectedValue("getDate", inputDate, expectedTimestamp);
  }

  @Test
  public void should_ConvertToNullForDate_WhenReceiveANull() throws Exception {
    String timeStamp = "null";
    assertCallMethodWithExpectedValue("getDate", timeStamp, timeStamp);
  }
  // endregion
  // region LocalDate tests
  // LocalDate uses java.time.format.DateTimeFormatter.ISO_LOCAL_DATE as default
  // format

  @Test
  public void should_ConvertToLocalDate_WhenReceiveALocalDate()
      throws Exception {
    String inputDate = "\"2019-12-13\"";
    String expectedTimestamp = "[2019,12,14]";
    assertCallMethodWithExpectedValue("addOneDayLocalDate", inputDate,
        expectedTimestamp);
  }

  @Test
  public void should_FailToConvertToLocalDate_WhenReceiveWrongFormat()
      throws Exception {
    String inputDate = "\"2019:12:13\"";
    ResponseEntity<String> responseEntity = callMethod("addOneDayLocalDate",
        inputDate);
    assertResponseCode(400, responseEntity);
  }
  // endregion

  // region LocalDateTime tests
  // LocalDate uses java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME as
  // default format
  @Test
  public void should_ConvertToLocalDateTime_WhenReceiveALocalDateTime()
      throws Exception {
    String inputDate = "\"2019-12-13T12:12:12\"";
    String expectedTimestamp = "[2019,12,14,13,12,12]";
    assertCallMethodWithExpectedValue("addOneDayOneHourLocalDateTime", inputDate,
        expectedTimestamp);
  }

  @Test
  public void should_FailToConvertToLocalDateTime_WhenReceiveWrongFormat()
      throws Exception {
    String inputDate = "\"2019-12-13\"";
    ResponseEntity<String> responseEntity = callMethod("addOneDayOneHourLocalDateTime",
        inputDate);
    assertResponseCode(400, responseEntity);
  }
  // endregion

}
