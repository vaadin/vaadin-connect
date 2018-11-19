package com.vaadin.connect.generator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
