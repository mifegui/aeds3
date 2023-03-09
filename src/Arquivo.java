import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Arquivo {

  private String stringPath = "../bd/banco.db";
  public Path path = Paths.get((stringPath));

  public boolean alreadyExists() {
    try {
      return path.toFile().exists();
    } catch (Exception e) {
      return false;
    }
  }

  private void initializeFile() {
    if (alreadyExists()) return;
    try {
      path.toFile().createNewFile();
    } catch (Exception e) {
      System.out.println((e));
    }
  }

  /*CREATE ANTIGO
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
*/
  //---------------------------------------------------------CREATE

  public void create(Anime anime) throws Exception {
    //pos(0) = tam arquivo(qnt); pos (1) = lapide; pos (2) = tam registro
    int posicao = 0;
    int tamanho = 0;
    int id = anime.getId();
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");

    // Ler id adicionar 1 e atualizar no arquivo.
    raf.seek(0);
    if (anime.getId() == -1) {
      try {
        id = raf.readInt();
      } catch (Exception e) { // Nao tem id
        raf.writeInt(anime.getId());
      }
      id++;
      raf.seek(0);
    }
    raf.writeInt(id);
    anime.setId(id);

    byte[] ba = anime.toByteArray();
    raf.seek(raf.length()); //mover para o fim do arquivo
    raf.writeChar(' ');
    raf.writeInt(ba.length); //escrever
    raf.write(ba); //escrever
    raf.close();
  }

  //---------------------------------------------------------READ

  public Anime read(int id) throws Exception {
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
    byte[] ba; //conjunto vazio
    char lapide;
    int tamanho;
    int lid = raf.readInt();
    int idQueAcabouDeler = -1;
    while (idQueAcabouDeler != lid) { //enquanto n atingir fim do arquivo
      lapide = raf.readChar();
      tamanho = raf.readInt();
      Anime anime = new Anime();
      ba = new byte[tamanho];
      raf.read(ba);
      anime.fromByteArray(ba);
      idQueAcabouDeler = anime.getId();

      if (lapide != '*') {
        if (anime.getId() == id) return anime;
      }
    }
    raf.close();
    return null; //caso tudo dê errado
  }

  //---------------------------------------------------------UPDATE
  public boolean update(Anime novo) throws Exception {
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
    long posicaoDepoisDoTamanho, posicaoAntesDaLapide;
    char lapide;
    int tamanho;
    Anime anime = new Anime();
    boolean result;
    byte[] ba;
    byte[] novoba;

    int lid = raf.readInt();

    int idQueAcabouDeler = -1;
    while (idQueAcabouDeler != lid) { //enquanto nao chegar no fim do arquivo
      posicaoAntesDaLapide = raf.getFilePointer();
      lapide = raf.readChar();
      tamanho = raf.readInt();
      posicaoDepoisDoTamanho = raf.getFilePointer();
      if (lapide != '*') {
        ba = new byte[tamanho];
        raf.read(ba);
        anime.fromByteArray(ba); //extrair objeto do registro
        idQueAcabouDeler = anime.getId();
        if (novo.getId() == anime.getId()) {
          novoba = novo.toByteArray(); //criar novoRegistro
          if (novoba.length <= tamanho) {
            raf.seek(posicaoDepoisDoTamanho);
            raf.write(novoba);
          } else {
            // "Apaga" o atual
            raf.seek(posicaoAntesDaLapide);
            raf.writeChar('*'); //lapide

            // Cria o novo no final do arquivo.
            raf.seek(raf.length());
            raf.writeChar(' '); //lapide
            raf.writeInt(novoba.length); //tamanho
            raf.write(novoba);
            result = delete(anime.getId());
          }
          raf.close();
          return true;
        }
      } else {
        raf.skipBytes(tamanho);
      }
    }
    raf.close();
    return false;
  }

  //---------------------------------------------------------DELETE
  public boolean delete(int id) throws Exception {
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
    long pos;
    int lapide;
    int tamanho;
    Anime anime = new Anime();
    byte[] ba;
    int lid = raf.readInt();
    while (id <= lid) { //enquanto nao chegar no fim do arquivo
      pos = raf.getFilePointer();
      lapide = raf.readChar();
      tamanho = raf.readInt();
      if (lapide != '*') {
        ba = new byte[tamanho];
        raf.read(ba);
        anime.fromByteArray(ba);
        if (anime.getId() == id) {
          raf.seek(pos); //mover para pos
          raf.writeChar('*'); //marca lapide como excluido
          raf.close();
          return true;
        }
      } else {
        raf.skipBytes(tamanho);
      }
    }
    raf.close();
    return false;
  }
}
