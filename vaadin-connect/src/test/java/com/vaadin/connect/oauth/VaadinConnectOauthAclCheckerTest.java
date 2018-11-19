package com.vaadin.connect.oauth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public class VaadinConnectOauthAclCheckerTest {

  private static SecurityContext securityContext;

  private VaadinConnectOAuthAclChecker checker;

  public static class MockSecurityContextHolderStrategy
      implements SecurityContextHolderStrategy {

    public SecurityContext getContext() {
      return securityContext;
    }

    public void clearContext() {
    }

    public void setContext(SecurityContext context) {
    }

    public SecurityContext createEmptyContext() {
      return securityContext;
    }
  }

  @Before
  public void before() {
    checker = new VaadinConnectOAuthAclChecker();

    SecurityContextHolder
        .setStrategyName(MockSecurityContextHolderStrategy.class.getName());

    GrantedAuthority authority = mock(GrantedAuthority.class);
    when(authority.getAuthority())
        .thenReturn("ROLE_USER");

    OAuth2Authentication authentication = mock(OAuth2Authentication.class);
    when(authentication.getAuthorities())
        .thenReturn(Arrays.asList(authority));

    securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication())
        .thenReturn(authentication);
  }
  
  @After
  public void after() {
    SecurityContextHolder.setStrategyName(null);
    checker = null;
  }

  public static class MyClass1 {
    public void myMethod() {
    }
  }

  @Test(expected = UnauthorizedUserException.class)
  public void should_Fail_When_NoOauth2Authentication() throws Exception {
    securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication())
        .thenReturn(new UsernamePasswordAuthenticationToken(null, null, null));

    Method method = MyClass1.class.getMethod("myMethod");
    checker.check(method);
  }

  @Test
  public void should_Pass_When_Oauth2Autentication() throws Exception {
    Method method = MyClass1.class.getMethod("myMethod");
    checker.check(method);
  }

  @DenyAll
  public static class MyClass2 {
    public void myMethod() {
    }

    @RolesAllowed({"ROLE_USER"})
    public void myMethod2() {
    }

    @PermitAll
    public void myMethod3() {
    }
  }

  @Test(expected = UnauthorizedUserException.class)
  public void should_Fail_When_DenyAll_Class() throws Exception {
    Method method = MyClass2.class.getMethod("myMethod");
    checker.check(method);
  }

  @Test()
  public void should_Pass_When_DenyAll_Class_ValidRole_Method() throws Exception {
    Method method = MyClass2.class.getMethod("myMethod2");
    checker.check(method);
  }

  @Test()
  public void should_Pass_When_DenyAll_ClassPermitAll_Method() throws Exception {
    Method method = MyClass2.class.getMethod("myMethod3");
    checker.check(method);
  }

  @RolesAllowed({"ROLE_ADMIN"})
  public static class MyClass3 {
    public void myMethod() {
    }

    @RolesAllowed({"ROLE_USER"})
    public void myMethod2() {
    }

    @PermitAll
    public void myMethod3() {
    }
  }

  @Test(expected = UnauthorizedUserException.class)
  public void should_Fail_When_InvalidRole_Class() throws Exception {
    Method method = MyClass3.class.getMethod("myMethod");
    checker.check(method);
  }

  @Test()
  public void should_Pass_When_InvalidRole_Class_ValidRole_Method() throws Exception {
    Method method = MyClass3.class.getMethod("myMethod2");
    checker.check(method);
  }

  @Test()
  public void should_Pass_When_InvalidRole_Class_PermitAll_Method() throws Exception {
    Method method = MyClass3.class.getMethod("myMethod3");
    checker.check(method);
  }

  @RolesAllowed("ROLE_USER")
  public static class MyClass4 {
    public void myMethod() {
    }
  }

  @Test()
  public void should_Pass_When_ValidRole_Class() throws Exception {
    Method method = MyClass4.class.getMethod("myMethod");
    checker.check(method);
  }
}
