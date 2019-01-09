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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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
@Mojo(name = "generate-openapi-spec", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class OpenApiSpecGeneratorMojo extends VaadinConnectMojoBase {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() {
    try {
      List<Path> sourcesPaths = project.getCompileSourceRoots().stream()
          .map(Paths::get).collect(Collectors.toList());
      URL[] urlsForClassLoader = getUrls();
      try (
          URLClassLoader classLoader = new URLClassLoader(urlsForClassLoader)) {
        new OpenApiSpecGenerator(readApplicationProperties())
            .generateOpenApiSpec(sourcesPaths, classLoader,
                openApiJsonFile.toPath());
      }
    } catch (DependencyResolutionRequiredException e) {
      throw new IllegalStateException(
          "All dependencies need to be resolved before running the OpenAPI spec generator. Please resolve the dependencies and try again.",
          e);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "I/O error happens when closing project's URLClassLoader after generating OpenAPI spec.",
          e);
    }
  }

  private URL[] getUrls() throws DependencyResolutionRequiredException {
    List<URL> pathUrls = new ArrayList<>();
    try {
      for (String mavenCompilePath : project.getCompileClasspathElements()) {
        pathUrls.add(new File(mavenCompilePath).toURI().toURL());
      }
      return pathUrls.toArray(new URL[pathUrls.size()]);
    } catch (MalformedURLException e) {
      throw new IllegalStateException(
          "Can't create URLs from project class paths for generating OpenAPI spec.",
          e);
    }
  }
}
