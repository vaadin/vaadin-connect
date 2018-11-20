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

public class GeneratorTest {

  @Test
  public void should_GenerateOpenApi_When_WithAllDefaultPaths() {
    Generator.main(new String[] {});
    Assert.assertTrue(
        Paths.get("target/generated-resources/openapi.json").toFile().exists());
  }

  @Test
  public void should_GenerateOpenApi_When_NoApplicationPropertiesInput() {
    Generator.main(new String[] { "input=src/test/java",
        "output=target/generated-resources/openapi1.json" });
    Assert.assertTrue(Paths.get("target/generated-resources/openapi1.json")
        .toFile().exists());
  }

  @Test
  public void should_GenerateOpenApiWithCustomApplicationProperties_When_InputApplicationPropertiesGiven()
      throws IOException {
    Generator.main(new String[] { "input=src/test/java",
        "output=target/generated-resources/openapi-custom-properties.json",
        "applicationProperties=src/test/resources/com/vaadin/connect/generator/application.properties"
            + ".for.testing" });
    Path resultPath = Paths
        .get("target/generated-resources/openapi-custom-properties.json");
    Assert.assertTrue(resultPath.toFile().exists());
    String actualJson = StringUtils.toEncodedString(
        Files.readAllBytes(resultPath), Charset.defaultCharset());
    String expectedJson = OpenApiJavaParserImplTest
        .getExpectedJson("expected-openapi-custom-application-properties.json");
    Assert.assertEquals(expectedJson, actualJson);

  }

}
