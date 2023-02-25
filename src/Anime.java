import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Anime {

  private int id;
  private String name;
  private float score;
  private String[] genres;
  private int episodes;
  private String aired;

  public Anime() {
    this.id = -1;
    this.name = null;
    this.score = -1;
    this.genres = null;
    this.episodes = -1;
    this.aired = null;
  }

  public Anime(
    int id,
    String name,
    float score,
    String[] genres,
    int episodes,
    String aired
  ) {
    this.id = id;
    this.name = name;
    this.score = score;
    this.genres = genres;
    this.episodes = episodes;
    this.aired = aired;
  }

  public void print() {
    System.out.println("Nome: " + this.name);
    System.out.println("Data de exibição" + this.aired);
    System.out.println("Nota: " + this.score);
    System.out.println("Episódios: " + this.episodes);
    System.out.println("Gêneros: ");
    System.out.print("[ ");
    for (int i = 0; i < this.genres.length; i++) {
      System.out.print(this.genres[i]);
      System.out.print(" ]");
    }
  }

  /**
   * Serve para converter um objeto Anime em um arranjo de bytes.
   */

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    dos.writeInt(this.id);
    dos.writeUTF(this.name);
    dos.writeFloat(this.score);
    dos.writeShort(this.genres.length);
    for (int i = 0; i < this.genres.length; i++) {
      dos.writeUTF(this.genres[i]);
    }
    dos.writeShort(this.episodes);
    dos.writeUTF(this.aired);

    dos.close();
    baos.close();

    return baos.toByteArray();
  }

  /**
   * Serve para converter um arranjo de bytes em um objeto Anime.
   */
  public void fromByteArray(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba); // Cria um array de bytes
    DataInputStream dis = new DataInputStream(bais); // Cria um fluxo de dados

    this.id = dis.readInt();
    this.name = dis.readUTF();
    this.score = dis.readFloat();
    int len = dis.readShort(); // Quantidade de generos
    for (int i = 0; i < len; i++) {
      this.genres[i] = dis.readUTF();
    }
    this.episodes = dis.readShort();
    this.aired = dis.readUTF();
  }
}
