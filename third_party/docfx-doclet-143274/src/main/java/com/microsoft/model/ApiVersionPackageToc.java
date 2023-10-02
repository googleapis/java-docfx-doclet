package com.microsoft.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ApiVersionPackageToc {
  static final String CLIENTS = "Clients";
  static final String REQUESTS_AND_RESPONSES = "Requests and responses";
  static final String SETTINGS = "Settings";
  static final String ALL_OTHERS = "All other classes and interfaces";
  static final String BUILDERS = "Builders";
  static final String ENUMS = "Enums";
  static final String INTERFACES = "Interfaces";
  static final String MESSAGES = "Messages";
  static final String EXCEPTIONS = "Exceptions";
  static final String PAGING = "Paging";
  static final String RESOURCE_NAMES = "Resource names";
  static final String UNCATEGORIZED = "Other";

  private final LinkedHashMap<String, List<TocItem>> visibleCategories = new LinkedHashMap<>();
  private final LinkedHashMap<String, List<TocItem>> hiddenCategories = new LinkedHashMap<>();

  public ApiVersionPackageToc() {
    // Order here determines final organization order.
    visibleCategories.put(CLIENTS, new ArrayList<>());
    visibleCategories.put(REQUESTS_AND_RESPONSES, new ArrayList<>());
    visibleCategories.put(SETTINGS, new ArrayList<>());

    hiddenCategories.put(BUILDERS, new ArrayList<>());
    hiddenCategories.put(ENUMS, new ArrayList<>());
    hiddenCategories.put(EXCEPTIONS, new ArrayList<>());
    hiddenCategories.put(MESSAGES, new ArrayList<>());
    hiddenCategories.put(PAGING, new ArrayList<>());
    hiddenCategories.put(RESOURCE_NAMES, new ArrayList<>());
    hiddenCategories.put(INTERFACES, new ArrayList<>());
    hiddenCategories.put(UNCATEGORIZED, new ArrayList<>());
  }

  public void addClient(TocItem tocItem) {
    visibleCategories.get(CLIENTS).add(tocItem);
  }

  public void addRequestOrResponse(TocItem tocItem) {
    visibleCategories.get(REQUESTS_AND_RESPONSES).add(tocItem);
  }

  public void addSettings(TocItem tocItem) {
    visibleCategories.get(SETTINGS).add(tocItem);
  }

  public void addBuilder(TocItem tocItem) {
    hiddenCategories.get(BUILDERS).add(tocItem);
  }

  public void addEnum(TocItem tocItem) {
    hiddenCategories.get(ENUMS).add(tocItem);
  }

  public void addException(TocItem tocItem) {
    hiddenCategories.get(EXCEPTIONS).add(tocItem);
  }

  public void addInterface(TocItem tocItem) {
    hiddenCategories.get(INTERFACES).add(tocItem);
  }

  public void addMessage(TocItem tocItem) {
    hiddenCategories.get(MESSAGES).add(tocItem);
  }

  public void addUncategorized(TocItem tocItem) {
    hiddenCategories.get(UNCATEGORIZED).add(tocItem);
  }

  public void addPaging(TocItem tocItem) {
    hiddenCategories.get(PAGING).add(tocItem);
  }

  public void addResourceName(TocItem tocItem) {
    hiddenCategories.get(RESOURCE_NAMES).add(tocItem);
  }

  /** Build a list of TocItems for inclusion in the library's table of contents */
  public List<TocItem> toList() {
    List<TocItem> toc = new ArrayList<>();

    visibleCategories.forEach(
        (name, category) -> {
          if (!category.isEmpty()) {
            toc.add(createCategory(name, category));
          }
        });

    TocItem allOthers = new TocItem(ALL_OTHERS, ALL_OTHERS, null);
    hiddenCategories.forEach(
        (name, category) -> {
          if (!category.isEmpty()) {
            allOthers.getItems().add(createCategory(name, category));
          }
        });
    if (allOthers.getItems().size() > 0) {
      toc.add(allOthers);
    }

    return toc;
  }

  private TocItem createCategory(String name, List<TocItem> items) {
    TocItem category = new TocItem(name, name, null);
    items.sort(Comparator.comparing(TocItem::getName));
    category.getItems().addAll(items);
    return category;
  }
}
