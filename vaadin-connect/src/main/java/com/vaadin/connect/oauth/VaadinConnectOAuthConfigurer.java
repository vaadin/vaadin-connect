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

package com.vaadin.connect.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import com.vaadin.connect.VaadinConnectProperties;

/**
 * A class to configure Spring OAUth2 Authorization Server with Vaadin Connect
 * defaults.
 */
@Component
@Import(VaadinConnectProperties.class)
public class VaadinConnectOAuthConfigurer
    extends AuthorizationServerConfigurerAdapter {
  private static final String[] SCOPES = new String[] { "read", "write" };
  private static final String[] GRANT_TYPES = new String[] { "password",
      "refresh_token" };

  private final PasswordEncoder encoder;
  private final VaadinConnectProperties vaadinConnectProperties;
  private final TokenStore tokenStore;
  private final JwtAccessTokenConverter accessTokenConverter;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetails;

  /**
   * Creates VaadinConnectOAuthConfigurer bean.
   *
   * @param encoder password encoder bean
   * @param vaadinConnectProperties Vaadin Connect properties bean
   * @param tokenStore token store bean
   * @param accessTokenConverter access token converter bean
   * @param authenticationConfiguration authentication configuration bean
   * @param userDetails custom user details service, optional
   * @param authenticationManager custom authentication manager, optional
   * @throws Exception
   *           if bean configuration fails due to
   *           {@link AuthenticationConfiguration#getAuthenticationManager()}
   */
  public VaadinConnectOAuthConfigurer(PasswordEncoder encoder,
      VaadinConnectProperties vaadinConnectProperties, TokenStore tokenStore,
      JwtAccessTokenConverter accessTokenConverter,
      AuthenticationConfiguration authenticationConfiguration,
      @Autowired(required = false) UserDetailsService userDetails,
      @Autowired(required = false) AuthenticationManager authenticationManager)
      throws Exception {
    this.encoder = encoder;
    this.vaadinConnectProperties = vaadinConnectProperties;
    this.tokenStore = tokenStore;
    this.accessTokenConverter = accessTokenConverter;
    this.userDetails = userDetails;
    this.authenticationManager = authenticationManager != null
        ? authenticationManager
        : authenticationConfiguration.getAuthenticationManager();
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    // This is required for 'password' and 'refresh_token' grants, which is
    // specified below
    endpoints.userDetailsService(userDetails)
        .authenticationManager(authenticationManager).tokenStore(tokenStore)
        .accessTokenConverter(accessTokenConverter);
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients)
      throws Exception {
    clients.inMemory()
        .withClient(vaadinConnectProperties.getVaadinConnectClientAppname())
        .secret(encoder
            .encode(vaadinConnectProperties.getVaadinConnectClientSecret()))
        .scopes(SCOPES).authorizedGrantTypes(GRANT_TYPES);
  }
}
