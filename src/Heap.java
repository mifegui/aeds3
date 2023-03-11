// Créditos: Super Eric

class Heap {

  Heap(int tamanho) {
    array = new HeapUnit[tamanho];
    this.tamanho = tamanho;
  }

  private int tamanho;
  private HeapUnit[] array;

  public HeapUnit pegarMenor() {
    return array[0];
  }

  public void adicionarEmCimaDoMenorEOrganizar(Anime a) {
    int segmento = array[0].segmento;
    if (array[0].valor > a.getId()) {
      segmento++;
    }

    array[0] = new HeapUnit(segmento, a);
    _organizar(0);
  }

  /**
   * O menor valor será deletado.
   * Chame quando nao mais nada pra colocar no heap, o heap será diminuido.
   **/
  public void diminuirHeapEOrganizar() {
    if (tamanho == 0) {
      array[0] = null;
      return;
    }
    array[0] = array[tamanho - 1];
    tamanho--;
    _organizar(0);
  }

  // Retorna se adicionou.
  // Se retornar falso é porque está cheio
  // A lógica de verdade do heap é feita no adicionarEmCimaDoMenorEOrganizar
  public boolean adicionarSoAteCheio(Anime anime) {
    return _adicionarAteCheio(anime, 0);
  }

  private boolean _adicionarAteCheio(Anime anime, int i) {
    HeapUnit unit = new HeapUnit(0, anime);

    if (array[0] == null) {
      array[0] = unit;
      return true;
    }

    if (array[tamanho - 1] != null) {
      return false;
    }
    if (filhoEsquerdaDo(i).unit == null) {
      array[filhoEsquerdaDo(i).i] = unit;
      return true;
    }
    if (filhoDireitaDo(i).unit == null) {
      array[filhoDireitaDo(i).i] = unit;
      return true;
    }

    // Nao vai entrar na direita do heap se adicionar na esquerda
    return (
      _adicionarAteCheio(anime, filhoEsquerdaDo(i).i) ||
      _adicionarAteCheio(anime, filhoDireitaDo(i).i)
    );
  }

  public boolean organizarInicial() {
    // O slide fala essa formula ai
    // Também disse 'até a posição zero' mas o slide em si só afundou
    // até a partir da psosição do primeiro pai, que eh essa formula aí
    return _organizar((tamanho - 1) / 2);
  }

  private boolean esquerdaEhMenor(int i) {
    return (
      filhoEsquerdaDo(i).unit != null &&
      (
        filhoEsquerdaDo(i).unit.segmento < deIndice(i).unit.segmento ||
        filhoEsquerdaDo(i).unit.valor < deIndice(i).unit.valor
      )
    );
  }

  private boolean direitaEhMenor(int i) {
    return (
      filhoDireitaDo(i).unit != null &&
      (
        filhoDireitaDo(i).unit.segmento < deIndice(i).unit.segmento ||
        filhoDireitaDo(i).unit.valor < deIndice(i).unit.valor
      )
    );
  }

  private boolean _organizar(int i) {
    if (deIndice(i).unit == null) return true;

    if (direitaEhMenor(i)) {
      // Trocando o pai pelo filho esquerdo
      HeapUnit aux = deIndice(i).unit;
      int auxI = filhoDireitaDo(i).i;
      array[i] = filhoDireitaDo(i).unit;
      array[auxI] = aux;

      // Seguindo a 'descida' do HeapUnit pelo heap abaixo
      return _organizar(auxI);
    }

    if (esquerdaEhMenor(i)) {
      // Trocando o pai pelo filho esquerdo
      HeapUnit aux = deIndice(i).unit;
      int auxI = filhoEsquerdaDo(i).i;
      array[i] = filhoEsquerdaDo(i).unit;
      array[auxI] = aux;

      // Seguindo a 'descida' do HeapUnit pelo heap abaixo
      return _organizar(auxI);
    }

    // N tem ngm na direita ou na esquerda que é menor que o HeapUnit de i atual
    return true;
  }

  private HeapUnitReturn deIndice(int i) {
    HeapUnit unit = array[i];
    return new HeapUnitReturn(unit, i);
  }

  private HeapUnitReturn filhoDireitaDo(int i) {
    int unitI = i * 2 + 2;
    HeapUnit unit = unitI < tamanho ? array[unitI] : null;
    return new HeapUnitReturn(unit, unitI);
  }

  private HeapUnitReturn paiDo(int i) {
    int unitI = ((i - 1) / 2);
    HeapUnit unit = array[unitI];
    return new HeapUnitReturn(unit, unitI);
  }

  private HeapUnitReturn filhoEsquerdaDo(int i) {
    int unitI = (i * 2 + 1);
    HeapUnit unit = unitI < tamanho ? array[unitI] : null;
    return new HeapUnitReturn(unit, unitI);
  }
}

class HeapUnit {

  int segmento;
  int valor;
  Anime anime;

  HeapUnit(int segmento, Anime anime) {
    this.segmento = segmento;
    this.valor = anime.getId();
    this.anime = anime;
  }
}

class HeapUnitReturn {

  HeapUnit unit;
  int i;

  HeapUnitReturn(HeapUnit unit, int i) {
    this.unit = unit;
    this.i = i;
  }
}
