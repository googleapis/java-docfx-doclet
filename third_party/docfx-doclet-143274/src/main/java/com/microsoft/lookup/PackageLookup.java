package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.Status;
import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

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

    public String extractStatus(String name) {
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
        String javatype = element.getKind().name().toLowerCase().replaceAll("_","");
        if (javatype.equals("package")){
            return javatype;
        }
        return null;
    }
}
