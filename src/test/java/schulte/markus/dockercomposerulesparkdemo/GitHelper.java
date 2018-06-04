package schulte.markus.dockercomposerulesparkdemo;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper to work with information about the GIT-repository and -log, from git.properties-file,
 * created while maven-building, using <a href="https://github.com/ktoso/maven-git-commit-id-plugin">maven-git-commit-id-plugin</a>.
 */
final class GitHelper {

  private static final String GIT_PROPERTIES_FILE_NAME = "git.properties";

  private static final String GIT_COMMIT_ID_ABBREV_KEY = "git.commit.id.abbrev";

  private GitHelper() {
  }

  static Optional<String> getCommitId() {
    final var gitPropertiesFile = new Properties();
    try {
      gitPropertiesFile.load(
        GitHelper.class.getClassLoader().getResourceAsStream(GitHelper.GIT_PROPERTIES_FILE_NAME)
      );
    } catch (final IOException e) {
      Logger.getLogger(GitHelper.class.getName())
        .log(Level.SEVERE, "IO problem handling " + GitHelper.GIT_PROPERTIES_FILE_NAME, e);
      return Optional.empty();
    }

    return Optional.of(gitPropertiesFile.getProperty(GitHelper.GIT_COMMIT_ID_ABBREV_KEY));
  }
}
