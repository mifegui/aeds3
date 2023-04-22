import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ListaInvertida {

  //   private double averages[][] = new double[101][size]; //Matriz da lista de notas
  public String airedDates[] = new String[200]; //Array contendo todos os airedDates
  private int airedDatesLen = 0;
  private int ids[][] = new int[200][200]; //Matriz da lista de airedDates
  private Path path;

  public void initializeFile() {
    if (path.toFile().exists()) {
      path.toFile().delete();
    }
    try {
      path.toFile().createNewFile();
    } catch (Exception e) {
      System.out.println((e));
    }
  }

  ListaInvertida(String path) {
    this.path = Paths.get(path);
  }

  public void addAiredToInvertedList(String aired, int pos) throws IOException {
    // check if airedDaet exists in airedDates
    String year = aired.substring(aired.length() - 4);
    int achou = -1;
    for (int i = 0; i < this.airedDatesLen; i++) {
      if (this.airedDates[i].equals(year)) {
        achou = i;
      }
    }
    if (achou == -1) {
      airedDates[airedDatesLen] = year;
      achou = airedDatesLen;
      this.airedDatesLen++;
    }

    if (year.contains(this.airedDates[achou])) { //Se a data de lançamento do anime for igual aa data de lançamento encontrado na matriz
      for (int col = 1; col < 200; col++) { //Percorrer as colunas daquele airedDate na matriz para inserir os id's dos animes
        if (this.ids[achou][col] == 0) { //Se estiver em uma posição vazia na matriz
          this.ids[achou][col] = pos; //Armazenar a pos do anime
          break;
        }
      }
    }
  }

  public void buildAiredDateArchive() throws IOException { //Construir o arquivo contendo a lista invertida pelos airedDateS
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");

    for (int ln = 0; ln < airedDatesLen; ln++) { //Percorrer todas a linhas da matriz
      boolean wasEmpty = arq.length() == 0;
      arq.writeUTF(this.airedDates[ln]); //Escrever o array de bytes contendo a data de lançamento no arquivo
      int cont = 0; //Contador para contabilizar a quantidade de animes que possuem um mesma data de lançamento
      int value[] = new int[200]; //Array para armazenar os id's que serão escritos no arquivo
      for (int col = 1, i = 0; col < 200; col++) { //Percorrer todas as colunas da matriz
        if (ids[ln][col] != 0) { //Se a posição da matriz armazenar algum id
          cont++; //Incrementa o contador
          value[i] = ids[ln][col]; //Armazena o id no array de id's
          i++; //Incrementa a variável de controle do array de id's
        }
      }
      arq.writeInt(cont); //Escreve o contador contendo a quantidade de animes de um mesma data de lançamento no arquivo
      for (int i = 0; i < cont; i++) { //Escreve todos os id's que possuem o mesma data de lançamento
        arq.writeInt(value[i]);
      }
    }

    arq.close();
  }

  public int[] invertedListSearch(String key) throws IOException { //Método para realizar a pesquisa da lista invertida através dos airedDateS
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");

    int[] idsAchados = new int[500];
    int id;
    String aD;
    int size;
    int pos = 0; //Iniciar a leitura do arquivo no começo do arquivo
    boolean end = false;

    do {
      arq.seek(pos);
      aD = arq.readUTF(); //Leitura do airedDate
      size = arq.readInt(); //Leitura da quantidade de animes que possuem aquele airedDate
      if (aD.equals(key)) { //Se o airedDate lido do arquivo for igual ao airedDate passado por parâmetro no método
        for (int i = 0; i < size; i++) { //Percorrer a quantidade de id's de animes que possuem o airedDate
          id = arq.readInt(); //Leitura do id do anime
          idsAchados[i] = id; //Armazenar o id no array de id's
        }
        end = true; //airedDate foi achado
      }
      pos += 4 + 6 + (size * 4); //Posicionar o ponteiro para o próximo airedDate
      if (pos >= arq.length()) end = true;
    } while (!end);

    arq.close();
    return idsAchados;
  }
}
