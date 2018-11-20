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
package com.vaadin.connect.generator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class DefaultClientJsGeneratorTest {

  @Test
  public void should_GenerateConnectClientDefault_When_NoApplicationPropertiesInput() {
    String output = "target/generated-resources/connect-client.default.js";
    DefaultClientJsGenerator.main(new String[] { "output=" + output });
    Path outputPath = Paths.get(output);
    Assert.assertTrue(outputPath.toFile().exists());
    try {
      String actualJson = StringUtils.toEncodedString(
          Files.readAllBytes(outputPath), Charset.defaultCharset()).trim();
      String expectedJson = OpenApiJavaParserImplTest
          .getExpectedJson("expected-connect-client-default.js");
      Assert.assertEquals(expectedJson, actualJson);
    } catch (IOException e) {
      Assert.fail();
    }
  }

  @Test
  public void should_GenerateConnectClientDefault_When_ApplicationPropertiesInput() {
    String output = "target/generated-resources/connect-client.default.js";
    DefaultClientJsGenerator.main(new String[] { "output=" + output,
        "applicationProperties=src/test/resources/com/vaadin/connect/generator/application.properties.for.testing" });
    Path outputPath = Paths.get(output);
    Assert.assertTrue(outputPath.toFile().exists());
    try {
      String actualJson = StringUtils.toEncodedString(
          Files.readAllBytes(outputPath), Charset.defaultCharset()).trim();
      String expectedJson = OpenApiJavaParserImplTest
          .getExpectedJson("expected-connect-client-custom.js");
      Assert.assertEquals(expectedJson, actualJson);
    } catch (IOException e) {
      Assert.fail();
    }
  }
}
