package com.vaadin.connect.oauth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A security annotation, granting anonymous access to the Vaadin Connect
 * service (or its method) it is placed onto.
 * <p>
 * This means that any user will be able to trigger an service method (if placed
 * on a service class) or the particular service method (if placed on a service
 * method) without providing an authentication token.
 * <p>
 * Note that this annotation is processed separately from the
 * {@link javax.annotation.security.DenyAll},
 * {@link javax.annotation.security.PermitAll} and
 * {@link javax.annotation.security.RolesAllowed} annotations, since those are
 * related to the authorized users.
 * <p>
 * For example, when the same Vaadin Connect service method is annotated with
 * both {@link javax.annotation.security.DenyAll} and {@link PermitAnonymous}
 * annotations, no authorized user will be able to access the method and any
 * anonymous user will be able to access it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface PermitAnonymous {
}
