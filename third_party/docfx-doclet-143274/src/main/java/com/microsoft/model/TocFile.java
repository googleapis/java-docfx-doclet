package com.microsoft.model;

import com.microsoft.util.YamlUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TocFile extends ArrayList<TocItem> implements YmlFile {

    private final static String TOC_FILE_HEADER = "### YamlMime:TableOfContent\n";
    private final static String TOC_FILE_NAME = "toc.yml";
    private final String outputPath;
    private final String projectName;

    public TocFile(String outputPath, String projectName) {
        this.outputPath = outputPath;
        this.projectName = projectName;
    }

    public void addTocItem(TocItem packageTocItem) {
        add(packageTocItem);
    }

    protected void sortByUid() { Collections.sort(this, Comparator.comparing(TocItem::getUid)); }

    @Override
    public String getFileContent() {
        sortByUid();
        List<Object> tocContents = new TocContents(projectName, this).getContents();
        return TOC_FILE_HEADER + YamlUtil.objectToYamlString(tocContents);
    }

    @Override
    public String getFileNameWithPath() {
        return outputPath + File.separator + TOC_FILE_NAME;
    }
}
