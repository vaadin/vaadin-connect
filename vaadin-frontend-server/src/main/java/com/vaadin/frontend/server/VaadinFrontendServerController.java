package com.vaadin.frontend.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

/**
 * The controller for serving the application resources and HTML entrypoints
 * for any request path.
 */
@Controller
public class VaadinFrontendServerController {
  
  private @Autowired ServletContext servletContext;

  /**
   * The request handler for static resources and HTML files. Mapped to all GET
   * requests. If the request path corresponds to a static resource, e. g.,
   * "GET /index.js", serves the resource. Otherwise, when the corresponding
   * resource is not found, serves the content of the "index.html".
   * @param request the request to handle
   * @return response with resource or "index.html" content
   */
  @GetMapping
  public ResponseEntity<InputStreamResource> serveResourceOrEntrypoint(WebRequest request) {
    String path = (String) request.getAttribute(
        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        RequestAttributes.SCOPE_REQUEST
    );
    InputStream inputStream = servletContext.getResourceAsStream(path);
    if (inputStream == null) {
      // Resource not found, use "index.html"
      path = "index.html";
      inputStream = servletContext.getResourceAsStream(path);
    }
    // Determine the content type
    String mimeType = servletContext.getMimeType(path);
    if (mimeType == null) {
      // Unknown content type (.map sourcemap?), default to safe octet stream
      mimeType = "application/octet-stream";
    }
    InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(mimeType))
        .body(inputStreamResource);
  }

}
