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

package com.vaadin.connect.plugin.generator.json;

import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaadin.connect.plugin.generator.GenericOpenApiTest;

public class OpenApiTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path outputPath;

  @Before
  public void setUpOutputFile() {
    outputPath = java.nio.file.Paths
        .get(temporaryFolder.getRoot().getAbsolutePath(), "openapi.json");
  }

  @Test
  public void should_GenerateOpenApi_When_NoApplicationPropertiesInput() {
    GenericOpenApiTest.verifyOpenApi(getClass().getPackage(), JsonTestService.class);
    GenericOpenApiTest.verifyJson(getClass().getPackage(), outputPath, null,
        getClass().getResource("expected-openapi.json"));
  }

  @Test
  public void should_GenerateOpenApiWithCustomApplicationProperties_When_InputApplicationPropertiesGiven() {
    GenericOpenApiTest.verifyOpenApi(getClass().getPackage(), JsonTestService.class);
    GenericOpenApiTest.verifyJson(getClass().getPackage(), outputPath,
            getClass().getResource("application.properties.for.testing"),
        getClass().getResource(
            "expected-openapi-custom-application-properties.json"));
  }
}
