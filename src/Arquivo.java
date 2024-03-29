import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Arquivo {

  private IndiceArvoreB indiceArvoreB;
  private IndiceHash indiceHash;
  public ListaInvertida listaInvertida;
  public Huffman huffman;
  public LZW lzw;

  private int m = 5; // quantidade de registros por bloco (memoria primaria)
  private int n = 4; // quantidade de caminhos
  private static int ordem = 8; // ordem da arvore b

  Arquivo(String path) {
    this.stringPath = path;
    this.path = Paths.get(stringPath);
    this.indiceArvoreB = new IndiceArvoreB(stringPath + ".arvoreBIndice.db");
    try {
      this.indiceHash = new IndiceHash(stringPath + ".hashIndice.db");
    } catch (Exception e) {
      System.out.println(e);
    }
    this.listaInvertida = new ListaInvertida(stringPath + ".listaInvertida.db");
    this.huffman = new Huffman("../bd/banco.db.");
    this.lzw = new LZW("../bd/banco.db.");
  }

  private String stringPath;
  public Path path;

  public boolean alreadyExists() {
    try {
      return path.toFile().exists();
    } catch (Exception e) {
      return false;
    }
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
    this.indiceArvoreB.initializeFile();
    this.listaInvertida.initializeFile();
  }

  private Anime lastCreated;

  public Anime create(Anime anime) throws Exception {
    //pos(0) = tam arquivo(qnt); pos (1) = lapide; pos (2) = tam registro
    int posicao = 0;
    int tamanho = 0;
    int id = anime.getId();
    RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");

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

    lastCreated = anime;
    byte[] ba = anime.toByteArray();
    raf.seek(raf.length()); //mover para o fim do arquivo
    this.indiceArvoreB.add(new Ponto(anime.getId(), raf.getFilePointer()));
    this.indiceHash.addchaves(anime.getId(), (int) raf.getFilePointer());
    this.listaInvertida.addAiredToInvertedList(
        anime.getAired(),
        (int) raf.getFilePointer()
      );
    raf.writeChar(' ');
    raf.writeInt(ba.length); //escrever
    raf.write(ba); //escrever
    raf.close();

    return anime;
  }

  //---------------------------------------------------------READ

  public Anime read(int id) throws Exception {
    RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
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
    RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
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
            this.indiceArvoreB.add(new Ponto(novo.getId(), raf.length()));
            this.indiceHash.updateAddress(novo.getId(), (int) raf.length());
            raf.writeChar(' '); //lapide
            raf.writeInt(novoba.length); //tamanho
            raf.write(novoba);
            // result = delete(anime.getId());
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
    RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
    long pos;
    char lapide;
    int tamanho;
    Anime anime = new Anime();
    byte[] ba;
    int lid = raf.readInt();
    int idQueAcabouDeler = -1;
    while (idQueAcabouDeler != lid) { //enquanto nao chegar no fim do arquivo
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

  private Anime readFromPos(long pos) throws Exception {
    RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");

    if (pos == -1) return null;
    raf.seek(pos);
    char lapide = raf.readChar();
    if (lapide == '*') {
      return null;
    }
    byte[] ba = new byte[raf.readInt()];
    raf.read(ba);
    Anime a = new Anime();
    a.fromByteArray(ba);
    return a;
  }

  public Anime[] getFromInvList(String airedDate) throws Exception {
    int[] pos = this.listaInvertida.invertedListSearch(airedDate);
    Anime[] as = new Anime[pos.length];

    for (int i = 0; i < pos.length; i++) {
      if (pos[i] != 0) as[i] = readFromPos(pos[i]);
    }

    return as;
  }

  public Anime readFromHash(int id) throws Exception {
    long pos = this.indiceHash.search(id);
    return readFromPos(pos);
  }

  public Anime readFromBTree(int id) throws Exception {
    long pos = this.indiceArvoreB.read(id);
    return readFromPos(pos);
  }

  /** */
  public void intercalacaoComum() throws Exception {
    // Objeto para manipulação da base de dados
    RandomAccessFile dados = new RandomAccessFile(stringPath, "rw");

    // Array de objetos RandomAccessFile para arquivos intermediários na ordenação
    RandomAccessFile[] arqs = vetorArq(n);

    // Array de objetos Anime para registros a serem ordenados em memória primária
    Anime[] vetor = new Anime[m];

    // Variáveis para controle da ordenação
    int quant = 0; // Quantidade de blocos de registros lidos/ordenados
    int pos; // Posição até onde o vetor foi preenchido
    int inter; // Número de intercalações
    int posMenor; // Armazena posição do menor item do vetor
    int[] auxN; // Contador de registros de cada arquivo lido escritos em algum arquivo de escrita
    boolean pOrd; // Se parte que foi ordenada de um arquivo de leitura chegou ao fim
    int eof; // Quantidade de arquivos de leitura que chegaram ao fim

    // Variáveis para manipulação de bytes
    int len; // Tamanho do vetor de bytes de um Anime
    byte[] ba; // Vetor de bytes de um Anime

    // Variáveis para determinar qual arquivo é de leitura ou de escrita
    // (primeira ou segunda metade do array 'arqs')
    int auxR, auxW;

    // Objeto para manipulação dos dados ordenados
    RandomAccessFile ordenado = null;

    /* Reinicia todos os arquivos */
    for (int i = 0; i < 2 * n; i++) arqs[i].setLength(0);

    /* Distribuicao */
    dados.seek(0);
    int lastint = dados.readInt();

    while (dados.getFilePointer() < dados.length()) {
      // armazenar em memoria primaria m registros
      for (pos = 0; dados.getFilePointer() < dados.length() && pos < m; pos++) {
        if (dados.readChar() == ' ') {
          len = dados.readInt();
          ba = new byte[len];
          dados.read(ba);
          vetor[pos] = new Anime();
          vetor[pos].fromByteArray(ba);
        } else {
          len = dados.readInt();
          dados.skipBytes(len);
        }
      }

      // ordenar registros em memória primária
      heapsort(vetor, pos);

      // coloca m registros nos arquivos temporários
      for (int i = 0; i < pos; i++) {
        ba = vetor[i].toByteArray();
        arqs[quant % n].writeInt(ba.length);
        arqs[quant % n].write(ba);
      }

      quant++;
    }

    /* Intercalacoes */
    // arqs de leitura (arqsR) = (inter%2==0)?arqs[i]:arqs[n+i]; ou arqs[i+auxR]
    // arqs de escrita (arqsW) = (inter%2==0)?arqs[n+i]:arqs[i]; ou arqs[i+auxW]
    // parte ordenada de um arq = Math.pow(2, inter)* m;
    inter = 0;
    vetor = new Anime[n]; // armazenar proximo item a ser intercalado de cada arquivo de leitura
    quant = 2;
    auxN = new int[n];

    while (quant > 1) { // verifica se intercalacao teve somente uma 'passada' (todos os registros foram para somente um arquivo)
      if (inter % 2 == 0) {
        auxR = 0;
        auxW = n;
      } else {
        auxR = n;
        auxW = 0;
      }
      quant = 0;
      eof = 0;

      // reiniciar arqs para reutilizacao
      for (int i = 0; i < n; i++) arqs[i + auxW].setLength(0);
      // reposiciona ponteiro em cada arqsR
      for (int i = 0; i < n; i++) arqs[i + auxR].seek(0);

      while (eof < n) { // verifica se os arqR chegaram ao fim
        for (int i = 0; i < n; i++) auxN[i] = 0;
        pOrd = true;

        // atribuir valores iniciais para 'vetor'
        for (int i = 0; i < n; i++) {
          // verifica se chegou no fim desse arquivo de leitura
          if (arqs[i + auxR].getFilePointer() < arqs[i + auxR].length()) {
            len = arqs[i + auxR].readInt();
            ba = new byte[len];
            arqs[i + auxR].read(ba);
            vetor[i] = new Anime();
            vetor[i].fromByteArray(ba);
          } else {
            vetor[i] = null;
            auxN[i] = (int) (Math.pow(2, inter) * m); // garante que seja identificado que a parte ordenada acabou
          }
        }

        while (pOrd) { // verifica se acabou partes ordenadas de todos os arqR
          posMenor = posMenor(vetor);
          if (vetor[posMenor] != null) {
            ba = vetor[posMenor].toByteArray();
            arqs[(quant % n) + auxW].writeInt(ba.length);
            arqs[(quant % n) + auxW].write(ba);
            auxN[posMenor]++;
          }

          if (
            arqs[posMenor + auxR].getFilePointer() <
            arqs[posMenor + auxR].length() &&
            auxN[posMenor] < (Math.pow(2, inter) * m)
          ) {
            len = arqs[posMenor + auxR].readInt();
            ba = new byte[len];
            arqs[posMenor + auxR].read(ba);
            vetor[posMenor] = new Anime();
            vetor[posMenor].fromByteArray(ba);
          } else {
            vetor[posMenor] = null;
            auxN[posMenor] = (int) (Math.pow(2, inter) * m);
          }

          pOrd = false;
          for (int i = 0; i < n; i++) {
            if (auxN[i] < Math.pow(2, inter) * m) {
              pOrd = true;
              i = n;
            }
          }
        }

        // verificar quantos arqR chegaram ao fim
        for (int i = 0; i < n; i++) if (
          arqs[i + auxR].getFilePointer() >= arqs[i + auxR].length()
        ) eof++;
        ordenado = arqs[(quant % n) + auxW];
        quant++;
      }

      inter++;
    }

    // transferir dados ordenados para um arquivo com nome padronizado
    putTempFileDataIntoMainFile(ordenado, lastint);
    deleteTempArqs(n);
  }

  // Sobrescreve banco.db com conteúdo de temp
  // Arquivo temporário n tem ultimo id por isso vc tem que passar
  private void putTempFileDataIntoMainFile(RandomAccessFile temp, int lastint)
    throws Exception {
    temp.seek(0);
    path.toFile().delete();
    RandomAccessFile banco = new RandomAccessFile(stringPath, "rw");
    banco.writeInt(lastint); // lastid
    while (temp.getFilePointer() < temp.length()) {
      banco.writeChar(' '); // lapide
      int len = temp.readInt();
      banco.writeInt(len);
      byte[] ba = new byte[len];
      temp.read(ba);
      banco.write(ba);
    }
    banco.close();
  }

  public void intercalacaoVariavel() throws Exception {
    RandomAccessFile dados = new RandomAccessFile(stringPath, "rw"); // abre arquivo de dados
    RandomAccessFile[] arqs = vetorArq(n); // cria vetor de arquivos intermediários para ordenação
    Anime[] vetor = new Anime[m]; // cria vetor de registros a serem ordenados em memória primária
    int len; // tamanho em bytes de um registro Anime
    byte[] ba; // vetor de bytes de um registro Anime
    int quant = 0; // contador de blocos de registros lidos/ordenados
    int pos; // posição até onde o vetor foi preenchido
    int inter; // contador do número de intercalações
    int auxR, auxW; // índices que determinam quais arquivos são de leitura ou escrita (cada um é metade do vetor de arquivos temporários)
    int posMenor; // índice da posição do menor item no vetor
    Long auxPos; // posição em arquivo de leitura para retornar caso parte ordenada tenha acabado
    Anime aux; // valor do item lido para verificar se a parte ordenada acabou
    int auxN; // quantidade de arquivos que já tiveram suas partes ordenadas intercaladas
    int eof; // quantidade de arquivos de leitura que chegaram ao fim
    RandomAccessFile ordenado = null; // arquivo que possui os dados ordenados

    /* Limpa todos os arquivos */
    for (int i = 0; i < 2 * n; i++) arqs[i].setLength(0);

    /* Distribuicao */
    dados.seek(0);
    int lastInt = dados.readInt();

    while (dados.getFilePointer() < dados.length()) {
      // armazenar em memoria primaria m registros
      for (pos = 0; dados.getFilePointer() < dados.length() && pos < m; pos++) {
        if (dados.readChar() == ' ') {
          len = dados.readInt();
          ba = new byte[len];
          dados.read(ba);
          vetor[pos] = new Anime();
          vetor[pos].fromByteArray(ba);
        } else {
          len = dados.readInt();
          dados.skipBytes(len);
        }
      }

      // ordenacao
      heapsort(vetor, pos);

      // distribuicao
      for (int i = 0; i < pos; i++) {
        ba = vetor[i].toByteArray();
        arqs[quant % n].writeInt(ba.length);
        arqs[quant % n].write(ba);
      }

      quant++;
    }

    /* Intercalacoes */
    // arqs de leitura (arqsR) = (inter%2==0)?arqs[i]:arqs[n+i]; ou arqs[i+auxR]
    // arqs de escrita (arqsW) = (inter%2==0)?arqs[n+i]:arqs[i]; ou arqs[i+auxW]
    inter = 0;
    vetor = new Anime[n]; // armazenar proximo item a ser intercalado de cada arquivo de leitura

    while (quant > 1) { // verifica se intercalacao teve somente uma 'passada' (todos os registros foram para somente um arquivo)
      if (inter % 2 == 0) {
        auxR = 0;
        auxW = n;
      } else {
        auxR = n;
        auxW = 0;
      }
      quant = 0;
      eof = 0;

      // reiniciar arqs para reutilizacao
      for (int i = 0; i < n; i++) arqs[i + auxW].setLength(0);
      // reposiciona ponteiro em cada arqsR
      for (int i = 0; i < n; i++) arqs[i + auxR].seek(0);

      while (eof < n) { // verifica se os arqR chegaram ao fim
        auxN = 0;
        // atribuir valores iniciais para 'vetor'
        for (int i = 0; i < n; i++) {
          if (arqs[i + auxR].getFilePointer() < arqs[i + auxR].length()) {
            len = arqs[i + auxR].readInt();
            ba = new byte[len];
            arqs[i + auxR].read(ba);
            vetor[i] = new Anime();
            vetor[i].fromByteArray(ba);
          } else {
            vetor[i] = null;
          }
        }

        while (auxN < n) { // verifica se acabou partes ordenadas de todos os arqR
          posMenor = posMenor(vetor);

          if (vetor[posMenor] != null) {
            ba = vetor[posMenor].toByteArray();
            arqs[(quant % n) + auxW].writeInt(ba.length);
            arqs[(quant % n) + auxW].write(ba);
          }
          if (
            arqs[posMenor + auxR].getFilePointer() <
            arqs[posMenor + auxR].length()
          ) {
            // verificacao se ainda se esta na parte ordenada do arquivo
            auxPos = arqs[posMenor + auxR].getFilePointer();
            len = arqs[posMenor + auxR].readInt();
            ba = new byte[len];
            arqs[posMenor + auxR].read(ba);
            aux = new Anime();
            aux.fromByteArray(ba);
            if (aux.getId() < vetor[posMenor].getId()) {
              vetor[posMenor] = null;
              auxN++;
              arqs[posMenor + auxR].seek(auxPos);
            } else {
              vetor[posMenor] = aux;
            }
          } else {
            vetor[posMenor] = null;
            auxN++;
          }
        }

        // verificar quantos arqR chegaram ao fim
        for (int i = 0; i < n; i++) if (
          arqs[i + auxR].getFilePointer() >= arqs[i + auxR].length()
        ) eof++;

        ordenado = arqs[(quant % n) + auxW];
        quant++;
      }

      inter++;
    }

    putTempFileDataIntoMainFile(ordenado, lastInt);
    deleteTempArqs(n);
  }

  public void intercalacaoSubs() throws Exception {
    int quantosArquivosTemps = n;
    int tamanhoDoHeap = m;
    RandomAccessFile[] arqTemps = vetorArq(quantosArquivosTemps);

    Heap heap = new Heap(tamanhoDoHeap);
    Anime a = readOne();
    int quant = 0;
    RandomAccessFile tempRead = new RandomAccessFile(path.toFile(), "rw");
    int lastint = tempRead.readInt();
    tempRead.close();

    boolean podeAdicionarMais = heap.adicionarSoAteCheio(a);
    // Passo 1
    while (podeAdicionarMais) {
      podeAdicionarMais = heap.adicionarSoAteCheio(readOne());
    }

    // Passo 2
    heap.organizarInicial();

    // Pega do heap
    HeapUnit doheap = heap.pegarMenor();
    while (doheap != null) {
      // Coloca no arquivo temporário
      byte[] ba = doheap.anime.toByteArray();
      arqTemps[(quant + doheap.segmento) % n].writeInt(ba.length);
      arqTemps[(quant + doheap.segmento) % n].write(ba);
      quant++;

      // Adiciona outro no heap
      Anime animeDaBase = readOne();
      if (animeDaBase != null) heap.adicionarEmCimaDoMenorEOrganizar(
        animeDaBase
      ); else heap.diminuirHeapEOrganizar();

      // Pega do heap
      doheap = heap.pegarMenor();
    }

    // Array de objetos RandomAccessFile para arquivos intermediários na ordenação
    RandomAccessFile[] arqs = arqTemps;

    // Array de objetos Anime para registros a serem ordenados em memória primária
    Anime[] vetor = new Anime[m];

    int pos; // Posição até onde o vetor foi preenchido
    int inter; // Número de intercalações
    int posMenor; // Armazena posição do menor item do vetor
    int[] auxN; // Contador de registros de cada arquivo lido escritos em algum arquivo de escrita
    boolean pOrd; // Se parte que foi ordenada de um arquivo de leitura chegou ao fim
    int eof; // Quantidade de arquivos de leitura que chegaram ao fim

    // Variáveis para manipulação de bytes
    int len; // Tamanho do vetor de bytes de um Anime
    byte[] ba; // Vetor de bytes de um Anime

    // Variáveis para determinar qual arquivo é de leitura ou de escrita
    // (primeira ou segunda metade do array 'arqs')
    int auxR, auxW;

    // Objeto para manipulação dos dados ordenados
    RandomAccessFile ordenado = null;

    /* Intercalacoes */
    // arqs de leitura (arqsR) = (inter%2==0)?arqs[i]:arqs[n+i]; ou arqs[i+auxR]
    // arqs de escrita (arqsW) = (inter%2==0)?arqs[n+i]:arqs[i]; ou arqs[i+auxW]
    // parte ordenada de um arq = Math.pow(2, inter)* m;
    inter = 0;
    vetor = new Anime[n]; // armazenar proximo item a ser intercalado de cada arquivo de leitura
    quant = 2;
    auxN = new int[n];

    while (quant > 1) { // verifica se intercalacao teve somente uma 'passada' (todos os registros foram para somente um arquivo)
      if (inter % 2 == 0) {
        auxR = 0;
        auxW = n;
      } else {
        auxR = n;
        auxW = 0;
      }
      quant = 0;
      eof = 0;

      // reiniciar arqs para reutilizacao
      for (int i = 0; i < n; i++) arqs[i + auxW].setLength(0);
      // reposiciona ponteiro em cada arqsR
      for (int i = 0; i < n; i++) arqs[i + auxR].seek(0);

      while (eof < n) { // verifica se os arqR chegaram ao fim
        for (int i = 0; i < n; i++) auxN[i] = 0;
        pOrd = true;

        // atribuir valores iniciais para 'vetor'
        for (int i = 0; i < n; i++) {
          // verifica se chegou no fim desse arquivo de leitura
          if (arqs[i + auxR].getFilePointer() < arqs[i + auxR].length()) {
            len = arqs[i + auxR].readInt();
            ba = new byte[len];
            arqs[i + auxR].read(ba);
            vetor[i] = new Anime();
            vetor[i].fromByteArray(ba);
          } else {
            vetor[i] = null;
            auxN[i] = (int) (Math.pow(2, inter) * m); // garante que seja identificado que a parte ordenada acabou
          }
        }

        while (pOrd) { // verifica se acabou partes ordenadas de todos os arqR
          posMenor = posMenor(vetor);
          if (vetor[posMenor] != null) {
            ba = vetor[posMenor].toByteArray();
            arqs[(quant % n) + auxW].writeInt(ba.length);
            arqs[(quant % n) + auxW].write(ba);
            auxN[posMenor]++;
          }

          if (
            arqs[posMenor + auxR].getFilePointer() <
            arqs[posMenor + auxR].length() &&
            auxN[posMenor] < (Math.pow(2, inter) * m)
          ) {
            len = arqs[posMenor + auxR].readInt();
            ba = new byte[len];
            arqs[posMenor + auxR].read(ba);
            vetor[posMenor] = new Anime();
            vetor[posMenor].fromByteArray(ba);
          } else {
            vetor[posMenor] = null;
            auxN[posMenor] = (int) (Math.pow(2, inter) * m);
          }

          pOrd = false;
          for (int i = 0; i < n; i++) {
            if (auxN[i] < Math.pow(2, inter) * m) {
              pOrd = true;
              i = n;
            }
          }
        }

        // verificar quantos arqR chegaram ao fim
        for (int i = 0; i < n; i++) if (
          arqs[i + auxR].getFilePointer() >= arqs[i + auxR].length()
        ) eof++;
        ordenado = arqs[(quant % n) + auxW];
        quant++;
      }

      inter++;
    }
    // transferir dados ordenados para um arquivo com nome padronizado
    putTempFileDataIntoMainFile(ordenado, lastint);
    deleteTempArqs(n);
  }

  private void resetFile() {
    path.toFile().delete();
  }

  //* Favor ignorar - não sendo usado *//
  private Arquivo intercalarMagico(
    Arquivo[] praColocar,
    int iUm,
    int iDois,
    int colocando,
    Arquivo[] array
  ) throws Exception {
    if (colocando > praColocar.length - 1) {
      for (int i = 0; i < array.length; i++) {
        array[i].resetFile();
      }
      return intercalarMagico(array, 0, 1, 0, praColocar);
    }
    // Se nao tem mais arquivos pelos quais ler
    if (iUm > array.length) {
      Anime a = array[iDois].readOne();
      if (a == null) return intercalarMagico(
        praColocar,
        iUm,
        ++iDois,
        colocando,
        array
      );
      praColocar[colocando].create(a);
      return intercalarMagico(praColocar, iUm, iDois, colocando, array);
    }
    if (iDois > array.length) {
      Anime a = array[iUm].readOne();
      if (a == null) return intercalarMagico(
        praColocar,
        ++iUm,
        iDois,
        colocando,
        array
      );
      praColocar[colocando].create(a);
      return intercalarMagico(praColocar, iUm, iDois, colocando, array);
    }

    Arquivo um = array[iUm];
    Arquivo dois = array[iDois];

    Anime aum = um.readOne();
    Anime adois = dois.readOne();

    if (aum == null && adois == null) return intercalarMagico(
      praColocar,
      ++iUm,
      ++iDois,
      colocando,
      array
    );
    if (adois != null && adois.getId() < dois.lastCreated.getId()) {
      dois.readOneTracker = dois.readOnePreviousTracker;
      return intercalarMagico(praColocar, iUm, iDois, ++colocando, array);
    }
    if (aum != null & aum.getId() < um.lastCreated.getId()) {
      um.readOneTracker = um.readOnePreviousTracker;
      return intercalarMagico(praColocar, iUm, iDois, ++colocando, array);
    }

    if (aum == null) {
      praColocar[colocando].create(adois);
      return intercalarMagico(praColocar, ++iUm, iDois, colocando, array);
    }

    if (adois == null) {
      praColocar[colocando].create(aum);
      return intercalarMagico(praColocar, iUm, ++iDois, colocando, array);
    }

    if (aum.getId() < adois.getId()) {
      praColocar[colocando].create(aum);
    } else {
      praColocar[colocando].create(adois);
    }
    return intercalarMagico(praColocar, iUm, iDois, colocando, array);
  }

  private Anime readOne() throws Exception {
    RandomAccessFile raf = new RandomAccessFile(path.toString(), "rw");
    Anime a = _readOne(raf);
    raf.close();
    return a;
  }

  private long readOneTracker = -1;
  private long readOnePreviousTracker = -1;

  private Anime _readOne(RandomAccessFile raf) throws Exception {
    if (readOneTracker != -1) {
      raf.seek(readOneTracker);
    }
    if (raf.getFilePointer() == 0) {
      raf.readInt(); // Descartar primeiro inteiro de id
    }
    if (raf.getFilePointer() == raf.length()) {
      return null;
    }
    Anime a = new Anime();
    if (raf.readChar() == ' ') {
      int len = raf.readInt();
      byte[] ba = new byte[len];
      raf.read(ba);
      a.fromByteArray(ba);
    } else {
      int len = raf.readInt();
      raf.skipBytes(len);
      return _readOne(raf);
    }
    readOnePreviousTracker = readOneTracker;
    readOneTracker = raf.getFilePointer();

    return a;
  }

  /*
   * Cria um vetor com 2*n arquivos (necessarios para intercalacao)
   */
  static RandomAccessFile[] vetorArq(int n) throws Exception {
    RandomAccessFile[] arqs = new RandomAccessFile[2 * n];
    String s;
    for (int i = 0; i < 2 * n; i++) {
      s = "arq";
      s += i;
      arqs[i] = new RandomAccessFile(s, "rw");
    }

    return arqs;
  }

  static void deleteTempArqs(int n) {
    String s;
    for (int i = 0; i < 2 * n; i++) {
      s = "arq";
      s += i;
      new File(s).delete();
    }
  }

  /*
   * Identifica posicao do menor item em um vetor
   */
  private static int posMenor(Anime[] vetor) {
    int pos = 0;
    for (int i = 1; i < vetor.length; i++) {
      if (vetor[pos] == null) pos = i; else if (
        vetor[i] != null && vetor[i].getId() < vetor[pos].getId()
      ) pos = i;
    }
    return pos;
  }

  /*
   * Inverte os valores de duas posicoes de um vetor
   */
  static void swap(Anime[] vetor, int p1, int p2) {
    Anime tmp = vetor[p1];
    vetor[p1] = vetor[p2];
    vetor[p2] = tmp;
  }

  /*
   * @params pos = identifica ate que posicao o vetor esta preenchido com dados validos
   */
  static void heapsort(Anime[] v, int pos) {
    // Construcao do heap
    int i; // limita ate onde o vetor esta desordenado
    int k; // posicao do 'ponteiro' no vetor

    for (i = 1; i < pos; i++) {
      for (
        k = i;
        k > 0 && v[(k - 1) / 2].getId() < v[k].getId();
        k = (k - 1) / 2
      ) {
        swap(v, (k - 1) / 2, k);
      }
    }

    // Destruicao do heap
    for (i = pos - 1; i > 0; i--) {
      swap(v, i, 0);
      int posMaiorFilho;
      k = 0;

      for (int tam = i - 1; tam > 0 && tam >= (k * 2) + 1; tam--) {
        if (
          k * 2 + 2 > tam || v[(k * 2) + 1].getId() > v[(k * 2) + 2].getId()
        ) posMaiorFilho = k * 2 + 1; else posMaiorFilho = k * 2 + 2;
        if (v[posMaiorFilho].getId() > v[k].getId()) swap(v, posMaiorFilho, k);
      }
    }
  }
}
