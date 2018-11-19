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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component
public class VaadinConnectOAuthAclChecker {

  public void check(Method method) throws UnauthorizedUserException {

    Class<?> clazz = method.getDeclaringClass();

    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();

    if (!(auth instanceof OAuth2Authentication)) {
      throw new UnauthorizedUserException(
          "Bad authentication, app should use oauth2");
    }

    Collection<GrantedAuthority> auths = ((OAuth2Authentication) auth)
        .getAuthorities();
    
    
    boolean methodForbidden = denyAll(method) || !roleAllowed(method, auths);
    boolean classForbidden = denyAll(clazz) || !roleAllowed(clazz, auths);
    
    if (classForbidden && !isAnnotated(method) || methodForbidden) {
      throw new UnauthorizedUserException(
          "Unauthorized access to vaadin service");
    }
  }

  private boolean isAnnotated(Method method) {
    return method.getAnnotation(PermitAll.class) != null
        || method.getAnnotation(DenyAll.class) != null
        || method.getAnnotation(RolesAllowed.class) != null;
  }

  private boolean roleAllowed(Method method,
      Collection<GrantedAuthority> auths) {
    return roleAllowed(method.getAnnotation(RolesAllowed.class), auths);
  }

  private boolean roleAllowed(Class<?> clazz,
      Collection<GrantedAuthority> auths) {
    return roleAllowed(clazz.getAnnotation(RolesAllowed.class), auths);
  }

  private boolean denyAll(Method method) {
    return method.getAnnotation(DenyAll.class) != null;
  }

  private boolean roleAllowed(RolesAllowed roles,
      Collection<GrantedAuthority> auths) {
    return roles == null
        || auths.stream().anyMatch(auth -> Arrays.stream(roles.value())
            .anyMatch(auth.toString()::equals));
  }

  private boolean denyAll(Class<?> clazz) {
    return clazz.getAnnotation(DenyAll.class) != null;
  }
}
