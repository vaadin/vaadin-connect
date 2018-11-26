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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaadin.connect.plugin.TestUtils;

public class VaadinConnectClientGeneratorTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path outputPath;

  @Before
  public void setUpOutputFile() {
    outputPath = Paths.get(temporaryFolder.getRoot().getAbsolutePath(),
        "connect-client.default.js");
  }

  @Test
  public void should_GenerateConnectClientDefault_When_NoApplicationPropertiesInput()
      throws Exception {
    VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
        new Properties());

    generator.generateVaadinConnectClientFile(outputPath);

    Assert.assertTrue(outputPath.toFile().exists());
    String actualJson = StringUtils.toEncodedString(
        Files.readAllBytes(outputPath), Charset.defaultCharset()).trim();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-connect-client-default.js");
    Assert.assertEquals(expectedJson, actualJson);
  }

  @Test
  public void should_GenerateConnectClientDefault_When_ApplicationPropertiesInput()
      throws Exception {
    VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
        TestUtils.readProperties(
            "src/test/resources/com/vaadin/connect/plugin/generator/application.properties.for.testing"));

    generator.generateVaadinConnectClientFile(outputPath);

    Assert.assertTrue(outputPath.toFile().exists());
    String actualJson = StringUtils.toEncodedString(
        Files.readAllBytes(outputPath), Charset.defaultCharset()).trim();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-connect-client-custom.js");
    Assert.assertEquals(expectedJson, actualJson);
  }
}
