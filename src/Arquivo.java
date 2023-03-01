import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Arquivo {

  private String stringPath = "../bd/banco.db";
  private Path path = Paths.get((stringPath));

  public boolean alreadyExists() {
    return path.toFile().exists();
  }

  private void initializeFile() {
    if (alreadyExists()) return;
    try {
      path.toFile().createNewFile();
    } catch (Exception e) {
      System.out.println((e));
    }
  }

  public void create(Anime anime) {
    if (!alreadyExists()) {
      initializeFile();
    }

    try {
      Files.write(path, anime.toByteArray(), StandardOpenOption.APPEND);
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
