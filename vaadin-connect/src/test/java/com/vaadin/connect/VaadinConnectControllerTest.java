package com.vaadin.connect;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VaadinConnectControllerTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void whenContextHasNoBeanData_exceptionIsThrown() {
    String beanName = "test";

    ApplicationContext contextMock = mock(ApplicationContext.class);
    when(contextMock.getType(beanName)).thenReturn(null);

    when(contextMock.getBeansWithAnnotation(VaadinService.class))
        .thenReturn(Collections.singletonMap(beanName, null));

    exception.expect(IllegalStateException.class);
    exception.expectMessage(beanName);
    new VaadinConnectController(null, contextMock);
  }
}
