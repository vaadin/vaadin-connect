/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.connect.demo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import static com.vaadin.connect.demo.DemoVaadinOAuthConfiguration.TEST_LOGIN;
import static com.vaadin.connect.demo.DemoVaadinOAuthConfiguration.TEST_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = DemoApplication.class)
public class VaadinConnectOauthTokenIT {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  private JacksonJsonParser parser = new JacksonJsonParser();

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity()).build();
  }

  private ResultActions getToken(String username, String password)
      throws Exception {

    return mockMvc.perform(post("/oauth/token")
        .with(httpBasic("vaadin-connect-client", "c13nts3cr3t"))
        .accept("application/json").param("username", username)
        .param("password", password).param("grant_type", "password"));
  }

  private ResultActions getToken(String refreshToken)
      throws Exception {

    return mockMvc.perform(post("/oauth/token")
        .with(httpBasic("vaadin-connect-client", "c13nts3cr3t"))
        .accept("application/json").param("client_id", "vaadin-connect-client")
        .param("refresh_token", refreshToken)
        .param("grant_type", "refresh_token"));
  }

  @Test
  public void should_NotGetValidToken_When_InvalidCredentials()
      throws Exception {
    getToken(TEST_LOGIN, "BAD-PASSWORD").andExpect(status().is(400));
  }

  @Test
  public void should_GetValidToken_When_ValidCredentials() throws Exception {
    String resultString = getToken(TEST_LOGIN, TEST_PASSWORD)
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn().getResponse().getContentAsString();

    String accessToken = parser.parseMap(resultString).get("access_token")
        .toString();
    String[] parts = accessToken.split("\\.");
    assertEquals(3, parts.length);
  }

  @Test
  public void should_GetValidToken_When_UsingRefreshToken() throws Exception {
    String resultString = getToken(TEST_LOGIN, TEST_PASSWORD)
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn().getResponse().getContentAsString();

    String refreshToken = parser.parseMap(resultString).get("refresh_token")
        .toString();

    resultString = getToken(refreshToken)
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn().getResponse().getContentAsString();

    String accessToken = parser.parseMap(resultString).get("access_token")
        .toString();
    String[] parts = accessToken.split("\\.");
    assertEquals(3, parts.length);
  }

  @Test
  public void should_AccessNonVaadinServices_Without_Authentication() throws Exception {
    mockMvc.perform(get("/hello")).andExpect(status().is(200));
  }

  @Test
  public void should_AccessHelloService_When_ValidToken() throws Exception {
    String resultString = getToken(TEST_LOGIN, TEST_PASSWORD).andReturn()
        .getResponse().getContentAsString();

    Object accessToken = parser.parseMap(resultString).get("access_token");

    requestHelloWith(accessToken).andExpect(content().string("Hello Word"));
  }

  @Test
  public void should_ReturnUnauthorized_When_TokenIsExpired() throws Exception {
    Map<String, Object> claims = getClaimsFromAccessToken();
    claims.put("exp",
        Instant.now().minus(1, ChronoUnit.MINUTES).getEpochSecond());
    String expiredAccessToken = generateNewToken(parser.formatMap(claims),
        "JustAnySigningK3y");

    requestHelloWith(expiredAccessToken).andExpect(status().is(401));
  }

  @Test
  public void should_ReturnUnauthorized_When_TokenHasNullClaim()
      throws Exception {
    Map<String, Object> claims = getClaimsFromAccessToken();
    claims.put("user_name", null);
    String invalidClaimToken = generateNewToken(parser.formatMap(claims),
        "JustAnySigningK3y");

    requestHelloWith(invalidClaimToken).andExpect(status().is(401));
  }

  @Test
  public void should_ReturnUnauthorized_When_TokenMissedARequiredClaim()
      throws Exception {
    Map<String, Object> claims = getClaimsFromAccessToken();
    claims.remove("user_name");
    String missingClaimToken = generateNewToken(parser.formatMap(claims),
        "JustAnySigningK3y");

    requestHelloWith(missingClaimToken).andExpect(status().is(401));
  }

  @Test
  public void should_ReturnUnauthorized_When_TokenIsSignedWithDifferentSigningKey()
      throws Exception {
    String claimsString = parser.formatMap(getClaimsFromAccessToken());
    String expiredAccessToken = generateNewToken(claimsString,
        "DifferentSigningKey");
    requestHelloWith(expiredAccessToken).andExpect(status().is(401));
  }

  private ResultActions requestHelloWith(Object accessToken) throws Exception {
    return mockMvc.perform(
        get("/hello").header("Authorization", "Bearer " + accessToken));
  }

  private String generateNewToken(String claims, String signingKey) {
    MacSigner defaultMacSigner = new MacSigner(signingKey);
    return JwtHelper.encode(claims, defaultMacSigner).getEncoded();
  }

  private Map<String, Object> getClaimsFromAccessToken() throws Exception {
    String resultString = getToken(TEST_LOGIN, TEST_PASSWORD).andReturn()
        .getResponse().getContentAsString();
    String accessToken = (String) parser.parseMap(resultString)
        .get("access_token");
    String[] accessTokenParts = accessToken.split("\\.");
    return parser.parseMap(
        new String(Base64Utils.decodeFromString(accessTokenParts[1])));
  }
}
