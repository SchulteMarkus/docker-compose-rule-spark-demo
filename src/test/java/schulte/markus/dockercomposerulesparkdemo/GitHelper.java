package schulte.markus.dockercomposerulesparkdemo;

import java.io.IOException;
import java.util.Properties;

/**
 * Helper to work with information about the GIT-repository and -log, from git.properties-file,
 * created while maven-building, using <a href="https://github.com/ktoso/maven-git-commit-id-plugin">maven-git-commit-id-plugin</a>.
 */
interface GitHelper {

  String GIT_PROPERTIES_FILE_NAME = "git.properties";

  String GIT_COMMIT_ID_ABBREV_KEY = "git.commit.id.abbrev";

  static String getCommitId() {
    final Properties gitPropertiesFile = new Properties();
    try {
      gitPropertiesFile
        .load(GitHelper.class.getClassLoader().getResourceAsStream(GIT_PROPERTIES_FILE_NAME));
    } catch (final IOException e) {
      throw new RuntimeException("IO problem handling " + GIT_PROPERTIES_FILE_NAME, e);
    }

    return gitPropertiesFile.getProperty(GIT_COMMIT_ID_ABBREV_KEY);
  }
}
