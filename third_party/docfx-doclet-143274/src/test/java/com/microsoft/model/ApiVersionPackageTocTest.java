package com.microsoft.model;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import org.junit.Test;

public class ApiVersionPackageTocTest {
  private static final TocItem TOC_ITEM = new TocItem("a", "b", "");

  private final ApiVersionPackageToc toc = new ApiVersionPackageToc();

  @Test
  public void testUncategorized() {
    toc.addUncategorized(TOC_ITEM);
    List<TocItem> tocItems = toc.toList();

    assertThat(tocItems).hasSize(1);

    TocItem allOthers = tocItems.get(0);
    assertThat(allOthers.getName()).isEqualTo(ApiVersionPackageToc.ALL_OTHERS);
    assertThat(allOthers.getItems()).hasSize(1);

    TocItem uncategorized = allOthers.getItems().get(0);
    assertThat(uncategorized.getName()).isEqualTo(ApiVersionPackageToc.UNCATEGORIZED);
    assertThat(uncategorized.getItems()).hasSize(1);
    assertThat(uncategorized.getItems().get(0)).isEqualTo(TOC_ITEM);
  }

  @Test
  public void testClients() {
    toc.addClient(TOC_ITEM);
    List<TocItem> tocItems = toc.toList();

    assertThat(tocItems).hasSize(1);

    TocItem clients = tocItems.get(0);
    assertThat(clients.getName()).isEqualTo(ApiVersionPackageToc.CLIENTS);
    assertThat(clients.getItems()).hasSize(1);
    assertThat(clients.getItems().get(0)).isEqualTo(TOC_ITEM);
  }


  @Test
  public void testVisibleAndHidden() {
    toc.addRequestOrResponse(TOC_ITEM);
    toc.addSettings(new TocItem("TestSettings", "TestSettings", ""));
    toc.addInterface(new TocItem("iTest", "iTest", ""));

    List<TocItem> tocItems = toc.toList();

    assertThat(tocItems).hasSize(3);

    TocItem reqsAndResponses = tocItems.get(0);
    assertThat(reqsAndResponses.getName()).isEqualTo(ApiVersionPackageToc.REQUESTS_AND_RESPONSES);
    assertThat(reqsAndResponses.getItems()).hasSize(1);
    assertThat(reqsAndResponses.getItems().get(0)).isEqualTo(TOC_ITEM);

    TocItem settings = tocItems.get(1);
    assertThat(settings.getName()).isEqualTo(ApiVersionPackageToc.SETTINGS);
    assertThat(settings.getItems()).hasSize(1);
    assertThat(settings.getItems().get(0).getName()).isEqualTo("TestSettings");

    TocItem allOthers = tocItems.get(2);
    assertThat(allOthers.getName()).isEqualTo(ApiVersionPackageToc.ALL_OTHERS);
    assertThat(allOthers.getItems()).hasSize(1);
    TocItem uncategorized = allOthers.getItems().get(0);
    assertThat(uncategorized.getName()).isEqualTo(ApiVersionPackageToc.INTERFACES);
    assertThat(uncategorized.getItems()).hasSize(1);
    assertThat(uncategorized.getItems().get(0).getName()).isEqualTo("iTest");
  }
}
