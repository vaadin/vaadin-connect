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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;

/**
 * ES modules generator for vaadin-connect.
 */
public class ESModuleGenerator {

  private static final String DEFAULT_OUTPUT_PATH = "frontend/src/generated/";

  /**
   * This main method will take arguments:
   * <ul>
   * <li><code>input="OpenApi.json file"</code><br>
   * </li>
   * <li><code>output="Output path of the generated js files"</code><br>
   * Default value is {@link ESModuleGenerator#DEFAULT_OUTPUT_PATH}.</li>
   * </ul>
   *
   * <pre>
   * Example:
   * <code>java -cp vaadin-connect.jar com.vaadin.connect.generator.ESModuleGenerator
   *     input=/home/user/output/openapi.json
   *     ouput=/home/user/myapp/frontend/src/generated/</code>
   *
   * </pre>
   *
   * @param args
   *          program arguments
   */
  public static void main(String[] args) {
    CodegenConfigurator configurator = new CodegenConfigurator();

    configurator.setLang(VaadinConnectJsGenerator.GENERATOR_NAME);

    Path inputPath = getInputPath(args);
    configurator.setInputSpecURL(inputPath.toUri().toString());

    Path outputPath = getOutputPath(args);
    configurator.setOutputDir(outputPath.toString());
    new VaadinConnectJSOnlyGenerator().opts(configurator.toClientOptInput())
        .generate();
  }

  private static Path getOutputPath(String[] args) {
    String input = Generator.getArgument(args, "output", DEFAULT_OUTPUT_PATH);
    return Paths.get(input).toAbsolutePath();
  }

  private static Path getInputPath(String[] args) {
    String input = Generator.getArgument(args, "input", null);
    if (input == null) {
      throw new IllegalArgumentException(
          "OpenApi json file is required for the generator.");
    }
    return Paths.get(input).toAbsolutePath();
  }

  private static class VaadinConnectJSOnlyGenerator extends DefaultGenerator {
    @Override
    public File writeToFile(String filename, String contents)
        throws IOException {
      // Only write js files
      if (filename.endsWith("js")) {
        return super.writeToFile(filename, contents);
      }
      return null;
    }
  }
}
