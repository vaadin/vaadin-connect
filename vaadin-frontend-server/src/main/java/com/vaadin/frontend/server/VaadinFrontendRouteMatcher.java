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
package com.vaadin.frontend.server;

/**
 * Provide a bean implementing this interface if you want to customize the path
 * end-points that match your single page application routing schema.
 *
 */
public interface VaadinFrontendRouteMatcher {

  /**
   * Return whether a request meets your routing schema.
   *
   * @param path
   *          the request path without the initial slash.
   * @return true if the path is a valid route. The default implementation
   *         returns true when the path does not have an extension.
   */
  default boolean isDynamicRoutePath(String path) {
    return path != null && !path.matches("^.*\\.[a-zA-Z0-9]+$");
  }
}
