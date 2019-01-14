package com.vaadin.frontend.server;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.vaadin.frontend.server.EnableVaadinFrontendServer;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test is inspired in the pattern used in
 * VaadinConnectOauthConfigurerTest.
 */
@RunWith(Parameterized.class)
public class VaadinFrontendServerTest {

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
        { "Spring should forward / location to index.html",
          null,
          Arrays.asList(ConfigureContext.class, RootTest.class) },
        { "Spring should serve static files",
          null,
          Arrays.asList(ConfigureContext.class, AssetFoundTest.class) },
        { "Spring should serve index.html location if exists",
          null,
          Arrays.asList(ConfigureContext.class, IndexFoundTest.class) },
        { "Spring should fail if an .html resource does not exist",
          null,
          Arrays.asList(ConfigureContext.class, HtmlNotFoundTest.class) },
        { "Spring should fail if an asset resource does not exist",
          null,
          Arrays.asList(ConfigureContext.class, EnableVaadinFrontend.class, AssetNotFoundTest.class) },
        { "Enabling frontend-server should serve static files",
          null,
          Arrays.asList(ConfigureContext.class, EnableVaadinFrontend.class, AssetFoundTest.class) },
        { "Enabling frontend-server should serve index.html location if exists",
          null,
          Arrays.asList(ConfigureContext.class, EnableVaadinFrontend.class, IndexFoundTest.class) },
        { "Enabling frontend-server should forward to / if .html is not found",
          null,
          Arrays.asList(ConfigureContext.class, EnableVaadinFrontend.class, HtmlForwardTest.class) },
        { "Enabling frontend-server should forward to / if a route is not found",
          null,
          Arrays.asList(ConfigureContext.class, EnableVaadinFrontend.class, RouteForwardTest.class) },
        { "Enabling frontend-server should fail if asset not found",
          null,
          Arrays.asList(ConfigureContext.class, EnableVaadinFrontend.class, AssetNotFoundTest.class) },
      }); // @formatter:on
  }

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private AnnotationConfigWebApplicationContext context;

  private List<Class<?>> resources;

  @FunctionalInterface
  public interface TestRunner {
    void run(AnnotationConfigWebApplicationContext ctx) throws Exception;
  }

  /**
   * Called as many times as items are returned in the
   * {@link VaadinFrontendServerTest#parameters()} method.
   * 
   * @param testMessage
   *          text shown when running the test
   * @param exception
   *          if not null it expects this exception to be thrown
   * @param resources
   *          classes to be loaded by spring
   */
  public VaadinFrontendServerTest(String testMessage,
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
  @Import(WebMvcAutoConfiguration.class)
  protected static class ConfigureContext {
  }

  @Configuration
  @EnableVaadinFrontendServer
  protected static class EnableVaadinFrontend {
  }

  @Configuration
  protected static class RootTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      MockHttpServletResponse response = getResource(context, "/")
          .andExpect(status().isOk()).andReturn().getResponse();
      assertEquals("index.html", response.getForwardedUrl());
    }
  }

  @Configuration
  protected static class IndexFoundTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      MockHttpServletResponse response = getResource(context, "/index.html")
          .andExpect(status().isOk()).andReturn().getResponse();
      assertEquals("INDEX-CONTENT", response.getContentAsString().trim());
    }
  }

  @Configuration
  protected static class HtmlNotFoundTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getResource(context, "/foo.html").andExpect(status().is(404));
    }
  }

  @Configuration
  protected static class AssetNotFoundTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      getResource(context, "/foo.css").andExpect(status().is(404));
    }
  }

  @Configuration
  protected static class AssetFoundTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      MockHttpServletResponse response = getResource(context, "/testfile.css")
          .andExpect(status().isOk()).andReturn().getResponse();
      assertEquals("CSS-CONTENT", response.getContentAsString().trim());
    }
  }


  @Configuration
  protected static class HtmlForwardTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      MockHttpServletResponse response = getResource(context, "/foo.html")
          .andExpect(status().isOk()).andReturn().getResponse();

      assertEquals("/", response.getForwardedUrl());
    }
  }

  @Configuration
  protected static class RouteForwardTest implements TestRunner {
    @Override
    public void run(AnnotationConfigWebApplicationContext context)
        throws Exception {
      MockHttpServletResponse response = getResource(context, "/foo/bar")
          .andExpect(status().isOk()).andReturn().getResponse();

      assertEquals("/", response.getForwardedUrl());
    }
  }

  private static ResultActions getResource(
      AnnotationConfigWebApplicationContext webContext, String resource)
      throws Exception {
    return MockMvcBuilders.webAppContextSetup(webContext).build()
        .perform(get(resource).header(HttpHeaders.ACCEPT, "*/*"));
  }
}
