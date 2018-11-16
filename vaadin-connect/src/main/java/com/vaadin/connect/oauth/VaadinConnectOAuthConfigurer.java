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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;

import com.vaadin.connect.VaadinConnectProperties;

/**
 * Class to configure the authentication of a vaadin-connect application
 *
 * Configure oauth by extending or importing this, and defining either
 * {@link UserDetailsService} or {@link AuthenticationManager}.
 *
 * This configurator automatically register a {@link BCryptPasswordEncoder}.
 * If you store passwords in your database using another encoding algorithm
 * define your own {@link PasswordEncoder}
 *
 * <pre class="code">
    &#64;Configuration
    public class MyVaadinConnectConfiguration {
      &#64;Autowired
      private AccountRepository accountRepository;

      &#64;Bean
      public UserDetailsService userDetailsService() {
        return username -> this.accountRepository
          .findByUsername(username)
          .map(account -> User.builder()
            .username(account.getUsername())
            .password(account.getPassword())
            .roles("USER")
            .build())
          .orElseThrow(() -> new UsernameNotFoundException(username));
      }
    }

    &#64;Configuration
    public class MyVaadinConnectConfiguration {
      &#64;Bean
      AuthenticationManager AuthenticationManager() {
        return new AuthenticationManager() {
          &#64;Override
          public Authentication authenticate(Authentication auth)
              throws AuthenticationException {

            return new UsernamePasswordAuthenticationToken(
                auth.getName(), auth.getCredentials(), new ArrayList<>());
          }
        };
      }
    }
 * </pre>
 */
@Import({ VaadinConnectProperties.class })
@Component
public class VaadinConnectOAuthConfigurer
    extends AuthorizationServerConfigurerAdapter {

  private static final String DEFAULT_SIGNING_KEY = "JustAnySigningK3y";
  private static final String[] SCOPES = new String[] { "read", "write" };
  private static final String[] GRANT_TYPES =
      new String[] { "password", "refresh_token" };

  @Autowired
  private AuthenticationConfiguration authenticationConfiguration;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  private VaadinConnectProperties vaadinConnectProperties;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints)
      throws Exception {
    // This is required for 'password' grants, which is specified below
    endpoints.authenticationManager(authenticationManager())
        .tokenStore(tokenStore())
        .accessTokenConverter(accessTokenConverter());
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients)
      throws Exception {
    clients.inMemory()
        .withClient(vaadinConnectProperties.getVaadinConnectClientAppname())
        .secret(encoder
            .encode(vaadinConnectProperties.getVaadinConnectClientSecret()))
        .scopes(SCOPES)
        .authorizedGrantTypes(GRANT_TYPES);
  }

  /**
   * @return the JwtAccessTokenConverter
   */
  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(DEFAULT_SIGNING_KEY);
    return converter;
  }

  /**
   * @return the TokenStore
   */
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  private AuthenticationManager authenticationManager() throws Exception {
    try {
      return applicationContext.getBean(AuthenticationManager.class);
    } catch (NoSuchBeanDefinitionException e) {
      return authenticationConfiguration.getAuthenticationManager();
    }
  }

  @Configuration
  @ConditionalOnMissingBean(PasswordEncoder.class)
  protected static class PasswordEncoderConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
  }
}
