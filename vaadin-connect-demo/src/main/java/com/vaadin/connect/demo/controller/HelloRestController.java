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
package com.vaadin.connect.demo.controller;

import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple controller for hello messages
 */
@RestController
@RequestMapping("/hello")
public class HelloRestController {

    /**
     * A remote method served via the '/hello' path.
     * When the app is correctly configured, it should be served only when
     * authentication and oauth credentials are correctly sent.
     *
     * @param request
     * @return a Hello message
     * @throws AccountNotFoundException
     */
    @GetMapping
    String sayHello(HttpServletRequest request) throws AccountNotFoundException {
        return "Hello Word";
    }
}
