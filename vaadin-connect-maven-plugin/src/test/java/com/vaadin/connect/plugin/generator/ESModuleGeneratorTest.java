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

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaadin.connect.plugin.TestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ESModuleGeneratorTest {
  @Rule
  public TemporaryFolder outputDirectory = new TemporaryFolder();

  @Test(expected = IllegalArgumentException.class)
  public void should_ThrowException_When_NoOpenApiInput() {
    VaadinConnectJsGenerator.launch(new File("whatever"),
        outputDirectory.getRoot());
  }

  @Test
  public void should_GenerateJSClass_When_ThereIsOpenApiInput()
      throws Exception {
    VaadinConnectJsGenerator.launch(
        getResourcePath("expected-openapi-custom-application-properties.json"),
        outputDirectory.getRoot());
    Path outputFilePath = Paths.get(outputDirectory.getRoot() + "/"
        + GeneratorTestClass.class.getSimpleName() + ".js");
    String actualJson = StringUtils.toEncodedString(
        Files.readAllBytes(outputFilePath), Charset.defaultCharset()).trim();
    String expectedJson = TestUtils.getExpectedJson(this.getClass(),
        "expected-GeneratorTestClass.js");
    Assert.assertEquals(expectedJson, actualJson);
  }

  @Test
  public void should_GenerateJSClass_When_ThereIsOpenApiInputAndNoTargetDirectory() {
    File nonExistingOutputDirectory = new File(outputDirectory.getRoot(),
        "whatever");
    assertFalse(nonExistingOutputDirectory.isDirectory());

    VaadinConnectJsGenerator.launch(
        getResourcePath("expected-openapi-custom-application-properties.json"),
        nonExistingOutputDirectory);
    assertTrue(nonExistingOutputDirectory.isDirectory());
    assertTrue(nonExistingOutputDirectory
        .listFiles((dir, name) -> name.endsWith(".js")).length > 0);
  }

  // The swagger codegen catches all the exceptions and rethrows with
  // RuntimeException
  @Test(expected = RuntimeException.class)
  public void should_ThrowException_When_PathHasTrailingSlash() {
    VaadinConnectJsGenerator.launch(
        getResourcePath("wrong-input-path-openapi.json"),
        outputDirectory.getRoot());
  }

  @Test(expected = RuntimeException.class)
  public void should_ThrowException_When_JsonHasGetOperation() {
    VaadinConnectJsGenerator.launch(
        getResourcePath("get-operation-openapi.json"),
        outputDirectory.getRoot());
  }

  @Test
  public void should_GenerateNoJsDoc_When_JsonHasNoJsDocOperation()
      throws Exception {
    VaadinConnectJsGenerator.launch(getResourcePath("no-jsdoc-operation.json"),
        outputDirectory.getRoot());

    Path outputPath = Paths
        .get(outputDirectory.getRoot() + "/GeneratorTestClass.js");
    String actual = StringUtils.toEncodedString(Files.readAllBytes(outputPath),
        Charset.defaultCharset()).trim();

    String expected = TestUtils.getExpectedJson(this.getClass(),
      "expected-no-jsdoc.js");

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void should_GeneratePartlyJsDoc_When_JsonHasParametersAndReturnType()
    throws Exception {
    VaadinConnectJsGenerator.launch(getResourcePath("parameters-and-return-jsdoc.json"),
      outputDirectory.getRoot());

    Path outputPath = Paths
      .get(outputDirectory.getRoot() + "/GeneratorTestClass.js");
    String actual = StringUtils.toEncodedString(Files.readAllBytes(outputPath),
      Charset.defaultCharset()).trim();

    String expected = TestUtils.getExpectedJson(this.getClass(),
      "expected-partly-jsdoc.js");

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void should_GenerateTwoClasses_When_OperationContainsTwoTags()
      throws Exception {
    VaadinConnectJsGenerator.launch(
        getResourcePath("multiple-tags-operation.json"),
        outputDirectory.getRoot());
    Path firstOutputFilePath = Paths
        .get(outputDirectory.getRoot() + "/MyFirstJsClass.js");
    Path secondOutputFilePath = Paths
        .get(outputDirectory.getRoot() + "/MySecondJsClass.js");
    String firstActualJson = StringUtils
        .toEncodedString(Files.readAllBytes(firstOutputFilePath),
            Charset.defaultCharset())
        .trim();
    String secondActualJson = StringUtils
        .toEncodedString(Files.readAllBytes(secondOutputFilePath),
            Charset.defaultCharset())
        .trim();
    String expectedFirstClass = TestUtils.getExpectedJson(this.getClass(),
        "expected-first-class-multiple-tags.js");
    String expectedSecondClass = TestUtils.getExpectedJson(this.getClass(),
        "expected-second-class-multiple-tags.js");
    Assert.assertEquals(expectedFirstClass, firstActualJson);
    Assert.assertEquals(expectedSecondClass, secondActualJson);
  }

  @Test
  public void should_GenerateDefaultClass_When_OperationHasNoTag()
      throws Exception {
    VaadinConnectJsGenerator.launch(getResourcePath("no-tag-operation.json"),
        outputDirectory.getRoot());
    Path outputFilePath = Paths.get(outputDirectory.getRoot() + "/Default.js");
    String actualJs = StringUtils.toEncodedString(
        Files.readAllBytes(outputFilePath), Charset.defaultCharset()).trim();
    String expectedFirstClass = TestUtils.getExpectedJson(this.getClass(),
        "expected-default-class-no-tag.js");
    Assert.assertEquals(expectedFirstClass, actualJs);
  }

  @Test
  public void should_RenderMultipleLinesHTMLCorrectly_When_JavaDocHasMultipleLines()
    throws Exception {
    VaadinConnectJsGenerator.launch(getResourcePath("multiplelines-description.json"),
      outputDirectory.getRoot());
    Path output = Paths.get(outputDirectory.getRoot() + "/GeneratorTestClass.js");
    String actualJs = StringUtils.toEncodedString(
      Files.readAllBytes(output), Charset.defaultCharset()).trim();
    String expectedJs = TestUtils.getExpectedJson(this.getClass(),
      "expected-multiple-lines-description.js");
    Assert.assertEquals(expectedJs, actualJs);
  }

  private File getResourcePath(String resourceName) {
    try {
      return new File(getClass().getResource(resourceName).toURI());
    } catch (URISyntaxException e) {
      throw new AssertionError(String
          .format("Failed to load resource with name '%s'", resourceName));
    }
  }
}
