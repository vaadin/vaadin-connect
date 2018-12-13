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
package com.vaadin.connect.typeconversion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.connect.VaadinService;

@VaadinService
public class VaadinConnectTypeConversionServices {
  public int addOneInt(int value) {
    return value + 1;
  }

  public boolean revertBoolean(boolean value) {
    return !value;
  }

  public byte addOneByte(byte value) {
    return (byte) (value + 1);
  }

  public char getChar(char value) {
    return value;
  }

  public short addOneShort(short value) {
    return (short) (value + 1);
  }

  public long addOneLong(long value) {
    return value + 1;
  }

  public float addOneFloat(float value) {
    return value + 1;
  }

  public double addOneDouble(double value) {
    return value + 1;
  }

  public Integer addOneIntBoxed(Integer value) {
    return value == null ? null : value + 1;
  }

  public Boolean revertBooleanBoxed(Boolean value) {
    return value == null ? null : !value;
  }

  public Byte addOneByteBoxed(Byte value) {
    return value == null ? null : (byte) (value + 1);
  }

  public Short addOneShortBoxed(Short value) {
    return value == null ? null : (short) (value + 1);
  }

  public Long addOneLongBoxed(Long value) {
    return value == null ? null : value + 1;
  }

  public Float addOneFloatBoxed(Float value) {
    return value == null ? null : value + 1;
  }

  public Double addOneDoubleBoxed(Double value) {
    return value == null ? null : value + 1;
  }

  public String addFooString(String value) {
    return value + "foo";
  }

  public Date getDate(Date value) {
    return value;
  }

  public LocalDate addOneDayLocalDate(LocalDate value) {
    return value.plus(1, ChronoUnit.DAYS);
  }

  public LocalDateTime addOneDayOneHourLocalDateTime(LocalDateTime value) {
    return value.plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
  }

  public int[] getAddOneArray(int[] value) {
    List<Integer> intList = new LinkedList<>();
    Arrays.stream(value).forEach(value1 -> intList.add(value1 + 1));
    return intList.stream().mapToInt(i -> i).toArray();
  }

  public String[] getFooStringArray(String[] value) {
    List<String> intList = new LinkedList<>();
    Arrays.stream(value).forEach(value1 -> intList.add(value1 + "-foo"));
    return intList.toArray(new String[intList.size()]);
  }

  public Object[] getObjectArray(Object[] value) {
    Arrays.stream(value).forEach(System.out::println);
    return value;
  }

  // For collections, we need to that type are parsed into correct type
  // parameter, otherwise, they will just be parsed to Collection<Object>
  public Collection<Integer> getIntegerCollection(Collection<Integer> value) {
    value.forEach(number -> number++);
    return value;
  }

  public Collection<Double> getDoubleCollection(Collection<Double> value) {
    value.forEach(number -> number++);
    return value;
  }

  public Collection<String> getStringCollection(Collection<String> value) {
    value.forEach(number -> {
      String temp = number + "temp";
    });
    return value;
  }

  public Collection<Object> getObjectCollection(Collection<Object> value) {
    value.forEach(System.out::println);
    return value;
  }

  public TestEnum getEnum(TestEnum value) {
    // Make sure that the key is parsed as an enum
    value.getValue();
    return value;
  }

  public Map<String, String> getFooMapStringString(Map<String, String> value) {
    Map<String, String> newMap = new LinkedHashMap<>();
    for (Map.Entry<String, String> stringStringEntry : value.entrySet()) {
      newMap.put(stringStringEntry.getKey(),
          stringStringEntry.getValue() + "-foo");
    }
    return newMap;
  }

  public Map<String, Integer> getAddOneMapStringInteger(
      Map<String, Integer> value) {
    Map<String, Integer> newMap = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : value.entrySet()) {
      newMap.put(entry.getKey(), entry.getValue() + 1);
    }
    return newMap;
  }

  public Map<String, Double> getAddOneMapStringDouble(
      Map<String, Double> value) {
    Map<String, Double> newMap = new LinkedHashMap<>();
    for (Map.Entry<String, Double> entry : value.entrySet()) {
      newMap.put(entry.getKey(), entry.getValue() + 1);
    }
    return newMap;
  }

  public Map<String, TestEnum> getNextEnumMapStringEnum(
      Map<String, TestEnum> value) {
    Map<String, TestEnum> newMap = new LinkedHashMap<>();
    for (Map.Entry<String, TestEnum> stringTestEnumEntry : value.entrySet()) {
      newMap.put(stringTestEnumEntry.getKey(),
          TestEnum.getTestEnum(stringTestEnumEntry.getValue().getValue() + 1));
    }
    return newMap;
  }

  public Map<TestEnum, Integer> getAddOneMapEnumInteger(
      Map<TestEnum, Integer> value) {
    Map<TestEnum, Integer> newMap = new HashMap<>();
    for (Map.Entry<TestEnum, Integer> testEnumStringEntry : value.entrySet()) {
      newMap.put(testEnumStringEntry.getKey(),
          testEnumStringEntry.getValue() + 1);
    }
    return newMap;
  }

  public VaadinConnectTestBean getBean(VaadinConnectTestBean value) {
    VaadinConnectTestBean newBean = new VaadinConnectTestBean();
    newBean.name = value.name + "-foo";
    newBean.address = value.address + "-foo";
    newBean.age = value.age + 1;
    newBean.isAdmin = !value.isAdmin;
    value.roles.add("User");
    newBean.roles = value.roles;
    newBean.testEnum = TestEnum.getTestEnum(value.testEnum.getValue() + 1);
    newBean.setCustomProperty(value.getCustomProperty() + "-foo");
    return newBean;
  }

  public enum TestEnum {
    FIRST(1), SECOND(2), THIRD(3);
    private final int value;

    TestEnum(int value) {
      this.value = value;
    }

    public int getValue() {
      return this.value;
    }

    public static TestEnum getTestEnum(int value) {
      for (TestEnum testEnum : TestEnum.values()) {
        if (testEnum.value == value) {
          return testEnum;
        }
      }
      return null;
    }
  }
}
