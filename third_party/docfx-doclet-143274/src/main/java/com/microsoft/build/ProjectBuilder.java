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

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.build.BuilderUtil.LANGS;

class ProjectBuilder {
    private final String projectName;

    ProjectBuilder(String projectName) {
        this.projectName = projectName;
    }

    void buildProjectMetadataFile(List<MetadataFileItem> packageItems, MetadataFile projectMetadataFile) {
        MetadataFileItem projectItem = new MetadataFileItem(LANGS, projectName);
        projectItem.setNameWithType(projectName);
        projectItem.setFullName(projectName);
        projectItem.setType("Namespace");
        projectItem.setJavaType("overview");

        List<String> children = new ArrayList<>();
        List<MetadataFileItem> references = new ArrayList<>();
        packageItems.stream().forEach(i -> {
            children.add(i.getUid());
            MetadataFileItem refItem = new MetadataFileItem(i.getUid());
            refItem.setName(i.getName());
            refItem.setNameWithType(i.getNameWithType());
            refItem.setFullName(i.getFullName());
            references.add(refItem);
        });

        projectItem.getChildren().addAll(children);
        projectMetadataFile.getReferences().addAll(references);
        projectMetadataFile.getItems().add(projectItem);
    }
}
