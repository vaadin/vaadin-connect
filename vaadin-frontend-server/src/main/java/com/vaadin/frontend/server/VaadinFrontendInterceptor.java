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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

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

  // We override `preHandle` instead of `postHandle` because in post the
  // response comes modified which prevents forwarding
  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object o) throws Exception {

    if (o instanceof ResourceHttpRequestHandler) {

      // To avoid calling ResourceHttpRequestHandler.handleRequest which
      // modifies the response, we call the protected method
      // ResourceHttpRequestHandler.getResource in order to guarantee to do the
      // same check that handleRequest does.
      Method getResource = o.getClass().getDeclaredMethod("getResource",
          HttpServletRequest.class);
      getResource.setAccessible(true);

      // Check whether the resource exists and forward to root if it doesn't
      if (getResource.invoke(o, request) == null) {
        // Do the forwarding
        request.getRequestDispatcher("/").forward(request, response);
        return false;
      }
    }

    return true;
  }
}
