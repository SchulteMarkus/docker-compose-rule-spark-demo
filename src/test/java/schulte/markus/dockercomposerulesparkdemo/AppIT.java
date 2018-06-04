package schulte.markus.dockercomposerulesparkdemo;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerMachine;
import java.io.IOException;
import org.apache.http.client.fluent.Request;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class AppIT {

  private static final String DOCKER_COMPOSE_YML_FILE = "src/test/resources/docker-compose.yml";

  private static final int SPARK_DEFAULT_PORT = 4567;

  private static final String SPARK_HELLO_WORLD_SERVICE_NAME = "spark-hello-world-service";

  private static final String SPARK_HELLO_WORLD_SERVICE_VERSION_ENV_VAR_NAME
    = "SPARK_HELLO_WORLD_SERVICE_VERSION";

  @Rule
  public final DockerComposeRule docker;

  {
    // As docker-image, use the one for this git-commit-id (which was created while "mvn package")
    final var gitCommitId = GitHelper.getCommitId();
    final var dockerMachine = DockerMachine.localMachine()
      .withAdditionalEnvironmentVariable(AppIT.SPARK_HELLO_WORLD_SERVICE_VERSION_ENV_VAR_NAME,
        gitCommitId)
      .build();

    this.docker = DockerComposeRule.builder()
      .file(AppIT.DOCKER_COMPOSE_YML_FILE)
      .machine(dockerMachine)
      .build();
  }

  @Test
  public void test() throws IOException {
    final var sparkHelloWorldServiceUrl = this.getSparkHelloWorldServiceUrl();

    final var content = Request.Get(sparkHelloWorldServiceUrl + App.PATH)
      .execute()
      .returnContent();
    Assert.assertEquals(App.HELLO_WORLD_HTML_CONTENT, content.asString());
  }

  /**
   * @return URL of started spark-hello-world-service (for this git-commit-id).
   * "http://127.0.0.1:32775", for example.
   */
  private String getSparkHelloWorldServiceUrl() {
    // Get ip and port of started spark-hello-world-service
    final var serviceDockerPort = this.docker.containers()
      .container(AppIT.SPARK_HELLO_WORLD_SERVICE_NAME)
      .port(AppIT.SPARK_DEFAULT_PORT);
    // Create url (http://...), which is the one for the just started spark-hello-world-service in it's docker-container
    return String
      .format("http://%s:%d", serviceDockerPort.getIp(), serviceDockerPort.getExternalPort());
  }
}
