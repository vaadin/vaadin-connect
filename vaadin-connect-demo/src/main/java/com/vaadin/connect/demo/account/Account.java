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
package com.vaadin.connect.demo.account;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Account {
  @Id
  @GeneratedValue
  private Long id;

  @NotEmpty(message = "Each account must have a non-empty username")
  private String username;

  @JsonIgnore
  private String password;


  public Account() {
  }

  /**
   * @param username
   * @param password
   */
  public Account(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * @return this account unique id
   */
  public Long getId() {
    return id;
  }

  /**
   * @return user name of this account
   */
  public String getUsername() {
    return username;
  }

  /**
   * @return password of this account
   */
  public String getPassword() {
    return password;
  }

}
