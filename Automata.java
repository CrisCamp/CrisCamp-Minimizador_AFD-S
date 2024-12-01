import java.util.*;

/**
 * La clase {@code Automata} representa un autómata finito con un conjunto de estados, un alfabeto, un estado inicial y un conjunto de estados de aceptación.
 * <p>
 * Proporciona métodos para agregar estados y definir transiciones y establecer estado inicial y estados de aceptacion.
 * </p>
 */
public class Automata {
    /**
     * El alfabeto del autómata.
     */
    Set<String> alfabeto = new HashSet<>();

    /**
     * Un mapa que contiene los estados del autómata. Las claves son los nombres de los estados y los valores son los objetos {@code Estado}.
     */
    Map<String, Estado> estados = new HashMap<>();

    /**
     * El estado inicial del autómata.
     */
    String estadoInicial;

    /**
     * El conjunto de estados de aceptación del autómata.
     */
    Set<String> estadosAceptacion = new HashSet<>();

    /**
     * Agrega un nuevo estado al autómata.
     *
     * @param nombre El nombre del estado.
     */
    public void agregarEstado(String nombre) {
        estados.putIfAbsent(nombre, new Estado(nombre));
    }

    /**
     * Define una transición entre dos estados del autómata.
     *
     * @param origen El nombre del estado de origen.
     * @param simbolo El símbolo de la transición.
     * @param destino El nombre del estado de destino.
     */
    public void definirTransicion(String origen, String simbolo, String destino) {
        estados.get(origen).agregarTransicion(simbolo, destino);
        alfabeto.add(simbolo);
    }
}