package com.google.docfx.doclet;

// This parses .repo-metadata.json files to create a new library overview

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class RepoMetadata {

  private static String apiShortName;
  private static String namePretty;
  private static String productDocumentationUri;
  private static String apiDescription;
  private static String clientDocumentationUri;
  private static String repo;
  private static String repoShort;
  private static String distributionName;

  private static String artifactId;
  private static String apiId;

  public static String getNamePretty() {
    return namePretty;
  }

  public static void setNamePretty(String namePretty) {
    RepoMetadata.namePretty = namePretty;
  }
  public String getApiShortName(){
    return apiShortName;
  }
  public static void setApiShortName(String apiShortName) {
    RepoMetadata.apiShortName = apiShortName;
  }

  public static String getProductDocumentationUri() {
    return productDocumentationUri;
  }

  public static void setProductDocumentationUri(String productDocumentationUri) {
    RepoMetadata.productDocumentationUri = productDocumentationUri;
  }

  public static String getApiDescription() {
    return apiDescription;
  }

  public static void setApiDescription(String apiDescription) {
    RepoMetadata.apiDescription = apiDescription;
  }

  public static String getClientDocumentationUri() {
    return clientDocumentationUri;
  }

  public static void setClientDocumentationUri(String clientDocumentationUri) {
    RepoMetadata.clientDocumentationUri = clientDocumentationUri;
  }

  public static String getRepo() {
    return repo;
  }

  public static void setRepo(String repo) {
    RepoMetadata.repo = repo;
  }

  public static String getRepoShort() {
    return repoShort;
  }

  public static void setRepoShort(String repoShort) {
    RepoMetadata.repoShort = repoShort;
  }

  public static String getDistributionName() {
    return distributionName;
  }

  public static void setDistributionName(String distributionName) {
    RepoMetadata.distributionName = distributionName;
  }

  public static String getApiId() {
    return apiId;
  }

  public static void setApiId(String apiId) {
    RepoMetadata.apiId = apiId;
  }

  // artifactId is parsed from distributionName
  public static String getArtifactId() {
    String substrings[] = distributionName.split(":");
    return substrings[substrings.length -1];
  }

  // GithubLink is created from repo and repoShort
  public static String getGithubLink() {
    String githubRootUri = "https://github.com/";
    String githubLink = githubRootUri + repo;
    if (Objects.equals(repo, "googleapis/google-cloud-java") || Objects.equals(repo, "googleapis/sdk-platform-java")){
      githubLink = githubLink + "/tree/main/" + repoShort;
    }
    return githubLink;
  }

  // MavenLink is created from distributionName
  public static String getMavenLink() {
    String mavenRootUri = "https://central.sonatype.com/artifact/";
    String substrings[] = distributionName.split(":");
    String groupName = substrings[0];
    String artifactId = substrings[substrings.length -1];
    String mavenLink = mavenRootUri + groupName + "/" + artifactId;
    return mavenLink;
  }
  public static RepoMetadata parseRepoMetadata(String fileName) {
    RepoMetadata repoMetadata = new RepoMetadata();
    Gson gson = new Gson();
    try(BufferedReader reader = new BufferedReader(new FileReader(fileName))){
      // convert .repo-metadata.json to a Map of values
      Map<?, ?> map = gson.fromJson(reader, Map.class);
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        switch (entry.getKey().toString()){
          case "api_shortname":
            repoMetadata.setApiShortName(entry.getValue().toString());
            break;
          case "name_pretty":
            repoMetadata.setNamePretty(entry.getValue().toString());
            break;
          case "product_documentation":
            repoMetadata.setProductDocumentationUri(entry.getValue().toString());
            break;
          case "api_description":
            repoMetadata.setApiDescription(entry.getValue().toString());
            break;
          case "client_documentation":
            repoMetadata.setClientDocumentationUri(entry.getValue().toString());
            break;
          case "repo":
            repoMetadata.setRepo(entry.getValue().toString());
            break;
          case "repo_short":
            repoMetadata.setRepoShort(entry.getValue().toString());
            break;
          case "distribution_name":
            repoMetadata.setDistributionName(entry.getValue().toString());
            break;
          case "api_id":
            repoMetadata.setApiId(entry.getValue().toString());
            break;
        }
      }
    } catch (IOException e) {
    }
    return repoMetadata;
  }
}