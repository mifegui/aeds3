import de.siegmar.fastcsv.reader.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Scanner;

public class Main {

  static Scanner sc = new Scanner(System.in);
  static Arquivo bd = new Arquivo();

  public static void main(String[] args) throws Exception {
    // RandomAccessFile raf = new RandomAccessFile("anime.data", "rw"); // Cria o arquivo
    // Anime anime = new Anime();

    // raf.seek(0); // Volta para o inicio do arquivo
    // int ultimoId = raf.readInt(); // Le o ultimo id utilizado
    setupDb();

    int choice = -1;
    do {
      choice = menu();

      switch (choice) {
        case 1:
          {
            create();
          } //create(novo_objeto)
        case 2:
          {
            read();
          } //read(id)
        case 3:
          {
            update();
          } //update(obj_atualizado)
        case 4:
          {
            delete();
          } //delete(id)
      }
    } while (choice != 0);
  }

  /**
   * Metodo para ler a opcao do usuario
   * Só retorna se for uma opcao valida
   * @return int com a opcao
   */
  static int readChoiceFromUser() throws Exception {
    int opcao = -1;
    do {
      try {
        System.out.print("~$ ");
        opcao = sc.nextInt();
        if (opcao < 0 || opcao > 4) System.out.println("~$ Opção inválida!");
      } catch (Exception e) { // Se a opcao não for um numero
        System.out.println("~$ Opção inválida!");
        sc.nextLine();
        break;
      }
    } while (opcao < 0 || opcao > 4);
    return opcao;
  }

  /**
   * menu executa o que o usuario quer
   * retorna o int que foi executado
   */
  static int menu() throws Exception {
    System.out.println("===========Menu=============");
    System.out.println("||                        ||");
    System.out.println("|| 0 Sair                 ||");
    System.out.println("|| 1 Criar anime          ||");
    System.out.println("|| 2 Ver lista de animes  ||");
    System.out.println("|| 3 Atualizar anime      ||");
    System.out.println("|| 4 Deletar anime        ||");
    System.out.println("============================\n");
    int choice = readChoiceFromUser();
    return choice;
  }

  public static void setupDb() throws Exception {
    int rowsToRead = 10;
    boolean skipedHeader = true;
    System.out.println(
      "Lendo " + rowsToRead + " linhas do .csv para criar arquivo..."
    );
    final Path path = Paths.get("../bd/data.csv");
    for (
      final Iterator<CsvRow> iterator = CsvReader
        .builder()
        .quoteCharacter('\"')
        .build(path)
        .iterator();
      iterator.hasNext() && rowsToRead > 0;
    ) {
      final CsvRow csvRow = iterator.next();
      if (skipedHeader) {
        skipedHeader = false;
        continue;
      }
      Anime a = new Anime(
        NumberFormat.getInstance().parse(csvRow.getField(0)).intValue(),
        csvRow.getField(1),
        NumberFormat.getInstance().parse(csvRow.getField(2)).floatValue(),
        csvRow.getField(3).split(","),
        NumberFormat.getInstance().parse(csvRow.getField(4)).intValue(),
        csvRow.getField(5)
      );
      bd.create(a);
      rowsToRead--;
    }
    System.out.println();
  }

  //---------------------------------------------------------
  //CRUD
  public static Anime ler() throws IOException {
    Anime anime = new Anime();

    System.out.print("Enter ID: ");
    int id = sc.nextInt();
    anime.setId(id);

    System.out.print("Enter name: ");
    String name = sc.next();
    anime.setName(name);

    System.out.print("Enter score: ");
    float score = sc.nextFloat();
    anime.setScore(score);

    System.out.print("Enter genres (space separated): ");
    String[] genres = sc.next().split(" ");
    anime.setGenres(genres);

    System.out.print("Enter number of episodes: ");
    int episodes = sc.nextInt();
    anime.setEpisodes(episodes);

    System.out.print("Enter aired date: ");
    String aired = sc.next();
    anime.setAired(aired);

    return (anime);
  }

  //---------------------------------------------------------
  public static void create() throws Exception {
    bd.create(ler());
  }

  //---------------------------------------------------------READ(id)
  public static void read() throws Exception {
    int id = sc.nextInt();
    bd.read(id);
  }

  //---------------------------------------------------------UPDATE(obj_att)
  public static void update() throws Exception {
    bd.update(ler());
  }

  //---------------------------------------------------------DELETE(id)
  public static void delete() throws Exception {
    int id = sc.nextInt();
    bd.delete(id);
  }
}
