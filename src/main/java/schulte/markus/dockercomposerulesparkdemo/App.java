package schulte.markus.dockercomposerulesparkdemo;

import spark.Spark;

public class App {

  static final String HELLO_WORLD_HTML_CONTENT
    = "<html><body><h1 id=\"h1-hello\">Hello world!</h1></body></html>";

  static final String PATH = "/hello";

  public static void main(final String... args) {
    Spark.get(App.PATH, (req, res) -> App.HELLO_WORLD_HTML_CONTENT);
  }
}
