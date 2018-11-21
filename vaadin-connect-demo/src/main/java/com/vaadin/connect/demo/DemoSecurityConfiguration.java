package com.vaadin.connect.demo;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Configures spring security, allowing access to static resources.
 */
@EnableWebSecurity
@Configuration
public class DemoSecurityConfiguration extends
WebSecurityConfigurerAdapter {
  /**
   * Allows access to static resources, bypassing Spring security.
   */
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers(
      // the index page
      "/",
      "/index.html",

      // the standard favicon URI
      "/favicon.ico",

      // the robot exclusion document
      "/robots.txt",

      // web app manifest
      "/manifest.json",

      // the frontend scripts
      "/frontend-es6/**",
      "/frontend-es5/**"
    );
  }
}
