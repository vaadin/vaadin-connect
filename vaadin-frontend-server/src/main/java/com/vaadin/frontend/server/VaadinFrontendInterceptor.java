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
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Intercepts calls to ResourceHttpRequestHandler in order to verify when the
 * resource does not exist and forward to '/'
 *
 * By default it only intercepts paths without any extension. If you want to
 * configure the interceptor to match your routing pattern, you need to provide
 * a {@link VaadinFrontendRouteMatcher} bean in your configuration.
 */
@Configuration
public class VaadinFrontendInterceptor
    implements HandlerInterceptor, WebMvcConfigurer {

  private final PathMatcher pathMatcher;

  /**
   * Default constructor.
   * 
   * @param routeMatcher
   *          the custom route matcher for the interceptor, if null it uses
   *          default implementation
   */
  public VaadinFrontendInterceptor(
      @Autowired(required = false) VaadinFrontendRouteMatcher routeMatcher) {
    pathMatcher = new DelegatingPathMatcher(routeMatcher);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this).addPathPatterns("*")
        .pathMatcher(pathMatcher);
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {

    if (handler instanceof ResourceHttpRequestHandler) {
      // Wrap the response to check if sendError is called
      HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(
          response) {
        @Override
        public void sendError(int sc) throws IOException {
          setStatus(sc);
        }
      };
      wrappedResponse.setStatus(SC_OK);

      // Check whether the static resource can be handled
      ((ResourceHttpRequestHandler) handler).handleRequest(request,
          wrappedResponse);

      // forward to root if not found
      if (wrappedResponse.getStatus() == SC_NOT_FOUND) {
        request.getRequestDispatcher("/").forward(request, response);
        // pretend that the response is OK since we forwarded it
        wrappedResponse.setStatus(SC_OK);
      }

      // handleRequest was already run, do not continue
      return false;
    }
    return true;
  }

  private static class DelegatingPathMatcher implements PathMatcher {
    private final VaadinFrontendRouteMatcher routeMatcher;

    private DelegatingPathMatcher(VaadinFrontendRouteMatcher routeMatcher) {
      this.routeMatcher = routeMatcher != null ? routeMatcher
          : new VaadinFrontendRouteMatcher() {
          };
    }
    @Override
    public boolean match(String pattern, String path) {
      return routeMatcher.isDynamicRoutePath(path);
    }
    @Override
    public boolean isPattern(String path) {
      throw new UnsupportedOperationException();
    }
    @Override
    public boolean matchStart(String pattern, String path) {
      throw new UnsupportedOperationException();
    }
    @Override
    public String extractPathWithinPattern(String pattern, String path) {
      throw new UnsupportedOperationException();
    }
    @Override
    public Map<String, String> extractUriTemplateVariables(String pattern,
        String path) {
      throw new UnsupportedOperationException();
    }
    @Override
    public Comparator<String> getPatternComparator(String path) {
      throw new UnsupportedOperationException();
    }
    @Override
    public String combine(String pattern1, String pattern2) {
      throw new UnsupportedOperationException();
    }
  }
}
