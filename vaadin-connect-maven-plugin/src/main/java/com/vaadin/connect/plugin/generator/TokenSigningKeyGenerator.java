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
package com.vaadin.connect.plugin.generator;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class generates token-signing-key and writes it back to the application
 * properties file.
 */
public class TokenSigningKeyGenerator {

  private static final int TOKEN_SIGNING_KEY_LENGTH = 6;
  static final String VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY = "vaadin.connect.auth.token-signing-key";

  private TokenSigningKeyGenerator() {
    // no implementation
  }

  /**
   * Generate a random token signing key and write it back to the giving
   * properties configuration object if the key doesn't exist.
   * 
   * @param propertiesConfiguration
   *          the properties configuration object which contains information of
   *          the application.properties
   * @return the properties object with token-signing-key
   */
  public static PropertiesConfiguration generateTokenSigningKey(
      PropertiesConfiguration propertiesConfiguration) {
    String tokenSigningKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    if (StringUtils.isNotBlank(tokenSigningKey)) {
      return propertiesConfiguration;
    }
    String oldComment = StringUtils.defaultIfBlank(propertiesConfiguration
        .getLayout().getComment(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY), "");
    String comment = StringUtils.appendIfMissing(oldComment,
        "\nThe token signing key is generated automatically by vaadin-connect-maven-plugin."
            + "\nIt's highly recommended to use your own key in production.");
    propertiesConfiguration.getLayout()
        .setComment(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY, comment);
    propertiesConfiguration.getLayout()
        .setSeparator(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY, "=");

    String randomTokenSigningKey = RandomStringUtils
        .randomAlphanumeric(TOKEN_SIGNING_KEY_LENGTH);
    propertiesConfiguration.setProperty(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY,
        randomTokenSigningKey);
    return propertiesConfiguration;
  }
}
