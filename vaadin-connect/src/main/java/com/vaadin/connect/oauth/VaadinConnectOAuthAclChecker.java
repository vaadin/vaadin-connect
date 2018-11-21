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

package com.vaadin.connect.oauth;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

/**
 * Component used for checking role-based ACL in Vaadin Services.
 *
 * It looks for {@link PermitAll}, {@link DenyAll} and {@link RolesAllowed}
 * annotations in service methods and classes containing these methods.
 *
 * Method-level annotation override Class-level ones.
 *
 * In the next example, since the class is denied to all, method1 is not
 * accessible to anyone, method2 can be executed by everyone, and method3 is
 * only allowed to accounts having the ROLE_USER
 *
 * <pre class="code">
 * &#64;VaadinService
 * &#64;DenyAll
 * public class DemoVaadinService {
 *
 *   public void method1() {
 *   }
 *
 *   &#64;PermitAll
 *   public void method2() {
 *   }
 *
 *   &#64;RolesAllowed("ROLE_USER")
 *   public void method3() {
 *   }
 * }
 * </pre>
 *
 */
@Component
public class VaadinConnectOAuthAclChecker {

  /**
   * Check that the service is accessible for the current user.
   *
   * @param method
   *          the vaadin service method to check ACL
   * @return an error String on failure, otherwise {@code null}
   */
  public String check(Method method) {
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();

    if (auth instanceof OAuth2Authentication) {
      return verifyAuthenticatedUser(method, (OAuth2Authentication) auth);
    } else if (auth instanceof AnonymousAuthenticationToken) {
      return verifyAnonymousUser(method);
    }
    return "Bad authentication, app should use oauth2";
  }

  private String verifyAnonymousUser(Method method) {
    if (method.isAnnotationPresent(PermitAnonymous.class) || method
        .getDeclaringClass().isAnnotationPresent(PermitAnonymous.class)) {
      return null;
    }
    return "Anonymous access is not allowed";

  }

  private String verifyAuthenticatedUser(Method method,
      OAuth2Authentication auth) {
    Class<?> clazz = method.getDeclaringClass();
    Collection<GrantedAuthority> authorities = auth.getAuthorities();

    boolean methodForbidden = denyAll(method)
        || !roleAllowed(method, authorities);
    boolean classForbidden = denyAll(clazz) || !roleAllowed(clazz, authorities);

    if (classForbidden && !isAnnotated(method) || methodForbidden) {
      return "Unauthorized access to vaadin service";
    }

    return null;
  }

  private boolean isAnnotated(Method method) {
    return method.isAnnotationPresent(PermitAll.class)
        || method.isAnnotationPresent(DenyAll.class)
        || method.isAnnotationPresent(RolesAllowed.class);
  }

  private boolean roleAllowed(Method method,
      Collection<GrantedAuthority> auths) {
    return roleAllowed(method.getAnnotation(RolesAllowed.class), auths);
  }

  private boolean roleAllowed(Class<?> clazz,
      Collection<GrantedAuthority> authorities) {
    return roleAllowed(clazz.getAnnotation(RolesAllowed.class), authorities);
  }

  private boolean roleAllowed(RolesAllowed roles,
      Collection<GrantedAuthority> authorities) {
    return roles == null || authorities.stream().anyMatch(
        auth -> Arrays.asList(roles.value()).contains(auth.getAuthority()));
  }

  private boolean denyAll(Method method) {
    return method.isAnnotationPresent(DenyAll.class);
  }

  private boolean denyAll(Class<?> clazz) {
    return clazz.isAnnotationPresent(DenyAll.class);
  }
}
