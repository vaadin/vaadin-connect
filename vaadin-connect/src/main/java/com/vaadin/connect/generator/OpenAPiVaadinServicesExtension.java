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
 * Object model for vaadin services extension in OpenAPI spec file. It supposes
 * to provide additional information for a service
 */
public class OpenAPiVaadinServicesExtension {
  private String description;

  /**
   * Get description of the service
   *
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set description for the service
   *
   * @param description
   *         description of the service
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Set description for the service and return the instance
   *
   * @param description
   *         descrition to be set
   * @return the current object model instance
   */
  public OpenAPiVaadinServicesExtension description(String description) {
    this.description = description;
    return this;
  }
}
