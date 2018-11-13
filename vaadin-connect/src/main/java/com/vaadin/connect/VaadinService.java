package com.vaadin.connect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Annotation to mark the services to be processed by
 * {@link VaadinConnectController} class. Each class annotated automatically
 * becomes a Spring {@link Component} bean.
 *
 * After the class is annotated and processed, it becomes available as a Vaadin
 * Service. This means that the class name and all its public methods (including
 * the ones inherited from a super classes that are not {@link Object}) can be
 * executed via the post call with the correct parameters sent in a request JSON
 * body. The methods' return values will be returned back as a response to the
 * calls. Refer to {@link VaadinConnectController} for more details.
 *
 * @see VaadinConnectController
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface VaadinService {
  /**
   * The name of a service to use. If nothing is specified, the name of the
   * annotated class is taken.
   *
   * @return the name of the service to use in post requests
   */
  String value() default "";
}
