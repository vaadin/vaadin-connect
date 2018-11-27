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

public class ESModuleGeneratorTest {

  @Test(expected = IllegalArgumentException.class)
  public void should_ThrowException_When_NoOpenApiInput() {
    ESModuleGenerator.main(new String[] {});
  }

  @Test
  public void should_GenerateJSClass_When_ThereIsOpenApiInput()
      throws IOException {
    String outputPath = "target/generated-resources/js";
    ESModuleGenerator.main(new String[] {
        "input=src/test/resources/com/vaadin/connect/generator/expected-openapi-custom-application-properties.json",
        "output=" + outputPath });
    Path outputFilePath = Paths.get(
        outputPath + "/" + GeneratorTestClass.class.getSimpleName() + ".js");
    String actualJson = StringUtils.toEncodedString(
        Files.readAllBytes(outputFilePath), Charset.defaultCharset()).trim();
    String expectedJson = OpenApiJavaParserImplTest
        .getExpectedJson("expected-GeneratorTestClass.js");
    Assert.assertEquals(expectedJson, actualJson);
  }

  // The swagger codegen catches all the exceptions and rethrows with
  // RuntimeException
  @Test(expected = RuntimeException.class)
  public void should_ThrowException_When_PathHasTrailingSlash() {
    String outputPath = "target/generated-resources/js";
    ESModuleGenerator.main(new String[] {
        "input=src/test/resources/com/vaadin/connect/generator/wrong-input-path-openapi.json",
        "output=" + outputPath });
  }

  @Test(expected = RuntimeException.class)
  public void should_ThrowException_When_JsonHasGetOperation() {
    String outputPath = "target/generated-resources/js";
    ESModuleGenerator.main(new String[] {
        "input=src/test/resources/com/vaadin/connect/generator/get-operation-openapi.json",
        "output=" + outputPath });
  }

  @Test
  public void should_GenerateTwoClasses_When_OperationContainsTwoTags()
      throws IOException {
    String outputPath = "target/generated-resources/js";
    ESModuleGenerator.main(new String[] {
        "input=src/test/resources/com/vaadin/connect/generator/multiple-tags-operation.json",
        "output=" + outputPath });
    Path firstOutputFilePath = Paths.get(outputPath + "/MyFirstJsClass.js");
    Path secondOutputFilePath = Paths.get(outputPath + "/MySecondJsClass.js");
    String firstActualJson = StringUtils
        .toEncodedString(Files.readAllBytes(firstOutputFilePath),
            Charset.defaultCharset())
        .trim();
    String secondActualJson = StringUtils
        .toEncodedString(Files.readAllBytes(secondOutputFilePath),
            Charset.defaultCharset())
        .trim();
    String expectedFirstClass = OpenApiJavaParserImplTest
        .getExpectedJson("expected-first-class-multiple-tags.js");
    String expectedSecondClass = OpenApiJavaParserImplTest
        .getExpectedJson("expected-second-class-multiple-tags.js");
    Assert.assertEquals(expectedFirstClass, firstActualJson);
    Assert.assertEquals(expectedSecondClass, secondActualJson);
  }

  @Test
  public void should_GenerateDefaultClass_When_OperationHasNoTag()
      throws IOException {
    String outputPath = "target/generated-resources/js";
    ESModuleGenerator.main(new String[] {
        "input=src/test/resources/com/vaadin/connect/generator/no-tag-operation.json",
        "output=" + outputPath });
    Path outputFilePath = Paths.get(outputPath + "/Default.js");
    String actualJs = StringUtils
        .toEncodedString(Files.readAllBytes(outputFilePath),
            Charset.defaultCharset())
        .trim();
    String expectedFirstClass = OpenApiJavaParserImplTest
        .getExpectedJson("expected-default-class-no-tag.js");
    Assert.assertEquals(expectedFirstClass, actualJs);
  }
}
