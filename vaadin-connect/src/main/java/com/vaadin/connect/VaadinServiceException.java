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

package com.vaadin.connect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VaadinServiceException extends RuntimeException {
  private final Object detail;

  public VaadinServiceException(String message) {
    super(message);
    this.detail = null;
  }

  public VaadinServiceException(String message, Object detail) {
    super(message);
    this.detail = detail;
  }

  public VaadinServiceException(Throwable cause) {
    super(cause);
    this.detail = null;
  }

  public VaadinServiceException(Throwable cause, Object detail) {
    super(cause);
    this.detail = detail;
  }

  public VaadinServiceException(String message, Throwable cause) {
    super(message, cause);
    this.detail = null;
  }

  public VaadinServiceException(String message, Throwable cause,
      Object detail) {
    super(message, cause);
    this.detail = detail;
  }

  public Object getDetail() {
    return detail;
  }

  public Map<String, Object> getSerializationData() {
    Map<String, Object> serializationData = new HashMap<>();
    serializationData.put("type",
        Optional.ofNullable(getCause()).orElse(this).getClass().getName());
    serializationData.put("message", getMessage());
    serializationData.put("detail", detail);
    return serializationData;
  }
}
