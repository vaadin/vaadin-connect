package com.vaadin.connect.oauth;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
public class VaadinConnectOauthAclCheckerTest {
  private static final String ROLE_USER = "ROLE_USER";

  @Rule
  public ExpectedException exception = ExpectedException.none();

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

  private static SecurityContext createAnonymousContext() {
    SecurityContext anonymousContext = mock(SecurityContext.class);
    when(anonymousContext.getAuthentication())
        .thenReturn(new AnonymousAuthenticationToken("key", "principal",
            Collections.singleton(new SimpleGrantedAuthority("ANONYMOUS"))));
    return anonymousContext;
  }

  @Before
  public void before() {
    checker = new VaadinConnectOAuthAclChecker();

    SecurityContextHolder
        .setStrategyName(MockSecurityContextHolderStrategy.class.getName());

    GrantedAuthority authority = mock(GrantedAuthority.class);
    when(authority.getAuthority()).thenReturn(ROLE_USER);

    OAuth2Authentication authentication = mock(OAuth2Authentication.class);
    when(authentication.getAuthorities()).thenReturn(Arrays.asList(authority));

    securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
  }

  @After
  public void after() {
    SecurityContextHolder.setStrategyName(null);
    checker = null;
  }

  private void shouldPass(Class<?> test) throws Exception {
    Method method = test.getMethod("test");
    assertNull(checker.check(method));
  }

  private void shouldFail(Class<?> test) throws Exception {
    Method method = test.getMethod("test");
    assertNotNull(checker.check(method));
  }

  @Test
  public void should_Fail_When_NoOauth2Authentication() throws Exception {
    class Test {
      public void test() {
      }
    }
    securityContext = createAnonymousContext();
    shouldFail(Test.class);
  }

  @Test
  public void should_Pass_When_Oauth2Authentication() throws Exception {
    class Test {
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test
  public void should_Fail_When_DenyAllClass() throws Exception {
    @DenyAll
    class Test {
      public void test() {
      }
    }
    shouldFail(Test.class);
  }

  @Test()
  public void should_Pass_When_DenyAllClass_ValidRoleMethod() throws Exception {
    @DenyAll
    class Test {
      @RolesAllowed(ROLE_USER)
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test()
  public void should_Pass_When_DenyAllClass_PermitAllMethod() throws Exception {
    @DenyAll
    class Test {
      @PermitAll
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test()
  public void should_Fail_When_InvalidRoleClass() throws Exception {
    @RolesAllowed({ "ROLE_ADMIN" })
    class Test {
      public void test() {
      }
    }
    shouldFail(Test.class);
  }

  @Test()
  public void should_Pass_When_InvalidRoleClass_ValidRoleMethod()
      throws Exception {
    @RolesAllowed({ "ROLE_ADMIN" })
    class Test {
      @RolesAllowed(ROLE_USER)
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test()
  public void should_Pass_When_InvalidRoleClass_PermitAllMethod()
      throws Exception {
    @RolesAllowed({ "ROLE_ADMIN" })
    class Test {
      @PermitAll
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test()
  public void should_Pass_When_ValidRoleClass() throws Exception {
    @RolesAllowed(ROLE_USER)
    class Test {
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test
  public void should_AllowAnonymousAccess_When_ClassIsAnnotated()
      throws Exception {
    @AnonymousAllowed
    class Test {
      public void test() {
      }
    }
    securityContext = createAnonymousContext();
    shouldPass(Test.class);
  }

  @Test
  public void should_AllowAnonymousAccess_When_MethodIsAnnotated()
      throws Exception {
    class Test {
      @AnonymousAllowed
      public void test() {
      }
    }
    securityContext = createAnonymousContext();
    shouldPass(Test.class);
  }

  @Test
  public void should_NotAllowAnonymousAccess_When_NoAnnotationsPresent()
      throws Exception {
    class Test {
      public void test() {
      }
    }
    securityContext = createAnonymousContext();
    shouldFail(Test.class);
  }

  @Test
  public void should_AllowAnyAuthenticatedAccess_When_PermitAllAndAnonymousAllowed()
      throws Exception {
    class Test {
      @PermitAll
      @AnonymousAllowed
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test
  public void should_AllowAnonymousAccess_When_PermitAllAndAnonymousAllowed()
      throws Exception {
    class Test {
      @PermitAll
      @AnonymousAllowed
      public void test() {
      }
    }

    securityContext = createAnonymousContext();
    shouldPass(Test.class);
  }

  @Test
  public void should_AllowAnyAuthenticatedAccess_When_RolesAllowedAndAnonymousAllowed()
      throws Exception {
    class Test {
      @RolesAllowed("ADMIN")
      @AnonymousAllowed
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test
  public void should_AllowAnonymousAccess_When_RolesAllowedAndAnonymousAllowed()
      throws Exception {
    class Test {
      @RolesAllowed("ADMIN")
      @AnonymousAllowed
      public void test() {
      }
    }
    securityContext = createAnonymousContext();
    shouldPass(Test.class);
  }

  @Test
  public void should_DisallowAnyAuthenticatedAccess_When_DenyAllAndAnonymousAllowed()
      throws Exception {
    class Test {
      @DenyAll
      @AnonymousAllowed
      public void test() {
      }
    }
    shouldFail(Test.class);
  }

  @Test
  public void should_DisallowNotMatchingRoleAccess_When_RolesAllowedAndPermitAll()
      throws Exception {
    class Test {
      @RolesAllowed("ADMIN")
      @PermitAll
      public void test() {
      }
    }
    shouldFail(Test.class);
  }

  @Test
  public void should_AllowSpecificRoleAccess_When_RolesAllowedAndPermitAll()
      throws Exception {
    class Test {
      @RolesAllowed(ROLE_USER)
      @PermitAll
      public void test() {
      }
    }
    shouldPass(Test.class);
  }

  @Test
  public void should_DisallowAnonymousAccess_When_DenyAllAndAnonymousAllowed()
      throws Exception {
    class Test {
      @DenyAll
      @AnonymousAllowed
      public void test() {
      }
    }
    securityContext = createAnonymousContext();
    shouldFail(Test.class);
  }

  @Test
  public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithDenyAll()
      throws Exception {
    @AnonymousAllowed
    class Test {
      @DenyAll
      public void test() {
      }
    }

    securityContext = createAnonymousContext();
    shouldFail(Test.class);
  }

  @Test
  public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithRolesAllowed()
      throws Exception {
    @AnonymousAllowed
    class Test {
      @RolesAllowed(ROLE_USER)
      public void test() {
      }
    }

    securityContext = createAnonymousContext();
    shouldFail(Test.class);
  }

  @Test
  public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithPermitAll()
      throws Exception {
    @AnonymousAllowed
    class Test {
      @PermitAll
      public void test() {
      }
    }

    securityContext = createAnonymousContext();
    shouldFail(Test.class);
  }

  @Test
  public void should_Throw_When_PrivateMethodIsPassed() throws Exception {
    class Test {
      private void test() {
      }
    }

    Method method = Test.class.getDeclaredMethod("test");
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(method.toString());
    checker.getSecurityTarget(method);
  }

  @Test
  public void should_ReturnEnclosingClassAsSecurityTarget_When_NoSecurityAnnotationsPresent()
      throws Exception {
    class Test {
      public void test() {
      }
    }
    assertEquals(Test.class,
        checker.getSecurityTarget(Test.class.getMethod("test")));
  }

  @Test
  public void should_ReturnEnclosingClassAsSecurityTarget_When_OnlyClassHasSecurityAnnotations()
      throws Exception {
    @AnonymousAllowed
    class Test {
      public void test() {
      }
    }
    assertEquals(Test.class,
        checker.getSecurityTarget(Test.class.getMethod("test")));
  }

  @Test
  public void should_ReturnMethodAsSecurityTarget_When_OnlyMethodHasSecurityAnnotations()
      throws Exception {
    class Test {
      @AnonymousAllowed
      public void test() {
      }
    }
    Method securityMethod = Test.class.getMethod("test");
    assertEquals(securityMethod, checker.getSecurityTarget(securityMethod));
  }

  @Test
  public void should_ReturnMethodAsSecurityTarget_When_BothClassAndMethodHaveSecurityAnnotations()
      throws Exception {
    @AnonymousAllowed
    class Test {
      @AnonymousAllowed
      public void test() {
      }
    }
    Method securityMethod = Test.class.getMethod("test");
    assertEquals(securityMethod, checker.getSecurityTarget(securityMethod));
  }
}
