package mx.unam.ciencias.edd;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Clase para montículos mínimos (<i>min heaps</i>).
 */
public class MonticuloMinimo<T extends ComparableIndexable<T>>
    implements Coleccion<T>, MonticuloDijkstra<T> {

    /* Clase interna privada para iteradores. */
    private class Iterador implements Iterator<T> {

        /* Índice del iterador. */
        private int indice;

        /* Nos dice si hay un siguiente elemento. */
        @Override public boolean hasNext() {
            return indice < elementos;
        }

        /* Regresa el siguiente elemento. */
        @Override public T next() {
            if(indice >= elementos)
                throw new NoSuchElementException();
            return arbol[indice++];
        }
    }

    /* Clase estática privada para adaptadores. */
    private static class Adaptador<T  extends Comparable<T>>
        implements ComparableIndexable<Adaptador<T>> {

        /* El elemento. */
        private T elemento;
        /* El índice. */
        private int indice;

        /* Crea un nuevo comparable indexable. */
        public Adaptador(T elemento) {
            this.elemento = elemento;
            indice = -1;
        }

        /* Regresa el índice. */
        @Override public int getIndice() {
            return indice;
        }

        /* Define el índice. */
        @Override public void setIndice(int indice) {
            this.indice = indice;
        }

        /* Compara un adaptador con otro. */
        @Override public int compareTo(Adaptador<T> adaptador) {
            return elemento.compareTo(adaptador.elemento);
        }
    }

    /* El número de elementos en el arreglo. */
    private int elementos;
    /* Usamos un truco para poder utilizar arreglos genéricos. */
    private T[] arbol;

    /* Truco para crear arreglos genéricos. Es necesario hacerlo así por cómo
       Java implementa sus genéricos; de otra forma obtenemos advertencias del
       compilador. */
    @SuppressWarnings("unchecked") private T[] nuevoArreglo(int n) {
        return (T[])(new ComparableIndexable[n]);
    }

    /**
     * Constructor sin parámetros. Es más eficiente usar {@link
     * #MonticuloMinimo(Coleccion)} o {@link #MonticuloMinimo(Iterable,int)},
     * pero se ofrece este constructor por completez.
     */
    public MonticuloMinimo() {
        arbol = nuevoArreglo(100);
    }

    /**
     * Constructor para montículo mínimo que recibe una colección. Es más barato
     * construir un montículo con todos sus elementos de antemano (tiempo
     * <i>O</i>(<i>n</i>)), que el insertándolos uno por uno (tiempo
     * <i>O</i>(<i>n</i> log <i>n</i>)).
     * @param coleccion la colección a partir de la cuál queremos construir el
     *                  montículo.
     */
    public MonticuloMinimo(Coleccion<T> coleccion) {
        this(coleccion, coleccion.getElementos());
    }

    /**
     * Constructor para montículo mínimo que recibe un iterable y el número de
     * elementos en el mismo. Es más barato construir un montículo con todos sus
     * elementos de antemano (tiempo <i>O</i>(<i>n</i>)), que el insertándolos
     * uno por uno (tiempo <i>O</i>(<i>n</i> log <i>n</i>)).
     * @param iterable el iterable a partir de la cuál queremos construir el
     *                 montículo.
     * @param n el número de elementos en el iterable.
     */
    public MonticuloMinimo(Iterable<T> iterable, int n) {
        arbol = nuevoArreglo(n);
        for(T t:iterable)
            agrega(t, elementos++);

        for(int i = (elementos/2) -1; i >= 0; i--)
            acomodaAbajo(i);
    }

    private void agrega(T e, int i){
        arbol[i] = e;
        e.setIndice(i);
    }

    /**
     * Agrega un nuevo elemento en el montículo.
     * @param elemento el elemento a agregar en el montículo.
     */
    @Override public void agrega(T elemento) {
        if (elementos == arbol.length) {
            T[] arbolGrande = nuevoArreglo(elementos * 2);
            for (int i = 0; i < elementos; i++)
                arbolGrande[i] = arbol[i];
            arbol = arbolGrande;
        }
        agrega(elemento, elementos);
        elementos++;
        acomodaArriba(elementos - 1);
    }

    private void acomodaArriba(int i){
        int p = (i - 1) / 2;
        if (i > 0 && arbol[i].compareTo(arbol[p]) < 0) {
            intercambia(i, p);
            acomodaArriba(p);
        }
    }

    private void intercambia(int i, int j){
        T t = arbol[i];
        arbol[i] = arbol[j];
        arbol[j] = t;
        arbol[i].setIndice(i);
        arbol[j].setIndice(j);

    }

    private void acomodaAbajo(int i){
        int hi = 2 * i + 1;
        int hd = 2 * i + 2;
        if(hi >= elementos && hd >= elementos)
            return;
        int menor = getHijoMenor(hi, hd);
        T hmenor = arbol[menor];
        T v = arbol[i];
        if (v.compareTo(hmenor) > 0) {
            intercambia(menor, i);
            acomodaAbajo(menor);
        }
    }

    private int getHijoMenor(int hi, int hd){
        if(hi >= elementos)
            return hd;
        if(hd >= elementos)
            return hi;
        if(hi < 0)
            return hd;
        return arbol[hi].compareTo(arbol[hd]) < 0 ? hi: hd;
    }

    /**
     * Elimina el elemento mínimo del montículo.
     * @return el elemento mínimo del montículo.
     * @throws IllegalStateException si el montículo es vacío.
     */
    @Override public T elimina() {
        if(esVacia())
            throw new IllegalStateException();
        T primer = arbol[0];
        intercambia(0, elementos-1);
        arbol[elementos-1].setIndice(-1);
        elementos--;
        acomodaAbajo(0);
        return primer;
    }

    /**
     * Elimina un elemento del montículo.
     * @param elemento a eliminar del montículo.
     */
    @Override public void elimina(T elemento) {
        if(elemento.getIndice() < 0 || elemento.getIndice() >= elementos)
            return;
        int o = elemento.getIndice();
        intercambia(o, elementos-1);
        elementos--;
        reordena(arbol[o]);
        arbol[elementos].setIndice(-1);
    }

    /**
     * Nos dice si un elemento está contenido en el montículo.
     * @param elemento el elemento que queremos saber si está contenido.
     * @return <code>true</code> si el elemento está contenido,
     *         <code>false</code> en otro caso.
     */
    @Override public boolean contiene(T elemento) {
        if(elemento.getIndice() < 0 || elemento.getIndice() >= elementos)
            return false;
        return arbol[elemento.getIndice()].equals(elemento);
    }

    /**
     * Nos dice si el montículo es vacío.
     * @return <code>true</code> si ya no hay elementos en el montículo,
     *         <code>false</code> en otro caso.
     */
    @Override public boolean esVacia() {
        return elementos == 0;
    }

    /**
     * Limpia el montículo de elementos, dejándolo vacío.
     */
    @Override public void limpia() {
        arbol = nuevoArreglo(elementos);
        elementos = 0;
    }

   /**
     * Reordena un elemento en el árbol.
     * @param elemento el elemento que hay que reordenar.
     */
    @Override public void reordena(T elemento) {
        acomodaArriba(elemento.getIndice());
        acomodaAbajo(elemento.getIndice());
    }

    /**
     * Regresa el número de elementos en el montículo mínimo.
     * @return el número de elementos en el montículo mínimo.
     */
    @Override public int getElementos() {
        return elementos;
    }

    /**
     * Regresa el <i>i</i>-ésimo elemento del árbol, por niveles.
     * @param i el índice del elemento que queremos, en <em>in-order</em>.
     * @return el <i>i</i>-ésimo elemento del árbol, por niveles.
     * @throws NoSuchElementException si i es menor que cero, o mayor o igual
     *         que el número de elementos.
     */
    @Override public T get(int i) {
        if(i<0 || i>= elementos)
            throw new NoSuchElementException();
        return arbol[i];
    }

    /**
     * Regresa una representación en cadena del montículo mínimo.
     * @return una representación en cadena del montículo mínimo.
     */
    @Override public String toString() {
        String r = "";
        for(T t: arbol)
            r+= t.toString() + ", ";
        return r;
    }

    /**
     * Nos dice si el montículo mínimo es igual al objeto recibido.
     * @param objeto el objeto con el que queremos comparar el montículo mínimo.
     * @return <code>true</code> si el objeto recibido es un montículo mínimo
     *         igual al que llama el método; <code>false</code> en otro caso.
     */
    @Override public boolean equals(Object objeto) {
        if (objeto == null || getClass() != objeto.getClass())
            return false;
        @SuppressWarnings("unchecked") MonticuloMinimo<T> monticulo =
            (MonticuloMinimo<T>)objeto;
        if(elementos != monticulo.getElementos())
            return false;
        for(int i = 0; i < elementos; i++)
            if(!arbol[i].equals(monticulo.arbol[i]))
                return false;
        return true;
    }

    /**
     * Regresa un iterador para iterar el montículo mínimo. El montículo se
     * itera en orden BFS.
     * @return un iterador para iterar el montículo mínimo.
     */
    @Override public Iterator<T> iterator() {
        return new Iterador();
    }

    /**
     * Ordena la colección usando HeapSort.
     * @param <T> tipo del que puede ser el arreglo.
     * @param coleccion la colección a ordenar.
     * @return una lista ordenada con los elementos de la colección.
     */
    public static <T extends Comparable<T>>
    Lista<T> heapSort(Coleccion<T> coleccion) {
        Lista<Adaptador<T>> l = new Lista<>();
        for(T t: coleccion)
            l.agrega(new Adaptador<T>(t));
        Lista<T> l2 = new Lista<>();
        MonticuloMinimo<Adaptador<T>> m = new MonticuloMinimo<>(l);
        while(!m.esVacia()){
            Adaptador<T> e = m.elimina();
            l2.agrega(e.elemento);
        }
        return l2;
    }
}
