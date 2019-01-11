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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.vaadin.connect.plugin.TestUtils;
import com.vaadin.connect.plugin.generator.service.GeneratorTestClass;

import static com.vaadin.connect.plugin.VaadinClientGeneratorMojo.DEFAULT_GENERATED_CONNECT_CLIENT_IMPORT_PATH;
import static com.vaadin.connect.plugin.VaadinClientGeneratorMojo.DEFAULT_GENERATED_CONNECT_CLIENT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ESModuleGeneratorTest {

  @BeforeClass
  public static void beforeClass() {
    ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
        .setLevel(Level.WARN);
  }

  @Rule
  public TemporaryFolder outputDirectory = new TemporaryFolder();

  @Test(expected = IllegalStateException.class)
  public void should_ThrowException_When_NoOpenApiInput() {
    VaadinConnectJsGenerator.launch(new File("whatever"),
        outputDirectory.getRoot());
  }

  @Test
  public void should_GenerateJSClass_When_ThereIsOpenApiInput() {
    VaadinConnectJsGenerator.launch(
        getResourcePath("expected-openapi-custom-application-properties.json"),
        outputDirectory.getRoot());

    List<String> expectedClasses = Arrays.asList(
        GeneratorTestClass.class.getSimpleName(),
        GeneratorTestClass.GeneratorAnonymousAllowedTestClass.class
            .getSimpleName());
    List<String> foundFiles = Stream.of(outputDirectory.getRoot().list())
        .filter(fileName -> fileName.endsWith(".js"))
        .collect(Collectors.toList());
    assertEquals(String.format(
        "Expected to have only %s classes processed in the test: '%s', but found the following files: '%s'",
        expectedClasses.size(), expectedClasses, foundFiles),
        expectedClasses.size(), foundFiles.size());

    expectedClasses.forEach(this::assertClassGeneratedJs);
  }

  @Test
  public void should_UseDefaultConnectClientPath_When_ItIsNotDefined()
      throws IOException {
    String expectedImport = String.format("import client from '%s';",
        DEFAULT_GENERATED_CONNECT_CLIENT_IMPORT_PATH);

    VaadinConnectJsGenerator.launch(
        getResourcePath("expected-openapi-custom-application-properties.json"),
        outputDirectory.getRoot());

    String trim = readFileInTempDir("GeneratorTestClass.js");

    assertTrue(trim.contains(expectedImport));
  }

  @Test
  public void should_UseCustomConnectClientPath_When_ItIsDefined()
      throws IOException {
    String customConnectClientPath = "../my-connect-client.js";
    String expectedImport = String.format("import client from '%s';",
        customConnectClientPath);

    VaadinConnectJsGenerator.launch(
        getResourcePath("expected-openapi-custom-application-properties.json"),
        outputDirectory.getRoot(), customConnectClientPath);

    String trim = readFileInTempDir("GeneratorTestClass.js");

    assertTrue(trim.contains(expectedImport));
  }

  @Test
  public void should_RemoveStaleGeneratedFiles_When_OpenAPIInputChanges() {
    Path defaultConnectClient = Paths.get(
        outputDirectory.getRoot().getAbsolutePath(),
        DEFAULT_GENERATED_CONNECT_CLIENT_NAME);
    VaadinConnectClientGenerator vaadinConnectClientGenerator = new VaadinConnectClientGenerator(
        new Properties());
    // First generating round
    vaadinConnectClientGenerator
        .generateVaadinConnectClientFile(defaultConnectClient);
    VaadinConnectJsGenerator.launch(
        getResourcePath("esmodule-generator-TwoServicesThreeMethods.json"),
        outputDirectory.getRoot());
    assertEquals(
        "Expect to have 2 generated JS files and a connect-client.default.js",
        3, outputDirectory.getRoot().list().length);
    // Second generating round
    VaadinConnectJsGenerator.launch(
        getResourcePath("esmodule-generator-OneServiceOneMethod.json"),
        outputDirectory.getRoot());
    assertEquals(
        "Expected to have 1 generated JS files and a connect-client.default.js",
        2, outputDirectory.getRoot().list().length);

    this.assertClassGeneratedJs("FooBarService");
  }

  private void assertClassGeneratedJs(String expectedClass) {
    Path outputFilePath = outputDirectory.getRoot().toPath()
        .resolve(expectedClass + ".js");
    String actualJs;
    try {
      actualJs = StringUtils.toEncodedString(Files.readAllBytes(outputFilePath),
          Charset.defaultCharset()).trim();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    String expectedJs = TestUtils.getExpectedJson(this.getClass(),
        String.format("expected-%s.js", expectedClass));

    Assert.assertEquals(
        String.format("Class '%s' has unexpected json produced", expectedClass),
        expectedJs, actualJs);
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

    String actual = readFileInTempDir("GeneratorTestClass.js");

    String expected = TestUtils.getExpectedJson(this.getClass(),
        "expected-no-jsdoc.js");
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void should_GeneratePartlyJsDoc_When_JsonHasParametersAndReturnType()
      throws Exception {
    VaadinConnectJsGenerator.launch(
        getResourcePath("parameters-and-return-jsdoc.json"),
        outputDirectory.getRoot());

    String actual = readFileInTempDir("GeneratorTestClass.js");

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
    Path firstOutputFilePath = outputDirectory.getRoot().toPath()
        .resolve("MyFirstJsClass.js");
    Path secondOutputFilePath = outputDirectory.getRoot().toPath()
        .resolve("MySecondJsClass.js");
    String firstActualJs = StringUtils
        .toEncodedString(Files.readAllBytes(firstOutputFilePath),
            Charset.defaultCharset())
        .trim();
    String secondActualJs = StringUtils
        .toEncodedString(Files.readAllBytes(secondOutputFilePath),
            Charset.defaultCharset())
        .trim();
    String expectedFirstClass = TestUtils.getExpectedJson(this.getClass(),
        "expected-first-class-multiple-tags.js");
    String expectedSecondClass = TestUtils.getExpectedJson(this.getClass(),
        "expected-second-class-multiple-tags.js");
    Assert.assertEquals(expectedFirstClass, firstActualJs);
    Assert.assertEquals(expectedSecondClass, secondActualJs);
  }

  @Test
  public void should_GenerateDefaultClass_When_OperationHasNoTag()
      throws Exception {
    VaadinConnectJsGenerator.launch(getResourcePath("no-tag-operation.json"),
        outputDirectory.getRoot());
    String actualJs = readFileInTempDir("Default.js");
    String expectedFirstClass = TestUtils.getExpectedJson(this.getClass(),
        "expected-default-class-no-tag.js");
    Assert.assertEquals(expectedFirstClass, actualJs);
  }

  @Test
  public void should_RenderMultipleLinesHTMLCorrectly_When_JavaDocHasMultipleLines()
      throws Exception {
    VaadinConnectJsGenerator.launch(
        getResourcePath("multiplelines-description.json"),
        outputDirectory.getRoot());
    String actualJs = readFileInTempDir("GeneratorTestClass.js");
    String expectedJs = TestUtils.getExpectedJson(this.getClass(),
        "expected-multiple-lines-description.js");
    Assert.assertEquals(expectedJs, actualJs);
  }

  @Test
  public void should_escapeParameter_When_ParameterNameUsesReservedWord()
      throws Exception {
    VaadinConnectJsGenerator.launch(getResourcePath("reserved-words.json"),
        outputDirectory.getRoot());

    String actual = readFileInTempDir("GeneratorTestClass.js");

    String expected = TestUtils.getExpectedJson(this.getClass(),
        "expected-reserved-words.js");
    Assert.assertEquals(expected, actual);
  }

  @Test(expected = IllegalStateException.class)
  public void should_ThrowError_WhenOpenAPIHasNoDescriptionInResponse() {
    VaadinConnectJsGenerator.launch(
        getResourcePath("no-description-response-openapi.json"),
        outputDirectory.getRoot());
  }

  @Test(expected = IllegalStateException.class)
  public void should_ThrowError_WhenOpenAPIHasInvalidTypeReference() {
    VaadinConnectJsGenerator.launch(
        getResourcePath("invalid-schema-type-openapi.json"),
        outputDirectory.getRoot());
  }

  private String readFileInTempDir(String fileName) throws IOException {
    Path outputPath = outputDirectory.getRoot().toPath().resolve(fileName);
    return StringUtils.toEncodedString(Files.readAllBytes(outputPath),
        Charset.defaultCharset()).trim();
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
