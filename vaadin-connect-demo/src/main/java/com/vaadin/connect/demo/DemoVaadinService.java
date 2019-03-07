package com.vaadin.connect.demo;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.vaadin.connect.VaadinService;
import com.vaadin.connect.auth.AnonymousAllowed;
import com.vaadin.connect.exception.VaadinConnectException;

@VaadinService
@DenyAll
public class DemoVaadinService {
  public static class ComplexRequest {
    private final String name;
    private final int count;

    public ComplexRequest(@JsonProperty("name") String name,
        @JsonProperty("count") int count) {
      this.name = name;
      this.count = count;
    }
  }

  /**
   * Same name class to check the alias work correctly.
   */
  public static class BeanWithTypeFromDependencies {
    String foo;
  }

  public static class ComplexResponse {
    private final String name;
    private final Map<Integer, List<String>> generatedResponse;

    public ComplexResponse(@JsonProperty("name") String name,
        @JsonProperty("generatedResponse") Map<Integer, List<String>> generatedResponse) {
      this.name = name;
      this.generatedResponse = generatedResponse;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ComplexResponse that = (ComplexResponse) o;
      return Objects.equals(name, that.name)
          && Objects.equals(generatedResponse, that.generatedResponse);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, generatedResponse);
    }
  }

  @RolesAllowed("ROLE_USER")
  public int addOne(int number) {
    return number + 1;
  }

  @AnonymousAllowed
  public boolean isOk() {
    return true;
  }

  /**
   * This method is to make sure that the generator won't fail the build when
   * resolving types from project's dependencies.
   *
   * @param version
   *          a version object from dependencies
   *
   * @return {@code null}
   */
  @PermitAll
  public BeanWithTypeFromDependencies testTypeResolverMethod(
      com.vaadin.connect.demo.account.BeanWithTypeFromDependencies version) {
    return null;
  }

  @PermitAll
  private String privateMethod() {
    return "no-op";
  }

  @PermitAll
  public void noReturnNoArguments() {
  }

  @PermitAll
  public String throwsException() {
    throw new IllegalStateException("Ooops");
  }

  @PermitAll
  public ComplexResponse complexEntitiesTest(ComplexRequest request) {
    Map<Integer, List<String>> results = new HashMap<>();
    for (int i = 0; i < request.count; i++) {
      List<String> subresults = new ArrayList<>(i);
      for (int j = 0; j < i; j++) {
        subresults.add(Integer.toString(j));
      }
      results.put(i, subresults);
    }
    return new ComplexResponse(request.name, results);
  }

  @RolesAllowed("ROLE_ADMIN")
  public void permitRoleAdmin() {
  }

  public void deniedByClass() {
  }

  @AnonymousAllowed
  public String hasAnonymousAccess() {
    return "anonymous success";
  }

  @PermitAll
  public int doNotSubmitZeroes(int number) {
    try {
      return 42 / number;
    } catch (ArithmeticException e) {
      throw new VaadinConnectException("You had one job to do!", e,
          Collections.singletonMap("wrong_parameter", number));
    }
  }

  @PermitAll
  public Map<String, String> echoMapObject(Map<String, String> param) {
    return param;
  }

  @PermitAll
  public Instant echoInstant(Instant instant) {
    return instant;
  }

  @PermitAll
  public Date echoDate(Date date) {
    return date;
  }

  @PermitAll

  public LocalDate echoLocalDate(LocalDate localDate) {
    return localDate;
  }

  @PermitAll
  public LocalDateTime echoLocalDateTime(LocalDateTime localDateTime) {
    return localDateTime;
  }
}
