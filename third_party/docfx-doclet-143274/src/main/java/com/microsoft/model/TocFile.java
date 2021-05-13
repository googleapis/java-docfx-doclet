package com.microsoft.model;

import com.microsoft.util.YamlUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TocFile extends ArrayList<TocItem> implements YmlFile {

    private final static String TOC_FILE_HEADER = "### YamlMime:TableOfContent\n";
    private final static String TOC_FILE_NAME = "toc.yml";
    private final String outputPath;

    public TocFile(String outputPath) {
        this.outputPath = outputPath;
    }

    public void addTocItem(TocItem packageTocItem) {
        add(packageTocItem);
    }

    protected void sortByUid() { Collections.sort(this, Comparator.comparing(TocItem::getUid)); }

    @Override
    public String getFileContent() {
        sortByUid();
        return TOC_FILE_HEADER + YamlUtil.objectToYamlString(this);
    }

    @Override
    public String getFileNameWithPath() {
        return outputPath + File.separator + TOC_FILE_NAME;
    }
}
