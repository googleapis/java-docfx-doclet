package com.microsoft.lookup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.docfx.doclet.ApiVersion;
import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.Status;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.PackageElement;
import jdk.javadoc.doclet.DocletEnvironment;

public class PackageLookup extends BaseLookup<PackageElement> {

  public PackageLookup(DocletEnvironment environment) {
    super(environment);
  }

  @Override
  protected ExtendedMetadataFileItem buildMetadataFileItem(PackageElement packageElement) {
    String qName = String.valueOf(packageElement.getQualifiedName());
    String sName = String.valueOf(packageElement.getSimpleName());

    ExtendedMetadataFileItem result = new ExtendedMetadataFileItem(qName);
    result.setId(sName);
    result.setHref(qName + ".yml");
    result.setName(qName);
    result.setNameWithType(qName);
    result.setFullName(qName);
    result.setType(determineType(packageElement));
    result.setJavaType(extractJavaType(packageElement));
    result.setSummary(determineComment(packageElement));
    result.setContent(determinePackageContent(packageElement));
    return result;
  }

  public String extractStatus(PackageElement packageElement) {
    String name = String.valueOf(packageElement.getQualifiedName());
    if (name.contains(Status.ALPHA.toString())) {
      return Status.ALPHA.toString();
    }
    if (name.contains(Status.BETA.toString())) {
      return Status.BETA.toString();
    }
    return null;
  }

  String determinePackageContent(PackageElement packageElement) {
    return "package " + packageElement.getQualifiedName();
  }

  public String extractJavaType(PackageElement element) {
    String javaType = element.getKind().name().toLowerCase();
    if (javaType.equals("package")) {
      return javaType;
    }
    return null;
  }

  /** Compare PackageElements by their parsed ApiVersion */
  private final Comparator<PackageElement> byComparingApiVersion =
      Comparator.comparing(pkg -> extractApiVersion(pkg).orElse(ApiVersion.NONE));

  public enum PackageGroup {
    VISIBLE,
    OLDER_AND_PRERELEASE
  }

  /**
   * Organize packages into PackageGroups, making some VISIBLE the rest hidden under the
   * OLDER_AND_PRERELEASE category.
   */
  public ImmutableListMultimap<PackageGroup, PackageElement> organize(
      List<PackageElement> packages) {

    ListMultimap<PackageGroup, PackageElement> organized =
        MultimapBuilder.enumKeys(PackageGroup.class).arrayListValues().build();

    Multimap<String, PackageElement> packagesGroups = groupVersions(packages);
    ImmutableList<String> alphabetizedPackageGroups =
        packagesGroups.keySet().stream().sorted().collect(ImmutableList.toImmutableList());

    for (String name : alphabetizedPackageGroups) {
      Collection<PackageElement> versions = packagesGroups.get(name);

      // The recommended package of each group is made visible.
      PackageElement recommendedVersion = getRecommended(versions);
      organized.put(PackageGroup.VISIBLE, recommendedVersion);

      // All others are added to "Older and prerelease versions"
      versions.stream()
          .filter(version -> !version.equals(recommendedVersion))
          .sorted(byComparingApiVersion)
          .forEach(
              version -> {
                organized.put(PackageGroup.OLDER_AND_PRERELEASE, version);
              });
    }

    return ImmutableListMultimap.copyOf(organized);
  }

  /**
   * This 'grouping' logic combines all versioned packages together in a single `a.b.c.v#` group.
   *
   * <p>For example: a.b.v1 and a.b.v2 will be in the same group, but a.b and a.b.c will be in their
   * own groups.
   *
   * <p>When packages are grouped, only one package within the group will be VISIBLE and the rest
   * will be placed in the OLDER_AND_PRERELEASE category.
   */
  @VisibleForTesting
  Multimap<String, PackageElement> groupVersions(List<PackageElement> packages) {
    return Multimaps.index(
        packages,
        (pkg) -> {
          String name = String.valueOf(pkg.getQualifiedName());
          int lastPackageIndex = name.lastIndexOf('.');
          String leafPackage = name.substring(lastPackageIndex + 1);
          // For package a.b.c.d, the value of leafPackage is 'd'.

          boolean packageIsApiVersion = ApiVersion.parse(leafPackage).isPresent();
          if (packageIsApiVersion) {
            String packageWithoutVersion = name.substring(0, lastPackageIndex);
            // For package a.b.c.v1, the value of packageWithoutVersion is a.b.c
            return packageWithoutVersion + ".v#"; // Use "v#" package to group all versions
          }
          // Using 'name' ensures this package is placed in a group of size 1.
          return name;
        });
  }

  public PackageElement getRecommended(Collection<PackageElement> packages) {
    Preconditions.checkArgument(!packages.isEmpty(), "Packages must not be empty.");

    if (packages.size() == 1) {
      return packages.iterator().next();
    }

    Map<ApiVersion, PackageElement> versions = new HashMap<>();
    for (PackageElement pkg : packages) {
      extractApiVersion(pkg).ifPresent(v -> versions.put(v, pkg));
    }

    ApiVersion recommended = ApiVersion.getRecommended(versions.keySet());
    return versions.get(recommended);
  }

  public Optional<ApiVersion> extractApiVersion(PackageElement pkg) {
    String name = String.valueOf(pkg.getQualifiedName());
    int lastPackageIndex = name.lastIndexOf('.');
    String leafPackage = name.substring(lastPackageIndex + 1);
    return ApiVersion.parse(leafPackage);
  }
}
