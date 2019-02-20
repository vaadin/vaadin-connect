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
package com.vaadin.connect.auth.server;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.vaadin.connect.VaadinConnectProperties;

/**
 * Class to configure the authentication of a vaadin-connect application.
 *
 * Configure oauth by annotating your app with the
 * {@link EnableVaadinConnectOAuthServer} and defining either a
 * {@link UserDetailsService} or a {@link AuthenticationManager} Bean.
 *
 * <pre class="code">
 * &#64;Configuration
 * public class MyApplicationConfiguration {
 *   &#64;Autowired
 *   private AccountRepository accountRepository;
 *
 *   &#64;Bean
 *   public UserDetailsService userDetailsService() {
 *     return username -&gt; this.accountRepository.findByUsername(username)
 *         .map(account -&gt; User.builder().username(account.getUsername())
 *             .password(account.getPassword()).roles("USER").build())
 *         .orElseThrow(() -&gt; new UsernameNotFoundException(username));
 *   }
 * }
 *
 * &#64;Configuration
 * public class MyApplicationConfiguration {
 *   &#64;Bean
 *   AuthenticationManager authenticationManager() {
 *     return new AuthenticationManager() {
 *       &#64;Override
 *       public Authentication authenticate(Authentication auth)
 *           throws AuthenticationException {
 *
 *         return new UsernamePasswordAuthenticationToken(auth.getName(),
 *             auth.getCredentials(), new ArrayList&lt;&gt;());
 *       }
 *     };
 *   }
 * }
 * </pre>
 *
 * This configurator automatically register a {@link BCryptPasswordEncoder}. If
 * you store passwords in your database using another encoding algorithm define
 * your own {@link PasswordEncoder} Bean.
 *
 * <pre class="code">
 * public class MyApplicationConfiguration {
 *   &#64;Bean
 *   public PasswordEncoder passwordEncoder() {
 *     // When using plain passwords stored in user database
 *     return NoOpPasswordEncoder.getInstance();
 *   }
 * }
 * </pre>
 */
@Configuration
@Import(VaadinConnectOAuthConfigurer.class)
public class VaadinConnectOAuthConfiguration
    extends AuthorizationServerConfigurerAdapter {
  private static final List<String> REQUIRED_CLAIMS = Arrays.asList("jti",
      "exp", "user_name", "authorities");
  private VaadinConnectProperties vaadinConnectProperties;

  /**
   * Default constructor.
   *
   * @param vaadinConnectProperties
   *        The Vaadin connect app configuration
   */
  public VaadinConnectOAuthConfiguration(
      VaadinConnectProperties vaadinConnectProperties) {
    this.vaadinConnectProperties = vaadinConnectProperties;
  }

  /**
   * Provide the {@link JwtAccessTokenConverter} Bean.
   *
   * @return the JwtAccessTokenConverter
   */
  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

    String sigKey = vaadinConnectProperties.getVaadinConnectTokenSigningKey();
    if (!sigKey.isEmpty()) {
      converter.setSigningKey(sigKey);
    }

    converter.setJwtClaimsSetVerifier(getJwtClaimsSetVerifier());
    return converter;
  }

  private JwtClaimsSetVerifier getJwtClaimsSetVerifier() {
    return claims -> {
      for (String requiredClaim : REQUIRED_CLAIMS) {
        if (claims.get(requiredClaim) == null) {
          throw new InvalidTokenException(
              "token does not contain the required claim: " + requiredClaim);
        }
      }
    };
  }

  /**
   * Provide the {@link TokenStore} Bean.
   *
   * @return the TokenStore
   */
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Configuration
  @ConditionalOnMissingBean(PasswordEncoder.class)
  protected static class PasswordEncoderConfiguration {

    /**
     * Provide the {@link PasswordEncoder} Bean.
     *
     * @return the PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
  }

  /**
   * Provide the {@link ResourceServerConfigurerAdapter} Bean allowing to
   * serve any end-point without authentication.
   *
   * It overrides {@link OAuth2ResourceServerConfiguration} which by default
   * requires authenticated requests for everything.
   *
   * User still can provide a customised
   * {@link ResourceServerConfigurerAdapter} Bean.
   *
   * @return ResourceServerConfigurerAdapter
   */
  @Bean
  @ConditionalOnMissingBean(ResourceServerConfigurer.class)
  public ResourceServerConfigurer resourceServer() {
    return new ResourceServerConfigurerAdapter() {
      @Override
      public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll();
      }
    };
  }
}
