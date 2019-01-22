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
package com.vaadin.connect.plugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.connect.plugin.generator.TokenSigningKeyGenerator;

/**
 * The mojo to generate signing for Vaadin Connect OAuth Server. The mojo checks
 * the signing key existence in
 * {@link VaadinConnectMojoBase#applicationProperties}. If the key is missing, a
 * new random key will be generated.
 */
@Mojo(name = "generate-token-signing-key", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TokenSigningKeyGeneratorMojo extends VaadinConnectMojoBase {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(TokenSigningKeyGeneratorMojo.class);

  @Override
  public void execute() {
    PropertiesConfiguration propertiesConfiguration = TokenSigningKeyGenerator
        .generateTokenSigningKey(readApplicationProperties());
    writeProperties(propertiesConfiguration);

  }

  private void writeProperties(
      PropertiesConfiguration propertiesConfiguration) {
    try {
      BufferedWriter bufferedWriter = Files.newBufferedWriter(
          applicationProperties.toPath(), StandardCharsets.UTF_8);
      propertiesConfiguration.write(bufferedWriter);
      LOGGER.info("Generating token-signing-key and saving into {}.",
          applicationProperties.getAbsolutePath());
    } catch (ConfigurationException | IOException e) {
      String message = String.format(
          "Can't generate token signing key to properties file '%s'."
              + "A random key will be used for the Authentication server every time the application starts.",
          applicationProperties.getAbsolutePath());
      LOGGER.error(message, e);
    }
  }
}
