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
package com.vaadin.connect.demo;

import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.connect.demo.account.Account;
import com.vaadin.connect.demo.account.AccountRepository;

/**
 * Class to configure the authentication of a vaadin-connect application
 */
@Configuration
public class DemoVaadinOAuthConfiguration {

  static final String TEST_LOGIN = "test_login";
  static final String TEST_PASSWORD = "test_password";

  @Bean
  public UserDetailsService userDetailsService(AccountRepository accountRepository) {
    return username -> accountRepository.findByUsername(username)
        .map(account -> User.builder().username(account.getUsername())
            .password(account.getPassword()).roles("USER").build())
        .orElseThrow(() -> new UsernameNotFoundException(username));
  }

  @Bean
  CommandLineRunner init(
      AccountRepository accountRepository,
      PasswordEncoder encoder) {
    return args -> {
      Stream.of("manolo", "viktor", "kirill", "anton", "tien")
          .forEach(username -> accountRepository
              .save(new Account(username, encoder.encode("abc123"))));

      accountRepository
          .save(new Account(TEST_LOGIN, encoder.encode(TEST_PASSWORD)));
    };
  }
}
