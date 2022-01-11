package com.microsoft.build;

import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.model.TocTypeMap;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.PackageElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.build.BuilderUtil.populateUidValues;

public class YmlFilesBuilder {
    private DocletEnvironment environment;
    private String outputPath;
    private ElementUtil elementUtil;
    private PackageLookup packageLookup;
    private String projectName;
    private ProjectBuilder projectBuilder;
    private PackageBuilder packageBuilder;
    private ClassBuilder classBuilder;
    private ReferenceBuilder referenceBuilder;

    public YmlFilesBuilder(DocletEnvironment environment, String outputPath,
                           String[] excludePackages, String[] excludeClasses, String projectName) {
        this.environment = environment;
        this.outputPath = outputPath;
        this.elementUtil = new ElementUtil(excludePackages, excludeClasses);
        this.packageLookup = new PackageLookup(environment);
        this.projectName = projectName;
        this.projectBuilder = new ProjectBuilder(projectName);
        ClassLookup classLookup = new ClassLookup(environment);
        this.referenceBuilder = new ReferenceBuilder(environment, classLookup, elementUtil);
        this.packageBuilder = new PackageBuilder(packageLookup, outputPath, referenceBuilder);
        this.classBuilder = new ClassBuilder(elementUtil, classLookup, new ClassItemsLookup(environment), outputPath, referenceBuilder);
    }

    public boolean build() {
        //  table of contents
        TocFile tocFile = new TocFile(outputPath, projectName);
        //  overview page
        MetadataFile projectMetadataFile = new MetadataFile(outputPath, "index.yml");
        //  package summary pages
        List<MetadataFile> packageMetadataFiles = new ArrayList<>();
        //  packages
        List<MetadataFileItem> packageItems = new ArrayList<>();
        //  class/enum/interface/etc. pages
        List<MetadataFile> classMetadataFiles = new ArrayList<>();

        for (PackageElement packageElement :
                elementUtil.extractPackageElements(environment.getIncludedElements())) {
            String packageUid = packageLookup.extractUid(packageElement);
            String packageStatus = packageLookup.extractStatus(packageElement);
            TocItem packageTocItem = new TocItem(packageUid, packageUid, packageStatus);
            //  build package summary
            packageMetadataFiles.add(packageBuilder.buildPackageMetadataFile(packageElement));
            // add package summary to toc
            packageTocItem.getItems().add(new TocItem(packageUid, "Package summary"));
            tocFile.addTocItem(packageTocItem);

            // build classes/interfaces/enums/exceptions/annotations
            TocTypeMap typeMap = new TocTypeMap();
            classBuilder.buildFilesForInnerClasses(packageElement, typeMap, classMetadataFiles);
            packageTocItem.getItems().addAll(joinTocTypeItems(typeMap));
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

        //  write to yaml files
        FileUtil.dumpToFile(projectMetadataFile);
        packageMetadataFiles.forEach(FileUtil::dumpToFile);
        classMetadataFiles.forEach(FileUtil::dumpToFile);
        FileUtil.dumpToFile(tocFile);

        return true;
    }

    List<TocItem> joinTocTypeItems(TocTypeMap tocTypeMap) {
        return tocTypeMap.getTitleList().stream()
                .filter(kindTitle -> tocTypeMap.get(kindTitle.getElementKind()).size() > 0)
                .flatMap(kindTitle -> {
                    tocTypeMap.get(kindTitle.getElementKind()).add(0, new TocItem(kindTitle.getTitle()));
                    return tocTypeMap.get(kindTitle.getElementKind()).stream();
                }).collect(Collectors.toList());
    }
}
