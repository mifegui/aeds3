import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZW {

  private int versao = 1;
  private String writePath;

  LZW(String writePath) {
    this.writePath = writePath;
  }

  private String byteToString(Byte b) {
    String bString = "";
    for (int i = 7; i >= 0; i--) {
      bString += b >> i & 1;
    }
    return bString;
  }

  private byte stringToByte(String s) {
    int valorInteiro = Integer.parseInt(s, 2);
    return (byte) valorInteiro;
  }

  /**
   * Inicializa o dicionario do arquivo a ser compactado
   * @param file
   * @return lista de todos os bytes presentes em file
   * @throws IOException
   */
  private ArrayList<String> dicionarioInicial(RandomAccessFile file)
    throws IOException {
    ArrayList<String> dicionario = new ArrayList<>();
    byte bits;
    String bString;

    while (file.getFilePointer() < file.length()) {
      bits = file.readByte();
      bString = byteToString(bits);

      if (!dicionario.contains(bString)) {
        dicionario.add(bString);
      }
    }
    return dicionario;
  }

  /**
   * Escreve o dicionario inicial no arquivo de compactacao
   * @param dicionario
   * @param file arquivo com o texto compactado
   * @throws IOException
   */
  private void dicionarioParaArquivo(
    ArrayList<String> dicionario,
    RandomAccessFile file
  ) throws IOException {
    file.writeInt(dicionario.size());

    for (int i = 0; i < dicionario.size(); i++) {
      file.writeByte(stringToByte(dicionario.get(i)));
    }
  }

  private ArrayList<String> arquivoParaDicionario(RandomAccessFile file)
    throws IOException {
    int tamanho = file.readInt();
    ArrayList<String> dicionario = new ArrayList<>(tamanho);

    for (int i = 0; i < tamanho; i++) {
      dicionario.add(byteToString(file.readByte()));
    }

    return dicionario;
  }

  /**
   * Compactacao de arquivo
   * @param arq
   * @return false caso o arq nao seja encontrado
   * @throws IOException
   */
  public boolean compactar(String arq) throws IOException {
    RandomAccessFile arqRead, arqWrite;
    String[] partes;
    ArrayList<String> dicionario;
    String prefixo = "";
    byte byteLido;
    int token = 0;

    try {
      arqRead = new RandomAccessFile(arq, "r");
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo para compactação não encontrado");
      return false;
    }

    // Inicializar novo arquivo
    partes = arq.split("\\.");
    arq = partes[0] + "lzwCompressao";
    arq += (partes.length > 1) ? "." + partes[1] : "";
    arqWrite = new RandomAccessFile(this.writePath + arq + "lzw", "rw");
    arqWrite.setLength(0);

    dicionario = dicionarioInicial(arqRead);
    dicionarioParaArquivo(dicionario, arqWrite);
    arqRead.seek(0);

    while (arqRead.getFilePointer() < arqRead.length()) {
      byteLido = arqRead.readByte();
      prefixo += byteToString(byteLido);
      if (dicionario.contains(prefixo)) {
        token = dicionario.indexOf(prefixo);
      } else {
        dicionario.add(prefixo);
        arqWrite.writeShort(token);
        prefixo = prefixo.substring(prefixo.length() - 8);
        token = dicionario.indexOf(prefixo);
      }
    }
    arqWrite.writeShort(token);

    arqRead.close();
    arqWrite.close();
    return true;
  }

  /**
   * Descompactacao de arquivo
   * @param arq
   * @return false caso o arq nao seja encontrado
   * @throws IOException
   */
  public boolean descompactar(String arq, String target) throws IOException {
    RandomAccessFile arqRead, arqWrite;
    String[] partes;
    ArrayList<String> dicionario;
    String prefixo = "", escrita = "";
    int token = 0;

    try {
      arqRead = new RandomAccessFile(arq, "r");
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo para descompactação não encontrado");
      return false;
    }

    /* Inicializar arquivo de descompressao */
    arqWrite = new RandomAccessFile(target, "rw");
    arqWrite.setLength(0);

    /* Descompactacao */
    dicionario = arquivoParaDicionario(arqRead);
    while (arqRead.getFilePointer() < arqRead.length()) {
      token = arqRead.readUnsignedShort();
      if (token == dicionario.size()) {
        escrita = prefixo + prefixo.substring(0, 8);
      } else escrita = dicionario.get(token);

      for (int i = 0; i < escrita.length(); i += 8) {
        arqWrite.writeByte(stringToByte(escrita.substring(i, i + 8)));
      }

      prefixo += escrita.substring(0, 8);

      if (dicionario.contains(prefixo)) {} else {
        dicionario.add(prefixo);
        prefixo = escrita;
      }
    }

    arqRead.close();
    arqWrite.close();
    return true;
  }
}
