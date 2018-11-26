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
package com.vaadin.connect.generator;

/**
 * Generator exception wrapper.
 */
public class GeneratorException extends RuntimeException {
  private static final String MORE_INFORMATION = " For more information, "
      + "please checkout the Vaadin Connect Generator documentation page at "
      + "https://vaadin.com/vaadin-connect."; // Link should be replaced later

  /**
   * Create a generator exception.
   *
   * @param s
   *          Error message
   */
  public GeneratorException(String s) {
    super(s + MORE_INFORMATION);
  }
}
