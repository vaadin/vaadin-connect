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
package com.vaadin.connect.plugin.generator;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.connect.VaadinService;

/**
 * This class is used for OpenApi generator test
 */
@VaadinService
public class GeneratorTestClass {
  /**
   * Get all users
   *
   * @return list of users
   */
  public List<User> getAllUsers() {
    return Collections.EMPTY_LIST;
  }

  /**
   * Get the map of user and roles
   *
   * @return map of user and roles
   */
  public Map<String, User> getAllUserRolesMap() {
    return Collections.EMPTY_MAP;
  }

  /**
   * Update a user
   *
   * @param user
   *          User to be updated
   */
  public void updateUser(User user) {
    // NO implementation
  }

  /**
   * Get number of users
   *
   * @return number of user
   */
  public int countUser() {
    return 0;
  }

  /**
   * Get user by id
   *
   * @param id
   *          id of user
   * @return user with given id
   */
  public User getUserById(int id) {
    return null;
  }

  /**
   * Get array int
   *
   * @param input
   *          input string array
   * @return array of int
   */
  public int[] getArrayInt(String[] input) {
    return new int[] { 1, 2 };
  }

  /**
   * Get boolean value
   *
   * @param input
   *          input map
   * @return boolean value
   */
  public boolean getBooleanValue(Map<String, User> input) {
    return false;
  }

  /**
   * Two parameters input method
   *
   * @param input
   *          first input description
   * @param secondInput
   *          second input description
   * @return boolean value
   */
  public boolean getTwoParameters(String input, int secondInput) {
    return false;
  }

  /**
   * Get instant nano
   * @param input input parameter
   * @return current time as an Instant
   */
  public java.time.Instant fullFQNMethod(java.lang.Integer input) {
    return Instant.now();
  }

  protected void hiddenMethod() {
    // No implementation
  }

  public static class User {
    private String name;
    private String password;
    private transient int hiddenField;
    private Map<String, Role> roles;
  }

  /**
   * Role bean
   */
  public static class Role {
    private String roleName;
  }
}
