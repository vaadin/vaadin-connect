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
package com.vaadin.connect.plugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.vaadin.connect.plugin.generator.VaadinConnectJsGenerator;

/**
 * ES modules generator for vaadin-connect.
 *
 * Uses the {@link VaadinConnectMojoBase#openApiJsonFile} file to generate the
 * open api v3 specification of the Vaadin Connect modules in
 * {@link VaadinConnectMojoBase#generatedFrontendDirectory} directory,
 * owerwriting the target files and creating the target directory, if needed.
 */
@Mojo(name = "generate-connect-modules", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class EsModuleGeneratorMojo extends VaadinConnectMojoBase {

  @Override
  public void execute() {
    VaadinConnectJsGenerator.launch(openApiJsonFile,
        generatedFrontendDirectory);
  }
}
