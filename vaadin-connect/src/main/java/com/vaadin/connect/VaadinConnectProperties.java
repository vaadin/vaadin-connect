package com.vaadin.connect;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class that contains all Vaadin Connect customizable properties.
 */
@Component
@ConfigurationProperties("vaadin.connect")
public class VaadinConnectProperties {
  @Value("${vaadin.connect.endpoint:/connect}")
  private String vaadinConnectEndpoint;

  /**
   * Customize the endpoint for all Vaadin Connect services. See default value
   * in the {@link VaadinConnectProperties#vaadinConnectEndpoint} field
   * annotation.
   *
   * @return endpoint that should be used to access any Vaadin Connect service
   */
  public String getVaadinConnectEndpoint() {
    return vaadinConnectEndpoint;
  }
}
