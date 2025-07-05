package mx.unam.ciencias.edd;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Clase para diccionarios (<em>hash tables</em>). Un diccionario generaliza el
 * concepto de arreglo, mapeando un conjunto de <em>llaves</em> a una colección
 * de <em>valores</em>.
 */
public class Diccionario<K, V> implements Iterable<V> {

    /* Clase interna privada para entradas. */
    private class Entrada {

        /* La llave. */
        public K llave;
        /* El valor. */
        public V valor;

        /* Construye una nueva entrada. */
        public Entrada(K llave, V valor) {
            this.llave = llave;
            this.valor = valor;
        }
    }

    /* Clase interna privada para iteradores. */
    private class Iterador {

        /* En qué lista estamos. */
        private int indice;
        /* Iterador auxiliar. */
        private Iterator<Entrada> iterador;

        /* Construye un nuevo iterador, auxiliándose de las listas del
         * diccionario. */
        public Iterador() {
            for(int i= 0; i < entradas.length; i++)
                if(entradas[i] != null){
                    iterador = entradas[i].iterator();
                    indice = i;
                    break;
                }
        }

        /* Nos dice si hay una siguiente entrada. */
        public boolean hasNext() {
            return iterador != null;
        }

        /* Regresa la siguiente entrada. */
        public Entrada siguiente() {
            if(iterador == null)
                throw new NoSuchElementException();
            Entrada e = iterador.next();
            if(!iterador.hasNext()){
                for (int i = indice + 1; i < entradas.length; i++)
                    if (entradas[i] != null) {
                        iterador = entradas[i].iterator();
                        indice = i;
                        break;
                    }
            }
            if(!iterador.hasNext())
                iterador = null;
            return e;
        }
    }

    /* Clase interna privada para iteradores de llaves. */
    private class IteradorLlaves extends Iterador
        implements Iterator<K> {

        /* Regresa el siguiente elemento. */
        @Override public K next() {
            return super.siguiente().llave;
        }
    }

    /* Clase interna privada para iteradores de valores. */
    private class IteradorValores extends Iterador
        implements Iterator<V> {

        /* Regresa el siguiente elemento. */
        @Override public V next() {
            return super.siguiente().valor;
        }
    }

    /** Máxima carga permitida por el diccionario. */
    public static final double MAXIMA_CARGA = 0.72;

    /* Capacidad mínima; decidida arbitrariamente a 2^6. */
    private static final int MINIMA_CAPACIDAD = 64;

    /* Dispersor. */
    private Dispersor<K> dispersor;
    /* Nuestro diccionario. */
    private Lista<Entrada>[] entradas;
    /* Número de valores. */
    private int elementos;

    /* Truco para crear un arreglo genérico. Es necesario hacerlo así por cómo
       Java implementa sus genéricos; de otra forma obtenemos advertencias del
       compilador. */
    @SuppressWarnings("unchecked")
    private Lista<Entrada>[] nuevoArreglo(int n) {
        return (Lista<Entrada>[])Array.newInstance(Lista.class, n);
    }

    /**
     * Construye un diccionario con una capacidad inicial y dispersor
     * predeterminados.
     */
    public Diccionario() {
        this(MINIMA_CAPACIDAD, (K llave) -> llave.hashCode());
    }

    /**
     * Construye un diccionario con una capacidad inicial definida por el
     * usuario, y un dispersor predeterminado.
     * @param capacidad la capacidad a utilizar.
     */
    public Diccionario(int capacidad) {
        this(capacidad, (K llave) -> llave.hashCode());
    }

    /**
     * Construye un diccionario con una capacidad inicial predeterminada, y un
     * dispersor definido por el usuario.
     * @param dispersor el dispersor a utilizar.
     */
    public Diccionario(Dispersor<K> dispersor) {
        this(MINIMA_CAPACIDAD, dispersor);
    }

    /**
     * Construye un diccionario con una capacidad inicial y un método de
     * dispersor definidos por el usuario.
     * @param capacidad la capacidad inicial del diccionario.
     * @param dispersor el dispersor a utilizar.
     */
    public Diccionario(int capacidad, Dispersor<K> dispersor) {
        this.dispersor = dispersor;
        if(capacidad < MINIMA_CAPACIDAD)
            this.entradas = nuevoArreglo(MINIMA_CAPACIDAD);

        int t = (int)Math.ceil(Math.log10(capacidad)/Math.log10(2));
        this.entradas = nuevoArreglo((int)Math.pow(2, t+1));
    }

    /**
     * Agrega un nuevo valor al diccionario, usando la llave proporcionada. Si
     * la llave ya había sido utilizada antes para agregar un valor, el
     * diccionario reemplaza ese valor con el recibido aquí.
     * @param llave la llave para agregar el valor.
     * @param valor el valor a agregar.
     * @throws IllegalArgumentException si la llave o el valor son nulos.
     */
    public void agrega(K llave, V valor) {
        if(llave == null || valor == null)
            throw new IllegalArgumentException();
        int hash = dispersor.dispersa(llave) & (entradas.length -1);

        Lista<Entrada> entrada = entradas[hash];

        if (entrada == null) {
            Lista<Entrada> l = new Lista<Entrada>();
            entradas[hash] = l;
            l.agrega(new Entrada(llave, valor));
            elementos++;
        } else {
            boolean rep = false;
            for (Entrada e : entrada)
                if (e.llave.equals(llave)) {
                    e.valor = valor;
                    rep = true;
                    break;
                }
            if (!rep){
                entrada.agrega(new Entrada(llave, valor));
                elementos++;
            }
        }

        int elems = elementos;

        if (carga() >= MAXIMA_CARGA){
            expande();
        }
    }

    private void expande(){
        Lista<Entrada>[] nuevo = nuevoArreglo(2 * entradas.length);
        Lista<Entrada>[] viejo = entradas;
        this.entradas = nuevo;
        this.elementos = 0;
        for (Lista<Entrada> l : viejo)
            if (l != null)
                for (Entrada e : l)
                    agrega(e.llave, e.valor);
    }

    /**
     * Regresa el valor del diccionario asociado a la llave proporcionada.
     * @param llave la llave para buscar el valor.
     * @return el valor correspondiente a la llave.
     * @throws IllegalArgumentException si la llave es nula.
     * @throws NoSuchElementException si la llave no está en el diccionario.
     */
    public V get(K llave) {
        if(llave == null)
            throw new IllegalArgumentException();
        int hash = dispersor.dispersa(llave) & (entradas.length -1);

        Lista<Entrada> entrada = entradas[hash];
        if(entrada == null)
            throw new NoSuchElementException();
        for(Entrada e: entrada)
            if(e.llave.equals(llave))
                return e.valor;
        throw new NoSuchElementException();
    }

    /**
     * Nos dice si una llave se encuentra en el diccionario.
     * @param llave la llave que queremos ver si está en el diccionario.
     * @return <code>true</code> si la llave está en el diccionario,
     *         <code>false</code> en otro caso.
     */
    public boolean contiene(K llave) {
        if(llave == null)
            return false;
        int hash = dispersor.dispersa(llave) & (entradas.length -1);

        Lista<Entrada> entrada = entradas[hash];
        if(entrada == null)
            return false;
        for(Entrada e: entrada)
            if(e.llave.equals(llave))
                return true;
        return false;
    }

    /**
     * Elimina el valor del diccionario asociado a la llave proporcionada.
     * @param llave la llave para buscar el valor a eliminar.
     * @throws IllegalArgumentException si la llave es nula.
     * @throws NoSuchElementException si la llave no se encuentra en
     *         el diccionario.
     */
    public void elimina(K llave) {
        if(llave == null)
            throw new IllegalArgumentException();
        int hash = dispersor.dispersa(llave) & (entradas.length -1);

        Lista<Entrada> entrada = entradas[hash];
        if(entrada == null)
            throw new NoSuchElementException();
        boolean delete = false;
        for(Entrada e: entrada)
            if(e.llave.equals(llave)){
                entrada.elimina(e);
                elementos--;
                delete = true;
            }

        if(!delete)
            throw new NoSuchElementException();

        if(entrada.esVacia())
            entradas[hash] = null;
    }

    /**
     * Nos dice cuántas colisiones hay en el diccionario.
     * @return cuántas colisiones hay en el diccionario.
     */
    public int colisiones() {
        int sum = 0;
        for(Lista<Entrada> l: entradas)
            if(l != null)
                sum += (l.getLongitud() -1);
        return sum;
    }

    /**
     * Nos dice el máximo número de colisiones para una misma llave que tenemos
     * en el diccionario.
     * @return el máximo número de colisiones para una misma llave.
     */
    public int colisionMaxima() {
        int max = 1;
        for(Lista<Entrada> l: entradas)
            if(l != null && l.getLongitud() >= max)
                max = l.getLongitud();
        return max-1;
    }

    /**
     * Nos dice la carga del diccionario.
     * @return la carga del diccionario.
     */
    public double carga() {
        return Double.valueOf(elementos) / entradas.length;
    }

    /**
     * Regresa el número de entradas en el diccionario.
     * @return el número de entradas en el diccionario.
     */
    public int getElementos() {
        return elementos;
    }

    /**
     * Nos dice si el diccionario es vacío.
     * @return <code>true</code> si el diccionario es vacío, <code>false</code>
     *         en otro caso.
     */
    public boolean esVacia() {
        return elementos == 0;
    }

    /**
     * Limpia el diccionario de elementos, dejándolo vacío.
     */
    public void limpia() {
        int e = entradas.length;
        this.entradas = nuevoArreglo(e);
        elementos = 0;
    }

    /**
     * Regresa una representación en cadena del diccionario.
     * @return una representación en cadena del diccionario.
     */
    @Override public String toString() {
        if(elementos == 0)
            return "{}";
        String s = "{ ";
        String f = "}";
        Iterador iterador = new Iterador();
        while(iterador.hasNext()){
            Entrada e = iterador.siguiente();
            String k = String.format("\'%s\': \'%s\', ", e.llave, e.valor);
            s+=k;
        }
        return s+f;
    }

    /**
     * Nos dice si el diccionario es igual al objeto recibido.
     * @param o el objeto que queremos saber si es igual al diccionario.
     * @return <code>true</code> si el objeto recibido es instancia de
     *         Diccionario, y tiene las mismas llaves asociadas a los mismos
     *         valores.
     */
    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        @SuppressWarnings("unchecked") Diccionario<K, V> d =
            (Diccionario<K, V>)o;
        if(d.elementos != elementos)
            return false;
        Iterator<K> ik = d.iteradorLlaves();
        while(ik.hasNext()){
                K k = ik.next();
                V u = d.get(k);
                V v = null;
            try{
                v = get(k);
            }catch(NoSuchElementException nse){
                return false;
            }
            if(v != null && !v.equals(u))
                return false;
        }
        return true;

    }

    /**
     * Regresa un iterador para iterar las llaves del diccionario. El
     * diccionario se itera sin ningún orden específico.
     * @return un iterador para iterar las llaves del diccionario.
     */
    public Iterator<K> iteradorLlaves() {
        return new IteradorLlaves();
    }

    /**
     * Regresa un iterador para iterar los valores del diccionario. El
     * diccionario se itera sin ningún orden específico.
     * @return un iterador para iterar los valores del diccionario.
     */
    @Override public Iterator<V> iterator() {
        return new IteradorValores();
    }
}
