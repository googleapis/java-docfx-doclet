package com.google.docfx.doclet;

// This parses .repo-metadata.json files to create a new library overview

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class RepoMetadata {

  @SerializedName("api_shortname")
  private String apiShortName;

  @SerializedName("name_pretty")
  private String namePretty;

  @SerializedName("product_documentation")
  private String productDocumentationUri;

  @SerializedName("api_description")
  private String apiDescription;

  @SerializedName("client_documentation")
  private String clientDocumentationUri;

  @SerializedName("rest_documentation")
  private String restDocumentationUri;

  @SerializedName("rpc_documentation")
  private String rpcDocumentationUri;

  @SerializedName("repo")
  private String repo;

  @SerializedName("repo_short")
  private String repoShort;

  @SerializedName("distribution_name")
  private String distributionName;

  @SerializedName("api_id")
  private String apiId;

  private String artifactId;

  public String getNamePretty() {
    return this.namePretty;
  }

  public void setNamePretty(String namePretty) {
    this.namePretty = namePretty;
  }

  public String getApiShortName() {
    return this.apiShortName;
  }

  public void setApiShortName(String apiShortName) {
    this.apiShortName = apiShortName;
  }

  public String getProductDocumentationUri() {
    return this.productDocumentationUri;
  }

  public Optional<String> getRestDocumentationUri() {
    return Optional.ofNullable(this.restDocumentationUri);
  }

  public Optional<String> getRpcDocumentationUri() {
    return Optional.ofNullable(this.rpcDocumentationUri);
  }

  public void setProductDocumentationUri(String productDocumentationUri) {
    this.productDocumentationUri = productDocumentationUri;
  }

  public String getApiDescription() {
    return this.apiDescription;
  }

  public void setApiDescription(String apiDescription) {
    this.apiDescription = apiDescription;
  }

  public String getClientDocumentationUri() {
    return this.clientDocumentationUri;
  }

  public void setClientDocumentationUri(String clientDocumentationUri) {
    this.clientDocumentationUri = clientDocumentationUri;
  }

  public String getRepo() {
    return this.repo;
  }

  public void setRepo(String repo) {
    this.repo = repo;
  }

  public String getRepoShort() {
    return this.repoShort;
  }

  public void setRepoShort(String repoShort) {
    this.repoShort = repoShort;
  }

  public String getDistributionName() {
    return this.distributionName;
  }

  public void setDistributionName(String distributionName) {
    this.distributionName = distributionName;
  }

  public String getApiId() {
    return this.apiId;
  }

  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  // artifactId is parsed from distributionName
  public String getArtifactId() {
    String substrings[] = this.distributionName.split(":");
    return substrings[substrings.length - 1];
  }

  // GithubLink is created from repo and repoShort
  public String getGithubLink() {
    String githubRootUri = "https://github.com/";
    String githubLink = githubRootUri + repo;
    if (Objects.equals(this.repo, "googleapis/google-cloud-java")
        || Objects.equals(this.repo, "googleapis/sdk-platform-java")) {
      githubLink = githubLink + "/tree/main/" + this.repoShort;
    }
    return githubLink;
  }

  // MavenLink is created from distributionName
  public String getMavenLink() {
    String mavenRootUri = "https://central.sonatype.com/artifact/";
    String substrings[] = this.distributionName.split(":");
    String groupName = substrings[0];
    String artifactId = substrings[substrings.length - 1];
    String mavenLink = mavenRootUri + groupName + "/" + artifactId;
    return mavenLink;
  }

  public RepoMetadata parseRepoMetadata(String fileName) {
    Gson gson = new Gson();
    try (FileReader reader = new FileReader(fileName)) {
      return gson.fromJson(reader, RepoMetadata.class);
    } catch (IOException e) {
      throw new RuntimeException(".repo-metadata.json is not found", e);
    }
  }
}
