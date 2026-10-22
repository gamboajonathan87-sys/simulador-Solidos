import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Ventana principal del simulador, estilo panel de ingenieria en modo oscuro.
 * Usa App (motor de calculo), Tema (componentes y colores) y Graficas (2D, 3D, acumulado).
 */
public class Ventana extends JFrame {

    // Valores sugeridos para cada problema: a, b, metodo, eje
    private static final String[][] SUGERIDOS = {
        {"-1", "2", "1", "0"},   // informe: arandelas, eje X
        {"0", "4", "0", "0"},    // config 1: disco, eje X
        {"0", "1", "1", "0"},    // config 2: arandelas, eje X
        {"0", "2", "2", "1"}     // config 3: capas, eje Y
    };
    private static final String[] METODOS = {"disco", "arandelas", "capas"};
    private static final String[] EJES = {"X", "Y"};

    private final Tema.ListaProblemas lista = new Tema.ListaProblemas(
        new String[]{"Ejercicio del informe", "Configuracion 1", "Configuracion 2", "Configuracion 3"},
        new String[]{"f = x + 3   g = x^2 + 1", "f = raiz(x)   g = 0",
                     "f = raiz(x)   g = x^2", "f = 2x   g = x^2"});
    private final Tema.Campo tfA = new Tema.Campo("-1");
    private final Tema.Campo tfB = new Tema.Campo("2");
    private final Tema.Segmentado segMetodo = new Tema.Segmentado("disco", "arandelas", "capas");
    private final Tema.Segmentado segEje = new Tema.Segmentado("Eje X", "Eje Y");
    private final Tema.Campo tfManual = new Tema.Campo("");

    private final Tema.Metrica mVolumen = new Tema.Metrica("Volumen");
    private final Tema.Metrica mMetodo = new Tema.Metrica("Metodo");
    private final Tema.Metrica mIntervalo = new Tema.Metrica("Intervalo");
    private final Tema.Metrica mDiferencia = new Tema.Metrica("Diferencia con tu resultado");

    private final Tema.Pildora estado = new Tema.Pildora("Listo", Tema.TEXTO_SUAVE);
    private final Tema.Consola consola = new Tema.Consola();

    private final Graficas.PanelRegion panelRegion = new Graficas.PanelRegion();
    private final Graficas.Panel3D panel3D = new Graficas.Panel3D();
    private final Graficas.PanelAcum panelAcum = new Graficas.PanelAcum();
    private final Tema.Escenario escenario = new Tema.Escenario();

    // Ultimo calculo valido (lo usan Comparar y Exportar)
    private boolean hayResultado = false;
    private double ultVolumen, ultA, ultB;
    private String ultMetodo = "";
    private int ultProblema = 0;

    public Ventana() {
        super("Simulador de solidos de revolucion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Tema.FONDO);

        JPanel raiz = new JPanel(new BorderLayout(16, 14));
        raiz.setOpaque(false);
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
        raiz.add(construirBarra(), BorderLayout.NORTH);
        raiz.add(construirLateral(), BorderLayout.WEST);
        raiz.add(construirCentro(), BorderLayout.CENTER);
        setContentPane(fondo(raiz));

        lista.alCambiar(i -> cargarSugeridos());
        cargarSugeridos();
        mVolumen.vaciar("unidades cubicas");
        mMetodo.vaciar("sin calcular");
        mIntervalo.vaciar("sin calcular");
        mDiferencia.vaciar("escribe tu resultado manual");

        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1360, pantalla.width - 40), Math.min(900, pantalla.height - 60));
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
    }

    private static JPanel fondo(JPanel contenido) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------------
    // Barra superior
    // ------------------------------------------------------------------
    private JComponent construirBarra() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);

        JLabel t = new JLabel("Simulador de solidos de revolucion");
        t.setFont(Tema.sans(Font.BOLD, 20));
        t.setForeground(Tema.TEXTO);
        JLabel s = new JLabel("disco  |  arandelas  |  capas cilindricas  |  integracion numerica (Simpson, n = 2000)");
        s.setFont(Tema.mono(Font.PLAIN, 12));
        s.setForeground(Tema.TEXTO_SUAVE);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        t.setAlignmentX(0f);
        s.setAlignmentX(0f);
        textos.add(t);
        textos.add(Box.createVerticalStrut(4));
        textos.add(s);
        barra.add(textos, BorderLayout.WEST);

        Tema.Interruptor reducir = new Tema.Interruptor("Reducir animaciones");
        reducir.poner(Tema.reducir);
        reducir.alCambiar(v -> Tema.reducir = v);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        der.setOpaque(false);
        der.add(reducir);
        der.add(estado);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    // ------------------------------------------------------------------
    // Panel lateral: problema y parametros
    // ------------------------------------------------------------------
    private static <T extends JComponent> T fila(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private static JPanel columna(Tema.Tarjeta t) {
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return t;
    }

    private JComponent construirLateral() {
        Tema.Tarjeta cProblema = new Tema.Tarjeta(null);
        columna(cProblema);
        cProblema.add(fila(Tema.titulo("Problema")));
        cProblema.add(Box.createVerticalStrut(2));
        cProblema.add(fila(Tema.etiqueta("Funciones f(x) y g(x) del solido")));
        cProblema.add(Box.createVerticalStrut(10));
        cProblema.add(fila(lista));

        Tema.Tarjeta cParam = new Tema.Tarjeta(null);
        columna(cParam);
        cParam.add(fila(Tema.titulo("Parametros")));
        cParam.add(Box.createVerticalStrut(12));

        JPanel limites = new JPanel(new GridLayout(1, 2, 10, 0));
        limites.setOpaque(false);
        limites.add(bloque("Limite a", tfA));
        limites.add(bloque("Limite b", tfB));
        cParam.add(fila(limites));
        cParam.add(Box.createVerticalStrut(14));
        cParam.add(fila(Tema.etiqueta("Metodo de calculo")));
        cParam.add(Box.createVerticalStrut(6));
        cParam.add(fila(segMetodo));
        cParam.add(Box.createVerticalStrut(14));
        cParam.add(fila(Tema.etiqueta("Eje de rotacion")));
        cParam.add(Box.createVerticalStrut(6));
        cParam.add(fila(segEje));
        cParam.add(Box.createVerticalStrut(18));
        Tema.Boton calcular = new Tema.Boton("Calcular volumen", true);
        calcular.addActionListener(e -> calcular());
        JPanel pc = new JPanel(new GridLayout(1, 1));
        pc.setOpaque(false);
        pc.add(calcular);
        cParam.add(fila(pc));

        tfA.addActionListener(e -> calcular());
        tfB.addActionListener(e -> calcular());

        Tema.PanelVertical contenido = new Tema.PanelVertical(null);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        cProblema.setAlignmentX(0f);
        cParam.setAlignmentX(0f);
        contenido.add(cProblema);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(cParam);

        JScrollPane sp = new JScrollPane(contenido);
        Tema.barraFina(sp);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setPreferredSize(new Dimension(346, 0));
        return sp;
    }

    private JComponent bloque(String texto, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(Tema.etiqueta(texto), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------------
    // Zona central: metricas, graficas, comparacion y consola
    // ------------------------------------------------------------------
    private JComponent construirCentro() {
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);

        JPanel metricas = new JPanel(new GridLayout(1, 4, 12, 0));
        metricas.setOpaque(false);
        metricas.add(mVolumen);
        metricas.add(mMetodo);
        metricas.add(mIntervalo);
        metricas.add(mDiferencia);
        centro.add(metricas, BorderLayout.NORTH);

        // Graficas con pestanas
        Tema.Pestanas pestanas = new Tema.Pestanas("Grafica 2D", "Solido 3D", "Volumen acumulado");
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createEmptyBorder(6, 10, 0, 10));
        cabecera.add(pestanas, BorderLayout.WEST);

        JPanel contenedor3D = new JPanel(new BorderLayout());
        contenedor3D.setOpaque(false);
        contenedor3D.add(panel3D, BorderLayout.CENTER);
        contenedor3D.add(barra3D(), BorderLayout.SOUTH);

        escenario.add(panelRegion, "2D");
        escenario.add(contenedor3D, "3D");
        escenario.add(panelAcum, "ACUM");
        pestanas.alCambiar(i -> escenario.mostrar(new String[]{"2D", "3D", "ACUM"}[i]));

        Tema.Tarjeta cGraficas = new Tema.Tarjeta(new BorderLayout()) {
            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = Tema.calidad(g);
                g2.clip(Tema.caja(0.5, 0.5, getWidth() - 1, getHeight() - 1, Tema.R_TARJETA));
                super.paintChildren(g2);
                g2.dispose();
                Graphics2D b = Tema.calidad(g);
                b.setColor(Tema.BORDE);
                b.draw(Tema.caja(0.5, 0.5, getWidth() - 1, getHeight() - 1, Tema.R_TARJETA));
                b.dispose();
            }
        };
        cGraficas.add(cabecera, BorderLayout.NORTH);
        cGraficas.add(escenario, BorderLayout.CENTER);
        centro.add(cGraficas, BorderLayout.CENTER);

        centro.add(construirInferior(), BorderLayout.SOUTH);
        return centro;
    }

    private JComponent barra3D() {
        Tema.Boton animar = new Tema.Boton("Animar giro", false);
        animar.addActionListener(e -> panel3D.animar());
        Tema.Interruptor auto = new Tema.Interruptor("Rotar vista automaticamente");
        auto.alCambiar(v -> panel3D.setAutoRotar(v));
        Tema.Boton vista = new Tema.Boton("Vista inicial", false);
        vista.addActionListener(e -> panel3D.vistaInicial());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        barra.setOpaque(true);
        barra.setBackground(Tema.SUPERFICIE);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));
        barra.add(animar);
        barra.add(auto);
        barra.add(vista);
        return barra;
    }

    private JComponent construirInferior() {
        Tema.Tarjeta tarjeta = new Tema.Tarjeta(new BorderLayout(18, 0));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        tarjeta.setPreferredSize(new Dimension(0, 196));

        JPanel izq = new JPanel();
        izq.setOpaque(false);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.add(fila(Tema.titulo("Validacion y exportacion")));
        izq.add(Box.createVerticalStrut(8));
        izq.add(fila(Tema.etiqueta("Tu resultado manual (unidades cubicas)")));
        izq.add(Box.createVerticalStrut(6));
        izq.add(fila(tfManual));
        izq.add(Box.createVerticalStrut(8));

        Tema.Boton comparar = new Tema.Boton("Comparar", true);
        comparar.addActionListener(e -> comparar());
        Tema.Boton exportar = new Tema.Boton("Exportar CSV / TXT", false);
        exportar.addActionListener(e -> exportar());
        Tema.Boton tabla = new Tema.Boton("Tabla de 3 configuraciones", false);
        tabla.addActionListener(e -> tablaPruebas());
        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botones.setOpaque(false);
        botones.add(comparar);
        botones.add(exportar);
        izq.add(fila(botones));
        izq.add(Box.createVerticalStrut(8));
        JPanel pt = new JPanel(new GridLayout(1, 1));
        pt.setOpaque(false);
        pt.add(tabla);
        izq.add(fila(pt));
        izq.setPreferredSize(new Dimension(340, 0));
        tfManual.addActionListener(e -> comparar());

        JPanel der = new JPanel(new BorderLayout(0, 6));
        der.setOpaque(false);
        der.add(Tema.titulo("Salida"), BorderLayout.NORTH);
        der.add(consola, BorderLayout.CENTER);

        tarjeta.add(izq, BorderLayout.WEST);
        tarjeta.add(der, BorderLayout.CENTER);
        return tarjeta;
    }

    // ------------------------------------------------------------------
    // Logica
    // ------------------------------------------------------------------
    private void cargarSugeridos() {
        String[] s = SUGERIDOS[lista.indice()];
        tfA.setText(s[0]);
        tfB.setText(s[1]);
        segMetodo.poner(Integer.parseInt(s[2]));
        segEje.poner(Integer.parseInt(s[3]));
    }

    private double leer(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static String f6(double v) {
        return String.format(Locale.US, "%.6f", v);
    }

    private void invalido(String detalleGrafica) {
        hayResultado = false;
        estado.poner("Datos invalidos", Tema.ERROR);
        mVolumen.poner("-", "sin resultado", Tema.TEXTO_SUAVE, Tema.ERROR, false);
        mMetodo.vaciar("sin calcular");
        mIntervalo.vaciar("sin calcular");
        mDiferencia.vaciar("escribe tu resultado manual");
        String t = "Datos no validos";
        panelRegion.limpiar(t, detalleGrafica, true);
        panel3D.limpiar(t, detalleGrafica, true);
        panelAcum.limpiar(t, detalleGrafica, true);
    }

    private void calcular() {
        App.problema = lista.indice();
        String metodo = METODOS[segMetodo.indice()];
        char eje = EJES[segEje.indice()].charAt(0);
        double a = leer(tfA.getText());
        double b = leer(tfB.getText());

        consola.limpiar();
        if (Double.isNaN(a) || Double.isNaN(b)) {
            consola.linea("ERROR  a y b deben ser numeros.", Tema.ERROR);
            invalido("Revisa los limites a y b.");
            return;
        }

        ArrayList<String> errores = App.validar(a, b, metodo, eje);
        if (!errores.isEmpty()) {
            consola.linea("Hay problemas con los datos:", Tema.ERROR);
            for (String error : errores) {
                consola.linea("  - " + error, Tema.ERROR);
            }
            invalido("Corrige lo indicado en la salida.");
            return;
        }

        double volumen = App.simpson(a, b, 2000, metodo);
        consola.linea("Datos validos", Tema.OK);
        consola.linea("Metodo     " + metodo + "   eje " + eje, Tema.TEXTO);
        consola.linea("Integral   " + App.textoIntegral(metodo), Tema.TEXTO);
        consola.linea("Intervalo  [" + Tema.compacto(a) + ", " + Tema.compacto(b) + "]", Tema.TEXTO);
        consola.linea("Volumen    " + f6(volumen) + " u^3", Tema.ACENTO);

        mVolumen.poner(f6(volumen), "unidades cubicas", Tema.ACENTO, Tema.TEXTO_SUAVE, true);
        mMetodo.poner(metodo, "giro alrededor del eje " + eje, Tema.TEXTO, Tema.TEXTO_SUAVE, true);
        mIntervalo.poner("[" + Tema.compacto(a) + ", " + Tema.compacto(b) + "]",
            "ancho " + Tema.compacto(b - a), Tema.TEXTO, Tema.TEXTO_SUAVE, true);
        mDiferencia.vaciar("escribe tu resultado manual");

        String nombre = App.NOMBRES[App.problema];
        panelRegion.mostrar(a, b, eje, nombre);
        panel3D.mostrar(a, b, eje, nombre);
        panelAcum.mostrar(a, b, metodo, nombre);

        estado.poner("Calculado", Tema.OK);
        hayResultado = true;
        ultVolumen = volumen;
        ultA = a;
        ultB = b;
        ultMetodo = metodo;
        ultProblema = App.problema;
    }

    // Seccion 8: comparacion con el procedimiento manual
    private void comparar() {
        if (!hayResultado) {
            consola.linea("Primero presiona Calcular con datos validos.", Tema.CURVA_G);
            return;
        }
        double manual = leer(tfManual.getText());
        if (Double.isNaN(manual)) {
            consola.linea("Escribe tu resultado manual como un numero (ej. 73.51).", Tema.CURVA_G);
            return;
        }
        double dif = Math.abs(manual - ultVolumen) / Math.abs(ultVolumen) * 100;
        boolean ok = dif < 1.0;
        String veredicto = ok ? "Concordancia" : "Sin concordancia";
        consola.linea("", Tema.TEXTO);
        consola.linea("Comparacion con el procedimiento manual", Tema.TEXTO_SUAVE);
        consola.linea("Manual      " + f6(manual), Tema.TEXTO);
        consola.linea("Simulador   " + f6(ultVolumen), Tema.TEXTO);
        consola.linea("Diferencia  " + String.format(Locale.US, "%.4f", dif) + " %   " + veredicto,
            ok ? Tema.OK : Tema.ERROR);
        if (!ok) {
            consola.linea("Revisa tu procedimiento manual.", Tema.ERROR);
        }
        mDiferencia.poner(String.format(Locale.US, "%.4f %%", dif), veredicto,
            ok ? Tema.OK : Tema.ERROR, ok ? Tema.OK : Tema.ERROR, true);
    }

    // Seccion 12: exportar a CSV o TXT
    private void exportar() {
        if (!hayResultado) {
            consola.linea("Primero presiona Calcular con datos validos.", Tema.CURVA_G);
            return;
        }
        FileDialog dialogo = new FileDialog(this, "Exportar datos", FileDialog.SAVE);
        dialogo.setFile("resultados.csv");
        dialogo.setVisible(true);
        if (dialogo.getFile() == null) {
            return;
        }
        String ruta = dialogo.getDirectory() + dialogo.getFile();
        String minus = ruta.toLowerCase();
        if (!minus.endsWith(".csv") && !minus.endsWith(".txt")) {
            ruta += ".csv";
        }
        try {
            App.problema = ultProblema;
            App.exportarCSV(ruta, ultA, ultB, ultMetodo, 100);
            consola.linea("", Tema.TEXTO);
            consola.linea("Datos exportados (x, f(x), g(x), Vacum(x)):", Tema.OK);
            consola.linea(ruta, Tema.TEXTO);
            estado.poner("Exportado", Tema.ACENTO);
        } catch (IOException ex) {
            consola.linea("No se pudo guardar el archivo: " + ex.getMessage(), Tema.ERROR);
        }
    }

    // Seccion 13: valores del simulador para las 3 configuraciones
    private void tablaPruebas() {
        int guardado = App.problema;
        String[] metodos = {"disco", "arandelas", "capas"};
        double[][] lim = {{0, 4}, {0, 1}, {0, 2}};
        consola.limpiar();
        consola.linea("Tabla de validacion (columna Simulador)", Tema.TEXTO_SUAVE);
        for (int k = 0; k < 3; k++) {
            App.problema = k + 1;
            double v = App.simpson(lim[k][0], lim[k][1], 2000, metodos[k]);
            consola.linea(String.format(Locale.US, "Config %d  [%s, %s]  %-9s  %s", k + 1,
                Tema.compacto(lim[k][0]), Tema.compacto(lim[k][1]), metodos[k], f6(v)), Tema.ACENTO);
        }
        App.problema = guardado;
        consola.linea("Completa Manual y Error % con tus calculos.", Tema.TEXTO_SUAVE);
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            Tema.instalar();
            new Ventana().setVisible(true);
        });
    }
}