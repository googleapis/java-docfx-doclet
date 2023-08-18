package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

@JsonPropertyOrder({"uid", "name", "fullName", "isExternal", "href"})
public class SpecViewModel {

  private String uid;
  private String name;
  private String fullName;
  private boolean isExternal;
  private String href;

  public SpecViewModel(String uid, String fullName) {
    this.uid = uid;
    this.name = getShortName(fullName);
    this.fullName = fullName;
  }

  public String getUid() {
    return uid;
  }

  public String getName() {
    return name;
  }

  public String getFullName() {
    return fullName;
  }

  String getShortName(String fullName) {

    StringBuilder singleValue = new StringBuilder();
    Optional.ofNullable(fullName)
        .ifPresent(
            Param -> {
              List<String> strList = new ArrayList<>();
              strList = Arrays.asList(StringUtils.split(Param, "."));
              Collections.reverse(strList);
              singleValue.append(strList.get(0));
            });
    return singleValue.toString();
  }

  public void setIsExternal(boolean isExternal) {
    this.isExternal = isExternal;
  }

  public boolean getIsExternal() {
    return isExternal;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getHref() {
    return href;
  }
}
