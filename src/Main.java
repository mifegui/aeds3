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
    } while (choice != 0);
  }

  /**
   * Metodo para ler a opcao do usuario
   * Só retorna se for uma opcao valida
   * @return int com a opcao
   */
  static int readChoiceFromUser() {
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
  static int menu() {
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
}
