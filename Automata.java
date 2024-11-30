import java.util.*;

public class Automata {
    Set<String> alfabeto = new HashSet<>();
    Map<String, Estado> estados = new HashMap<>();
    String estadoInicial;
    Set<String> estadosAceptacion = new HashSet<>();

    public void agregarEstado(String nombre) {
        estados.putIfAbsent(nombre, new Estado(nombre));
    }

    public void definirTransicion(String origen, String simbolo, String destino) {
        estados.get(origen).agregarTransicion(simbolo, destino);
        alfabeto.add(simbolo);
    }
}