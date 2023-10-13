package com.microsoft.doclet;

import com.microsoft.build.YmlFilesBuilder;
import java.util.*;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.apache.commons.lang3.StringUtils;

public class DocFxDoclet implements Doclet {

  private Reporter reporter;

  @Override
  public void init(Locale locale, Reporter reporter) {
    reporter.print(Kind.NOTE, "Doclet using locale: " + locale);
    this.reporter = reporter;
  }

  @Override
  public boolean run(DocletEnvironment environment) {
    Objects.requireNonNull(repoMetadataFilePath, "repoMetadataFilePath must not be null.");

    reporter.print(Kind.NOTE, "artifactVersion: " + artifactVersion);
    reporter.print(Kind.NOTE, "librariesBomVersion: " + librariesBomVersion);
    reporter.print(Kind.NOTE, "repoMetadataFilePath: " + repoMetadataFilePath);
    reporter.print(Kind.NOTE, "Output path: " + outputPath);
    reporter.print(Kind.NOTE, "Excluded packages: " + Arrays.toString(excludePackages));
    reporter.print(Kind.NOTE, "Excluded classes: " + Arrays.toString(excludeClasses));
    reporter.print(Kind.NOTE, "Project name: " + projectName);
    reporter.print(Kind.NOTE, "Disable changelog: " + disableChangelog);
    reporter.print(Kind.NOTE, "Disable libraryOverview: " + disableLibraryOverview);

    return (new YmlFilesBuilder(
            environment,
            outputPath,
            excludePackages,
            excludeClasses,
            projectName,
            disableChangelog,
            disableLibraryOverview,
            artifactVersion,
            librariesBomVersion,
            repoMetadataFilePath))
        .build();
  }

  @Override
  public String getName() {
    return "DocFxDoclet";
  }

  private String outputPath;
  private String[] excludePackages = {};
  private String[] excludeClasses = {};
  private String projectName;
  private boolean disableChangelog;
  private boolean disableLibraryOverview;
  private String artifactVersion;
  private String librariesBomVersion;
  private String repoMetadataFilePath;

  @Override
  public Set<? extends Option> getSupportedOptions() {
    Option[] options = {
      new CustomOption("Output path", Arrays.asList("-outputpath", "--output-path", "-d"), "path") {
        @Override
        public boolean process(String option, List<String> arguments) {
          outputPath = arguments.get(0);
          return true;
        }
      },
      new CustomOption(
          "Exclude packages",
          Arrays.asList("-excludepackages", "--exclude-packages", "-ep"),
          "packages") {
        @Override
        public boolean process(String option, List<String> arguments) {
          excludePackages = StringUtils.split(arguments.get(0), ":");
          return true;
        }
      },
      new CustomOption(
          "Exclude classes",
          Arrays.asList("-excludeclasses", "--exclude-classes", "-ec"),
          "classes") {
        @Override
        public boolean process(String option, List<String> arguments) {
          excludeClasses = StringUtils.split(arguments.get(0), ":");
          return true;
        }
      },
      new CustomOption(
          "Project name", Arrays.asList("-projectname", "--project-name", "-pn"), "name") {
        @Override
        public boolean process(String option, List<String> arguments) {
          //  using artifact id as projectName - remove "-parent" since generation runs in parent
          // pom
          projectName = arguments.get(0).replaceAll("-parent", "");
          return true;
        }
      },
      new CustomOption(
          "Disable changelog",
          Arrays.asList("-disable-changelog", "--disable-changelog"),
          "disableChangelog") {
        @Override
        public boolean process(String option, List<String> arguments) {
          if (arguments.get(0).equalsIgnoreCase("false")) {
            disableChangelog = false;
          } else {
            disableChangelog = true;
          }
          return true;
        }
      },
      new CustomOption(
          "Disable libraryOverview",
          Arrays.asList("-disable-libraryOverview", "--disable-libraryOverview"),
          "disablelibraryOverview") {
        @Override
        public boolean process(String option, List<String> arguments) {
          if (arguments.get(0).equalsIgnoreCase("false")) {
            disableLibraryOverview = false;
          } else {
            disableLibraryOverview = true;
          }
          return true;
        }
      },
      new CustomOption(
          "Artifact Version",
          Arrays.asList("-artifactVersion", "--artifactVersion"),
          "artifactVersion") {
        @Override
        public boolean process(String option, List<String> arguments) {
          artifactVersion = arguments.get(0);
          return true;
        }
      },
      new CustomOption(
          "libraries-bom Version",
          Arrays.asList("-librariesBomVersion", "--librariesBomVersion"),
          "librariesBomVersion") {
        @Override
        public boolean process(String option, List<String> arguments) {
          librariesBomVersion = arguments.get(0);
          return true;
        }
      },
      new CustomOption(
          "repo-metadata.json File Path",
          Arrays.asList("-repoMetadataFilePath", "--repoMetadataFilePath"),
          "repoMetadataFilePath") {
        @Override
        public boolean process(String option, List<String> arguments) {
          repoMetadataFilePath = arguments.get(0);
          return true;
        }
      },

      // Support next properties for compatibility with Gradle javadoc task.
      // According to javadoc spec - these properties used by StandardDoclet and used only when
      // 'doclet' parameter not populated. But Gradle javadoc not align with this rule and
      // passes them in spite of 'doclet' parameter existence
      new FakeOptionForCompatibilityWithStandardDoclet(
          "Fake support of doctitle property", "-doctitle"),
      new FakeOptionForCompatibilityWithStandardDoclet(
          "Fake support of windowtitle property", "-windowtitle")
    };
    return new HashSet<>(Arrays.asList(options));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  abstract static class CustomOption implements Option {

    private final String description;
    private final List<String> names;
    private final String parameters;

    public CustomOption(String description, List<String> names, String parameters) {
      this.description = description;
      this.names = names;
      this.parameters = parameters;
    }

    @Override
    public int getArgumentCount() {
      return 1;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public Kind getKind() {
      return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
      return names;
    }

    @Override
    public String getParameters() {
      return parameters;
    }
  }

  static class FakeOptionForCompatibilityWithStandardDoclet extends CustomOption {

    public FakeOptionForCompatibilityWithStandardDoclet(String description, String name) {
      super(description, Collections.singletonList(name), "none");
    }

    @Override
    public boolean process(String option, List<String> arguments) {
      return true;
    }
  }
}
