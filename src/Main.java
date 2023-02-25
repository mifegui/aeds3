import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main {

  static Scanner sc = new Scanner(System.in);

  public static void main(String[] args) throws IOException {
    // RandomAccessFile raf = new RandomAccessFile("anime.data", "rw"); // Cria o arquivo
    // Anime anime = new Anime();

    // raf.seek(0); // Volta para o inicio do arquivo
    // int ultimoId = raf.readInt(); // Le o ultimo id utilizado

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
    System.out.println("|| 4 Deletar anime         ||");
    System.out.println("============================\n");
    int choice = readChoiceFromUser();
    return choice;
  }
}
