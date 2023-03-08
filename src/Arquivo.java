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

  public void create(Anime anime)throws IOException{
    try{//primeiros 4 bytes        |   1 byte(char)  |   5° byte
        //pos(0) = tam arquivo(qnt); pos (1) = lapide; pos (2) = tam registro
        int posicao = 0;
        int tamanho = 0;
        int id = 0;
        byte[] ba = anime.toByteArray();
        RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
        raf.seek(0);
        //"precisava" pegar ultimo id para somar, mas n fiz isso :(
        raf.seek(raf.length());//mover para o fim do arquivo
        raf.writeChar(" ");
        raf.writeInt(ba.length());//escrever
        raf.write(ba);//escrever
        raf.close();
    }
  }
    //---------------------------------------------------------READ

  public Anime read(int id)throws IOException{
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
    byte[] ba;//conjunto vazio
    Anime anime = new Anime();
    char lapide;
    int tamanho;
    raf.seek(4);//mover para o primeiro registro
    while(raf.getFilePointer()<raf.length())//enquanto n atingir fim do arquivo
    {
      lapide = raf.readChar();
      tamanho = raf.readInt();
      ba = new byte[tamanho];
      if(lapide != "*"){
        anime.fromByteArray(ba);
        if(anime.getId()==id)
          return anime;
      }
    }
    raf.close();
    return null;//caso tudo dê errado
  }
  //---------------------------------------------------------UPDATE
  public boolean update(Anime novo){
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
    int posicao;
    char lapide;
    int tamanho;
    Anime anime = new Anime();
    boolean result;
    byte[] ba;
    byte[] novoba;
    raf.seek(4);//mover para o primeiro registro do arquivo
    while(raf.getFilePointer()<raf.length()){//enquanto nao chegar no fim do arquivo
      posicao = raf.getFilePointer();//salvar posicao do ponteiro
      lapide = raf.readChar();
      if(lapide != "*"){
        tamanho = raf.readInt();
        ba = new byte[tamanho];
        raf.read(ba);
        anime.fromByteArray(ba);//extrair objeto do registro
        if(novo.getId() == anime.getId()){
          novoba = raf.toByteArray();//criar novoRegistro
          if(novoba.length()<=tamanho){
            raf.seek(posicao+6);
            raf.write(novoba);
          }else{
            raf.seek(raf.length());
            raf.writeChar(' ');//lapide
            raf.writeInt(novoab.length());//tamanho
            raf.write(novoba);
            result = delete(anime.getId());
          }
          raf.close();
          return true;
        }
      }
    }
    raf.close();
    return false;
  }
  //---------------------------------------------------------DELETE
  public boolean delete(int id){
    RandomAccessFile raf = new RandomAccessFile("../bd/banco.db", "rw");
    int pos;
    int lapide;
    int tamanho;
    Anime anime = new Anime();
    byte[] ba;
    raf.seek(4);//mover para o primeiro registro
    while(raf.getFilePointer()<raf.length())//enquanto nao chegar no fim do arquivo
    {
      pos = raf.getFilePointer();
      lapide = raf.readChar();
      if(lapide != "*"){
        tamanho = raf.readInt();
        ba = new byte[tamanho];
        anime.fromByteArray(ba);
        if(anime.getId() == id){
          raf.seek(pos);//mover para pos
          raf.writeChar('*');//marca lapide como excluido
          raf.close();
          return true;
        }
      }
    }
    raf.close();
    return false;
  }
}
