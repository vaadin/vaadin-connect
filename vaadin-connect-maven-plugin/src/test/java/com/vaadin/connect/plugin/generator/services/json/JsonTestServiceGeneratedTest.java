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

package com.vaadin.connect.plugin.generator.services.json;

import java.io.File;
import java.util.Collections;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.Test;

import com.vaadin.connect.plugin.generator.OpenApiSpecGenerator;
import com.vaadin.connect.plugin.generator.VaadinConnectTsGenerator;
import com.vaadin.connect.plugin.generator.services.AbstractServiceGenerationTest;

import static com.vaadin.connect.plugin.VaadinClientGeneratorMojo.DEFAULT_GENERATED_CONNECT_CLIENT_IMPORT_PATH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonTestServiceGeneratedTest
    extends AbstractServiceGenerationTest {

  public JsonTestServiceGeneratedTest() {
    super(Collections.singletonList(JsonTestService.class));
  }

  @Test
  public void should_GenerateOpenApi_When_NoApplicationPropertiesInput() {
    String expectedImport = String.format("import client from '%s';",
        DEFAULT_GENERATED_CONNECT_CLIENT_IMPORT_PATH);
    verifyGenerationFully(null,
        getClass().getResource("expected-openapi.json"));

    getTsFiles(outputDirectory.getRoot()).stream().map(File::toPath)
        .map(this::readFile).forEach(
            fileContents -> assertTrue(fileContents.contains(expectedImport)));
  }

  @Test
  public void should_GenerateOpenApiWithCustomApplicationProperties_When_InputApplicationPropertiesGiven() {
    verifyGenerationFully(
        AbstractServiceGenerationTest.class
            .getResource("../application.properties.for.testing"),
        getClass().getResource(
            "expected-openapi-custom-application-properties.json"));
  }

  @Test
  public void should_GenerateJsClassWithCustomClientPath_When_CustomClientPathGiven() {
    String customConnectClientPath = "../my-connect-client.js";
    String expectedImport = String.format("import client from '%s';",
        customConnectClientPath);

    new OpenApiSpecGenerator(new PropertiesConfiguration()).generateOpenApiSpec(
        Collections
            .singletonList(java.nio.file.Paths.get("src/test/java", getClass()
                .getPackage().getName().replace('.', File.separatorChar))),
        openApiJsonOutput);

    VaadinConnectTsGenerator.launch(openApiJsonOutput.toFile(),
        outputDirectory.getRoot(), customConnectClientPath);

    getTsFiles(outputDirectory.getRoot()).stream().map(File::toPath)
        .map(this::readFile).forEach(
            fileContents -> assertTrue(fileContents.contains(expectedImport)));
  }

  @Test
  public void should_GenerateJsClass_When_ThereIsOpenApiInputAndNoTargetDirectory() {
    File nonExistingOutputDirectory = new File(outputDirectory.getRoot(),
        "whatever");
    assertFalse(nonExistingOutputDirectory.isDirectory());

    VaadinConnectTsGenerator.launch(new File(getClass()
        .getResource("expected-openapi-custom-application-properties.json")
        .getPath()), nonExistingOutputDirectory);
    assertTrue(nonExistingOutputDirectory.isDirectory());
    assertFalse(getTsFiles(nonExistingOutputDirectory).isEmpty());
  }

}
