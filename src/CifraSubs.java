/*
 Cada byte é deslocado 12 posições para a direita na tabela de substituição
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CifraSubs {

  private static String Tabela = criarTabelaSubstituicao();
  private static String Tabela_Cifrada = criarTabelaCifrada();

  private static String criarTabelaSubstituicao() {
    StringBuilder tabela = new StringBuilder();
    for (int i = 0; i < 256; i++) {
      tabela.append((char) i);
    }
    return tabela.toString();
  }

  private static String criarTabelaCifrada() {
    StringBuilder tabela = new StringBuilder();
    for (int i = 0; i < 256; i++) {
      tabela.append((char) ((i + 12) % 256));
    }
    return tabela.toString();
  }

  public static String criptografa(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      sb.append((char) cifrarByte((byte) s.charAt(i)));
    }

    return sb.toString();
  }

  public static String descriptografa(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      sb.append((char) descifrarByte((byte) s.charAt(i)));
    }
    return sb.toString();
  }

  /*
   * Cifra um byte utilizando a tabela cifrada
   */
  public static byte cifrarByte(byte b) {
    int index = Tabela.indexOf(b & 0xFF);
    if (index != -1) {
      return (byte) Tabela_Cifrada.charAt(index);
    }
    return b;
  }

  /*
   * Descifra um byte utilizando a tabela original
   */
  public static byte descifrarByte(byte b) {
    int index = Tabela_Cifrada.indexOf(b & 0xFF);
    if (index != -1) {
      return (byte) Tabela.charAt(index);
    }
    return b;
  }
}
