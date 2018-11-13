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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * Extend this and provide a proper way for getting users to have your
 * vaadin-connect application authentication working.
 */
@SuppressWarnings("deprecation")
public class VaadinConnectOAuthConfigurer extends AuthorizationServerConfigurerAdapter {
  
  private final String DEFAULT_CLIENT_APP_NAME = "vaadin-connect-client";
  private final String DEFAULT_CLIENT_APP_SECRET = "c13nts3cr3t";
  private final String DEFAULT_SIGNING_KEY = "JustAnySigningK3y";
  
  @Autowired
  private AuthenticationConfiguration authenticationConfiguration;

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
    .withClient(getClientApp())
    .secret(getClientAppSecret())
    .scopes("read", "write")
    .authorizedGrantTypes("password", "refresh_token");
  }
  
  /**
   * @return the client app name
   */
  public String getClientApp() {
    return DEFAULT_CLIENT_APP_NAME;
  }

  /**
   * @return the secret key of your client application
   */
  public String getClientAppSecret() {
    return DEFAULT_CLIENT_APP_SECRET;
  }
  
  /**
   * @return the signing key of the application
   */
  public String getSigningKey() {
    return DEFAULT_SIGNING_KEY;
  }
  
  /**
   * @param the username
   * @return the UserDetails 
   */
  public UserDetails getUserDetails(String username) {
    throw new UsernameNotFoundException(
        "You need to provide a proper way in your app to get the UserDetails given an username");
  }

  /**
   * @return the JwtAccessTokenConverter
   */
  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(getSigningKey());
    return converter;
  }

  /**
   * @return the TokenStore
   */
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception{
    // This is required for 'password' grants, which is specified below
    endpoints.authenticationManager(authenticationManager())
      .tokenStore(tokenStore())
      .accessTokenConverter(accessTokenConverter());
  }
  
  /**
   * Override this method if your database uses a custom password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  /**
   * @return the UserDetailsService
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return username -> getUserDetails(username);
  }
  
  /**
   * Override this method if you need a custom authentication manager not based in
   * user/password authentication.
   * 
   * @return the AuthenticationManager
   * @throws Exception
   */
  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
