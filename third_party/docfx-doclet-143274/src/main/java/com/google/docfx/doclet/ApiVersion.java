package com.google.docfx.doclet;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class ApiVersion implements Comparable<ApiVersion> {
  public static ApiVersion NONE = new ApiVersion(0, 0, null, 0);

  private static final Pattern VALID_VERSION_REGEX =
      Pattern.compile("^v(\\d+)p?(\\d+)?(alpha|beta)?(\\d+)?");

  /**
   * Creates an ApiVersion instance, if the given input matches the correct format.
   *
   * <p>Supported Format:
   *
   * <pre>
   * v1p2beta3
   *  │└┤└──┤│
   *  │ │   ││
   *  │ │   │└─── Optional: Prerelease major version
   *  │ │   └──── Optional: Stability level. See <a href="https://google.aip.dev/181">AIP 181</a>
   *  │ └──────── Optional: Minor version
   *  └────────── Required: Major version
   * </pre>
   *
   * @return Optional.empty() if the given input doesn't match the version pattern
   */
  public static Optional<ApiVersion> parse(@Nullable String input) {
    if (input != null) {
      Matcher matcher = VALID_VERSION_REGEX.matcher(input);
      if (matcher.matches()) {
        return Optional.of(
            new ApiVersion(
                safeParseInt(matcher.group(1)),
                safeParseInt(matcher.group(2)),
                matcher.group(3),
                safeParseInt(matcher.group(4))));
      }
    }
    return Optional.empty();
  }

  private static int safeParseInt(@Nullable String input) {
    if (input == null) {
      return 0;
    }
    return Integer.parseInt(input);
  }

  private final int major;
  private final int minor;
  private final String stability;
  private final int prerelease;

  private ApiVersion(int major, int minor, String stability, int prerelease) {
    this.major = major;
    this.minor = minor;
    this.stability = stability;
    this.prerelease = prerelease;
  }

  public boolean isStable() {
    return stability == null;
  }

  @Override
  public int compareTo(ApiVersion other) {
    if (major != other.major) {
      return major - other.major;
    }
    if (minor != other.minor) {
      return minor - other.minor;
    }
    if (!Objects.equals(stability, other.stability)) {
      if (stability == null) {
        return 1; // Other is a prerelease version, but this is not.
      }
      if (other.stability == null) {
        return -1; // This is a prerelease version, but the other is not.
      }
      return stability.compareTo(other.stability); // Based on alphabetical order
    }
    return prerelease - other.prerelease;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ApiVersion.class)
        .add("major", major)
        .add("minor", minor)
        .add("stability", stability)
        .add("prerelease", prerelease)
        .toString();
  }
}
