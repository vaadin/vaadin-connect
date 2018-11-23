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
package com.vaadin.connect.oauth;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.stereotype.Component;

import com.vaadin.connect.VaadinConnectProperties;

/**
 * A resource configurer adapter for Vaadin Connect that allows all Vaadin
 * Connect requests (authorized and anonymous) to access the
 * {@link com.vaadin.connect.VaadinConnectController} where they get validated
 * with {@link VaadinConnectOAuthAclChecker} for access permissions.
 *
 * @see VaadinConnectOAuthAclChecker
 */
@Component
public class VaadinConnectResourceServerConfigurer
    extends ResourceServerConfigurerAdapter {
  private final VaadinConnectProperties connectProperties;

  /**
   * Creates the configurer instance.
   *
   * @param connectProperties
   *          the properties to get the endpoint information from
   */
  public VaadinConnectResourceServerConfigurer(
      VaadinConnectProperties connectProperties) {
    this.connectProperties = connectProperties;
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(
            matchEndpointRequests(connectProperties.getVaadinConnectEndpoint()))
        .permitAll().anyRequest().authenticated();
  }

  private String matchEndpointRequests(String connectServiceEndpoint) {
    String endpointAntMatcher = connectServiceEndpoint.endsWith("/")
        ? connectServiceEndpoint
        : connectServiceEndpoint + '/';
    return endpointAntMatcher + "**";
  }
}
