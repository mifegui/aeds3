import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class KMP {

  /**
   * Identifica quantas vezes o padrao desejado aparece no arquivo
   * @param padrao
   * @param arq
   * @return quantidade de padroes encontrados em arq
   * @throws IOException
   */
  public static int encontrar(String padrao, RandomAccessFile arq)
    throws IOException {
    return encontrar(padrao, arq, false, false);
  }

  /**
   * Permite definir condicao de parada da leitura do arquivo
   * @param ocorrenciaUnica false: arquivo eh lido ate o fim
   *                        true: condicao de parada eh a primeira ocorrencia do padrao
   */
  public static int encontrar(
    String padrao,
    RandomAccessFile arq,
    boolean ocorrenciaUnica
  ) throws IOException {
    return encontrar(padrao, arq, ocorrenciaUnica, true);
  }

  /**
   * @param comparar true: retorno eh a quantidade de comparacoes; false: retorno eh a quantidade de padroes identificados
   */
  protected static int encontrar(
    String padrao,
    RandomAccessFile arq,
    boolean ocorrenciaUnica,
    boolean comparar
  ) throws IOException {
    int encontrados = 0, comparacoes = 0, estado = 0;
    String texto = arqToString(arq);
    int[] transicao = transicaoFalha(padrao);

    for (int i = 0; i < texto.length(); i++) {
      /* volta estados anteriores ate que encontre um que satisfaca a entrada do texto  */
      while (estado > 0 && texto.charAt(i) != padrao.charAt(estado)) {
        estado = transicao[estado - 1];
        comparacoes++;
      }

      if (texto.charAt(i) == padrao.charAt(estado)) {
        estado++;
      } else estado = 0;

      comparacoes++;

      if (estado == padrao.length()) { // se padrao for encontrado
        encontrados++;
        estado = transicao[estado - 1];
        if (ocorrenciaUnica) i = texto.length();
      }
    }
    return (comparar) ? comparacoes : encontrados;
  }

  /**
   * Constroi a transicao de falha do padrao a ser encontrado
   * @param padrao
   * @return diagrama de estados do padrao
   */
  protected static int[] transicaoFalha(String padrao) {
    int[] vetorFalha = new int[padrao.length()];

    vetorFalha[0] = 0;
    for (int i = 1, j; i < vetorFalha.length; i++) {
      j = vetorFalha[i - 1];

      /* Percorre pelo "automato" ate encontrar onde o carater na posicao i possa ser encaixado */
      while (j > 0 && padrao.charAt(i) != padrao.charAt(j)) {
        j = vetorFalha[j - 1];
      }

      if (padrao.charAt(i) == padrao.charAt(j)) {
        vetorFalha[i] = j + 1;
      } else vetorFalha[i] = 0; // se o carater nao eh prefixo
    }

    return vetorFalha;
  }

  /**
   * Passa todo o arquivo para memoria primaria
   * @param arq
   * @return
   * @throws IOException
   */
  private static String arqToString(RandomAccessFile arq) throws IOException {
    StringBuilder s = new StringBuilder(100000);
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(arq.getFD()))
    );
    char[] buffer = new char[100000];
    int bytesRead;
    while ((bytesRead = reader.read(buffer)) != -1) {
      s.append(buffer, 0, bytesRead);
    }
    return s.toString();
  }
}
