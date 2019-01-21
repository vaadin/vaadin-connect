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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaadin.connect.plugin.TestUtils;

public class OpenApiSpecGeneratorTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path outputPath;

  @Before
  public void setUpOutputFile() {
    outputPath = Paths.get(temporaryFolder.getRoot().getAbsolutePath(),
        "openapi.json");
  }

  @Test
  public void should_GenerateOpenApi_When_NoApplicationPropertiesInput() {
    new OpenApiSpecGenerator(new PropertiesConfiguration()).generateOpenApiSpec(
        Collections.singletonList(Paths
            .get("src/test/java/com/vaadin/connect/plugin/generator/service")),
        outputPath);

    Assert.assertTrue(outputPath.toFile().exists());
  }

  @Test
  public void should_GenerateOpenApiWithCustomApplicationProperties_When_InputApplicationPropertiesGiven()
      throws Exception {
    new OpenApiSpecGenerator(TestUtils.readProperties(
        "src/test/resources/com/vaadin/connect/plugin/generator/application.properties.for.testing"))
            .generateOpenApiSpec(Collections.singletonList(Paths.get(
                "src/test/java/com/vaadin/connect/plugin/generator/service")),
                outputPath);

    Assert.assertTrue(outputPath.toFile().exists());

    String actualJson = StringUtils.toEncodedString(
        Files.readAllBytes(outputPath), Charset.defaultCharset());
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-openapi-custom-application-properties.json");

    Assert.assertEquals(expectedJson, actualJson);
  }

}
