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

package com.vaadin.connect.plugin.generator.collectionservice;

import org.junit.Test;

import com.vaadin.connect.plugin.generator.GenericOpenApiTest;

public class OpenApiTest {
  @Test
  public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName() {
    GenericOpenApiTest.verifyOpenApi(getClass().getPackage(),
        CollectionService.class);
  }
}