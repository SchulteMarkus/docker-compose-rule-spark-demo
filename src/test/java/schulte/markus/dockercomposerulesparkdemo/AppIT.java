package schulte.markus.dockercomposerulesparkdemo;

import java.io.IOException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.eclipse.jetty.http.HttpScheme;
import org.junit.Assert;
import org.junit.Test;

public class AppIT {

  private static final int SPARK_DEFAULT_PORT = 4567;

  private static final String URI =
    HttpScheme.HTTP + "://localhost:" + SPARK_DEFAULT_PORT + "/" + App.PATH;

  @Test
  public void test() throws IOException {
    App.main();

    final String gitCommitId = GitHelper.getGitRepositoryProperties();

    final Content content = Request.Get(URI).execute().returnContent();
    Assert.assertEquals("Hello World", content.asString());
  }
}
