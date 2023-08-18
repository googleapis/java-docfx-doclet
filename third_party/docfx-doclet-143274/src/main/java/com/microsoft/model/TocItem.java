package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocItem {

  private String uid;
  private String name;
  private String status;
  private String heading;
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

  public TocItem(String heading) {
    this.heading = heading;
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

  public String getStatus() {
    return status;
  }

  public String getHeading() {
    return heading;
  }
}
