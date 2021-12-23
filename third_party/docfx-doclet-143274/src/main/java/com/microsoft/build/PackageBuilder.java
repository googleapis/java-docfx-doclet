/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.build;

import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;

import javax.lang.model.element.PackageElement;

import static com.microsoft.build.BuilderUtil.LANGS;
import static com.microsoft.build.BuilderUtil.populateItemFields;

class PackageBuilder {
    private final PackageLookup packageLookup;
    private final String outputPath;
    private final ReferenceBuilder referenceBuilder;

    PackageBuilder(PackageLookup packageLookup, String outputPath, ReferenceBuilder referenceBuilder) {
        this.packageLookup = packageLookup;
        this.outputPath = outputPath;
        this.referenceBuilder = referenceBuilder;
    }

    MetadataFile buildPackageMetadataFile(PackageElement packageElement) {
        String fileName = packageLookup.extractHref(packageElement);
        MetadataFile packageMetadataFile = new MetadataFile(outputPath, fileName);
        MetadataFileItem packageItem = new MetadataFileItem(LANGS, packageLookup.extractUid(packageElement));
        packageItem.setId(packageLookup.extractId(packageElement));
        referenceBuilder.addChildrenReferences(packageElement, packageItem.getChildren(), packageMetadataFile.getReferences());
        populateItemFields(packageItem, packageLookup, packageElement);
        packageMetadataFile.getItems().add(packageItem);
        return packageMetadataFile;
    }
}
