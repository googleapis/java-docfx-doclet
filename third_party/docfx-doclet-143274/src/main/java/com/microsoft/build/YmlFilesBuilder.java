package com.microsoft.build;

import static com.microsoft.build.BuilderUtil.populateUidValues;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.docfx.doclet.ApiVersion;
import com.google.docfx.doclet.RepoMetadata;
import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.lookup.PackageLookup.PackageGroup;
import com.microsoft.model.LibraryOverviewFile;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.model.TocTypeMap;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.PackageElement;
import jdk.javadoc.doclet.DocletEnvironment;

public class YmlFilesBuilder {
  private static final String OLDER_AND_PRERELEASE = "Older and prerelease packages";

  private final DocletEnvironment environment;
  private final String outputPath;
  private final ElementUtil elementUtil;
  private final PackageLookup packageLookup;
  private final String projectName;
  private final boolean disableChangelog;

  private final boolean disableLibraryOverview;

  private final String artifactVersion;

  private final String librariesBomVersion;

  private final String repoMetadataFilePath;
  private final ProjectBuilder projectBuilder;
  private final PackageBuilder packageBuilder;
  private final ClassBuilder classBuilder;
  private final ReferenceBuilder referenceBuilder;

  public YmlFilesBuilder(
      DocletEnvironment environment,
      String outputPath,
      String[] excludePackages,
      String[] excludeClasses,
      String projectName,
      boolean disableChangelog,
      boolean disableLibraryOverview,
      String artifactVersion,
      String librariesBomVersion,
      String repoMetadataFilePath) {
    this.environment = environment;
    this.outputPath = outputPath;
    this.artifactVersion = artifactVersion;
    this.librariesBomVersion = librariesBomVersion;
    this.repoMetadataFilePath = repoMetadataFilePath;
    this.elementUtil = new ElementUtil(excludePackages, excludeClasses);
    this.packageLookup = new PackageLookup(environment);
    this.projectName = projectName;
    this.disableChangelog = disableChangelog;
    this.disableLibraryOverview = disableLibraryOverview;
    this.projectBuilder = new ProjectBuilder(projectName);
    ClassLookup classLookup = new ClassLookup(environment, elementUtil);
    this.referenceBuilder = new ReferenceBuilder(environment, classLookup, elementUtil);
    this.packageBuilder = new PackageBuilder(packageLookup, outputPath, referenceBuilder);
    this.classBuilder =
        new ClassBuilder(
            elementUtil,
            classLookup,
            new ClassItemsLookup(environment, elementUtil),
            outputPath,
            referenceBuilder);
  }

  public boolean build() {
    Processor processor = new Processor();
    processor.repoMetadata = processor.repoMetadata.parseRepoMetadata(this.repoMetadataFilePath);
    processor.process();

    //  write to yaml files
    if (disableLibraryOverview) {
      FileUtil.dumpToFile(processor.projectMetadataFile);
    }
    processor.packageMetadataFiles.forEach(FileUtil::dumpToFile);
    processor.classMetadataFiles.forEach(FileUtil::dumpToFile);
    processor.packageOverviewFiles.forEach(FileUtil::dumpToFile);
    FileUtil.dumpToFile(processor.tocFile);

    // Generate new library overview page
    if (!disableLibraryOverview) {
      LibraryOverviewFile libraryOverviewFile =
          new LibraryOverviewFile(
              outputPath,
              "overview.md",
              artifactVersion,
              librariesBomVersion,
              repoMetadataFilePath,
              processor.recommendedApiVersion);
      FileUtil.dumpToFile(libraryOverviewFile);
    }
    return true;
  }

  @VisibleForTesting
  class Processor {
    //  table of contents
    final TocFile tocFile =
        new TocFile(outputPath, projectName, disableChangelog, disableLibraryOverview);
    //  overview page if not using new libraryOverview

    final MetadataFile projectMetadataFile = new MetadataFile(outputPath, "overview.yml");
    //  package summary pages
    private final List<MetadataFile> packageMetadataFiles = new ArrayList<>();

    private final List<PackageOverviewFile> packageOverviewFiles = new ArrayList<>();
    //  packages
    private final List<MetadataFileItem> packageItems = new ArrayList<>();
    //  class/enum/interface/etc. pages
    private final List<MetadataFile> classMetadataFiles = new ArrayList<>();

    private final List<PackageElement> allPackages =
        elementUtil.extractPackageElements(environment.getIncludedElements());

    private String recommendedApiVersion = "";

    private RepoMetadata repoMetadata = new RepoMetadata();

    @VisibleForTesting
    void process() {
      ImmutableListMultimap<PackageGroup, PackageElement> organizedPackagesWithoutStubs =
          packageLookup.organize(
              allPackages.stream()
                  .filter(pkg -> !packageLookup.isApiVersionStubPackage(pkg))
                  .collect(Collectors.toList()));

      // Get recommended ApiVersion for new Library Overview
      HashSet<ApiVersion> versions = new HashSet<>();
      for (PackageElement pkg : allPackages) {
        packageLookup.extractApiVersion(pkg).ifPresent(versions::add);
      }

      if (!versions.isEmpty()) {
        recommendedApiVersion = ApiVersion.getRecommended(versions).toString();
      }

      for (PackageElement element : organizedPackagesWithoutStubs.get(PackageGroup.VISIBLE)) {
        tocFile.addTocItem(buildPackage(element));
      }

      ImmutableList<PackageElement> olderPackages =
          organizedPackagesWithoutStubs.get(PackageGroup.OLDER_AND_PRERELEASE);
      if (!olderPackages.isEmpty()) {
        TocItem older = new TocItem(OLDER_AND_PRERELEASE, OLDER_AND_PRERELEASE, null);
        for (PackageElement element : olderPackages) {
          older.getItems().add(buildPackage(element));
        }
        tocFile.addTocItem(older);
      }

      for (MetadataFile packageFile : packageMetadataFiles) {
        packageItems.addAll(packageFile.getItems());
        String packageFileName = packageFile.getFileName();
        for (MetadataFile classFile : classMetadataFiles) {
          String classFileName = classFile.getFileName();
          if (packageFileName.equalsIgnoreCase(classFileName)) {
            packageFile.setFileName(packageFileName.replaceAll("\\.yml$", "(package).yml"));
            classFile.setFileName(classFileName.replaceAll("\\.yml$", "(class).yml"));
            break;
          }
        }
      }
      // build project summary page if disableLibraryOverview=true
      if (disableLibraryOverview) {
        projectBuilder.buildProjectMetadataFile(packageItems, projectMetadataFile);
      }

      // post-processing
      populateUidValues(packageMetadataFiles, classMetadataFiles);
      referenceBuilder.updateExternalReferences(classMetadataFiles);
    }

    private TocItem buildPackage(PackageElement element) {
      String packageUid = packageLookup.extractUid(element);
      String packageStatus = packageLookup.extractStatus(element);

      TocItem packageTocItem = new TocItem(packageUid, packageUid, packageStatus);

      // New package overview
      TocItem packageSummary = new TocItem(packageUid, "Package summary", packageUid + ".md", true);
      packageTocItem.getItems().add(packageSummary);
      packageOverviewFiles.add(
          packageBuilder.buildPackageOverviewFile(
              element, repoMetadata, artifactVersion, recommendedApiVersion));

      // build classes/interfaces/enums/exceptions/annotations
      TocTypeMap typeMap = new TocTypeMap();
      classBuilder.buildFilesForInnerClasses(element, typeMap, classMetadataFiles);
      packageTocItem.getItems().addAll(joinTocTypeItems(typeMap));

      // build stubs
      packageLookup
          .findStubPackages(element, allPackages)
          .forEach((PackageElement stub) -> packageTocItem.getItems().add(buildPackage(stub)));

      return packageTocItem;
    }
  }

  List<TocItem> joinTocTypeItems(TocTypeMap tocTypeMap) {
    return tocTypeMap.getTitleList().stream()
        .filter(kindTitle -> tocTypeMap.get(kindTitle.getElementKind()).size() > 0)
        .flatMap(
            kindTitle -> {
              tocTypeMap.get(kindTitle.getElementKind()).add(0, new TocItem(kindTitle.getTitle()));
              return tocTypeMap.get(kindTitle.getElementKind()).stream();
            })
        .collect(Collectors.toList());
  }
}
