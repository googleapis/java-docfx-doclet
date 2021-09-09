package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocItem {

    private final String uid;
    private final String name;
    private String status;
    private List<TocItem> items = new ArrayList<>();

    public TocItem(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public TocItem(String uid, String name, String status) {
        this.uid = uid;
        this.name = name;
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public List<TocItem> getItems() {
        return items;
    }

    public String getStatus() { return status; }
}
