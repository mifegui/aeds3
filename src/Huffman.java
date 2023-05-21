import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Huffman {

  private int versao = 1;
  private String writePath;

  Huffman(String writePath) {
    this.writePath = writePath;
  }

  /**
   * Inicialização para compactação do arquivo, inclui escrita da frequencia de cada simbolo no novo arquivo
   * @param arq nome do arquivo a ser compactado
   * @return HashMap com todos os simbolos presentes no arquivo e suas representacoes em binario
   * @throws IOException
   */
  private HashMap<Byte, String> inicializar(
    String leitura,
    RandomAccessFile escrita
  ) throws IOException {
    HashMap<Byte, Integer> frequencia = frequenciaSimbolos(leitura);
    /* Escrever as frequencias para construcao de arvore na descompactacao */
    escrita.writeInt(frequencia.size());
    for (Map.Entry<Byte, Integer> entry : frequencia.entrySet()) {
      escrita.writeByte(entry.getKey());
      escrita.writeInt(entry.getValue());
    }
    HashMap<Byte, String> arvore = simboloToBits(construirArvore(frequencia));
    return arvore;
  }

  /**
   * Calculo da frequencia de cada simbolo
   * @param arq nome do arquivo a ser compactado
   * @return HashMap com todos os simbolos presentes no arquivo e suas frequencias no texto
   * @throws IOException
   */
  private HashMap<Byte, Integer> frequenciaSimbolos(String arq)
    throws IOException {
    HashMap<Byte, Integer> frequencia = new HashMap<>();
    RandomAccessFile file;
    byte bits;
    try {
      file = new RandomAccessFile(arq, "r");
    } catch (FileNotFoundException e) {
      System.out.println(
        "Arquivo de leitura para criação do HashMap não encontrado"
      );
      return null;
    }

    while (file.getFilePointer() < file.length()) {
      bits = file.readByte();

      if (frequencia.containsKey(bits)) {
        frequencia.replace(bits, frequencia.get(bits) + 1);
      } else {
        frequencia.put(bits, 1);
      }
    }
    file.close();
    return frequencia;
  }

  /**
   * Construcao da arvore de Huffman
   * @param frequencia HashMap com as frequencias dos simbolos
   * @return raiz da arvore
   */
  private NoHuffman construirArvore(HashMap<Byte, Integer> frequencia) {
    NoHuffman raiz = null;

    // Passar hash para Lista
    ArrayList<NoHuffman> lista = new ArrayList<>();
    for (Map.Entry<Byte, Integer> elem : frequencia.entrySet()) {
      lista.add(new NoHuffman(elem.getKey(), elem.getValue()));
    }

    // Construir arvore
    while (lista.size() > 1) {
      lista.sort(Comparator.comparingInt(NoHuffman::getFrequencia));
      raiz =
        new NoHuffman(
          lista.get(0),
          lista.get(1),
          lista.get(0).getFrequencia() + lista.get(1).getFrequencia()
        );
      lista.remove(1);
      lista.remove(0);
      lista.add(raiz);
    }
    return raiz;
  }

  /**
   * Definicao das representacoes de cada simbolo do texto
   * @param raiz da arvore de Huffman
   * @return HashMap com os simbolos do texto e suas representacoes
   */
  private HashMap<Byte, String> simboloToBits(NoHuffman raiz) {
    if (raiz == null) return null;
    return simboloToBits(raiz, "", new HashMap<Byte, String>(256));
  }

  private HashMap<Byte, String> simboloToBits(
    NoHuffman no,
    String codigo,
    HashMap<Byte, String> arvore
  ) {
    if (no.ehFolha()) {
      arvore.put(no.getSimbolo(), codigo);
    } else {
      simboloToBits(no.esq, codigo + "0", arvore);
      simboloToBits(no.dir, codigo + "1", arvore);
    }
    return arvore;
  }

  /**
   * Compactacao de arquivo
   * @param arqNome
   * @return false caso o arquivo para compactacao nao tenha sido encontrado
   * @throws IOException
   */
  public boolean compactar(String arqNome) throws IOException {
    RandomAccessFile arqRead = null;
    RandomAccessFile arqWrite;
    String arqNovo;
    HashMap<Byte, String> padroes;
    String[] partes;
    String bits = "";
    int bitsLen;
    byte byteEscrita;
    int byteInt;

    try {
      arqRead = new RandomAccessFile(arqNome, "r");
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo para compactação não encontrado");
      return false;
    }

    // Criacao do arquivo de compactacao
    partes = arqNome.split("\\.");
    arqNovo = partes[0] + "huffCompressao";
    arqNovo += (partes.length > 1) ? "." + partes[1] : "";
    arqWrite = new RandomAccessFile(this.writePath + arqNovo + "huffman", "rw");
    arqWrite.setLength(0);

    padroes = inicializar(arqNome, arqWrite);

    // Leitura do arqRead e escrita em arqWrite
    while (arqRead.getFilePointer() < arqRead.length()) {
      byteEscrita = arqRead.readByte();
      bits += padroes.get(byteEscrita);

      while (bits.length() >= 8) {
        byteInt = Integer.parseInt(bits.substring(0, 8), 2);
        arqWrite.write((byte) byteInt);
        bits = (bits.length() > 8) ? bits.substring(8) : "";
      }
    }

    // Escrita do ultimo byte e quantidade de bits validos nele
    if (bits.length() > 0) {
      bitsLen = bits.length();
      for (int i = 0; i < 8 - bitsLen; i++) bits += '0';
      byteInt = Integer.parseInt(bits, 2);
      arqWrite.write((byte) byteInt);
      arqWrite.write((byte) bitsLen);
    } else {
      arqWrite.write((byte) -1);
      arqWrite.write((byte) 0);
    }

    arqRead.close();
    arqWrite.close();
    return true;
  }

  /**
   * Passar arquivo em bytes para uma unica string
   * @param arqRead
   * @return arquivo em forma de string
   * @throws IOException
   */
  private String lerArquivoByte(RandomAccessFile arqRead) throws IOException {
    StringBuilder s = new StringBuilder("");
    byte b;
    while (arqRead.getFilePointer() < arqRead.length()) {
      b = arqRead.readByte();
      for (int i = 7; i >= 0; i--) {
        s.append(b >> i & 1);
      }
    }
    return s.toString();
  }

  /**
   * Descompactacao de arquivo
   * @param arqNome
   * @return false caso o arquivo para descompactacao nao tenha sido encontrado
   * @throws IOException
   */
  public boolean descompactar(String arqNome, String target)
    throws IOException {
    HashMap<Byte, Integer> frequencia = new HashMap<>();
    int tamanho;
    NoHuffman raiz, no;
    RandomAccessFile arqRead = null;
    RandomAccessFile arqWrite;
    String[] partes;
    String compactado;

    try {
      arqRead = new RandomAccessFile(arqNome, "r");
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo para descompactação não encontrado");
      return false;
    }

    /* Determinar a raiz da arvore de Huffman */
    tamanho = arqRead.readInt();
    for (int i = 0; i < tamanho; i++) {
      frequencia.put(arqRead.readByte(), arqRead.readInt());
    }
    raiz = construirArvore(frequencia);

    /* Inicializar arquivo de descompressao */
    arqWrite = new RandomAccessFile(target, "rw");
    arqWrite.setLength(0);

    /* Descomprimir */
    compactado = lerArquivoByte(arqRead);
    arqRead.close();
    tamanho =
      Integer.parseInt(compactado.substring(compactado.length() - 8), 2);
    no = raiz;
    for (int i = 0; i <= compactado.length() - 16 + tamanho; i++) {
      if (no.ehFolha()) {
        arqWrite.writeByte(no.getSimbolo());
        no = raiz;
        if (i < compactado.length() - 16 + tamanho) i--;
      } else {
        if (compactado.charAt(i) == '0') no = no.esq; else no = no.dir;
      }
    }

    arqWrite.close();
    return true;
  }
}

class NoHuffman {

  NoHuffman esq;
  NoHuffman dir;
  private boolean folha;
  private byte simbolo;
  private int frequencia;

  NoHuffman(byte simbolo, int frequencia) {
    esq = null;
    dir = null;
    folha = true;
    this.simbolo = simbolo;
    this.frequencia = frequencia;
  }

  NoHuffman(NoHuffman esq, NoHuffman dir, int frequencia) {
    this.esq = esq;
    this.dir = dir;
    folha = false;
    this.frequencia = frequencia;
  }

  public String toString() {
    return simbolo + ": " + frequencia + " ";
  }

  public boolean ehFolha() {
    return folha;
  }

  public int getFrequencia() {
    return frequencia;
  }

  public byte getSimbolo() {
    return simbolo;
  }
}
