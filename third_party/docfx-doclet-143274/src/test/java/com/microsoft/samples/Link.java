package com.microsoft.samples;

import java.util.Collection;

public class Link {

  /**
   * Gets the URI.
   *
   * <p>/** Gets the method.
   */
  private String method;

  /**
   * Initializes a new instance of the Link class.
   *
   * @param uri The URI.
   */
  /** Gets the link headers. */
  private Collection<KeyValuePair<String, String>> headers;

  public Link() {}

  public String getMethod() {
    return method;
  }

  public void setMethod(String value) {
    method = value;
  }

  public Collection<KeyValuePair<String, String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Collection<KeyValuePair<String, String>> value) {
    headers = value;
  }
}
