import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * La clase {@code AFDMinimizado} proporciona una interfaz gráfica para minimizar autómatas finitos deterministas (AFD).
 * <p>
 * Permite al usuario ingresar estados, alfabeto, transiciones, estado inicial y estados de aceptación, y muestra el resultado de la minimización.
 * </p>
 * @see Automata
 */
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

    /**
     * Crea una nueva instancia de {@code AFDMinimizado}.
     */
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

    /**
     * Carga los estados y el alfabeto ingresados por el usuario en el autómata.
     * <p>
     * Este método lee los estados y el alfabeto desde los campos de texto correspondientes y los agrega al autómata.
     * </p>
     * 
     * @throws IllegalArgumentException si los campos de texto están vacíos o contienen datos inválidos.
     */
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

    /**
     * Minimiza el autómata finito determinista (AFD).
     * <p>
     * Este método aplica el algoritmo de minimización al autómata actual y muestra el resultado en la interfaz gráfica.
     * </p>
     * 
     * @throws IllegalStateException si el autómata no está completamente definido (faltan estados, transiciones, etc.).
     */
    private void minimizarAutomata() {
        automata.estadoInicial = estadoInicialField.getText().trim();
        automata.estadosAceptacion.clear();
        automata.estadosAceptacion.addAll(Arrays.asList(estadosAceptacionField.getText().trim().split(" ")));

        Automata minimizado = minimizarAutomata(automata);
        mostrarAutomata(minimizado);
    }

    /**
     * Realiza la minimización del autómata finito determinista (AFD) paso a paso.
     * <p>
     * Este método guía al usuario a través del proceso de minimización del AFD, mostrando los pasos en la interfaz gráfica.
     * </p>
     * 
     * @throws IllegalStateException si el autómata no está completamente definido (faltan estados, transiciones, etc.).
     */
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

    /**
     * Obtiene una lista detallada de los pasos necesarios para minimizar el autómata finito determinista (AFD).
     * <p>
     * Este método devuelve una lista de cadenas que describen cada paso del proceso de minimización del AFD.
     * </p>
     * 
     * @return Una lista de {@code IteracionPaso} que describen los pasos de la minimización.
     * @throws IllegalStateException si el autómata no está completamente definido (faltan estados, transiciones, etc.).
     */
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

    /**
     * Asigna nombres a las particiones resultantes del proceso de minimización del autómata finito determinista (AFD).
     * <p>
     * Este método asigna nombres únicos a cada partición de estados obtenida durante la minimización del AFD.
     * </p>
     * 
     * @param particiones Una lista de conjuntos de estados que representan las particiones.
     * @return Un mapa que asocia cada partición con su nombre asignado.
     * @throws IllegalArgumentException si las particiones están vacías o son nulas.
     */
    private Map<String, List<String>> asignarNombresAParticiones(List<List<String>> particiones) {
        Map<String, List<String>> particionConNombres = new LinkedHashMap<>();
        for (int i = 0; i < particiones.size(); i++) {
            particionConNombres.put("S" + i, particiones.get(i));
        }
        return particionConNombres;
    }

    /**
     * Divide un grupo de estados en subgrupos basados en la evaluación de transiciones.
     * <p>
     * Este método toma un grupo de estados y los divide en subgrupos según las transiciones definidas en el autómata y las evaluaciones proporcionadas.
     * </p>
     * 
     * @param grupo El grupo de estados a dividir.
     * @param automata El autómata que contiene las transiciones y estados.
     * @param particiones Las particiones actuales del autómata.
     * @param evaluaciones Un mapa que contiene las evaluaciones de las transiciones.
     * @return Una lista de listas de estados que representan los subgrupos resultantes.
     * @throws IllegalArgumentException si el grupo, el autómata, las particiones o las evaluaciones son nulas o vacías.
     */
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

    /**
     * Inicializa las particiones del autómata finito determinista (AFD) para el proceso de minimización.
     * <p>
     * Este método crea las particiones iniciales dividiendo los estados del autómata en estados de aceptación y no aceptación.
     * </p>
     * 
     * @param automata El autómata que contiene los estados y las transiciones.
     * @return Una lista de listas de estados que representan las particiones iniciales.
     * @throws IllegalArgumentException si el autómata es nulo o no contiene estados.
     */
    private List<List<String>> inicializarParticiones(Automata automata) {
        List<List<String>> particiones = new ArrayList<>();
        List<String> noAceptadores = new ArrayList<>(automata.estados.keySet());
        noAceptadores.removeAll(automata.estadosAceptacion);
        particiones.add(new ArrayList<>(automata.estadosAceptacion));
        particiones.add(noAceptadores);
        return particiones;
    }

    /**
     * Determina si dos estados son equivalentes en el contexto del autómata finito determinista (AFD).
     * <p>
     * Este método compara las transiciones de dos estados y verifica si pertenecen a las mismas particiones para todos los símbolos del alfabeto.
     * </p>
     * 
     * @param estado1 El primer estado a comparar.
     * @param estado2 El segundo estado a comparar.
     * @param automata El autómata que contiene los estados y las transiciones.
     * @param particiones Las particiones actuales del autómata.
     * @return {@code true} si los estados son equivalentes, {@code false} en caso contrario.
     * @throws IllegalArgumentException si alguno de los parámetros es nulo o si los estados no existen en el autómata.
     */
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

    /**
     * Obtiene la partición a la que pertenece un estado dado.
     * <p>
     * Este método busca en las particiones actuales del autómata y devuelve la partición que contiene el estado especificado.
     * </p>
     * 
     * @param particiones Las particiones actuales del autómata.
     * @param estado El estado para el cual se desea encontrar la partición.
     * @return La partición que contiene el estado especificado, o una lista vacía si el estado no se encuentra en ninguna partición.
     * @throws IllegalArgumentException si las particiones son nulas o el estado es nulo.
     */
    private List<String> obtenerParticion(List<List<String>> particiones, String estado) {
        for (List<String> particion : particiones) {
            if (particion.contains(estado)) return particion;
        }
        return new ArrayList<>();
    }

    /**
     * Minimiza el autómata finito determinista (AFD) proporcionado.
     * <p>
     * Este método aplica el algoritmo de minimización al AFD, dividiendo los estados en particiones y refinándolas hasta que no haya más cambios.
     * </p>
     * 
     * @param automata El autómata que se va a minimizar.
     * @return Un nuevo autómata que representa el AFD minimizado.
     * @throws IllegalArgumentException si el autómata es nulo o no está completamente definido.
     */
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

    /**
     * Construye un nuevo autómata finito determinista (AFD) minimizado a partir de las particiones proporcionadas.
     * <p>
     * Este método crea un nuevo AFD utilizando las particiones resultantes del proceso de minimización y asigna nombres a los nuevos estados.
     * </p>
     * 
     * @param automata El autómata original que se ha minimizado.
     * @param particiones Las particiones resultantes del proceso de minimización.
     * @return Un nuevo autómata que representa el AFD minimizado.
     * @throws IllegalArgumentException si el autómata o las particiones son nulas.
     */
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

    /**
     * Muestra la representación textual del autómata finito determinista (AFD) en el área de resultados.
     * <p>
     * Este método construye una cadena que describe el estado inicial, los estados de aceptación y las transiciones del autómata, y la muestra en el área de resultados.
     * </p>
     * 
     * @param automata El autómata que se va a mostrar.
     * @throws IllegalArgumentException si el autómata es nulo.
     */
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

    /**
     * La clase {@code IteracionPaso} representa una iteración del proceso de minimización del autómata finito determinista (AFD).
     * <p>
     * Esta clase almacena la información de una iteración específica, incluyendo el número de iteración, las particiones y las evaluaciones de transiciones.
     * </p>
     */
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

    /**
     * El método principal que inicia la aplicación de minimización de autómatas finitos deterministas (AFD).
     * <p>
     * Este método utiliza {@code SwingUtilities.invokeLater} para asegurar que la creación y actualización de la interfaz gráfica de usuario se realice en el hilo de despacho de eventos de Swing.
     * </p>
     * 
     * @param args Los argumentos de la línea de comandos (no se utilizan en esta aplicación).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AFDMinimizado().setVisible(true));
    }
}
