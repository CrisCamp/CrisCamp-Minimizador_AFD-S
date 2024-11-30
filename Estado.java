import java.util.HashMap;
import java.util.Map;

public class Estado {
    String nombre;
    Map<String, String> transiciones = new HashMap<>();

    public Estado(String nombre) {
        this.nombre = nombre;
    }

    public void agregarTransicion(String simbolo, String destino) {
        transiciones.put(simbolo, destino);
    }

    public String obtenerTransicion(String simbolo) {
        return transiciones.getOrDefault(simbolo, "VACIO");
    }
}