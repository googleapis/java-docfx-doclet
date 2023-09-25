package com.microsoft.build;

import static com.microsoft.build.BuilderUtil.populateUidValues;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.lookup.PackageLookup.PackageGroup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.model.TocTypeMap;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import java.util.ArrayList;
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
      boolean disableChangelog) {
    this.environment = environment;
    this.outputPath = outputPath;
    this.elementUtil = new ElementUtil(excludePackages, excludeClasses);
    this.packageLookup = new PackageLookup(environment);
    this.projectName = projectName;
    this.disableChangelog = disableChangelog;
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
    processor.process();

    //  write to yaml files
    FileUtil.dumpToFile(processor.projectMetadataFile);
    processor.packageMetadataFiles.forEach(FileUtil::dumpToFile);
    processor.classMetadataFiles.forEach(FileUtil::dumpToFile);
    FileUtil.dumpToFile(processor.tocFile);

    return true;
  }

  @VisibleForTesting
  class Processor {
    //  table of contents
    private final TocFile tocFile = new TocFile(outputPath, projectName, disableChangelog);
    //  overview page
    private final MetadataFile projectMetadataFile = new MetadataFile(outputPath, "overview.yml");
    //  package summary pages
    private final List<MetadataFile> packageMetadataFiles = new ArrayList<>();
    //  packages
    private final List<MetadataFileItem> packageItems = new ArrayList<>();
    //  class/enum/interface/etc. pages
    private final List<MetadataFile> classMetadataFiles = new ArrayList<>();

    private final List<PackageElement> allPackages =
        elementUtil.extractPackageElements(environment.getIncludedElements());

    @VisibleForTesting
    void process() {
      ImmutableListMultimap<PackageGroup, PackageElement> organizedPackagesWithoutStubs =
          packageLookup.organize(
              allPackages.stream()
                  .filter(pkg -> !packageLookup.isApiVersionStubPackage(pkg))
                  .collect(Collectors.toList()));

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
      // build project summary page
      projectBuilder.buildProjectMetadataFile(packageItems, projectMetadataFile);

      // post-processing
      populateUidValues(packageMetadataFiles, classMetadataFiles);
      referenceBuilder.updateExternalReferences(classMetadataFiles);
    }

    private TocItem buildPackage(PackageElement element) {
      String packageUid = packageLookup.extractUid(element);
      String packageStatus = packageLookup.extractStatus(element);

      TocItem packageTocItem = new TocItem(packageUid, packageUid, packageStatus);
      packageTocItem.getItems().add(new TocItem(packageUid, "Package summary"));

      //  build package summary
      packageMetadataFiles.add(packageBuilder.buildPackageMetadataFile(element));

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
