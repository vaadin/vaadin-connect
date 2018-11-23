package com.vaadin.connect.oauth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.REFRESH_TOKEN;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.ATI;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.AUTHORITIES;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.CLIENT_ID;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.EXP;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.JTI;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.SCOPE;
import static org.springframework.security.oauth2.provider.token.UserAuthenticationConverter.USERNAME;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * This test is inspired in the pattern used in
 * `AuthorizationServerConfigurationTests.java` in the spring oauth2 project
 * https://github.com/spring-projects/spring-security-oauth
 */
@RunWith(Parameterized.class)
public class VaadinConnectOAuthConfigurerTest {

  /**
   * This test class is instantiated and run as many times as elements in the
   * next list. Each element in the list has an array of three elements: the
   * description of the test, an exception that should be thrown when running
   * the test, and a list of spring classes that should be registered in the
   * webcontext. One of these classes should implement the {@link TestRunner}
   * interface
   */
  @Parameters(name = "{index}: {0}")
  public static List<Object[]> parameters() {
    return Arrays.asList( // @formatter:off
      new Object[][] {
        { "Should fail if Vaadin oauth has not been correctly configured",
          AssertionError.class,
          Arrays.asList(EnableVaadinOauth.class, ValidTokenTest.class) },
        { "Should not produce a valid token if UserDetailsService is not provided",
          null,
          Arrays.asList(EnableVaadinOauth.class, InvalidTokenTest.class) },
        { "Should produce a valid token when UserDetailsService is provided",
          null,
          Arrays.asList(EnableVaadinOauth.class, ConfigureUserDetailsService.class, ValidTokenTest.class) },
        { "Should not produce a valid token if username is incorrect",
          null,
          Arrays.asList(EnableVaadinOauth.class, ConfigureUserDetailsService.class, InvalidUserTest.class) },
        { "Should not produce a valid token if password is incorrect",
          null,
          Arrays.asList(EnableVaadinOauth.class, ConfigureUserDetailsService.class, InvalidPasswordTest.class) },
        { "Should not produce a valid token if grant type is invalid",
          null,
          Arrays.asList(EnableVaadinOauth.class, ConfigureUserDetailsService.class, InvalidGrantTest.class) },
        { "Should not produce a valid token if client secret is incorrect",
          null,
          Arrays.asList(EnableVaadinOauth.class, ConfigureUserDetailsService.class, InvalidSecretTest.class) },
        { "Should produce a valid token when providing custom UserDetailsService and PasswordEncoder",
          null,
          Arrays.asList(EnableVaadinOauth.class, ConfigureUserDetailsService.class, CustomUserDetailsPasswordTest.class) },
        { "Should produce a valid token when providing a custom AuthenticationManage",
          null,
          Arrays.asList(EnableVaadinOauth.class, CustomAuthenticationManagerTest.class) },
      }); // @formatter:on
  }

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private AnnotationConfigWebApplicationContext context;

  private List<Class<?>> resources;

  private static JacksonJsonParser jsParser = new JacksonJsonParser();

  @FunctionalInterface
  public interface TestRunner {
    void run(AnnotationConfigWebApplicationContext ctx) throws Exception;
  }

  /**
   * Called as many times as items are returned in the
   * {@link VaadinConnectOAuthConfigurerTest#parameters()} method.
   */
  public VaadinConnectOAuthConfigurerTest(String testMessage,
      Class<Throwable> exception, List<Class<?>> resources) {
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

  /**
   * The test to run. It looks for classes implementing the {@link TestRunner}
   * interface and calls it.
   */
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
  protected static class ConfigureUserDetailsService {
    // Lock the account if user is validated more than 2 times
    // Used to check that UserDetailsChecker is run when using refresh tokens
    int usageCount = 0;

    @Bean
    public UserDetailsService userDetailsService() {
      // Password in database should be BCrypted by default.
      String crypted = new BCryptPasswordEncoder().encode("bar");

      return username -> {
        if (username.equals("foo")) {
          return User.builder()
              .username("foo")
              .password(crypted)
              .roles("baz")
              .accountLocked(++usageCount > 2)
              .build();
        } else {
          return null;
        }
      };
    }
  }

  @Configuration
  @EnableVaadinConnectOAuthServer
  @EnableWebMvc
  protected static class EnableVaadinOauth {
  }

  /**
   * Send a request to get an oauth2 access token.
   *
   * The oauth2 process to take a valid token, needs to pass an HTTP basic
   * access user and a password pair that are named in oauth as application-name
   * and application-secret.
   *
   * In addition the request must send a form with the real username, password
   * and grant type parameters.
   *
   * In spring oauth2, the web endpoint is hardcoded to `/oauth/token`
   */
  private static ResultActions getToken(
      AnnotationConfigWebApplicationContext webContext,
      String client, String userOrClient, String passwordOrRefresh,
      String granttype)
      throws Exception {

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    if ("refresh_token".equals(granttype)) {
      map.add("client_id", userOrClient);
      map.add("refresh_token", passwordOrRefresh);
    } else {
      map.add("username", userOrClient);
      map.add("password", passwordOrRefresh);
    }

    return MockMvcBuilders
        .webAppContextSetup(webContext)
        .apply(springSecurity())
        .build()
        .perform(post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, "Basic " +
                Base64Utils.encodeToString(client.getBytes()))
            .header(HttpHeaders.ACCEPT, "application/json")
            .params(map)
            .param("grant_type", granttype));
  }

  @Configuration
  protected static class InvalidTokenTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "password")
              .andExpect(status().is(400));
    }
  }

  @Configuration
  protected static class ValidTokenTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {

      // Validate via User/Password
      String refreshToken = assertValidTokenResponse(
          getToken(context,
              "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "password"));

      // Re-validate via refreshToken
      refreshToken = assertValidTokenResponse(
          getToken(context,
              "vaadin-connect-client:c13nts3cr3t", "vaadin-connect-client",
              refreshToken, "refresh_token"));

      // This re-validate should fail because user is locked after the second
      // validation
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "vaadin-connect-client",
          refreshToken, "refresh_token").andExpect(status().is(401));
    }
  }

  private static String assertValidTokenResponse(ResultActions results)
      throws Exception {
    String resultString = results
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andReturn().getResponse().getContentAsString();

    Map<String, Object> responseMap = jsParser.parseMap(resultString);
    String accessToken = responseMap.get(ACCESS_TOKEN).toString();
    String refreshToken = responseMap.get(REFRESH_TOKEN).toString();
    assertNotNull(accessToken);
    assertNotNull(refreshToken);

    Map<String, Object> accessTokenBody = assertJWTStructure(accessToken);
    assertNull(accessTokenBody.get(ATI));
    int accessTokenExpiration = Integer
        .parseInt(accessTokenBody.get(EXP).toString());

    Map<String, Object> refreshTokenBody = assertJWTStructure(refreshToken);
    assertNotNull(refreshTokenBody.get(ATI));
    int refreshTokenExpiration = Integer
        .parseInt(refreshTokenBody.get(EXP).toString());

    assertTrue(refreshTokenExpiration > accessTokenExpiration);
    return refreshToken;
  }

  private static Map<String, Object> assertJWTStructure(String token) {
    String[] parts = token.split("\\.");
    assertEquals(3, parts.length);

    Map<String, Object> tokenHeader = jsParser
        .parseMap(new String(Base64.getDecoder().decode(parts[0])));
    assertEquals("HS256", tokenHeader.get("alg"));
    assertEquals("JWT", tokenHeader.get("typ"));

    Map<String, Object> tokenBody = jsParser
        .parseMap(new String(Base64.getDecoder().decode(parts[1])));
    assertNotNull(tokenBody.get(EXP));
    assertNotNull(tokenBody.get(JTI));
    assertNotNull(tokenBody.get(AUTHORITIES));
    assertNotNull(tokenBody.get(SCOPE));
    assertEquals("vaadin-connect-client", tokenBody.get(CLIENT_ID));
    assertEquals("foo", tokenBody.get(USERNAME));
    return tokenBody;
  }

  @Configuration
  protected static class InvalidUserTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "NO-USER", "bar", "password")
              .andExpect(status().is(401));
    }
  }

  @Configuration
  protected static class InvalidPasswordTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "foo", "NO-PASS", "password")
              .andExpect(status().is(400));
    }
  }

  @Configuration
  protected static class InvalidGrantTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "NO-GRANT")
              .andExpect(status().is(400));
    }
  }

  @Configuration
  protected static class InvalidSecretTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:NO-SECRET", "foo", "bar", "password")
              .andExpect(status().is(401));
    }
  }

  @Configuration
  protected static class CustomUserDetailsPasswordTest implements TestRunner {
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new PasswordEncoder() {
        @Override
        public boolean matches(CharSequence rawPassword,
            String encodedPassword) {
          return encodedPassword.equals(this.encode(rawPassword));
        }

        @Override
        public String encode(CharSequence rawPassword) {
          return "ENCODED-" + rawPassword;
        }
      };
    }

    @Bean
    public UserDetailsService userDetailsService() {
      return account -> User.builder().username("foo").password("ENCODED-bar")
          .roles("baz").build();
    }

    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "foo", "bar", "password")
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
              .andExpect(jsonPath("$.access_token", notNullValue()));
    }
  }

  @Configuration
  protected static class CustomAuthenticationManagerTest implements TestRunner {
    @Bean
    AuthenticationManager authenticationManager() {
      return auth -> new UsernamePasswordAuthenticationToken(
          auth.getName(), auth.getCredentials(), new ArrayList<>());
    }

    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getToken(context,
          "vaadin-connect-client:c13nts3cr3t", "ANY-USER", "ANY-PASS", "password")
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
              .andExpect(jsonPath("$.access_token", notNullValue()));
    }
  }
}
