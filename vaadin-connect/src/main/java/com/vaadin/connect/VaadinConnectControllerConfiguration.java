package com.vaadin.connect;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Value;
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
  @Value("${vaadin.connect.base.url:/connect}")
  private String vaadinConnectBaseUrl;

  /**
   * Customize the base url that Vaadin Connect services are located at. See
   * default value in the
   * {@link VaadinConnectControllerConfiguration#vaadinConnectBaseUrl} field
   * annotation.
   *
   * @return base url that should be used to access any Vaadin Connect service
   */
  public String getVaadinConnectBaseUrl() {
    return vaadinConnectBaseUrl;
  }

  /**
   * Registers {@link VaadinConnectController} to use
   * {@link VaadinConnectControllerConfiguration#getVaadinConnectBaseUrl()} as a
   * base url for all Vaadin Connect services.
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
                getVaadinConnectBaseUrl())
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
