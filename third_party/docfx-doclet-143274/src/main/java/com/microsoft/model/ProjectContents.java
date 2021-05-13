package com.microsoft.model;

import java.util.List;

//  wraps guides + tocItems with product name hierarchy
//  [name: project-name, [items: [...]]]
public class ProjectContents {
    private final String name;
    private final List<Object> items;

    public ProjectContents(String name, List<Object> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public List<Object> getItems() {
        return items;
    }
}