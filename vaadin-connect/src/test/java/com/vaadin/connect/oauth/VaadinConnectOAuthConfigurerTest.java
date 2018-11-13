package com.vaadin.connect.oauth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(Parameterized.class)
public class VaadinConnectOAuthConfigurerTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private AnnotationConfigWebApplicationContext context;

  private List<Class<?>> resources;

  @FunctionalInterface
  public interface TestRunner {
    void run(AnnotationConfigWebApplicationContext t) throws Exception;
  }

  @Parameters( name = "{index}: {0}" )
  public static List<Object[]> parameters() {
    return Arrays.asList(
        new Object[][] {
            { "Should throw a dependency exception if VaadinConnectOAuthConfigurer has not been loaded", UnsatisfiedDependencyException.class,
                Arrays.asList(EnableSpringWebSecurity.class, ValidTokenTest.class) },
            { "Should not produce a valid token if VaadinConnectOAuthConfigurer has not been correctly configured", null,
                Arrays.asList(EnableSpringWebSecurity.class, UnconfiguredVaadinOauth.class, InvalidTokenTest.class) },
            { "Should produce a valid token when VaadinConnectOAuthConfigurer is convigured and valid credentials are provided", null,
                Arrays.asList(EnableSpringWebSecurity.class, ConfiguredVaadinOauth.class, ValidTokenTest.class) },
            { "Should not produce a valid token if username is incorrect", null,
                Arrays.asList(EnableSpringWebSecurity.class, ConfiguredVaadinOauth.class, InvalidUserTest.class) },
            { "Should not produce a valid token if password is incorrect", null,
                Arrays.asList(EnableSpringWebSecurity.class, ConfiguredVaadinOauth.class, InvalidPasswordTest.class) },
            { "Should not produce a valid token if grant type is invalid", null,
                Arrays.asList(EnableSpringWebSecurity.class, ConfiguredVaadinOauth.class, InvalidGrantTest.class) },
            { "Should not produce a valid token if client secret is incorrect", null,
                Arrays.asList(EnableSpringWebSecurity.class, ConfiguredVaadinOauth.class, InvalidSecretTest.class) },
            { "Should produce a valid token when overriding VaadinConnectOAuthConfigurer with custom auth services and parameters", null,
                Arrays.asList(EnableSpringWebSecurity.class, OverrideVaadinOauthConfigTest.class) },
        });
  }

  public VaadinConnectOAuthConfigurerTest(String testMessage, Class<Throwable> exception, List<Class<?>> resources) {
    context = new AnnotationConfigWebApplicationContext();
    context.setServletContext(new MockServletContext());
    if (exception != null) {
      expected.expect(exception);
    }
    for (Class<?> resource : resources) {
      context.register(resource);
    }
    this.resources = resources;
  }

  @Test
  public void test() throws Exception {
    context.refresh();
    for (Class<?> resource : resources) {
      if (TestRunner.class.isAssignableFrom(resource)) {
        ((TestRunner) context.getBean(resource)).run(context);
      }
    }
  }

  @Configuration
  protected static class UnconfiguredVaadinOauth extends VaadinConnectOAuthConfigurer {
  }

  @Configuration
  protected static class ConfiguredVaadinOauth extends VaadinConnectOAuthConfigurer {
    @Override
    public UserDetails getUserDetails(String username) {
      if (username.equals("foo")) {
        return User.builder().username("foo").password("bar").roles("baz").build();
      } else {
        return null;
      }
    }
  }

  @Configuration
  @EnableWebSecurity
  @EnableAuthorizationServer
  @EnableWebMvc
  protected static class EnableSpringWebSecurity {
  }

  private static ResultActions getToken(AnnotationConfigWebApplicationContext webContext,
      String client, String username, String password, String granttype) throws Exception {

    return MockMvcBuilders
        .webAppContextSetup(webContext)
        .apply(springSecurity())
        .build()
        .perform(post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils
                .encodeToString(client.getBytes()))
            .header(HttpHeaders.ACCEPT, "application/json")
            .param("username", username)
            .param("password", password)
            .param("grant_type", granttype));
  }

  @Configuration
  protected static class InvalidTokenTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "password")
          .andExpect(status().is(400));
    }
  }

  @Configuration
  protected static class ValidTokenTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "password")
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
          .andExpect(jsonPath("$.access_token", notNullValue()));
    }
  }

  @Configuration
  protected static class InvalidUserTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "vaadin-connect-client:c13nts3cr3t", "INVALID-USER", "bar", "password")
          .andExpect(status().is(401));
    }
  }

  @Configuration
  protected static class InvalidPasswordTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "vaadin-connect-client:c13nts3cr3t", "foo", "INVALID-PASS", "password")
          .andExpect(status().is(400));
    }
  }

  @Configuration
  protected static class InvalidGrantTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "INVALID-GRANT")
          .andExpect(status().is(400));
    }
  }

  @Configuration
  protected static class InvalidSecretTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "vaadin-connect-client:INVALID-SECRET", "foo", "bar", "password")
          .andExpect(status().is(401));
    }
  }

  @Configuration
  protected static class OverrideVaadinOauthConfigTest extends VaadinConnectOAuthConfigurer implements TestRunner {

    @Override
    public String getClientApp() {
      return "TEST-app";
    }

    @Override
    public String getClientAppSecret() {
      return "ENCODED-TEST-secret";
    }

    @Override
    public PasswordEncoder passwordEncoder() {
      return new PasswordEncoder() {
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
          return encodedPassword.equals(this.encode(rawPassword));
        }

        @Override
        public String encode(CharSequence rawPassword) {
          return "ENCODED-" + rawPassword;
        }
      };
    }

    @Override
    public UserDetailsService userDetailsService() {
      return account -> User.builder().username("foo").password("ENCODED-bar").roles("baz").build();
    }

    @Override
    public void run(AnnotationConfigWebApplicationContext context) throws Exception {
      getToken(context, "TEST-app:TEST-secret", "foo", "bar", "password")
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
          .andExpect(jsonPath("$.access_token", notNullValue()));
    }
  }

}
