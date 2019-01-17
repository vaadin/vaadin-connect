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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates token-signing-key and writes it back to the application
 * properties file.
 */
public class TokenSigningKeyGenerator {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(TokenSigningKeyGenerator.class);
  private static final int TOKEN_SIGNING_KEY_LENGTH = 6;
  static final String VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY = "vaadin.connect.auth.token-signing-key";

  /**
   * Generate a random token signing key and write it back to the giving
   * properties configuration object if the key doesn't exist.
   * 
   * @param propertiesConfiguration
   *          the properties configuration object which contains information of
   *          the application.properties
   */
  public static void generateTokenSigningKey(
      PropertiesConfiguration propertiesConfiguration) {
    String tokenSigningKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    if (StringUtils.isNotBlank(tokenSigningKey)) {
      return;
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

    writeProperties(propertiesConfiguration);
  }

  private static void writeProperties(
      PropertiesConfiguration propertiesConfiguration) {
    try {
      LOGGER.info("Generating token-signing-key and saving into {}.",
          propertiesConfiguration.getFile().getAbsolutePath());
      propertiesConfiguration.save();
    } catch (ConfigurationException e) {
      String message = String.format(
          "Can't generate token signing key to properties file   %s."
              + "A random key will be used for the Authentication server every time the application starts.",
          propertiesConfiguration.getFile().getAbsolutePath());
      LOGGER.error(message, e);
    }
  }
}
