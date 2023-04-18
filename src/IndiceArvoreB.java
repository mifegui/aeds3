import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

class IndiceArvoreB {

  private Path path;

  IndiceArvoreB(String path) {
    this.path = Paths.get(path);
  }

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

  public long read(int id) throws Exception {
    RandomAccessFile indice = new RandomAccessFile(path.toFile(), "rw");
    Anime a = null;
    No no = new No();
    boolean achou = false;
    long direcao;
    byte[] ba = new byte[152];

    indice.seek(0);
    direcao = indice.readLong();

    while (!achou && direcao > -1) {
      indice.seek(direcao);
      indice.read(ba);
      no.fromByteArray(ba);

      direcao = -1;
      for (int i = 0; i < no.getN(); i++) {
        if (id < no.no[i].id) {
          direcao = no.p[i];
          break;
        } else if (id == no.no[i].id) {
          achou = true;
          indice.close();
          return no.no[i].pos;
        }
      }
    }

    indice.close();

    return -1;
  }

  /**
   *
   * ADD da BTree
   */
  public void add(Ponto p1) throws Exception {
    short ordem = 8;
    RandomAccessFile indice = new RandomAccessFile(path.toFile(), "rw");
    byte[] noBytes = new byte[152];
    Retorno rtn = new Retorno();
    No raiz, nRaiz;
    int raizN; // checagem se sera necessario criar nova raiz

    indice.seek(0);
    // Criar a raiz se for o primeiro ponto que estamos adicionando
    if (indice.length() == 0) {
      indice.writeLong(0);
      raiz = new No();
      long raizPos = indice.getFilePointer();
      raiz.addPonto(p1, raizPos);
      indice.write(raiz.toByteArray());
      indice.seek(0);
      indice.writeLong(raizPos);
      return;
    }

    // Encontrar no raiz
    indice.seek(indice.readLong());
    raiz = new No(indice.getFilePointer());
    indice.read(noBytes);
    raiz.fromByteArray(noBytes);

    raizN = raiz.getN();
    indice.close();

    rtn = add(p1, raiz);

    // caso ainda se deva acrescentar algum registro a raiz
    if (rtn.promovido != null) {
      indice = new RandomAccessFile(path.toFile(), "rw");

      if (raiz.getN() < ordem - 1 && raizN < ordem - 1) {
        raiz.addPonto(rtn.promovido, rtn.novoNo.getPos());
      } else { // caso haja a necessidade de criacao de uma nova raiz
        // raiz anterior = pontEsq da nRaiz
        nRaiz = new No();
        nRaiz.p[0] = raiz.getPos();
        // nNo a ser criado aqui = pontDir da raiz
        nRaiz.p[1] = rtn.novoNo.getPos();
        // raiz.r[ordem/2] = registro da raiz
        nRaiz.no[0] = rtn.promovido.copy();
        nRaiz.setN(nRaiz.getN() + 1);

        indice.seek(indice.length());
        nRaiz.setPos(indice.getFilePointer());
        indice.write(nRaiz.toByteArray());
        indice.seek(0);
        indice.writeLong(nRaiz.getPos());
      }

      indice.seek(raiz.getPos());
      indice.write(raiz.toByteArray());

      indice.close();
    }
  }

  private Retorno add(Ponto p1, No no) throws Exception {
    short ordem = 8;
    No nNo = new No();
    Retorno rtn = new Retorno(); // retorno recursivo
    Retorno fim = new Retorno(); // retorno da funcao
    byte[] noBytes = new byte[152];
    Ponto tmp;
    long pontDir = -1;
    RandomAccessFile indice = new RandomAccessFile(path.toFile(), "rw");

    if (no.ehFolha()) {
      if (no.getN() >= ordem - 1) {
        if (p1.getId() < no.no[no.no.length - 1].id) {
          tmp = new Ponto();
          tmp = p1.copy();
          p1 = no.getPonto(ordem - 1).copy();
          no.setPonto(ordem - 1, tmp);
          no.ordena();
        }

        for (int i = ordem / 2 + 1; i < no.no.length; i++) {
          nNo.addPonto(no.no[i], no.p[i + 1]);
          no.no[i].reset();
          no.p[i + 1] = -1;
        }
        nNo.addPonto(p1, -1);
        no.setN(ordem / 2);

        fim.novoNo = nNo;
        fim.promovido = no.no[ordem / 2].copy();
        no.no[ordem / 2].reset();

        indice.seek(indice.length());
        nNo.setPos(indice.getFilePointer());
        indice.write(nNo.toByteArray());
        indice.seek(no.getPos());
        indice.write(no.toByteArray());
      } else {
        // caso o elemento caia na folha
        no.addPonto(p1, -1); // ponteiros de folhas sempre valem -1
        fim.promovido = null;
        fim.novoNo = null;
        indice.seek(no.getPos());
        indice.write(no.toByteArray());
      }
    } else {
      // Descobrir qual ponteiro do no seguir
      nNo.setPos(no.direcao(p1.getId()));
      indice.seek(nNo.getPos());
      indice.read(noBytes);
      nNo.fromByteArray(noBytes);
      indice.close();

      rtn = add(p1, nNo);

      indice = new RandomAccessFile(path.toFile(), "rw");
      if (rtn.promovido != null) {
        if (no.getN() < ordem - 1) {
          no.addPonto(rtn.promovido, rtn.novoNo.getPos());
          indice.seek(no.getPos());
          indice.write(no.toByteArray());
          fim.promovido = null;
          fim.novoNo = null;
        } else { // criar novo no
          nNo = new No();
          if (rtn.promovido.getId() < no.no[no.no.length - 1].id) {
            tmp = new Ponto();
            tmp = rtn.promovido.copy();
            rtn.promovido = no.getPonto(ordem - 1).copy();
            no.setPonto(ordem - 1, tmp);

            pontDir = no.p[no.p.length - 1];
            no.p[no.p.length - 1] = rtn.novoNo.getPos();
          } else pontDir = rtn.novoNo.getPos();
          no.ordena();

          nNo.p[0] = no.p[ordem / 2 + 1];
          no.p[ordem / 2 + 1] = -1;

          // preencher nNo
          for (int i = ordem / 2 + 1; i < no.no.length; i++) {
            nNo.addPonto(no.no[i], no.p[i + 1]);
            no.no[i].reset();
            no.p[i + 1] = -1;
          }
          nNo.addPonto(rtn.promovido, pontDir);
          no.setN(ordem / 2);

          fim.promovido = no.no[ordem / 2].copy();
          fim.novoNo = nNo;
          no.no[ordem / 2].reset();

          indice.seek(indice.length());
          nNo.setPos(indice.getFilePointer());
          indice.write(nNo.toByteArray());
          indice.seek(no.getPos());
          indice.write(no.toByteArray());
        }
      } else {
        fim.promovido = null;
        fim.novoNo = null;
      }
    }

    indice.close();
    return fim;
  }
}

/**
 * PONTO-------------------------------------------------------------
 */
class Ponto {

  protected int id;
  protected long pos;

  Ponto() {
    id = 0;
    pos = -1;
  }

  Ponto(int id, long pos) {
    this.id = id;
    this.pos = pos;
  }

  public void reset() {
    id = 0;
    pos = -1;
  }

  public int getId() {
    return this.id;
  }

  public Ponto copy() {
    Ponto copy = new Ponto();
    copy.id = this.id;
    copy.pos = this.pos;
    return copy;
  }

  public byte[] toByteArray() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(id);
    dos.writeLong(pos);
    return baos.toByteArray();
  }
}

/**
 * NO------------------------------------------------------------
 */
class No extends Ponto {

  private int ordem = 8;
  private int n = 0;
  private long pos = -1;
  long[] p = new long[ordem];
  Ponto[] no = new Ponto[ordem - 1]; //ate 7 pontos(ordem-1)

  No() {
    n = 0;
    for (int i = 0; i < (ordem - 1); i++) {
      p[i] = -1;
      no[i] = new Ponto();
    }
    p[ordem - 1] = -1;
  }

  No(long pos) {
    this.pos = pos;
    n = 0;
    for (int i = 0; i < (ordem - 1); i++) {
      p[i] = -1;
      no[i] = new Ponto();
    }
    p[ordem - 1] = -1;
  }

  public void addPonto(Ponto novo, long Dir) {
    if (n < ordem - 1) {
      no[n] = novo.copy();
      p[n + 1] = Dir;
      n++;
      ordena();
    }
  }

  public void ordena() {
    Ponto tmp = new Ponto();
    long pos_tmp;
    if (n > 1) {
      for (int i = 0; i < n - 1; i++) {
        if (no[n - 1].id < no[i].id) {
          tmp = no[n - 1].copy();
          no[n - 1] = no[i].copy();
          no[i] = tmp.copy();
          pos_tmp = p[n];
          p[n] = p[i + 1];
          p[i + 1] = pos_tmp;
        }
      }
    }
  }

  public boolean ehFolha() {
    if (p[0] == -1) return true;
    return false;
  }

  /* Define qual ponteiro de um no deve se seguir para encontrar um id */
  public long direcao(int id) {
    long end = -1;
    if (!ehFolha()) {
      end = p[n];
      for (int i = 0; i < n; i++) {
        if (id < no[i].id) end = p[i];
      }
    }
    return end;
  }

  public int getN() {
    return n;
  }

  public long getPos() {
    return pos;
  }

  public Ponto[] getPonto() {
    return no;
  }

  public Ponto getPonto(int pos) {
    return no[pos - 1];
  }

  public void setN(int n) {
    this.n = n;
  }

  public void setPos(long pos) {
    this.pos = pos;
  }

  public void setPonto(int pos, Ponto p) {
    no[pos - 1] = p.copy();
  }

  public byte[] toByteArray() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(n);
    for (int i = 0; i < ordem - 1; i++) {
      dos.writeLong(p[i]);
      dos.write(no[i].toByteArray());
    }
    dos.writeLong(p[ordem - 1]);
    return baos.toByteArray();
  }

  public void fromByteArray(byte[] ba) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    n = dis.readInt();
    for (int i = 0; i < ordem - 1; i++) {
      p[i] = dis.readLong();
      no[i].id = dis.readInt();
      no[i].pos = dis.readLong();
    }
    p[ordem - 1] = dis.readLong();
  }
}

class Retorno {

  No novoNo;
  Ponto promovido;

  Retorno() {
    novoNo = new No();
    promovido = new Ponto();
  }

  Retorno(No no, Ponto rg) {
    novoNo = no;
    promovido = rg;
  }
}
