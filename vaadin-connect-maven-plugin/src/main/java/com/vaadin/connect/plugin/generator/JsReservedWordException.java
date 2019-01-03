/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.connect.plugin.generator;

/**
 * It is thrown when a JavaScript reserved word is used as a service name.
 */
public class JsReservedWordException extends RuntimeException {

  private static final long serialVersionUID = -1979926612962819685L;

  /**
   * Constructor for the exception.
   * 
   * @param className
   *          the class name of the service
   * @param methodName
   *          the method name breaking JS language conventions
   */
  public JsReservedWordException(String className, String methodName) {
    super("The method name '" + methodName + "' in the service class '"
        + className + "' is a JavaScript reserved word");
  }
}
