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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Intercepts calls to ResourceHttpRequestHandler in order to verify when the
 * resource does not exist and forward to '/'
 * 
 * It only intercepts paths without extension or with the the `.html` one.
 */
@Configuration
public class VaadinFrontendInterceptor
    implements HandlerInterceptor, WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new VaadinFrontendInterceptor())
        .addPathPatterns("/**/*")
        .excludePathPatterns("/**/{filename:.*\\.(?!html)[a-z]+}");
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {

    if (handler instanceof ResourceHttpRequestHandler) {
      // Check whether the static resource can be handled
      ((ResourceHttpRequestHandler) handler).handleRequest(request,
          // Use a wrapper to catch the not found error
          new HttpServletResponseWrapper(response) {
            @Override
            public void sendError(int sc) throws IOException {
              if (sc == HttpServletResponse.SC_NOT_FOUND) {
                // forward to root if not found
                try {
                  request.getRequestDispatcher("/").forward(request, response);
                } catch (ServletException e) {
                  throw new IOException(e);
                }
              }
            }
          });
    }

    return true;
  }
}
