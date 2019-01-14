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
package com.vaadin.connect.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class VaadinFrontendServerIT {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  public void should_Return200_When_RequestFooBarPath() throws Exception {
    mockMvc.perform(get("/foobar"))
        .andExpect(MockMvcResultMatchers.status().is(200));
  }

  @Test
  public void should_Return200_When_RequestRootPath() throws Exception {
    mockMvc.perform(get("/")).andExpect(MockMvcResultMatchers.status().is(200));
  }

  @Test
  public void should_Return200_When_RequestAJsFile() throws Exception {
    mockMvc.perform(get("/index.js"))
        .andExpect(MockMvcResultMatchers.status().is(200));
  }

  @Test
  public void should_Return404_When_RequestFooJS() throws Exception {
    mockMvc.perform(get("/foo.js"))
        .andExpect(MockMvcResultMatchers.status().is(404));
  }
}
