import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class IndiceHash {

  int profundidadeGlobal = 1, qtdMax = 100;
  ArrayList<Integer> diretorio = new ArrayList<Integer>();
  Path path;

  public IndiceHash(String path) throws IOException {
    this.path = Paths.get(path);
    RandomAccessFile arq = new RandomAccessFile(path, "rw");
    int pos = 0;
    diretorio.add(pos);
    arq.seek(pos);
    arq.writeInt(0); //diretorio local
    arq.writeInt(0); //quantidade de elementos
    pos += 4 + 4 + (qtdMax * 8);
    diretorio.add(pos);
    arq.seek(pos);
    arq.writeInt(0); //diretorio local
    arq.writeInt(0); //quantidade de elementos

    arq.close();
  }

  public void addchaves(int id, int address) throws IOException {
    //System.out.println("ID: "+id);
    int bucket = (int) (id % (Math.pow(2, profundidadeGlobal)));
    //System.out.println("Bucket: "+bucket);
    writeArq(id, address, bucket);
  }

  public void writeArq(int id, int address, int bucket) throws IOException {
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");
    int profundidadeLocal, qtdElementos;

    int posBucket = diretorio.get(bucket);
    arq.seek(posBucket);
    //System.out.println("POSiÇAO do bucket - onde ele começa: "+posBucket);

    profundidadeLocal = arq.readInt();
    qtdElementos = arq.readInt();
    // System.err.println("profL: "+profundidadeLocal);
    // System.out.println("qtd: "+qtdElementos);
    // System.out.println(qtdElementos+" < "+qtdMax);

    if (qtdElementos < qtdMax) { //bucket com espaço livre
      if (profundidadeLocal == 0) { //se o buckt estiver completamente vazio
        arq.seek(posBucket);
        //System.out.println("POSIÇÂO onde ta escrevendo a prof: "+posBucket);
        profundidadeLocal = profundidadeGlobal;
        arq.writeInt(profundidadeLocal);
      }
      posBucket += 4;
      arq.seek(posBucket);
      //System.out.println("POSIÇÃO onde ta escrevendo a qtd: "+posBucket);
      qtdElementos++;
      arq.writeInt(qtdElementos);

      posBucket += 4 + (qtdElementos * 8) - 8; //posiciona no fim do bucket, -8 por causa do incremento da qtdElementos na linha 44
      arq.seek(posBucket);
      //System.out.println("POSIÇÃO onde ta escrevendo o ELEMENTO: "+posBucket);
      arq.writeInt(id); //insere novo elemento
      arq.writeInt(address);
    } else { //bucket cheio
      //aumenta o diretorio
      if (profundidadeLocal == profundidadeGlobal) {
        int profundidadeAntiga = profundidadeGlobal;
        profundidadeGlobal++;
        for (
          int i = (int) Math.pow(2, profundidadeAntiga);
          i < Math.pow(2, profundidadeGlobal);
          i++
        ) {
          int pont = i * (4 + 4 + (qtdMax * 8));
          diretorio.add(pont); //ajusta os novos ponteiros, que apontam para seus respectivos buscktes
          arq.seek(pont);
          arq.writeInt(0); //inicializa os novos buckets com 0
          arq.writeInt(0);
        }
        //depois de aumentar o diretorio, reajusta o buscket cheio
        redirecionarchavess(posBucket, bucket);
        //agora o bucket não vai mais estar cheio, então insere o novo id
        writeArq(id, address, bucket);
      } else {
        redirecionarchavess(posBucket, bucket);
        //agora o bucket não vai mais estar cheio, então insere o novo id
        writeArq(id, address, bucket);
      }
    }

    arq.close();
  }

  //pos = posição no arquivo e bucket = posição no array diretorio
  public void redirecionarchavess(int pos, int bucket) throws IOException {
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");
    arq.seek(pos);
    /*profLocal++;
        arq.writeInt(profLocal); //aumentando a profundidade local*/
    arq.writeInt(profundidadeGlobal);
    arq.writeInt(0); //elemina todos os elementos daquele bucket

    pos += 8;
    arq.seek(pos); //posiciona o ponteiro no inicio das chaves do bucket

    //percorre todos os elementos do bucket cheio
    for (int i = 0; i < qtdMax; i++) {
      int elemento = arq.readInt();
      int address = arq.readInt();
      int novoBucket = (int) (elemento % (Math.pow(2, profundidadeGlobal)));

      //distribui os elementos para outros buckets ou para o mesmo bucket
      if (novoBucket == bucket) {
        writeArq(elemento, address, bucket); //distribui os elementos para outros buckets
      } else {
        writeArq(elemento, address, novoBucket);
      }
    }

    arq.close();
  }

  public void readHash() throws IOException {
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");
    int pos = 0;
    int cont = 0;
    do {
      arq.seek(pos);
      System.out.println("Burcket " + cont);
      cont++;
      int profLocal = arq.readInt();
      System.out.println("ProfL: " + profLocal);
      int qtd = arq.readInt();
      System.out.println("Qtd: " + qtd);

      for (int i = 0; i < qtd; i++) {
        System.out.println("chaves: " + arq.readInt());
        System.out.println("Address: " + arq.readInt());
      }
      System.out.println("");
      pos += 8 + (8 * qtdMax);
    } while (cont < diretorio.size());

    arq.close();
  }

  public int search(int id) throws IOException {
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");
    int address = -1;
    int bucket = (int) (id % (Math.pow(2, profundidadeGlobal)));
    int pos = diretorio.get(bucket);

    arq.seek(pos);
    int profLocal = arq.readInt();
    int qtd = arq.readInt();
    for (int i = 0; i < qtd; i++) {
      int chaves = arq.readInt();
      address = arq.readInt();
      if (chaves == id) {
        arq.close();
        return address;
      }
    }
    address = -1; //se não tiver achado o elemento

    arq.close();
    return address;
  }

  public void updateAddress(int id, int address) throws IOException {
    RandomAccessFile arq = new RandomAccessFile(path.toFile(), "rw");
    int bucket = (int) (id % (Math.pow(2, profundidadeGlobal)));
    int pos = diretorio.get(bucket);

    arq.seek(pos);
    int profLocal = arq.readInt();
    int qtd = arq.readInt();

    for (int i = 0; i < qtd; i++) {
      int chaves = arq.readInt();
      if (chaves == id) {
        arq.writeInt(address);
        break;
      } else {
        arq.readInt();
      }
    }

    arq.close();
  }
}
