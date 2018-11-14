package com.vaadin.connect;

import java.lang.reflect.Method;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * A configuration class for customizing the {@link VaadinConnectController}
 * class.
 */
@Configuration
public class VaadinConnectControllerConfiguration {
  private final VaadinConnectProperties vaadinConnectProperties;

  /**
   * Initializes the connect configuration.
   *
   * @param vaadinConnectProperties
   *          Vaadin Connect properties
   */
  public VaadinConnectControllerConfiguration(
      VaadinConnectProperties vaadinConnectProperties) {
    this.vaadinConnectProperties = vaadinConnectProperties;
  }

  /**
   * Registers {@link VaadinConnectController} to use
   * {@link VaadinConnectProperties#getVaadinConnectEndpoint()} as an endpoint
   * for all Vaadin Connect services.
   *
   * @return updated configuration for {@link VaadinConnectController}
   */
  @Bean
  public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {

          @Override
          protected void registerHandlerMethod(Object handler, Method method,
              RequestMappingInfo mapping) {
            if (VaadinConnectController.class
                .equals(method.getDeclaringClass())) {
              PatternsRequestCondition connectServicePattern = new PatternsRequestCondition(
                  vaadinConnectProperties.getVaadinConnectEndpoint())
                      .combine(mapping.getPatternsCondition());

              mapping = new RequestMappingInfo(mapping.getName(),
                  connectServicePattern, mapping.getMethodsCondition(),
                  mapping.getParamsCondition(), mapping.getHeadersCondition(),
                  mapping.getConsumesCondition(),
                  mapping.getProducesCondition(), mapping.getCustomCondition());
            }

            super.registerHandlerMethod(handler, method, mapping);
          }
        };
      }
    };
  }
}
