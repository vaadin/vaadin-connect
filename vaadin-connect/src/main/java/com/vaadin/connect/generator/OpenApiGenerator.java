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

import java.nio.file.Path;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * OpenApi generator interface.
 */
public interface OpenApiGenerator {
  /**
   * Set source path of the project to generate OpenApi spec from.
   *
   * @param sourcePath
   *         source path of the project
   */
  void setSourcePath(Path sourcePath);

  /**
   * Set basic information for the OpenApi spec file.
   *
   * @param configuration
   *         open api basic configuration
   */
  void setOpenApiConfiguration(OpenApiConfiguration configuration);

  /**
   * Get the generated OpenApi spec.
   *
   * @return the generated OpenApi spec
   */
  OpenAPI getOpenApi();

  /**
   * Force regenerate OpenApi spec
   *
   * @return the generated OpenApi spec
   */
  OpenAPI generateOpenApi();
}
