package com.vaadin.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

@VaadinService
public class TestService {

  public String test() {
    return "test successful";
  }

  public Map<String, List<TestObject>> complexTest(int count) {
    Map<String, List<TestObject>> result = new HashMap<>(count, 1);
    for (int i = 0; i < count; i++) {
      List<TestObject> inner = new ArrayList<>(count);
      for (int j = 0; j < i; j++) {
        inner.add(new TestObject(j));
      }
      result.put(Integer.toString(i), inner);
    }
    return result;
  }

  public List<Object> testMultipleParameters(String arg1, int arg2,
      double arg3) {
    return Arrays.asList(arg1, arg2, arg3);
  }

  public void noReturnValue(String testParam) {
    System.out.println(testParam);
    // testing that the method invocation is possible
  }

  public static class TestObject {
    private final int number;

    @JsonIgnore
    private final int number2;

    public TestObject(int number) {
      this.number = number;
      this.number2 = number;
    }
  }
}
