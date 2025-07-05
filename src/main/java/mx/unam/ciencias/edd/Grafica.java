package mx.unam.ciencias.edd;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.event.ListSelectionListener;

/**
 * Clase para gráficas. Una gráfica es un conjunto de vértices y aristas, tales
 * que las aristas son un subconjunto del producto cruz de los vértices.
 */
public class Grafica<T> implements Coleccion<T> {

    /* Clase interna privada para iteradores. */
    private class Iterador implements Iterator<T> {

        /* Iterador auxiliar. */
        private Iterator<Vertice> iterador;

        /* Construye un nuevo iterador, auxiliándose de la lista de vértices. */
        public Iterador() {
            iterador = vertices.iterator();
        }

        /* Nos dice si hay un siguiente elemento. */
        @Override public boolean hasNext() {
            return iterador.hasNext();
        }

        /* Regresa el siguiente elemento. */
        @Override public T next() {
            return iterador.next().elemento;
        }
    }

    /* Clase interna privada para vértices. */
    private class Vertice implements VerticeGrafica<T>,
                          ComparableIndexable<Vertice> {

        /* El elemento del vértice. */
        private T elemento;
        /* El color del vértice. */
        private Color color;
        /* La distancia del vértice. */
        private double distancia;
        /* El índice del vértice. */
        private int indice;
        /* El diccionario de vecinos del vértice. */
        private Diccionario<T, Vecino> vecinos;

        /* Crea un nuevo vértice a partir de un elemento. */
        public Vertice(T elemento) {
            this.elemento = elemento;
            this.color = Color.NINGUNO;
            this.vecinos = new Diccionario<>();
        }

        /* Regresa el elemento del vértice. */
        @Override public T get() {
            return elemento;
        }

        /* Regresa el grado del vértice. */
        @Override public int getGrado() {
            return vecinos.getElementos();
        }

        /* Regresa el color del vértice. */
        @Override public Color getColor() {
            return color;
        }

        /* Regresa un iterable para los vecinos. */
        @Override public Iterable<? extends VerticeGrafica<T>> vecinos() {
            return vecinos;
        }

        /* Define el índice del vértice. */
        @Override public void setIndice(int indice) {
            this.indice = indice;
        }

        /* Regresa el índice del vértice. */
        @Override public int getIndice() {
            return indice;
        }

        /* Compara dos vértices por distancia. */
        @Override public int compareTo(Vertice vertice) {
            return Double.compare(distancia, vertice.distancia);
        }
    }

    /* Clase interna privada para vértices vecinos. */
    private class Vecino implements VerticeGrafica<T> {

        /* El vértice vecino. */
        public Vertice vecino;
        /* El peso de la arista conectando al vértice con su vértice vecino. */
        public double peso;

        /* Construye un nuevo vecino con el vértice recibido como vecino y el
         * peso especificado. */
        public Vecino(Vertice vecino, double peso) {
            this.vecino = vecino;
            this.peso = peso;
        }

        /* Regresa el elemento del vecino. */
        @Override public T get() {
            return vecino.get();
        }

        /* Regresa el grado del vecino. */
        @Override public int getGrado() {
            return vecino.vecinos.getElementos();
        }

        /* Regresa el color del vecino. */
        @Override public Color getColor() {
            return vecino.color;
        }

        /* Regresa un iterable para los vecinos del vecino. */
        @Override public Iterable<? extends VerticeGrafica<T>> vecinos() {
            return vecino.vecinos;
        }
    }

    /* Interface para poder usar lambdas al buscar el elemento que sigue al
     * reconstruir un camino. */
    @FunctionalInterface
    private interface BuscadorCamino<T> {
        /* Regresa true si el vértice se sigue del vecino. */
        public boolean seSiguen(Grafica<T>.Vertice v, Grafica<T>.Vecino a);
    }

    /* Vértices. */
    private Diccionario<T, Vertice> vertices;
    /* Número de aristas. */
    private int aristas;

    /**
     * Constructor único.
     */
    public Grafica() {
        vertices = new Diccionario<>();
        aristas = 0;
    }

    /**
     * Regresa el número de elementos en la gráfica. El número de elementos es
     * igual al número de vértices.
     * @return el número de elementos en la gráfica.
     */
    @Override public int getElementos() {
        return vertices.getElementos();
    }

    /**
     * Regresa el número de aristas.
     * @return el número de aristas.
     */
    public int getAristas() {
        return aristas;
    }

    /**
     * Agrega un nuevo elemento a la gráfica.
     * @param elemento el elemento a agregar.
     * @throws IllegalArgumentException si el elemento ya había sido agregado a
     *         la gráfica.
     */
    @Override public void agrega(T elemento) {
        if(elemento == null || contiene(elemento))
            throw new IllegalArgumentException("Elemento repetido");
        vertices.agrega(elemento, new Vertice(elemento));
    }

    /**
     * Conecta dos elementos de la gráfica. Los elementos deben estar en la
     * gráfica. El peso de la arista que conecte a los elementos será 1.
     * @param a el primer elemento a conectar.
     * @param b el segundo elemento a conectar.
     * @throws NoSuchElementException si a o b no son elementos de la gráfica.
     * @throws IllegalArgumentException si a o b ya están conectados, o si a es
     *         igual a b.
     */
    public void conecta(T a, T b) {
        conecta(a, b, 1);
    }

    /**
     * Conecta dos elementos de la gráfica. Los elementos deben estar en la
     * gráfica.
     * @param a el primer elemento a conectar.
     * @param b el segundo elemento a conectar.
     * @param peso el peso de la nueva vecino.
     * @throws NoSuchElementException si a o b no son elementos de la gráfica.
     * @throws IllegalArgumentException si a o b ya están conectados, si a es
     *         igual a b, o si el peso es no positivo.
     */
    public void conecta(T a, T b, double peso) {
        if(peso < 0)
            throw new IllegalArgumentException("ya estan conectados");
        if(a.equals(b))
            throw new IllegalArgumentException("Elementos iguales");
        Vertice u = vertices.get(a);
        Vertice v = vertices.get(b);
        if(u.vecinos.contiene(b))
            throw new IllegalArgumentException("ya estan conectados");
        u.vecinos.agrega(b, new Vecino(v, peso));
        v.vecinos.agrega(a, new Vecino(u, peso));
        aristas++;
    }

    /**
     * Desconecta dos elementos de la gráfica. Los elementos deben estar en la
     * gráfica y estar conectados entre ellos.
     * @param a el primer elemento a desconectar.
     * @param b el segundo elemento a desconectar.
     * @throws NoSuchElementException si a o b no son elementos de la gráfica.
     * @throws IllegalArgumentException si a o b no están conectados.
     */
    public void desconecta(T a, T b) {
        Vertice u = vertices.get(a);
        Vertice v = vertices.get(b);
        if(!u.vecinos.contiene(b))
            throw new IllegalArgumentException("no estan conectados");
        u.vecinos.elimina(b);
        v.vecinos.elimina(a);
        aristas--;
    }

    /**
     * Nos dice si el elemento está contenido en la gráfica.
     * @return <code>true</code> si el elemento está contenido en la gráfica,
     *         <code>false</code> en otro caso.
     */
    @Override public boolean contiene(T elemento) {
        return verticE(elemento) != null;
    }

    private Vecino vecino(Vertice v, Vertice u){
        for(Vecino x :v.vecinos)
            if(x.get().equals(u.elemento))
                return x;
        return null;
    }

    /**
     * Elimina un elemento de la gráfica. El elemento tiene que estar contenido
     * en la gráfica.
     * @param elemento el elemento a eliminar.
     * @throws NoSuchElementException si el elemento no está contenido en la
     *         gráfica.
     */
    @Override public void elimina(T elemento) {
        Vertice v = vertices.get(elemento);
        aristas = aristas-v.getGrado();
        for(Vecino u : v.vecinos){
            u.vecino.vecinos.elimina(elemento);
        }
        vertices.elimina(elemento);
    }

    /**
     * Nos dice si dos elementos de la gráfica están conectados. Los elementos
     * deben estar en la gráfica.
     * @param a el primer elemento.
     * @param b el segundo elemento.
     * @return <code>true</code> si a y b son vecinos, <code>false</code> en otro caso.
     * @throws NoSuchElementException si a o b no son elementos de la gráfica.
     */
    public boolean sonVecinos(T a, T b) {
        Vertice u = (Vertice)vertice(a);
        Vertice v = (Vertice)vertice(b);
        return vecino(u, v) != null;
    }

    /**
     * Regresa el peso de la arista que comparten los vértices que contienen a
     * los elementos recibidos.
     * @param a el primer elemento.
     * @param b el segundo elemento.
     * @return el peso de la arista que comparten los vértices que contienen a
     *         los elementos recibidos.
     * @throws NoSuchElementException si a o b no son elementos de la gráfica.
     * @throws IllegalArgumentException si a o b no están conectados.
     */
    public double getPeso(T a, T b) {
        Vertice u = (Vertice)vertice(a);
        Vertice v = (Vertice)vertice(b);
        Vecino p = vecino(v, u);
        if(p == null)
            throw new IllegalArgumentException();
        return p.peso;
    }

    /**
     * Define el peso de la arista que comparten los vértices que contienen a
     * los elementos recibidos.
     * @param a el primer elemento.
     * @param b el segundo elemento.
     * @param peso el nuevo peso de la arista que comparten los vértices que
     *        contienen a los elementos recibidos.
     * @throws NoSuchElementException si a o b no son elementos de la gráfica.
     * @throws IllegalArgumentException si a o b no están conectados, o si peso
     *         es menor o igual que cero.
     */
    public void setPeso(T a, T b, double peso) {
        Vertice u = (Vertice)vertice(a);
        Vertice v = (Vertice)vertice(b);
        Vecino x = vecino(v, u);
        Vecino y = vecino(u, v);
        if(x == null || peso <0)
            throw new IllegalArgumentException("No hay arista");
        x.peso = peso;
        y.peso = peso;
    }

    /**
     * Regresa el vértice correspondiente el elemento recibido.
     * @param elemento el elemento del que queremos el vértice.
     * @throws NoSuchElementException si elemento no es elemento de la gráfica.
     * @return el vértice correspondiente el elemento recibido.
     */
    public VerticeGrafica<T> vertice(T elemento) {
        VerticeGrafica<T> v = verticE(elemento);
        if(v == null)
            throw new NoSuchElementException();
        return v;
    }

    private VerticeGrafica<T> verticE(T elemento){
        for(Vertice a : vertices)
            if(a.elemento.equals(elemento))
                return a;
        return null;

    }

    /**
     * Define el color del vértice recibido.
     * @param vertice el vértice al que queremos definirle el color.
     * @param color el nuevo color del vértice.
     * @throws IllegalArgumentException si el vértice no es válido.
     */
    public void setColor(VerticeGrafica<T> vertice, Color color) {
        if(vertice == null ||
           vertice.getClass()!=Vertice.class &&
           vertice.getClass() != Vecino.class)
            throw new IllegalArgumentException();
        if(vertice.getClass() == Vertice.class){
            Vertice v = (Vertice)vertice;
            v.color = color;
        }
        if(vertice.getClass() == Vecino.class){
            Vecino u = (Vecino)vertice;
            u.vecino.color = color;
        }
    }

    /**
     * Nos dice si la gráfica es conexa.
     * @return <code>true</code> si la gráfica es conexa, <code>false</code> en
     *         otro caso.
     */
    public boolean esConexa() {
        int c = 0;
        paraCadaVertice(v -> setColor(v, Color.ROJO));
        for(Vertice v : vertices){
            if(v.color == Color.ROJO){
                bfsA(v.elemento);
                c++;
            }
        }
        paraCadaVertice(v -> setColor(v, Color.NINGUNO));
        return c == 1;
    }

    private void bfsA(T elemento) {
        Vertice u = (Vertice) vertice(elemento);
        paraCadaVertice(x -> setColor(x, Color.ROJO));
        u.color = Color.NEGRO;
        Cola<Vertice> cola = new Cola<>();
        cola.mete(u);
        while (!cola.esVacia()) {
            Vertice v = cola.saca();
            for (Vecino w : v.vecinos)
                if (w.getColor() == Color.ROJO) {
                    w.vecino.color = Color.NEGRO;
                    cola.mete(w.vecino);
                }
        }
    }

    /**
     * Realiza la acción recibida en cada uno de los vértices de la gráfica, en
     * el orden en que fueron agregados.
     * @param accion la acción a realizar.
     */
    public void paraCadaVertice(AccionVerticeGrafica<T> accion) {
        for(Vertice v:vertices)
            accion.actua(v);
    }

    /**
     * Realiza la acción recibida en todos los vértices de la gráfica, en el
     * orden determinado por BFS, comenzando por el vértice correspondiente al
     * elemento recibido. Al terminar el método, todos los vértices tendrán
     * color {@link Color#NINGUNO}.
     * @param elemento el elemento sobre cuyo vértice queremos comenzar el
     *        recorrido.
     * @param accion la acción a realizar.
     * @throws NoSuchElementException si el elemento no está en la gráfica.
     */
    public void bfs(T elemento, AccionVerticeGrafica<T> accion) {
        Vertice v = (Vertice)vertice(elemento);
        if(v == null)
            throw new NoSuchElementException();
        paraCadaVertice(y -> setColor(y, Color.ROJO));
        Cola<Vertice> c = new Cola<>();
        v.color = Color.NEGRO;
        c.mete(v);
        while(!c.esVacia()){
            Vertice u = c.saca();
            accion.actua(u);
            for(Vecino x:u.vecinos)
                if(x.getColor() == Color.ROJO){
                    x.vecino.color = Color.NEGRO;
                    c.mete(x.vecino);
                }
        }
        paraCadaVertice(y -> setColor(y, Color.NINGUNO));
    }

    /**
     * Realiza la acción recibida en todos los vértices de la gráfica, en el
     * orden determinado por DFS, comenzando por el vértice correspondiente al
     * elemento recibido. Al terminar el método, todos los vértices tendrán
     * color {@link Color#NINGUNO}.
     * @param elemento el elemento sobre cuyo vértice queremos comenzar el
     *        recorrido.
     * @param accion la acción a realizar.
     * @throws NoSuchElementException si el elemento no está en la gráfica.
     */
    public void dfs(T elemento, AccionVerticeGrafica<T> accion) {
        Vertice v = (Vertice)vertice(elemento);
        if(v == null)
            throw new NoSuchElementException();
        paraCadaVertice(x -> setColor(x, Color.ROJO));
        Pila<Vertice> p = new Pila<>();
        v.color = Color.NEGRO;
        p.mete(v);
        while(!p.esVacia()){
            Vertice u = p.saca();
            accion.actua(u);
            for(Vecino x:u.vecinos)
                if(x.getColor() == Color.ROJO){
                    x.vecino.color = Color.NEGRO;
                    p.mete(x.vecino);
                }
        }
        paraCadaVertice(x -> setColor(x, Color.NINGUNO));
    }

    /**
     * Nos dice si la gráfica es vacía.
     * @return <code>true</code> si la gráfica es vacía, <code>false</code> en
     *         otro caso.
     */
    @Override public boolean esVacia() {
        return vertices.esVacia();
    }

    /**
     * Limpia la gráfica de vértices y aristas, dejándola vacía.
     */
    @Override public void limpia() {
        vertices.limpia();
        aristas = 0;
    }

    /**
     * Regresa una representación en cadena de la gráfica.
     * @return una representación en cadena de la gráfica.
     */
    @Override public String toString() {
        paraCadaVertice((v -> setColor(v, Color.ROJO)));
        String s = "{";
        for(Vertice v:vertices)
            s+= String.format("%s, ", v.elemento);
        s += "}, {";
        for (Vertice v:vertices){
            for (Vecino u:v.vecinos){
                if(u.getColor() == Color.NINGUNO)
                    continue;
                s += String.format("(%s, %s), ", v.elemento, u.get());
            }
            v.color = Color.NINGUNO;
        }
        s += "}";
        return s;
    }

    /**
     * Nos dice si la gráfica es igual al objeto recibido.
     * @param objeto el objeto con el que hay que comparar.
     * @return <code>true</code> si la gráfica es igual al objeto recibido;
     *         <code>false</code> en otro caso.
     */
    @Override public boolean equals(Object objeto) {
        if (objeto == null || getClass() != objeto.getClass())
            return false;
        @SuppressWarnings("unchecked") Grafica<T> grafica = (Grafica<T>)objeto;
        if(aristas != grafica.aristas)
            return false;
        for(Vertice v:vertices){
            if(!grafica.contiene(v.elemento))
                return false;
            Vertice u = (Vertice)grafica.vertice(v.elemento);
            for(Vecino w : u.vecinos)
                if(!sonVecinos(v.get(), w.get()))
                    return false;
        }
        return true;
    }

    /**
     * Regresa un iterador para iterar la gráfica. La gráfica se itera en el
     * orden en que fueron agregados sus elementos.
     * @return un iterador para iterar la gráfica.
     */
    @Override public Iterator<T> iterator() {
        return new Iterador();
    }

    /**
     * Calcula una trayectoria de distancia mínima entre dos vértices.
     * @param origen el vértice de origen.
     * @param destino el vértice de destino.
     * @return Una lista con vértices de la gráfica, tal que forman una
     *         trayectoria de distancia mínima entre los vértices <code>a</code> y
     *         <code>b</code>. Si los elementos se encuentran en componentes conexos
     *         distintos, el algoritmo regresa una lista vacía.
     * @throws NoSuchElementException si alguno de los dos elementos no está en
     *         la gráfica.
     */
    public Lista<VerticeGrafica<T>> trayectoriaMinima(T origen, T destino) {
        Lista<VerticeGrafica<T>> l = new Lista<>();
        if(!contiene(origen) || !contiene(destino))
            throw new NoSuchElementException();
        Vertice o = (Vertice)vertice(origen);
        Vertice d = (Vertice)vertice(destino);
        if(origen.equals(destino)){
            l.agrega(o);
            return l;
        }
        for(Vertice v : vertices)
            v.distancia = Double.MAX_VALUE;
        o.distancia = 0;
        Cola<Vertice> c = new Cola<>();
        c.mete(o);
        while (!c.esVacia()) {
            Vertice v = c.saca();
            for (Vecino vecino : v.vecinos)
                if (vecino.vecino.distancia == Double.MAX_VALUE) {
                    vecino.vecino.distancia = v.distancia + 1;
                    c.mete(vecino.vecino);
                }
        }
        if(d.distancia == Double.MAX_VALUE)
            return l;
        l.agrega(d);
        reconstruye(l, d, origen, (u, v) -> u.distancia - v.vecino.distancia == 1.0);
        return l.reversa();
    }

    private void reconstruye(Lista<VerticeGrafica<T>> l, Vertice u, T o, BuscadorCamino<T> b){
        if(u.distancia == 0)
            return;
        Vertice r = u;
        for (Vecino v : u.vecinos){
            if (b.seSiguen(u, v)) {
                r = v.vecino;
                l.agrega(v.vecino);
                break;
            }
        }
        reconstruye(l, r, o, b);
    }
    /**
     * Calcula la ruta de peso mínimo entre el elemento de origen y el elemento
     * de destino.
     * @param origen el vértice origen.
     * @param destino el vértice destino.
     * @return una trayectoria de peso mínimo entre el vértice <code>origen</code> y
     *         el vértice <code>destino</code>. Si los vértices están en componentes
     *         conexas distintas, regresa una lista vacía.
     * @throws NoSuchElementException si alguno de los dos elementos no está en
     *         la gráfica.
     */
    public Lista<VerticeGrafica<T>> dijkstra(T origen, T destino) {
        Vertice o = (Vertice)vertice(origen);
        Vertice d = (Vertice)vertice(destino);
        for(Vertice v : vertices)
            v.distancia = Double.MAX_VALUE;

        o.distancia = 0;
        MonticuloDijkstra<Vertice> m = new MonticuloMinimo<>(vertices, vertices.getElementos());
        while(!m.esVacia()){
            Vertice u = m.elimina();
            for(Vecino v : u.vecinos)
                if(v.vecino.distancia > u.distancia + v.peso){
                    v.vecino.distancia = u.distancia + v.peso;
                    m.reordena(v.vecino);
                }
        }
        return re((u, v) -> v.vecino.distancia + v.peso == u.distancia, d);
    }
    private Lista<VerticeGrafica<T>> re(BuscadorCamino<T> b, Vertice d){
        Lista<VerticeGrafica<T>> l = new Lista<>();
        Vertice r = d;
        if(r.distancia == Double.MAX_VALUE)
            return l;
        l.agrega(r);
        while (r.distancia != 0) {
            for (Vecino v : r.vecinos)
                if (b.seSiguen(r, v)) {
                    l.agrega(v.vecino);
                    r = v.vecino;
                }
        }
        return l.reversa();
    }
}
