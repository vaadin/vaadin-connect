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

package com.vaadin.connect.plugin;

import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.vaadin.connect.plugin.generator.OpenApiSpecGenerator;

/**
 * Generates the OpenAPI specification file based on the application contents.
 * Uses the {@link VaadinConnectMojoBase#applicationProperties} to read the data
 * needed for the generation and generates the file into the *
 * {@link VaadinConnectMojoBase#openApiJsonFile} path.
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
 *      specification</a>
 */
@Mojo(name = "generate-openapi-spec", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class OpenApiSpecGeneratorMojo extends VaadinConnectMojoBase {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() {
    new OpenApiSpecGenerator(readApplicationProperties()).generateOpenApiSpec(
        project.getCompileSourceRoots().stream().map(Paths::get)
            .collect(Collectors.toList()),
        openApiJsonFile.toPath());
  }
}
