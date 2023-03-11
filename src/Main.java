import de.siegmar.fastcsv.reader.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

  static Scanner sc = new Scanner(System.in);
  static Arquivo bd = new Arquivo("../bd/banco.db");

  public static void main(String[] args) throws Exception {
    // RandomAccessFile raf = new RandomAccessFile("anime.data", "rw"); // Cria o arquivo
    // Anime anime = new Anime();

    // raf.seek(0); // Volta para o inicio do arquivo
    // int ultimoId = raf.readInt(); // Le o ultimo id utilizado
    setupDb(false);

    int choice = -1;
    do {
      choice = menu();

      switch (choice) {
        case 1:
          {
            setupDb(true);
            break;
          } //create(novo_objeto)
        case 2:
          {
            create();
            break;
          } //create(novo_objeto)
        case 3:
          {
            read();
            break;
          } //read(id)
        case 4:
          {
            update();
            break;
          } //update(obj_atualizado)
        case 5:
          {
            delete();
            break;
          } //delete(id)
        case 6:
          {
            ordenarComum();
            break;
          } //delete(id)
        case 7:
          {
            ordenarVariavel();
            break;
          } //delete(id)
        case 8:
          {
            ordenarSubs();
            break;
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
    int MAX = 8;
    do {
      try {
        System.out.print("~$ ");
        opcao = sc.nextInt();
        if (opcao < 0 || opcao > MAX) System.out.println("~$ Opção inválida!");
      } catch (Exception e) { // Se a opcao não for um numero
        System.out.println("~$ Opção inválida!");
        sc.nextLine();
        break;
      }
    } while (opcao < 0 || opcao > MAX);
    return opcao;
  }

  /**
   * menu executa o que o usuario quer
   * retorna o int que foi executado
   */
  static int menu() throws Exception {
    System.out.println("===========Menu=======================");
    System.out.println("||                                   ||");
    System.out.println("|| 0 Sair                            ||");
    System.out.println("|| 1 Regerar banco                   ||");
    System.out.println("|| 2 Criar anime                     ||");
    System.out.println("|| 3 Ver anime por id                ||");
    System.out.println("|| 4 Atualizar anime                 ||");
    System.out.println("|| 5 Deletar anime                   ||");
    System.out.println("|| 6 Ordenar Intercalação Comum      ||");
    System.out.println("|| 7 Ordenar Intercalação Variavel   ||");
    System.out.println("|| 8 Ordenar por Substituição        ||");
    System.out.println("======================================\n");
    int choice = readChoiceFromUser();
    return choice;
  }

  public static void ordenarVariavel() throws Exception {
    bd.intercalacaoVariavel();
  }

  public static void ordenarComum() throws Exception {
    bd.intercalacaoComum();
  }

  public static void ordenarSubs() throws Exception {
    bd.intercalacaoSubs();
  }

  public static void setupDb(boolean force) throws Exception {
    if (bd.alreadyExists() && !force) return;
    bd.initializeFile();

    int rowsToRead = 12250;
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
  public static Anime ler(boolean askId) throws IOException {
    Anime anime = new Anime();

    if (askId) {
      System.out.print("Enter ID: ");
      int id = sc.nextInt();
      anime.setId(id);
    }

    System.out.print("Enter name: ");
    sc.nextLine();
    String name = sc.nextLine();
    anime.setName(name);

    System.out.print("Enter score: ");
    float score = sc.nextFloat();
    anime.setScore(score);

    System.out.print("Enter genres (space separated): ");
    sc.nextLine();
    String[] genres = sc.nextLine().split(" ");
    // Espaço para ser separador do UTF
    for (int i = 1; i < genres.length; i++) {
      genres[i] = " " + genres[i];
    }
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
    bd.create(ler(false));
  }

  //---------------------------------------------------------READ(id)
  public static void read() throws Exception {
    System.out.print("ID: ");
    int id = sc.nextInt();
    Anime anime = bd.read(id);
    if (anime == null) System.out.println(
      "Anime não encontrado!"
    ); else anime.print();
  }

  //---------------------------------------------------------UPDATE(obj_att)
  public static void update() throws Exception {
    bd.update(ler(true));
  }

  //---------------------------------------------------------DELETE(id)
  public static void delete() throws Exception {
    int id = sc.nextInt();
    bd.delete(id);
  }
}
