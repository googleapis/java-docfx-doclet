package com.microsoft.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.docfx.doclet.ApiVersion;
import java.util.Collections;
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

  @Test
  public void stability() {
    assertTrue(parse("v12").isStable());
    assertTrue(parse("v1p3").isStable());

    assertFalse(parse("v1alpha").isStable());
    assertFalse(parse("v1p1beta1").isStable());
  }

  @Test
  public void equals() {
    assertThat(parse("v1")).isEqualTo(parse("v1p0"));
    assertThat(parse("v1p0")).isEqualTo(parse("v1"));
    assertThat(parse("v2p1beta")).isEqualTo(parse("v2p1beta0"));
  }

  private ApiVersion parse(String s) {
    return ApiVersion.parse(s).orElseThrow(() -> new IllegalStateException("Unable to parse " + s));
  }

  @Test
  public void testRecommendation_PrioritizesReleaseVersions() {
    ImmutableList<ApiVersion> versions =
        ImmutableList.of(parse("v101beta"), parse("v2p1"), parse("v1p14alpha15"), parse("v1"));

    ApiVersion recommended = ApiVersion.getRecommended(versions);
    assertThat(recommended).isEqualTo(parse("v2p1"));
  }

  @Test
  public void testRecommendation_ChoosesLatestPrerelease_WhenNoReleaseVersionsAvailable() {
    ImmutableList<ApiVersion> versions =
        ImmutableList.of(
            parse("v101beta"), parse("v1p14alpha15"), parse("v102alpha"), parse("v1p2beta3"));

    ApiVersion recommended = ApiVersion.getRecommended(versions);
    assertThat(recommended).isEqualTo(parse("v102alpha"));
  }

  @Test
  public void testRecommendation_SingleOption() {
    ImmutableList<ApiVersion> versions = ImmutableList.of(parse("v1p14alpha15"));

    ApiVersion recommended = ApiVersion.getRecommended(versions);
    assertThat(recommended).isEqualTo(parse("v1p14alpha15"));
  }

  @Test
  public void testRecommendation_doesNotAllowEmptyInput() {
    assertThrows(
        IllegalArgumentException.class, () -> ApiVersion.getRecommended(Collections.emptyList()));
  }

  @Test
  public void testToString() {
    assertThat(parse("v1").toString()).isEqualTo("v1");
    assertThat(parse("v1p0").toString()).isEqualTo("v1p0");
  }
}
