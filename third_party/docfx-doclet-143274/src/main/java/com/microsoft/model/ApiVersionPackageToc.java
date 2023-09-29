package com.microsoft.model;

import java.util.ArrayList;
import java.util.Comparator;
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

  private final List<TocItem> clients = new ArrayList<>();
  private final List<TocItem> requestsAndResponses = new ArrayList<>();
  private final List<TocItem> settings = new ArrayList<>();
  private final List<TocItem> builders = new ArrayList<>();
  private final List<TocItem> enums = new ArrayList<>();
  private final List<TocItem> exceptions = new ArrayList<>();
  private final List<TocItem> interfaces = new ArrayList<>();
  private final List<TocItem> messages = new ArrayList<>();
  private final List<TocItem> paging = new ArrayList<>();
  private final List<TocItem> resourceNames = new ArrayList<>();
  private final List<TocItem> uncategorized = new ArrayList<>();

  public void addClient(TocItem tocItem) {
    clients.add(tocItem);
  }

  public void addRequestOrResponse(TocItem tocItem) {
    requestsAndResponses.add(tocItem);
  }

  public void addSettings(TocItem tocItem) {
    settings.add(tocItem);
  }

  public void addBuilder(TocItem tocItem) {
    builders.add(tocItem);
  }

  public void addEnum(TocItem tocItem) {
    enums.add(tocItem);
  }

  public void addException(TocItem tocItem) {
    exceptions.add(tocItem);
  }

  public void addInterface(TocItem tocItem) {
    interfaces.add(tocItem);
  }

  public void addMessage(TocItem tocItem) {
    messages.add(tocItem);
  }

  public void addUncategorized(TocItem tocItem) {
    uncategorized.add(tocItem);
  }

  public void addPaging(TocItem tocItem) {
    paging.add(tocItem);
  }

  public void addResourceName(TocItem tocItem) {
    resourceNames.add(tocItem);
  }

  /** Build a list of TocItems for inclusion in the library's table of contents */
  public List<TocItem> toList() {
    List<TocItem> toc = new ArrayList<>();
    if (!clients.isEmpty()) {
      toc.add(createCategory(CLIENTS, clients));
    }
    if (!requestsAndResponses.isEmpty()) {
      toc.add(createCategory(REQUESTS_AND_RESPONSES, requestsAndResponses));
    }
    if (!settings.isEmpty()) {
      toc.add(createCategory(SETTINGS, settings));
    }
    if (!builders.isEmpty()
        || !enums.isEmpty()
        || !exceptions.isEmpty()
        || !interfaces.isEmpty()
        || !messages.isEmpty()
        || !paging.isEmpty()
        || !resourceNames.isEmpty()
        || !uncategorized.isEmpty()) {
      TocItem allOthers = new TocItem(ALL_OTHERS, ALL_OTHERS, null);
      if (!builders.isEmpty()) {
        allOthers.getItems().add(createCategory(BUILDERS, builders));
      }
      if (!enums.isEmpty()) {
        allOthers.getItems().add(createCategory(ENUMS, enums));
      }
      if (!exceptions.isEmpty()) {
        allOthers.getItems().add(createCategory(EXCEPTIONS, exceptions));
      }
      if (!messages.isEmpty()) {
        allOthers.getItems().add(createCategory(MESSAGES, messages));
      }
      if (!paging.isEmpty()) {
        allOthers.getItems().add(createCategory(PAGING, paging));
      }
      if (!resourceNames.isEmpty()) {
        allOthers.getItems().add(createCategory(RESOURCE_NAMES, resourceNames));
      }
      if (!interfaces.isEmpty()) {
        allOthers.getItems().add(createCategory(INTERFACES, interfaces));
      }
      if (!uncategorized.isEmpty()) {
        allOthers.getItems().add(createCategory(UNCATEGORIZED, uncategorized));
      }
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
