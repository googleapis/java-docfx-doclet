package com.microsoft.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.docfx.doclet.ApiVersion;
import org.junit.Test;

public class ApiVersionTest {

  @Test
  public void badFormat() {
    assertFalse(ApiVersion.parse("badFormat").isPresent());
    assertFalse(ApiVersion.parse("1p1").isPresent());
    assertFalse(ApiVersion.parse("v1p2p3").isPresent());
    assertFalse(ApiVersion.parse("v1a").isPresent());
    assertFalse(ApiVersion.parse("v1beta2p3").isPresent());
  }

  @Test
  public void goodFormat() {
    assertTrue(ApiVersion.parse("v99").isPresent());
    assertTrue(ApiVersion.parse("v1p2").isPresent());
    assertTrue(ApiVersion.parse("v2alpha").isPresent());
    assertTrue(ApiVersion.parse("v3beta").isPresent());
    assertTrue(ApiVersion.parse("v3beta1").isPresent());
    assertTrue(ApiVersion.parse("v3beta1").isPresent());
    assertTrue(ApiVersion.parse("v3p1beta2").isPresent());
  }

  @Test
  public void testAisGreaterThanB() {
    String[][] comparisons = {
      {"v3", "v2"},
      {"v1p1", "v1"},
      {"v1", "v1alpha"},
      {"v1", "v1beta"},
      {"v1beta", "v1alpha"},
      {"v1beta", "v1alpha2"},
      {"v1beta1", "v1alpha"},
      {"v1beta1", "v1beta"},
      {"v1beta2", "v1beta1"},
      {"v2p1alpha0", "v1p1alpha0"},
    };

    for (String[] testcase : comparisons) {
      ApiVersion a = parse(testcase[0]);
      ApiVersion b = parse(testcase[1]);
      assertThat(a).isGreaterThan(b);
    }
  }

  private ApiVersion parse(String s) {
    return ApiVersion.parse(s).orElseThrow(() -> new IllegalStateException("Unable to parse " + s));
  }

  @Test
  public void stability() {
    assertTrue(ApiVersion.parse("v12").orElseThrow().isStable());
    assertTrue(ApiVersion.parse("v1p3").orElseThrow().isStable());

    assertFalse(ApiVersion.parse("v1alpha").orElseThrow().isStable());
    assertFalse(ApiVersion.parse("v1p1beta1").orElseThrow().isStable());
  }
}
