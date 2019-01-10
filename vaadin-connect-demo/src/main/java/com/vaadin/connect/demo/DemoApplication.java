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
package com.vaadin.connect.demo;

import com.vaadin.frontend.server.EnableVaadinFrontendServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.connect.oauth.EnableVaadinConnectOAuthServer;

/**
 * Main class of the Vaadin connect demo module.
 */
@SpringBootApplication
@EnableVaadinConnectOAuthServer
@EnableVaadinFrontendServer
public class DemoApplication {

  /**
   * Main method to run the application.
   *
   * @param args
   *          arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
