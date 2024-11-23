import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AFDMinimizado extends JFrame {
    private JTextField estadoInicialField;
    private JTextField estadosAceptacionField;
    private JTextField estadosField;
    private JTextField alfabetoField;
    private JComboBox<String> estadoOrigenBox;
    private JComboBox<String> simboloBox;
    private JComboBox<String> estadoDestinoBox;
    private JTextArea resultadoArea;
    private JTable particionesTable;
    private DefaultTableModel particionesTableModel;
    private Automata automata;
    private DefaultListModel<String> transicionesListModel;

    public AFDMinimizado() {
        automata = new Automata();
        setTitle("Minimizador de AFD");
        setSize(900, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de entrada de datos
        JPanel inputPanel = new JPanel(new GridLayout(9, 1));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Estado inicial
        inputPanel.add(new JLabel("Estado inicial:"));
        estadoInicialField = new JTextField();
        inputPanel.add(estadoInicialField);

        // Estados de aceptación
        inputPanel.add(new JLabel("Estados de aceptación (separados por espacios):"));
        estadosAceptacionField = new JTextField();
        inputPanel.add(estadosAceptacionField);

        // Estados del autómata
        inputPanel.add(new JLabel("Estados del autómata (separados por espacios):"));
        estadosField = new JTextField();
        inputPanel.add(estadosField);

        // Alfabeto
        inputPanel.add(new JLabel("Alfabeto (símbolos separados por espacios):"));
        alfabetoField = new JTextField();
        inputPanel.add(alfabetoField);

        // Botón para cargar estados y alfabeto en las listas desplegables
        JButton aceptarButton = new JButton("Aceptar");
        aceptarButton.addActionListener(e -> cargarEstadosYAlfabeto());
        inputPanel.add(aceptarButton);

        add(inputPanel, BorderLayout.NORTH);

        // Panel para agregar transiciones
        JPanel transicionPanel = new JPanel(new FlowLayout());
        transicionPanel.setBorder(BorderFactory.createTitledBorder("Agregar Transiciones"));

        estadoOrigenBox = new JComboBox<>();
        simboloBox = new JComboBox<>();
        estadoDestinoBox = new JComboBox<>();
        JButton agregarTransicionButton = new JButton("Agregar Transición");

        transicionPanel.add(new JLabel("Estado Origen:"));
        transicionPanel.add(estadoOrigenBox);
        transicionPanel.add(new JLabel("Símbolo:"));
        transicionPanel.add(simboloBox);
        transicionPanel.add(new JLabel("Estado Destino:"));
        transicionPanel.add(estadoDestinoBox);
        transicionPanel.add(agregarTransicionButton);

        // Lista de transiciones agregadas
        transicionesListModel = new DefaultListModel<>();
        JList<String> transicionesList = new JList<>(transicionesListModel);
        JScrollPane transicionesScroll = new JScrollPane(transicionesList);
        transicionesScroll.setPreferredSize(new Dimension(550, 100));

        // Botones para minimizar
        JButton minimizarButton = new JButton("Minimizar AFD");
        minimizarButton.addActionListener(e -> minimizarAutomata());

        JButton pasoAPasoButton = new JButton("Minimización Paso a Paso");
        pasoAPasoButton.addActionListener(e -> minimizacionPasoAPaso());

        JPanel transicionesPanel = new JPanel(new BorderLayout());
        transicionesPanel.add(transicionPanel, BorderLayout.NORTH);
        transicionesPanel.add(transicionesScroll, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel();
        botonesPanel.add(minimizarButton);
        botonesPanel.add(pasoAPasoButton);
        transicionesPanel.add(botonesPanel, BorderLayout.SOUTH);

        add(transicionesPanel, BorderLayout.CENTER);

        // Tabla para visualización
        particionesTableModel = new DefaultTableModel(new Object[]{"Iteración", "Partición", "Estados", "Evaluación"}, 0);
        particionesTable = new JTable(particionesTableModel);

        JScrollPane particionesScroll = new JScrollPane(particionesTable);
        particionesScroll.setBorder(BorderFactory.createTitledBorder("Particiones y Evaluaciones Paso a Paso"));

        add(particionesScroll, BorderLayout.EAST);

        // Área de resultados finales
        resultadoArea = new JTextArea(10, 50);
        resultadoArea.setEditable(false);
        JScrollPane resultadoScroll = new JScrollPane(resultadoArea);
        add(resultadoScroll, BorderLayout.SOUTH);

        // Acción para agregar una transición
        agregarTransicionButton.addActionListener(e -> {
            String origen = (String) estadoOrigenBox.getSelectedItem();
            String simbolo = (String) simboloBox.getSelectedItem();
            String destino = (String) estadoDestinoBox.getSelectedItem();

            if (origen != null && simbolo != null && destino != null) {
                automata.definirTransicion(origen, simbolo, destino);
                transicionesListModel.addElement(origen + "," + simbolo + " -> " + destino);
            }
        });
    }

    private void cargarEstadosYAlfabeto() {
        // Cargar estados en el autómata y actualizar listas desplegables de estados
        String[] todosLosEstados = estadosField.getText().trim().split(" ");
        for (String estado : todosLosEstados) {
            automata.agregarEstado(estado);
        }
        estadoOrigenBox.setModel(new DefaultComboBoxModel<>(todosLosEstados));
        estadoDestinoBox.setModel(new DefaultComboBoxModel<>(todosLosEstados));

        // Cargar alfabeto y actualizar lista desplegable de símbolos
        String[] alfabetoArray = alfabetoField.getText().trim().split(" ");
        automata.alfabeto.addAll(Arrays.asList(alfabetoArray));
        simboloBox.setModel(new DefaultComboBoxModel<>(alfabetoArray));
    }

    private void minimizarAutomata() {
        automata.estadoInicial = estadoInicialField.getText().trim();
        automata.estadosAceptacion.clear();
        automata.estadosAceptacion.addAll(Arrays.asList(estadosAceptacionField.getText().trim().split(" ")));

        Automata minimizado = minimizarAutomata(automata);
        mostrarAutomata(minimizado);
    }

    private void minimizacionPasoAPaso() {
        automata.estadoInicial = estadoInicialField.getText().trim();
        automata.estadosAceptacion.clear();
        automata.estadosAceptacion.addAll(Arrays.asList(estadosAceptacionField.getText().trim().split(" ")));

        List<IteracionPaso> pasos = obtenerPasosMinimizacionDetallados(automata);
        particionesTableModel.setRowCount(0); // Limpia la tabla

        for (IteracionPaso paso : pasos) {
            for (Map.Entry<String, List<String>> particion : paso.particiones.entrySet()) {
                particionesTableModel.addRow(new Object[]{
                        paso.iteracion,
                        particion.getKey(),
                        particion.getValue(),
                        paso.evaluaciones.get(particion.getKey())
                });
            }
        }
    }

    private List<IteracionPaso> obtenerPasosMinimizacionDetallados(Automata automata) {
        List<IteracionPaso> pasos = new ArrayList<>();
        List<List<String>> particiones = inicializarParticiones(automata);
        boolean cambio;
        int iteracion = 1;

        // Agregar primera partición inicial
        IteracionPaso inicial = new IteracionPaso(iteracion++);
        inicial.particiones.putAll(asignarNombresAParticiones(particiones));
        for (Map.Entry<String, List<String>> entry : inicial.particiones.entrySet()) {
            inicial.evaluaciones.put(entry.getKey(), "Partición inicial: " + entry.getValue());
        }
        pasos.add(inicial);

        do {
            IteracionPaso paso = new IteracionPaso(iteracion);
            cambio = false;
            List<List<String>> nuevasParticiones = new ArrayList<>();
            Map<String, List<String>> particionConNombres = asignarNombresAParticiones(particiones);

            for (List<String> grupo : particiones) {
                List<List<String>> subdividido = dividirGrupoConEvaluacion(grupo, automata, particiones, paso.evaluaciones);
                nuevasParticiones.addAll(subdividido);
                if (subdividido.size() > 1) cambio = true;
            }

            particiones = nuevasParticiones;
            paso.particiones.putAll(asignarNombresAParticiones(particiones));
            pasos.add(paso);
            iteracion++;
        } while (cambio);

        return pasos;
    }

    private Map<String, List<String>> asignarNombresAParticiones(List<List<String>> particiones) {
        Map<String, List<String>> particionConNombres = new LinkedHashMap<>();
        for (int i = 0; i < particiones.size(); i++) {
            particionConNombres.put("S" + i, particiones.get(i));
        }
        return particionConNombres;
    }

    private List<List<String>> dividirGrupoConEvaluacion(List<String> grupo, Automata automata, List<List<String>> particiones, Map<String, String> evaluaciones) {
        List<List<String>> resultado = new ArrayList<>();
        for (String estado : grupo) {
            boolean encontrado = false;
            for (List<String> subGrupo : resultado) {
                if (equivalente(estado, subGrupo.get(0), automata, particiones)) {
                    subGrupo.add(estado);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                List<String> nuevoGrupo = new ArrayList<>();
                nuevoGrupo.add(estado);
                resultado.add(nuevoGrupo);
            }
        }
        return resultado;
    }

    private List<List<String>> inicializarParticiones(Automata automata) {
        List<List<String>> particiones = new ArrayList<>();
        List<String> noAceptadores = new ArrayList<>(automata.estados.keySet());
        noAceptadores.removeAll(automata.estadosAceptacion);
        particiones.add(new ArrayList<>(automata.estadosAceptacion));
        particiones.add(noAceptadores);
        return particiones;
    }

    private boolean equivalente(String estado1, String estado2, Automata automata, List<List<String>> particiones) {
        for (String simbolo : automata.alfabeto) {
            String destino1 = automata.estados.get(estado1).obtenerTransicion(simbolo);
            String destino2 = automata.estados.get(estado2).obtenerTransicion(simbolo);
            if (!obtenerParticion(particiones, destino1).equals(obtenerParticion(particiones, destino2))) {
                return false;
            }
        }
        return true;
    }

    private List<String> obtenerParticion(List<List<String>> particiones, String estado) {
        for (List<String> particion : particiones) {
            if (particion.contains(estado)) return particion;
        }
        return new ArrayList<>();
    }

    private Automata minimizarAutomata(Automata automata) {
        List<List<String>> particiones = inicializarParticiones(automata);
        boolean cambio;

        do {
            cambio = false;
            List<List<String>> nuevasParticiones = new ArrayList<>();

            for (List<String> grupo : particiones) {
                List<List<String>> subdividido = dividirGrupoConEvaluacion(grupo, automata, particiones, new LinkedHashMap<>());
                nuevasParticiones.addAll(subdividido);
                if (subdividido.size() > 1) cambio = true;
            }
            particiones = nuevasParticiones;
        } while (cambio);

        return construirAutomataMinimizado(automata, particiones);
    }

    private Automata construirAutomataMinimizado(Automata automata, List<List<String>> particiones) {
        Automata minimizado = new Automata();
        Map<List<String>, String> representacion = new HashMap<>();
        int contador = 0;

        for (List<String> particion : particiones) {
            String nombre = "S" + contador++;
            minimizado.agregarEstado(nombre);
            representacion.put(particion, nombre);

            if (particion.contains(automata.estadoInicial)) {
                minimizado.estadoInicial = nombre;
            }
            if (!Collections.disjoint(particion, automata.estadosAceptacion)) {
                minimizado.estadosAceptacion.add(nombre);
            }
        }

        for (List<String> particion : particiones) {
            String nombre = representacion.get(particion);
            for (String simbolo : automata.alfabeto) {
                String destino = automata.estados.get(particion.get(0)).obtenerTransicion(simbolo);
                List<String> particionDestino = obtenerParticion(particiones, destino);
                if (!particionDestino.isEmpty()) {
                    String nombreDestino = representacion.get(particionDestino);
                    minimizado.definirTransicion(nombre, simbolo, nombreDestino);
                }
            }
        }
        return minimizado;
    }

    private void mostrarAutomata(Automata automata) {
        StringBuilder resultado = new StringBuilder();
        resultado.append("Estado inicial: ").append(automata.estadoInicial).append("\n");
        resultado.append("Estados de aceptación: ").append(automata.estadosAceptacion).append("\n");
        resultado.append("Transiciones:\n");
        for (Estado estado : automata.estados.values()) {
            for (Map.Entry<String, String> transicion : estado.transiciones.entrySet()) {
                resultado.append(estado.nombre).append(",").append(transicion.getKey()).append(" -> ").append(transicion.getValue()).append("\n");
            }
        }
        resultadoArea.setText(resultado.toString());
    }

    private class IteracionPaso {
        int iteracion;
        Map<String, List<String>> particiones;
        Map<String, String> evaluaciones;

        IteracionPaso(int iteracion) {
            this.iteracion = iteracion;
            this.particiones = new LinkedHashMap<>();
            this.evaluaciones = new LinkedHashMap<>();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AFDMinimizado().setVisible(true));
    }
}
