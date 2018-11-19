package com.vaadin.connect.oauth;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.stereotype.Component;

import com.vaadin.connect.VaadinConnectProperties;

@Component
public class VaadinConnectWebSecurityConfigurer
    extends ResourceServerConfigurerAdapter {
  private final VaadinConnectProperties connectProperties;

  public VaadinConnectWebSecurityConfigurer(
      VaadinConnectProperties connectProperties) {
    this.connectProperties = connectProperties;
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    String connectEndpoint = connectProperties.getVaadinConnectEndpoint();
    if (!connectEndpoint.startsWith("/")) {
      connectEndpoint = '/' + connectEndpoint;
    }
    if (!connectEndpoint.endsWith("/")) {
      connectEndpoint = connectEndpoint + '/';
    }
    connectEndpoint = connectEndpoint + "**";

    http.authorizeRequests().antMatchers(connectEndpoint).permitAll()
        .anyRequest().authenticated();
  }
}
