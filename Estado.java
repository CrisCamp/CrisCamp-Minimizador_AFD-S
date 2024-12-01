import java.util.HashMap;
import java.util.Map;

/**
 * La clase {@code Estado} representa un estado en un autómata con transiciones etiquetadas.
 * <p>
 * Cada estado tiene un nombre y un conjunto de transiciones que mapean símbolos a estados de destino.
 * </p>
 */
public class Estado {
    /**
     * El nombre del estado.
     */
    String nombre;

    /**
     * Un mapa que contiene las transiciones del estado. Las claves son los símbolos y los valores son los estados de destino.
     */
    Map<String, String> transiciones = new HashMap<>();

    /**
     * Crea un nuevo estado con el nombre especificado.
     *
     * @param nombre El nombre del estado.
     */
    public Estado(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Agrega una transición al estado.
     *
     * @param simbolo El símbolo de la transición.
     * @param destino El estado de destino de la transición.
     */
    public void agregarTransicion(String simbolo, String destino) {
        transiciones.put(simbolo, destino);
    }

    /**
     * Obtiene el estado de destino para una transición dada.
     *
     * @param simbolo El símbolo de la transición.
     * @return El estado de destino para el símbolo dado, o {@code "VACIO"} si no existe una transición para el símbolo.
     */
    public String obtenerTransicion(String simbolo) {
        return transiciones.getOrDefault(simbolo, "VACIO");
    }
}