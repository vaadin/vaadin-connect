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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Enable Vaadin Frontend Server which is a Single Page Application Web Server.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(VaadinFrontendInterceptor.class)
public @interface EnableVaadinFrontendServer {

  /**
   * Pattern used in the application for routing views. These paths should be
   * redirected to `/` when not found.
   * 
   * Default is to include all requests
   */
  String[] dynamicRoutesPattern() default "/**/*";

  /**
   * Pattern used for static content. These paths should not be redirected to
   * `/` when not found
   * 
   * A String Array of Ant patterns. Default is any file with extension
   * excluding `.html`
   */
  String[] staticContentPattern() default "/**/{filename:.*\\.(?!html)[a-z]+}";
}
