import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;


public final class Tema {
    private Tema() { }

    public static final Color FONDO = new Color(0x0E1214);
    public static final Color SUPERFICIE = new Color(0x151B1E);
    public static final Color ELEVADA = new Color(0x1C2427);
    public static final Color BORDE = new Color(0x2B363A);
    public static final Color BORDE_FUERTE = new Color(0x415156);
    public static final Color TEXTO = new Color(0xE7EEEC);
    public static final Color TEXTO_SUAVE = new Color(0x9AABAF);
    public static final Color ACENTO = new Color(0x4FB3BF);
    public static final Color SOBRE_ACENTO = new Color(0x0A1417);
    public static final Color CURVA_G = new Color(0xE3A54B);
    public static final Color EJE = new Color(0xE5685A);
    public static final Color OK = new Color(0x67C58F);
    public static final Color ERROR = new Color(0xF0806F);

    public static final int R_TARJETA = 10;
    public static final int R_CONTROL = 8;

    public static Color mezcla(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
            (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
            (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    public static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    
    private static final String[] FAM_SANS = {"Segoe UI", "SF Pro Text", "Inter", "Helvetica Neue", "Dialog"};
    private static final String[] FAM_MONO = {"Cascadia Mono", "JetBrains Mono", "Consolas", "SF Mono", "Menlo",
                                              "DejaVu Sans Mono", "Monospaced"};
    private static final Set<String> INSTALADAS = new HashSet<>(Arrays.asList(
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

    private static String elegir(String[] candidatas) {
        for (String c : candidatas) {
            if (INSTALADAS.contains(c)) {
                return c;
            }
        }
        return candidatas[candidatas.length - 1];
    }

    public static final String SANS = elegir(FAM_SANS);
    public static final String MONO = elegir(FAM_MONO);

    public static Font sans(int estilo, int tam) {
        return new Font(SANS, estilo, tam);
    }

    public static Font mono(int estilo, int tam) {
        return new Font(MONO, estilo, tam);
    }

    

   
    public static volatile boolean reducir = Boolean.getBoolean("tema.reducir");

    
    static volatile boolean teclado = false;

    
    public static double suave(double t) {
        return bezier(0.23, 1, 0.32, 1, t);
    }

    private static double bezier(double x1, double y1, double x2, double y2, double x) {
        if (x <= 0) {
            return 0;
        }
        if (x >= 1) {
            return 1;
        }
        double lo = 0, hi = 1, t = x;
        for (int i = 0; i < 24; i++) {
            double xt = punto(x1, x2, t);
            if (Math.abs(xt - x) < 1e-5) {
                break;
            }
            if (xt < x) {
                lo = t;
            } else {
                hi = t;
            }
            t = (lo + hi) / 2;
        }
        return punto(y1, y2, t);
    }

    private static double punto(double a, double b, double t) {
        double u = 1 - t;
        return 3 * u * u * t * a + 3 * u * t * t * b + t * t * t;
    }

    
    public static final class Valor {
        private final JComponent dueno;
        private double desde, hasta, actual;
        private long inicio;
        private int duracion;

        public Valor(JComponent dueno, double inicial) {
            this.dueno = dueno;
            this.actual = inicial;
            this.desde = inicial;
            this.hasta = inicial;
        }

        public double get() {
            return actual;
        }

        public void fijar(double v) {
            actual = v;
            desde = v;
            hasta = v;
            Reloj.quitar(this);
            dueno.repaint();
        }

        public void ir(double destino, int ms) {
            if (destino == hasta) {
                return;
            }
            if (reducir || ms <= 0) {
                fijar(destino);
                return;
            }
            desde = actual;
            hasta = destino;
            inicio = System.nanoTime();
            duracion = ms;
            Reloj.agregar(this);
        }

        boolean paso(long ahora) {
            double p = Math.min(1.0, (ahora - inicio) / (duracion * 1e6));
            actual = desde + (hasta - desde) * suave(p);
            dueno.repaint();
            return p < 1.0;
        }
    }

   
    private static final class Reloj {
        private static final List<Valor> ACTIVOS = new ArrayList<>();
        private static final Timer TIMER = new Timer(12, e -> {
            long ahora = System.nanoTime();
            for (Iterator<Valor> it = ACTIVOS.iterator(); it.hasNext();) {
                if (!it.next().paso(ahora)) {
                    it.remove();
                }
            }
            if (ACTIVOS.isEmpty()) {
                ((Timer) e.getSource()).stop();
            }
        });

        static void agregar(Valor v) {
            if (!ACTIVOS.contains(v)) {
                ACTIVOS.add(v);
            }
            if (!TIMER.isRunning()) {
                TIMER.start();
            }
        }

        static void quitar(Valor v) {
            ACTIVOS.remove(v);
        }
    }

    
    public static Graphics2D calidad(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        return g2;
    }

    public static RoundRectangle2D caja(double x, double y, double w, double h, double radio) {
        return new RoundRectangle2D.Double(x, y, w, h, radio * 2, radio * 2);
    }

    
    public static String compacto(double v) {
        String s = String.format(Locale.US, "%.6f", v);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s.equals("-0") ? "0" : s;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(sans(Font.PLAIN, 12));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(sans(Font.BOLD, 14));
        l.setForeground(TEXTO);
        return l;
    }

   
    public static void instalar() {
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (ev.getID() == KeyEvent.KEY_PRESSED) {
                teclado = true;
            } else if (ev.getID() == MouseEvent.MOUSE_PRESSED) {
                teclado = false;
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    private static boolean verFoco(Component c) {
        return teclado && c.isFocusOwner();
    }

    private static final FocusAdapter REPINTAR = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            e.getComponent().repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            e.getComponent().repaint();
        }
    };

    
    public static class Tarjeta extends JPanel {
        public Tarjeta(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            g2.setColor(SUPERFICIE);
            g2.fill(caja(0.5, 0.5, getWidth() - 1, getHeight() - 1, R_TARJETA));
            g2.setColor(BORDE);
            g2.draw(caja(0.5, 0.5, getWidth() - 1, getHeight() - 1, R_TARJETA));
            g2.dispose();
        }
    }

    
    public static class PanelVertical extends JPanel implements Scrollable {
        public PanelVertical(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    
    public static class Boton extends JButton {
        private final boolean primario;
        private final Valor sobre = new Valor(this, 0);
        private final Valor presion = new Valor(this, 0);

        public Boton(String texto, boolean primario) {
            super(texto);
            this.primario = primario;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(sans(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(REPINTAR);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre.ir(1, 140);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre.ir(0, 180);
                }
            });
            getModel().addChangeListener(e -> presion.ir(getModel().isArmed() && getModel().isPressed() ? 1 : 0, 110));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 36, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            int w = getWidth(), h = getHeight();
            double hv = sobre.get(), pr = presion.get();

            double esc = 1 - 0.02 * pr;
            g2.translate(w / 2.0, h / 2.0);
            g2.scale(esc, esc);
            g2.translate(-w / 2.0, -h / 2.0);

            Color fondo, borde;
            if (primario) {
                fondo = mezcla(ACENTO, mezcla(ACENTO, Color.WHITE, 0.14), hv);
                borde = fondo;
            } else {
                fondo = mezcla(alfa(ELEVADA, 0), ELEVADA, hv);
                borde = mezcla(BORDE, BORDE_FUERTE, hv);
            }
            g2.setColor(fondo);
            g2.fill(caja(0.5, 0.5, w - 1, h - 1, R_CONTROL));
            g2.setColor(borde);
            g2.draw(caja(0.5, 0.5, w - 1, h - 1, R_CONTROL));

            if (verFoco(this)) {
                g2.setColor(primario ? SOBRE_ACENTO : ACENTO);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(caja(2, 2, w - 4, h - 4, R_CONTROL - 2));
            }

            g2.setFont(getFont());
            g2.setColor(primario ? SOBRE_ACENTO : TEXTO);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2f, (h - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    
    public static class Campo extends JTextField {
        private final Valor foco = new Valor(this, 0);
        private final Valor sobre = new Valor(this, 0);

        public Campo(String texto) {
            super(texto);
            setOpaque(false);
            setFont(mono(Font.PLAIN, 14));
            setForeground(TEXTO);
            setCaretColor(ACENTO);
            setSelectionColor(alfa(ACENTO, 110));
            setSelectedTextColor(TEXTO);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    foco.ir(1, 150);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    foco.ir(0, 180);
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre.ir(1, 140);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre.ir(0, 180);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            g2.setColor(ELEVADA);
            g2.fill(caja(0.5, 0.5, getWidth() - 1, getHeight() - 1, R_CONTROL));
            g2.dispose();

            super.paintComponent(g);

            g2 = calidad(g);
            g2.setColor(mezcla(mezcla(BORDE, BORDE_FUERTE, sobre.get()), ACENTO, foco.get()));
            g2.setStroke(new BasicStroke((float) (1 + 0.5 * foco.get())));
            g2.draw(caja(0.75, 0.75, getWidth() - 1.5, getHeight() - 1.5, R_CONTROL));
            g2.dispose();
        }
    }

    
    public static class Segmentado extends JComponent {
        private final String[] etiquetas;
        private int sel = 0;
        private int sobre = -1;
        private final Valor pos = new Valor(this, 0);
        private final List<IntConsumer> oyentes = new ArrayList<>();

        public Segmentado(String... etiquetas) {
            this.etiquetas = etiquetas;
            setFocusable(true);
            setOpaque(false);
            setFont(sans(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(REPINTAR);
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    seleccionar(indiceEn(e.getX()));
                    requestFocusInWindow();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    sobre = indiceEn(e.getX());
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = -1;
                    repaint();
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        seleccionar(Math.max(0, sel - 1));
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        seleccionar(Math.min(etiquetas.length - 1, sel + 1));
                    }
                }
            });
        }

        public void alCambiar(IntConsumer c) {
            oyentes.add(c);
        }

        public int indice() {
            return sel;
        }

        
        public void poner(int i) {
            sel = i;
            pos.ir(i, 200);
        }

        private void seleccionar(int i) {
            if (i == sel) {
                return;
            }
            poner(i);
            for (IntConsumer c : oyentes) {
                c.accept(i);
            }
        }

        private int indiceEn(int x) {
            double segW = (getWidth() - 6) / (double) etiquetas.length;
            return Math.max(0, Math.min(etiquetas.length - 1, (int) ((x - 3) / segW)));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(etiquetas.length * 96, 36);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            int w = getWidth(), h = getHeight(), pad = 3;
            double segW = (w - 2.0 * pad) / etiquetas.length;

            g2.setColor(FONDO);
            g2.fill(caja(0.5, 0.5, w - 1, h - 1, R_CONTROL));
            g2.setColor(BORDE);
            g2.draw(caja(0.5, 0.5, w - 1, h - 1, R_CONTROL));

            double x = pad + pos.get() * segW;
            g2.setColor(mezcla(ELEVADA, ACENTO, 0.18));
            g2.fill(caja(x, pad, segW, h - 2.0 * pad, R_CONTROL - 2));
            g2.setColor(alfa(ACENTO, 170));
            g2.draw(caja(x + 0.5, pad + 0.5, segW - 1, h - 2.0 * pad - 1, R_CONTROL - 2));

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < etiquetas.length; i++) {
                double cerca = Math.max(0, 1 - Math.abs(pos.get() - i));
                Color base = mezcla(TEXTO_SUAVE, TEXTO, sobre == i ? 0.75 : 0);
                g2.setColor(mezcla(base, TEXTO, cerca));
                double cx = pad + segW * i + segW / 2;
                g2.drawString(etiquetas[i], (float) (cx - fm.stringWidth(etiquetas[i]) / 2.0),
                    (h - fm.getHeight()) / 2f + fm.getAscent());
            }

            if (verFoco(this)) {
                g2.setColor(ACENTO);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(caja(1, 1, w - 2, h - 2, R_CONTROL));
            }
            g2.dispose();
        }
    }

    
    public static class Pestanas extends JComponent {
        private final String[] etiquetas;
        private int sel = 0;
        private final Valor ix = new Valor(this, 0);
        private final Valor iw = new Valor(this, 0);
        private final Valor[] activo;
        private final Valor[] sobre;
        private final List<IntConsumer> oyentes = new ArrayList<>();
        private static final int PAD = 18;

        public Pestanas(String... etiquetas) {
            this.etiquetas = etiquetas;
            this.activo = new Valor[etiquetas.length];
            this.sobre = new Valor[etiquetas.length];
            for (int i = 0; i < etiquetas.length; i++) {
                activo[i] = new Valor(this, i == 0 ? 1 : 0);
                sobre[i] = new Valor(this, 0);
            }
            setFocusable(true);
            setOpaque(false);
            setFont(sans(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(REPINTAR);
            ix.fijar(inicio(0));
            iw.fijar(ancho(0));
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = indiceEn(e.getX());
                    if (i >= 0) {
                        seleccionar(i);
                        requestFocusInWindow();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = indiceEn(e.getX());
                    for (int k = 0; k < sobre.length; k++) {
                        sobre[k].ir(k == i ? 1 : 0, 140);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    for (Valor v : sobre) {
                        v.ir(0, 180);
                    }
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        seleccionar(Math.max(0, sel - 1));
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        seleccionar(Math.min(etiquetas.length - 1, sel + 1));
                    }
                }
            });
        }

        public void alCambiar(IntConsumer c) {
            oyentes.add(c);
        }

        public int indice() {
            return sel;
        }

        private int ancho(int i) {
            return getFontMetrics(getFont()).stringWidth(etiquetas[i]) + PAD * 2;
        }

        private int inicio(int i) {
            int x = 0;
            for (int k = 0; k < i; k++) {
                x += ancho(k);
            }
            return x;
        }

        private int indiceEn(int x) {
            for (int i = 0; i < etiquetas.length; i++) {
                if (x >= inicio(i) && x < inicio(i) + ancho(i)) {
                    return i;
                }
            }
            return -1;
        }

        private void seleccionar(int i) {
            if (i == sel) {
                return;
            }
            sel = i;
            ix.ir(inicio(i), 240);
            iw.ir(ancho(i), 240);
            for (int k = 0; k < activo.length; k++) {
                activo[k].ir(k == i ? 1 : 0, 200);
            }
            for (IntConsumer c : oyentes) {
                c.accept(i);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(inicio(etiquetas.length), 44);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(BORDE);
            g2.fillRect(0, h - 1, w, 1);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < etiquetas.length; i++) {
                Color base = mezcla(TEXTO_SUAVE, TEXTO, sobre[i].get() * 0.7);
                g2.setColor(mezcla(base, TEXTO, activo[i].get()));
                g2.drawString(etiquetas[i], inicio(i) + PAD, (h - 2 - fm.getHeight()) / 2f + fm.getAscent());
            }

            g2.setColor(ACENTO);
            g2.fill(caja(ix.get() + PAD * 0.5, h - 3, iw.get() - PAD, 3, 1.5));

            if (verFoco(this)) {
                g2.setColor(ACENTO);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(caja(ix.get() + 2, 4, iw.get() - 4, h - 10, R_CONTROL));
            }
            g2.dispose();
        }
    }

    
    public static class ListaProblemas extends JComponent {
        private static final int ALTO_FILA = 52;
        private final String[] titulos;
        private final String[] detalles;
        private int sel = 0;
        private final Valor pos = new Valor(this, 0);
        private final Valor[] sobre;
        private final List<IntConsumer> oyentes = new ArrayList<>();

        public ListaProblemas(String[] titulos, String[] detalles) {
            this.titulos = titulos;
            this.detalles = detalles;
            this.sobre = new Valor[titulos.length];
            for (int i = 0; i < sobre.length; i++) {
                sobre[i] = new Valor(this, 0);
            }
            setFocusable(true);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(REPINTAR);
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    seleccionar(filaEn(e.getY()));
                    requestFocusInWindow();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    int f = filaEn(e.getY());
                    for (int k = 0; k < sobre.length; k++) {
                        sobre[k].ir(k == f ? 1 : 0, 140);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    for (Valor v : sobre) {
                        v.ir(0, 180);
                    }
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        seleccionar(Math.max(0, sel - 1));
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        seleccionar(Math.min(titulos.length - 1, sel + 1));
                    }
                }
            });
        }

        public void alCambiar(IntConsumer c) {
            oyentes.add(c);
        }

        public int indice() {
            return sel;
        }

        public void poner(int i) {
            sel = i;
            pos.ir(i, 220);
        }

        private void seleccionar(int i) {
            if (i == sel) {
                return;
            }
            poner(i);
            for (IntConsumer c : oyentes) {
                c.accept(i);
            }
        }

        private int filaEn(int y) {
            return Math.max(0, Math.min(titulos.length - 1, y / ALTO_FILA));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, ALTO_FILA * titulos.length);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            int w = getWidth();

            for (int i = 0; i < titulos.length; i++) {
                double t = sobre[i].get();
                if (t > 0.01) {
                    g2.setColor(alfa(ELEVADA, (int) (150 * t)));
                    g2.fill(caja(0, i * ALTO_FILA + 2, w, ALTO_FILA - 4, R_CONTROL));
                }
            }

            double y = pos.get() * ALTO_FILA;
            g2.setColor(ELEVADA);
            g2.fill(caja(0, y + 2, w, ALTO_FILA - 4, R_CONTROL));
            g2.setColor(BORDE_FUERTE);
            g2.draw(caja(0.5, y + 2.5, w - 1, ALTO_FILA - 5, R_CONTROL));
            g2.setColor(ACENTO);
            g2.fill(caja(0, y + 15, 3, ALTO_FILA - 30, 1.5));

            for (int i = 0; i < titulos.length; i++) {
                double cerca = Math.max(0, 1 - Math.abs(pos.get() - i));
                int top = i * ALTO_FILA;
                g2.setFont(sans(Font.BOLD, 13));
                g2.setColor(mezcla(mezcla(TEXTO_SUAVE, TEXTO, sobre[i].get() * 0.8), TEXTO, cerca));
                g2.drawString(titulos[i], 16, top + 22);
                g2.setFont(mono(Font.PLAIN, 12));
                g2.setColor(mezcla(TEXTO_SUAVE, mezcla(TEXTO_SUAVE, TEXTO, 0.7), cerca));
                g2.drawString(detalles[i], 16, top + 39);
            }

            if (verFoco(this)) {
                g2.setColor(ACENTO);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(caja(1, y + 3, w - 2, ALTO_FILA - 6, R_CONTROL));
            }
            g2.dispose();
        }
    }

    
    public static class Metrica extends JComponent {
        private final String etiqueta;
        private String valor = "-";
        private String detalle = "";
        private Color colorValor = TEXTO;
        private Color colorDetalle = TEXTO_SUAVE;
        private final Valor destello = new Valor(this, 0);

        public Metrica(String etiqueta) {
            this.etiqueta = etiqueta;
            setOpaque(false);
        }

        /** Cambia el contenido. Si destacar es true, el borde destella en el color de acento. */
        public void poner(String valor, String detalle, Color colorValor, Color colorDetalle, boolean destacar) {
            this.valor = valor;
            this.detalle = detalle;
            this.colorValor = colorValor;
            this.colorDetalle = colorDetalle;
            if (destacar) {
                destello.fijar(1);
                destello.ir(0, 700);
            }
            repaint();
        }

        public void vaciar(String detalle) {
            poner("-", detalle, TEXTO_SUAVE, TEXTO_SUAVE, false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(180, 96);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(SUPERFICIE);
            g2.fill(caja(0.5, 0.5, w - 1, h - 1, R_TARJETA));
            g2.setColor(mezcla(BORDE, ACENTO, destello.get()));
            g2.draw(caja(0.5, 0.5, w - 1, h - 1, R_TARJETA));

            g2.setFont(sans(Font.PLAIN, 12));
            g2.setColor(TEXTO_SUAVE);
            g2.drawString(etiqueta, 16, 27);

            int tam = 26;
            Font f = mono(Font.BOLD, tam);
            while (tam > 14 && g2.getFontMetrics(f).stringWidth(valor) > w - 32) {
                tam--;
                f = mono(Font.BOLD, tam);
            }
            g2.setFont(f);
            g2.setColor(colorValor);
            g2.drawString(valor, 16, 59);

            g2.setFont(sans(Font.PLAIN, 12));
            g2.setColor(colorDetalle);
            g2.drawString(detalle, 16, h - 13);
            g2.dispose();
        }
    }

    
    public static class Pildora extends JComponent {
        private String texto = "";
        private Color color = TEXTO_SUAVE;
        private Color anterior = TEXTO_SUAVE;
        private final Valor cambio = new Valor(this, 1);

        public Pildora(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            this.anterior = color;
            setOpaque(false);
        }

        public void poner(String texto, Color color) {
            this.anterior = mezcla(anterior, this.color, cambio.get());
            this.texto = texto;
            this.color = color;
            cambio.fijar(0);
            cambio.ir(1, 200);
            revalidate();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(sans(Font.BOLD, 12));
            return new Dimension(fm.stringWidth(texto) + 28, 28);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            Color c = mezcla(anterior, color, cambio.get());
            int w = getWidth(), h = getHeight();
            g2.setColor(alfa(c, 30));
            g2.fill(caja(0.5, 0.5, w - 1, h - 1, h / 2.0));
            g2.setColor(alfa(c, 120));
            g2.draw(caja(0.5, 0.5, w - 1, h - 1, h / 2.0));
            g2.setFont(sans(Font.BOLD, 12));
            g2.setColor(c);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, (w - fm.stringWidth(texto)) / 2f, (h - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    
    public static class Interruptor extends JComponent {
        private final String texto;
        private boolean activo = false;
        private final Valor pos = new Valor(this, 0);
        private final List<Consumer<Boolean>> oyentes = new ArrayList<>();

        public Interruptor(String texto) {
            this.texto = texto;
            setFocusable(true);
            setOpaque(false);
            setFont(sans(Font.PLAIN, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(REPINTAR);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    alternar();
                    requestFocusInWindow();
                }
            });
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                        alternar();
                    }
                }
            });
        }

        public void alCambiar(Consumer<Boolean> c) {
            oyentes.add(c);
        }

        public boolean activo() {
            return activo;
        }

        public void poner(boolean v) {
            activo = v;
            pos.ir(v ? 1 : 0, 160);
        }

        private void alternar() {
            poner(!activo);
            for (Consumer<Boolean> c : oyentes) {
                c.accept(activo);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(texto) + 14 + 36, 28);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = calidad(g);
            int w = getWidth(), h = getHeight();
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(TEXTO_SUAVE);
            g2.drawString(texto, 0, (h - fm.getHeight()) / 2f + fm.getAscent());

            double t = pos.get();
            int tw = 36, th = 20, tx = w - tw, ty = (h - th) / 2;
            g2.setColor(mezcla(ELEVADA, ACENTO, t));
            g2.fill(caja(tx, ty, tw, th, th / 2.0));
            g2.setColor(mezcla(BORDE_FUERTE, ACENTO, t));
            g2.draw(caja(tx + 0.5, ty + 0.5, tw - 1, th - 1, th / 2.0));
            g2.setColor(mezcla(TEXTO_SUAVE, SOBRE_ACENTO, t));
            g2.fillOval((int) Math.round(tx + 3 + t * 16), ty + 3, 14, 14);

            if (verFoco(this)) {
                g2.setColor(ACENTO);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(caja(tx - 2, ty - 2, tw + 4, th + 4, th / 2.0 + 2));
            }
            g2.dispose();
        }
    }

    
    public static class Escenario extends JPanel {
        private final CardLayout cartas = new CardLayout();
        private final Valor velo = new Valor(this, 0);

        public Escenario() {
            setLayout(cartas);
            setOpaque(false);
        }

        public void mostrar(String nombre) {
            cartas.show(this, nombre);
            velo.fijar(1);
            velo.ir(0, 240);
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            double v = velo.get();
            if (v > 0.004) {
                g.setColor(alfa(SUPERFICIE, (int) Math.round(255 * v)));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    
    private static class BarraUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() { }

        private JButton vacio() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return vacio();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return vacio();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = calidad(g);
            g2.setColor(alfa(TEXTO_SUAVE, isThumbRollover() ? 150 : 90));
            g2.fill(caja(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 3));
            g2.dispose();
        }
    }

    public static void barraFina(JScrollPane sp) {
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setViewportBorder(null);
        sp.getVerticalScrollBar().setUI(new BarraUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public static class Consola extends JPanel {
        private final JTextPane texto = new JTextPane();

        public Consola() {
            super(new BorderLayout());
            setOpaque(false);
            texto.setEditable(false);
            texto.setOpaque(false);
            texto.setFont(mono(Font.PLAIN, 13));
            texto.setForeground(TEXTO);
            texto.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 8));
            JScrollPane sp = new JScrollPane(texto);
            barraFina(sp);
            add(sp, BorderLayout.CENTER);
        }

        public void limpiar() {
            texto.setText("");
        }

        public void linea(String s, Color c) {
            StyledDocument doc = texto.getStyledDocument();
            SimpleAttributeSet at = new SimpleAttributeSet();
            StyleConstants.setForeground(at, c);
            StyleConstants.setFontFamily(at, MONO);
            StyleConstants.setFontSize(at, 13);
            try {
                doc.insertString(doc.getLength(), s + "\n", at);
            } catch (BadLocationException ex) {
                // no ocurre: siempre se inserta al final
            }
            texto.setCaretPosition(doc.getLength());
        }
    }
}
