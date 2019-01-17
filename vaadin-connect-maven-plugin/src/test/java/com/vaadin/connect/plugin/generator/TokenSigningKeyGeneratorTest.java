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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.vaadin.connect.plugin.generator.TokenSigningKeyGenerator.VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY;

/**
 * Token signing key generator test class
 */
public class TokenSigningKeyGeneratorTest {
  private PropertiesConfiguration propertiesConfiguration;
  private static final String APPLICATION_PROPERTIES = "application.properties";
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() {
    Path outputPath = temporaryFolder.getRoot().toPath()
        .resolve(APPLICATION_PROPERTIES);
    propertiesConfiguration = new PropertiesConfiguration();
    propertiesConfiguration.setFile(outputPath.toFile());
  }

  @Test
  public void should_GenerateRandomSigningKeyWithNoSpaceSeparator_When_ItDoesNotExist()
      throws ConfigurationException, IOException {
    mockProperties("my.important.property=my.value");
    TokenSigningKeyGenerator.generateTokenSigningKey(propertiesConfiguration);
    String generatedKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    Assert.assertTrue(StringUtils.isNotBlank(generatedKey));
    Assert.assertTrue(readApplicationProperties()
        .contains(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY + "=" + generatedKey));
  }

  @Test
  public void should_GenerateRandomSigningKey_When_ItIsBlank()
      throws ConfigurationException, IOException {
    mockProperties(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY + "=");
    TokenSigningKeyGenerator.generateTokenSigningKey(propertiesConfiguration);
    String generatedKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    Assert.assertTrue(StringUtils.isNotBlank(generatedKey));
  }

  @Test
  public void should_NotGenerateRandomSigningKey_When_ItIsDefined()
      throws ConfigurationException, IOException {
    mockProperties(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY + "=MyCus7omK3y");
    TokenSigningKeyGenerator.generateTokenSigningKey(propertiesConfiguration);
    String signingKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    Assert.assertEquals("MyCus7omK3y", signingKey);
  }

  @Test
  public void should_PreserveOtherPropertiesAndComments_When_TheyAreDefined()
      throws ConfigurationException, IOException {
    String oldProperties = "mockProperty1 = a\n" + "# my important comment.\n"
        + "my-important-property=myvalue";
    mockProperties(oldProperties);

    TokenSigningKeyGenerator.generateTokenSigningKey(propertiesConfiguration);

    String signingKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    Assert.assertTrue(StringUtils.isNotBlank(signingKey));
    Assert.assertTrue(readApplicationProperties().contains(oldProperties));
  }

  @Test
  public void should_PreserveOldCommentsOfSigningKeyProperty_When_PropertyValueIsEmpty()
      throws ConfigurationException, IOException {
    String importantComment = "# my important comment.\n";
    String blankDefinedProperty = VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY + "=";
    mockProperties(importantComment + blankDefinedProperty);

    TokenSigningKeyGenerator.generateTokenSigningKey(propertiesConfiguration);

    String signingKey = propertiesConfiguration
        .getString(VAADIN_CONNECT_AUTH_TOKEN_SIGNING_KEY);
    Assert.assertTrue(StringUtils.isNotBlank(signingKey));
    Assert.assertTrue(readApplicationProperties().contains(importantComment));
  }

  private void mockProperties(String content)
      throws IOException, ConfigurationException {
    try (InputStream inputStream = IOUtils.toInputStream(content)) {
      propertiesConfiguration.load(inputStream);
    }
    propertiesConfiguration.save();
  }

  private String readApplicationProperties() throws IOException {
    Path outputPath = temporaryFolder.getRoot().toPath()
        .resolve(APPLICATION_PROPERTIES);
    return StringUtils.toEncodedString(Files.readAllBytes(outputPath),
        Charset.defaultCharset()).trim();
  }
}
