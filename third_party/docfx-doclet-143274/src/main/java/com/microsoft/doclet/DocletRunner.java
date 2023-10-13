package com.microsoft.doclet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.microsoft.util.OptionsFileUtil;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

/**
 * To use runner just pass as commandline param to main method: - name of file with doclet name,
 * parameter file, and argument file.
 *
 * <p>For example: <code>java DocletRunner $HOME/java-aiplatform/target/site/apidocs/options
 *     $HOME/java-aiplatform/target/site/apidocs/argfile</code>
 */
public class DocletRunner {

  public static void main(final String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java DocletRunner <options file> <argfile>");
      return;
    }

    run(
        args,
        new EnvironmentToArgumentsBuilder()
            .addIfExists("artifactVersion")
            .addIfExists("librariesBomVersion")
            .addIfExists("repoMetadataFilePath")
            .build());
  }

  @VisibleForTesting
  static void run(final String[] args, List<String> env) {
    List<String> combined = new ArrayList<>(env);
    for (String arg : args) {
      if (!(new java.io.File(arg)).isFile()) {
        System.err.println(String.format("File '%s' does not exist", args[0]));
      }
      combined.addAll(OptionsFileUtil.processOptionsFile(arg));
    }
    ToolProvider.getSystemDocumentationTool()
        .run(null, null, null, combined.toArray(new String[0]));
  }

  @VisibleForTesting
  static class EnvironmentToArgumentsBuilder {
    private final ImmutableList.Builder<String> env = new ImmutableList.Builder<>();

    public EnvironmentToArgumentsBuilder addIfExists(String name) {
      String value = System.getenv(name);
      if (value != null) {
        return add(name, value);
      }
      return this;
    }

    @VisibleForTesting
    EnvironmentToArgumentsBuilder add(String name, String value) {
      env.add("-" + name, value);
      return this;
    }

    public ImmutableList<String> build() {
      return env.build();
    }
  }
}
