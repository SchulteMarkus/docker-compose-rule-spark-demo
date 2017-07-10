package schulte.markus.dockercomposerulesparkdemo;

import spark.Spark;

public class App {

  private static final String PATH = "/hello";

  public static void main(final String... args) {
    Spark.get(PATH, (req, res) -> "Hello World");
  }
}
