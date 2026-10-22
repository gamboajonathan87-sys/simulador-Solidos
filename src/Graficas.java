import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Locale;


public final class Graficas {

    private Graficas() { }

    
    
    static double paso(double rango) {
        double bruto = rango / 6;
        double potencia = Math.pow(10, Math.floor(Math.log10(bruto)));
        double m = bruto / potencia;
        double bonito = m < 1.5 ? 1 : (m < 3.5 ? 2 : (m < 7.5 ? 5 : 10));
        return bonito * potencia;
    }

    static String num(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) {
            return String.valueOf((long) Math.rint(v));
        }
        return String.format(Locale.US, "%.2f", v);
    }

    static String n4(double v) {
        return String.format(Locale.US, "%.4f", v);
    }

   
    static int leyenda(Graphics2D g2, int x, int y, Color color, boolean caja, String texto) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(color);
        if (caja) {
            g2.fill(Tema.caja(x, y - 10, 14, 11, 3));
        } else {
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 1, y - 5, x + 13, y - 5);
        }
        g2.setColor(Tema.TEXTO_SUAVE);
        g2.drawString(texto, x + 22, y);
        return x + 22 + fm.stringWidth(texto) + 22;
    }

    
    static int filaLeyenda(Graphics2D g2, int anchoTotal, int xIni, int yIni,
                           String[] textos, Color[] colores, boolean[] cajas) {
        g2.setFont(Tema.sans(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        int lx = xIni, ly = yIni;
        for (int i = 0; i < textos.length; i++) {
            int necesita = 22 + fm.stringWidth(textos[i]);
            if (lx + necesita > anchoTotal - 20 && lx > xIni) {
                lx = xIni;
                ly += 18;
            }
            lx = leyenda(g2, lx, ly, colores[i], cajas[i], textos[i]);
        }
        return ly;
    }

   
    static void encabezado(Graphics2D g2, int x, String titulo, String subtitulo) {
        g2.setFont(Tema.sans(Font.BOLD, 16));
        g2.setColor(Tema.TEXTO);
        g2.drawString(titulo, x, 28);
        g2.setFont(Tema.mono(Font.PLAIN, 12));
        g2.setColor(Tema.TEXTO_SUAVE);
        g2.drawString(subtitulo, x, 48);
    }

   
    static void vacio(Graphics2D g2, int w, int h, String titulo, String detalle, boolean error) {
        int cx = w / 2, cy = h / 2;
        Color c = error ? Tema.ERROR : Tema.TEXTO_SUAVE;
        g2.setColor(Tema.alfa(c, 28));
        g2.fill(Tema.caja(cx - 22, cy - 62, 44, 44, 12));
        g2.setColor(Tema.alfa(c, 120));
        g2.draw(Tema.caja(cx - 22 + 0.5, cy - 62 + 0.5, 43, 43, 12));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(c);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(cx - 10, cy - 30);
        p.curveTo(cx - 4, cy - 52, cx + 4, cy - 52, cx + 10, cy - 30);
        g2.draw(p);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(Tema.sans(Font.BOLD, 15));
        g2.setColor(error ? Tema.ERROR : Tema.TEXTO);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, cx - fm.stringWidth(titulo) / 2, cy + 6);
        g2.setFont(Tema.sans(Font.PLAIN, 13));
        g2.setColor(Tema.TEXTO_SUAVE);
        fm = g2.getFontMetrics();
        g2.drawString(detalle, cx - fm.stringWidth(detalle) / 2, cy + 28);
    }

   
    static void tooltip(Graphics2D g2, int x, int y, int limiteDer, int limiteIzq, String[] lineas,
                        Color[] colores, double opacidad) {
        g2.setFont(Tema.mono(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        int ancho = 0;
        for (String s : lineas) {
            ancho = Math.max(ancho, fm.stringWidth(s));
        }
        int w = ancho + 24, h = lineas.length * 18 + 14;
        int tx = x + 14;
        if (tx + w > limiteDer) {
            tx = x - 14 - w;
        }
        tx = Math.max(limiteIzq, tx);
        Composite previo = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive((float) Math.max(0, Math.min(1, opacidad))));
        g2.setColor(Tema.ELEVADA);
        g2.fill(Tema.caja(tx, y, w, h, 8));
        g2.setColor(Tema.BORDE_FUERTE);
        g2.draw(Tema.caja(tx + 0.5, y + 0.5, w - 1, h - 1, 8));
        for (int i = 0; i < lineas.length; i++) {
            g2.setColor(colores[i]);
            g2.drawString(lineas[i], tx + 12, y + 22 + i * 18);
        }
        g2.setComposite(previo);
    }

    private static final float[] PUNTEADO = {5f, 5f};

    static BasicStroke punteado(float ancho) {
        return new BasicStroke(ancho, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, PUNTEADO, 0f);
    }

    
    abstract static class Base extends JComponent {
        protected boolean hay = false;
        protected String vacioTitulo = "Sin datos todavia";
        protected String vacioDetalle = "Elige un problema y presiona Calcular.";
        protected boolean vacioError = false;
        protected final Tema.Valor aparece = new Tema.Valor(this, 1);

        Base() {
            setOpaque(false);
        }

        void limpiar() {
            limpiar("Sin datos todavia", "Elige un problema y presiona Calcular.", false);
        }

        void limpiar(String titulo, String detalle, boolean error) {
            hay = false;
            vacioTitulo = titulo;
            vacioDetalle = detalle;
            vacioError = error;
            repaint();
        }

        protected void revelar(int ms) {
            aparece.fijar(0);
            aparece.ir(1, ms);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.calidad(g);
            g2.setColor(Tema.SUPERFICIE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (!hay) {
                vacio(g2, getWidth(), getHeight(), vacioTitulo, vacioDetalle, vacioError);
            } else {
                dibujar(g2, getWidth(), getHeight());
            }
            g2.dispose();
        }

        protected abstract void dibujar(Graphics2D g2, int w, int h);
    }

    
    static class PanelRegion extends Base {
        private double a, b;
        private char eje;
        private String titulo = "";
        private double xLo, xHi, yLo, yHi;
        private final int izq = 64, arr = 112;
        private int ancho, alto;
        private double hoverX = Double.NaN;
        private final Tema.Valor lectura = new Tema.Valor(this, 0);

        PanelRegion() {
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (!hay || ancho <= 0) {
                        return;
                    }
                    double x = xLo + (e.getX() - izq) * (xHi - xLo) / ancho;
                    boolean dentro = x >= a && x <= b && e.getY() >= arr && e.getY() <= arr + alto;
                    if (dentro) {
                        hoverX = x;
                    }
                    lectura.ir(dentro ? 1 : 0, dentro ? 140 : 180);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lectura.ir(0, 180);
                }
            };
            addMouseMotionListener(raton);
            addMouseListener(raton);
        }

        void mostrar(double a, double b, char eje, String titulo) {
            this.a = a;
            this.b = b;
            this.eje = eje;
            this.titulo = titulo;
            this.hay = true;
            this.hoverX = Double.NaN;
            lectura.fijar(0);
            revelar(520);
        }

        private double px(double x) {
            return izq + (x - xLo) / (xHi - xLo) * ancho;
        }

        private double py(double y) {
            return arr + (yHi - y) / (yHi - yLo) * alto;
        }

        @Override
        protected void dibujar(Graphics2D g2, int w, int h) {
            int der = 28, aba = 48;
            ancho = w - izq - der;
            alto = h - arr - aba;
            if (ancho < 50 || alto < 50) {
                return;
            }

            double xMin = Math.min(0, a), xMax = Math.max(0, b);
            double padX = 0.1 * (xMax - xMin);
            xLo = xMin - padX;
            xHi = xMax + padX;

            double yMin = 0, yMax = 0;
            for (int i = 0; i <= 200; i++) {
                double x = a + (b - a) * i / 200;
                yMin = Math.min(yMin, Math.min(App.f(x), App.g(x)));
                yMax = Math.max(yMax, Math.max(App.f(x), App.g(x)));
            }
            if (yMax - yMin < 1e-9) {
                yMax = yMin + 1;
            }
            double padY = 0.1 * (yMax - yMin);
            yLo = yMin - padY;
            yHi = yMax + padY;

            encabezado(g2, izq, "Region plana que se hace girar", titulo);
            filaLeyenda(g2, w, izq, 78,
                new String[]{"f(x) curva superior", "g(x) curva inferior",
                             "Region", "Eje de rotacion (" + eje + ")"},
                new Color[]{Tema.ACENTO, Tema.CURVA_G, Tema.alfa(Tema.ACENTO, 120), Tema.EJE},
                new boolean[]{false, false, true, false});

           
            g2.setFont(Tema.mono(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.setStroke(new BasicStroke(1f));
            double pasoX = paso(xHi - xLo);
            for (double t = Math.ceil(xLo / pasoX) * pasoX; t <= xHi + 1e-9; t += pasoX) {
                int X = (int) Math.round(px(t));
                g2.setColor(Tema.alfa(Tema.BORDE, 140));
                g2.drawLine(X, arr, X, arr + alto);
                g2.setColor(Tema.TEXTO_SUAVE);
                String s = num(t);
                g2.drawString(s, X - fm.stringWidth(s) / 2, arr + alto + 18);
            }
            double pasoY = paso(yHi - yLo);
            for (double t = Math.ceil(yLo / pasoY) * pasoY; t <= yHi + 1e-9; t += pasoY) {
                int Y = (int) Math.round(py(t));
                g2.setColor(Tema.alfa(Tema.BORDE, 140));
                g2.drawLine(izq, Y, izq + ancho, Y);
                g2.setColor(Tema.TEXTO_SUAVE);
                String s = num(t);
                g2.drawString(s, izq - 10 - fm.stringWidth(s), Y + 4);
            }
            g2.setColor(Tema.BORDE_FUERTE);
            g2.drawRect(izq, arr, ancho, alto);
            g2.setFont(Tema.mono(Font.BOLD, 12));
            g2.setColor(Tema.TEXTO_SUAVE);
            g2.drawString("x", izq + ancho / 2, arr + alto + 38);
            g2.drawString("y", izq - 6, arr - 10);

            // Contenido que se revela de izquierda a derecha
            double t = aparece.get();
            Shape clipPrevio = g2.getClip();
            g2.clipRect(izq - 2, arr - 4, (int) Math.ceil((ancho + 4) * t) + 1, alto + 8);

            int n = 300;
            Path2D.Double region = new Path2D.Double();
            for (int i = 0; i <= n; i++) {
                double x = a + (b - a) * i / n;
                if (i == 0) {
                    region.moveTo(px(x), py(App.f(x)));
                } else {
                    region.lineTo(px(x), py(App.f(x)));
                }
            }
            for (int i = n; i >= 0; i--) {
                double x = a + (b - a) * i / n;
                region.lineTo(px(x), py(App.g(x)));
            }
            region.closePath();
            g2.setColor(Tema.alfa(Tema.ACENTO, 70));
            g2.fill(region);

            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(Tema.alfa(Tema.TEXTO_SUAVE, 170));
            g2.drawLine(izq, (int) Math.round(py(0)), izq + ancho, (int) Math.round(py(0)));
            g2.drawLine((int) Math.round(px(0)), arr, (int) Math.round(px(0)), arr + alto);

            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Tema.EJE);
            if (eje == 'X') {
                int Y0 = (int) Math.round(py(0));
                g2.drawLine(izq, Y0, izq + ancho, Y0);
            } else {
                int X0 = (int) Math.round(px(0));
                g2.drawLine(X0, arr, X0, arr + alto);
            }

            Path2D.Double cf = new Path2D.Double();
            Path2D.Double cg = new Path2D.Double();
            for (int i = 0; i <= n; i++) {
                double x = a + (b - a) * i / n;
                if (i == 0) {
                    cf.moveTo(px(x), py(App.f(x)));
                    cg.moveTo(px(x), py(App.g(x)));
                } else {
                    cf.lineTo(px(x), py(App.f(x)));
                    cg.lineTo(px(x), py(App.g(x)));
                }
            }
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Tema.ACENTO);
            g2.draw(cf);
            g2.setColor(Tema.CURVA_G);
            g2.draw(cg);
            g2.setClip(clipPrevio);

            
            g2.setStroke(punteado(1.3f));
            g2.setColor(Tema.alfa(Tema.TEXTO_SUAVE, 200));
            int XA = (int) Math.round(px(a));
            int XB = (int) Math.round(px(b));
            g2.drawLine(XA, arr, XA, arr + alto);
            g2.drawLine(XB, arr, XB, arr + alto);
            g2.setStroke(new BasicStroke(1f));
            g2.setFont(Tema.mono(Font.PLAIN, 12));
            fm = g2.getFontMetrics();
            g2.setColor(Tema.TEXTO);
            g2.drawString("a = " + num(a), XA + 6, arr + alto - 8);
            String textoB = "b = " + num(b);
            g2.drawString(textoB, XB - fm.stringWidth(textoB) - 6, arr + alto - 8);

            
            double o = lectura.get();
            if (o > 0.01 && !Double.isNaN(hoverX)) {
                double fx = App.f(hoverX), gx = App.g(hoverX);
                int X = (int) Math.round(px(hoverX));
                g2.setStroke(punteado(1f));
                g2.setColor(Tema.alfa(Tema.TEXTO, (int) (110 * o)));
                g2.drawLine(X, arr, X, arr + alto);
                g2.setStroke(new BasicStroke(1f));
                punto(g2, X, (int) Math.round(py(fx)), Tema.ACENTO, o);
                punto(g2, X, (int) Math.round(py(gx)), Tema.CURVA_G, o);
                tooltip(g2, X, arr + 10, izq + ancho, izq,
                    new String[]{"x    = " + String.format(Locale.US, "%.3f", hoverX),
                                 "f(x) = " + String.format(Locale.US, "%.4f", fx),
                                 "g(x) = " + String.format(Locale.US, "%.4f", gx)},
                    new Color[]{Tema.TEXTO, Tema.ACENTO, Tema.CURVA_G}, o);
            }
        }

        private void punto(Graphics2D g2, int x, int y, Color c, double o) {
            g2.setColor(Tema.alfa(c, (int) (60 * o)));
            g2.fillOval(x - 8, y - 8, 16, 16);
            g2.setColor(Tema.alfa(c, (int) (255 * o)));
            g2.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    
    static class PanelAcum extends Base {
        private double a, b, vMax;
        private String titulo = "", metodo = "";
        private double[] xs, vs;
        private double hoverX = Double.NaN;
        private final int izq = 76, arr = 92, der = 36, aba = 56;
        private int anchoPlot;
        private final Tema.Valor lectura = new Tema.Valor(this, 0);

        PanelAcum() {
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (!hay || anchoPlot <= 0) {
                        return;
                    }
                    double x = a + (e.getX() - izq) * (b - a) / anchoPlot;
                    boolean dentro = x >= a && x <= b;
                    if (dentro) {
                        hoverX = x;
                    }
                    lectura.ir(dentro ? 1 : 0, dentro ? 140 : 180);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lectura.ir(0, 180);
                }
            };
            addMouseMotionListener(raton);
            addMouseListener(raton);
        }

        void mostrar(double a, double b, String metodo, String titulo) {
            this.a = a;
            this.b = b;
            this.metodo = metodo;
            this.titulo = titulo;
            int n = 200;
            xs = new double[n + 1];
            vs = new double[n + 1];
            for (int i = 0; i <= n; i++) {
                xs[i] = a + (b - a) * i / n;
                vs[i] = App.volumenAcumulado(a, xs[i], metodo);
            }
            vMax = Math.max(vs[n], 1e-9);
            hoverX = Double.NaN;
            lectura.fijar(0);
            hay = true;
            revelar(700);
        }

        @Override
        protected void dibujar(Graphics2D g2, int w, int h) {
            int ancho = w - izq - der;
            int alto = h - arr - aba;
            anchoPlot = ancho;
            if (ancho < 50 || alto < 50) {
                return;
            }
            double yHi = vMax * 1.1;

            encabezado(g2, izq, "Volumen acumulado Vacum(x)", titulo + "  |  metodo: " + metodo);

            g2.setFont(Tema.mono(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.setStroke(new BasicStroke(1f));
            double pasoX = paso(b - a);
            for (double t = Math.ceil(a / pasoX) * pasoX; t <= b + 1e-9; t += pasoX) {
                int X = (int) Math.round(izq + (t - a) / (b - a) * ancho);
                g2.setColor(Tema.alfa(Tema.BORDE, 140));
                g2.drawLine(X, arr, X, arr + alto);
                g2.setColor(Tema.TEXTO_SUAVE);
                String s = num(t);
                g2.drawString(s, X - fm.stringWidth(s) / 2, arr + alto + 18);
            }
            double pasoY = paso(yHi);
            for (double t = 0; t <= yHi + 1e-9; t += pasoY) {
                int Y = (int) Math.round(arr + alto - t / yHi * alto);
                g2.setColor(Tema.alfa(Tema.BORDE, 140));
                g2.drawLine(izq, Y, izq + ancho, Y);
                g2.setColor(Tema.TEXTO_SUAVE);
                String s = num(t);
                g2.drawString(s, izq - 10 - fm.stringWidth(s), Y + 4);
            }
            g2.setColor(Tema.BORDE_FUERTE);
            g2.drawRect(izq, arr, ancho, alto);
            g2.setFont(Tema.mono(Font.BOLD, 12));
            g2.setColor(Tema.TEXTO_SUAVE);
            g2.drawString("x", izq + ancho / 2, arr + alto + 38);
            g2.drawString("V (u^3)", izq - 60, arr - 12);

            double t = aparece.get();
            Shape clipPrevio = g2.getClip();
            g2.clipRect(izq - 2, arr - 6, (int) Math.ceil((ancho + 4) * t) + 1, alto + 12);

            Path2D.Double curva = new Path2D.Double();
            Path2D.Double area = new Path2D.Double();
            for (int i = 0; i < xs.length; i++) {
                double X = izq + (xs[i] - a) / (b - a) * ancho;
                double Y = arr + alto - vs[i] / yHi * alto;
                if (i == 0) {
                    curva.moveTo(X, Y);
                    area.moveTo(X, arr + alto);
                    area.lineTo(X, Y);
                } else {
                    curva.lineTo(X, Y);
                    area.lineTo(X, Y);
                }
            }
            area.lineTo(izq + ancho, arr + alto);
            area.closePath();
            g2.setPaint(new GradientPaint(0, arr, Tema.alfa(Tema.ACENTO, 80),
                                          0, arr + alto, Tema.alfa(Tema.ACENTO, 0)));
            g2.fill(area);
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Tema.ACENTO);
            g2.draw(curva);
            g2.setClip(clipPrevio);

           
            int XB = izq + ancho;
            int YB = (int) Math.round(arr + alto - vs[vs.length - 1] / yHi * alto);
            double fin = Math.max(0, (t - 0.9) / 0.1);
            g2.setColor(Tema.alfa(Tema.ACENTO, (int) (60 * fin)));
            g2.fillOval(XB - 9, YB - 9, 18, 18);
            g2.setColor(Tema.alfa(Tema.ACENTO, (int) (255 * fin)));
            g2.fillOval(XB - 5, YB - 5, 10, 10);
            g2.setFont(Tema.mono(Font.BOLD, 12));
            fm = g2.getFontMetrics();
            String tot = "V total = " + n4(vs[vs.length - 1]);
            g2.setColor(Tema.alfa(Tema.TEXTO, (int) (255 * fin)));
            g2.drawString(tot, XB - fm.stringWidth(tot) - 10, YB + 40);

            double o = lectura.get();
            if (o > 0.01 && !Double.isNaN(hoverX)) {
                int idx = (int) Math.round((hoverX - a) / (b - a) * (xs.length - 1));
                double xv = xs[idx], vv = vs[idx];
                int X = (int) Math.round(izq + (xv - a) / (b - a) * ancho);
                int Y = (int) Math.round(arr + alto - vv / yHi * alto);
                g2.setStroke(punteado(1f));
                g2.setColor(Tema.alfa(Tema.TEXTO, (int) (110 * o)));
                g2.drawLine(X, arr, X, arr + alto);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Tema.alfa(Tema.ACENTO, (int) (60 * o)));
                g2.fillOval(X - 8, Y - 8, 16, 16);
                g2.setColor(Tema.alfa(Tema.TEXTO, (int) (255 * o)));
                g2.fillOval(X - 4, Y - 4, 8, 8);
                tooltip(g2, X, arr + 10, izq + ancho, izq,
                    new String[]{"x     = " + String.format(Locale.US, "%.3f", xv),
                                 "Vacum = " + n4(vv)},
                    new Color[]{Tema.TEXTO, Tema.ACENTO}, o);
            }
        }
    }

    
    static class Panel3D extends Base {

        private static class Cara {
            final double[][] p;
            final Color base;
            final double thetaFin;

            Cara(double[][] p, Color base, double thetaFin) {
                this.p = p;
                this.base = base;
                this.thetaFin = thetaFin;
            }
        }

        private static class Dibujo {
            int[] xs = new int[4];
            int[] ys = new int[4];
            double prof;
            Color color;
        }

        private static final double YAW0 = 0.6, PITCH0 = 0.45;
        private static final Color COL_F = new Color(0x4FB3BF);
        private static final Color COL_G = new Color(0xE3A54B);
        private static final Color COL_TAPA = new Color(0x8797A0);

        private final ArrayList<Cara> caras = new ArrayList<>();
        private boolean hayG = false, hayTapas = false;
        private double a, b, maxAbs = 1;
        private char eje = 'X';
        private String titulo = "";
        private double yaw = YAW0, pitch = PITCH0;
        private final Tema.Valor giro = new Tema.Valor(this, 1);
        private int ultX, ultY;
        private final javax.swing.Timer timerAuto;

        Panel3D() {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    ultX = e.getX();
                    ultY = e.getY();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    yaw += (e.getX() - ultX) * 0.01;
                    pitch += (e.getY() - ultY) * 0.01;
                    pitch = Math.max(-1.5, Math.min(1.5, pitch));
                    ultX = e.getX();
                    ultY = e.getY();
                    repaint();
                }
            };
            addMouseListener(raton);
            addMouseMotionListener(raton);
            timerAuto = new javax.swing.Timer(30, e -> {
                yaw += 0.02;
                repaint();
            });
        }

        void mostrar(double a, double b, char eje, String titulo) {
            this.a = a;
            this.b = b;
            this.eje = eje;
            this.titulo = titulo;
            construir();
            hay = true;
            giro.fijar(1);
            revelar(420);
        }

        @Override
        void limpiar(String titulo, String detalle, boolean error) {
            timerAuto.stop();
            super.limpiar(titulo, detalle, error);
        }

        void animar() {
            if (!hay) {
                return;
            }
            aparece.fijar(1);
            giro.fijar(0);
            giro.ir(1, 2400);
        }

        void setAutoRotar(boolean activo) {
            if (activo) {
                timerAuto.start();
            } else {
                timerAuto.stop();
            }
        }

        void vistaInicial() {
            yaw = YAW0;
            pitch = PITCH0;
            repaint();
        }

    
        private double[] par(boolean usaF, double x) {
            double h = usaF ? App.f(x) : App.g(x);
            return eje == 'X' ? new double[]{x, h} : new double[]{h, x};
        }

        private double[] punto(double axial, double radio, double th) {
            if (eje == 'X') {
                return new double[]{axial, radio * Math.cos(th), radio * Math.sin(th)};
            }
            return new double[]{radio * Math.cos(th), axial, radio * Math.sin(th)};
        }

        private void agregar(double[][] p, Color c, double thetaFin) {
            for (double[] q : p) {
                for (double v : q) {
                    maxAbs = Math.max(maxAbs, Math.abs(v));
                }
            }
            caras.add(new Cara(p, c, thetaFin));
        }

        private void superficie(boolean usaF, Color c, int nu, int nt) {
            for (int i = 0; i < nu; i++) {
                double x0 = a + (b - a) * i / nu;
                double x1 = a + (b - a) * (i + 1) / nu;
                double[] c0 = par(usaF, x0);
                double[] c1 = par(usaF, x1);
                for (int j = 0; j < nt; j++) {
                    double th0 = 2 * Math.PI * j / nt;
                    double th1 = 2 * Math.PI * (j + 1) / nt;
                    agregar(new double[][]{
                        punto(c0[0], c0[1], th0), punto(c1[0], c1[1], th0),
                        punto(c1[0], c1[1], th1), punto(c0[0], c0[1], th1)}, c, th1);
                }
            }
        }

        private void construir() {
            caras.clear();
            hayG = false;
            hayTapas = false;
            maxAbs = 1e-9;
            int nu = 40, nt = 60;
            double tol = 1e-9;

            boolean gEsCero = true;
            for (int i = 0; i <= nu; i++) {
                double x = a + (b - a) * i / nu;
                if (Math.abs(App.g(x)) > tol) {
                    gEsCero = false;
                }
            }
            superficie(true, COL_F, nu, nt);
            if (eje == 'Y' || !gEsCero) {
                superficie(false, COL_G, nu, nt);
                hayG = true;
            }
            for (double x : new double[]{a, b}) {
                double vf = App.f(x), vg = App.g(x);
                if (vf - vg <= tol) {
                    continue;
                }
                if (eje == 'Y' && x <= tol) {
                    continue;
                }
                for (int j = 0; j < nt; j++) {
                    double th0 = 2 * Math.PI * j / nt;
                    double th1 = 2 * Math.PI * (j + 1) / nt;
                    double[][] q;
                    if (eje == 'X') {
                        q = new double[][]{punto(x, vg, th0), punto(x, vf, th0),
                                           punto(x, vf, th1), punto(x, vg, th1)};
                    } else {
                        q = new double[][]{punto(vg, x, th0), punto(vf, x, th0),
                                           punto(vf, x, th1), punto(vg, x, th1)};
                    }
                    agregar(q, COL_TAPA, th1);
                }
                hayTapas = true;
            }
        }

        private double[] proy(double[] p, double escala, int cx, int cy) {
            double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
            double cosP = Math.cos(pitch), sinP = Math.sin(pitch);
            double x1 = p[0] * cosY + p[2] * sinY;
            double z1 = -p[0] * sinY + p[2] * cosY;
            double y2 = p[1] * cosP - z1 * sinP;
            double z2 = p[1] * sinP + z1 * cosP;
            return new double[]{cx + escala * x1, cy - escala * y2, x1, y2, z2};
        }

        private void linea(Graphics2D g2, double[] p, double[] q, double escala, int cx, int cy) {
            double[] u = proy(p, escala, cx, cy);
            double[] v = proy(q, escala, cx, cy);
            g2.drawLine((int) Math.round(u[0]), (int) Math.round(u[1]),
                        (int) Math.round(v[0]), (int) Math.round(v[1]));
        }

        @Override
        protected void dibujar(Graphics2D g2, int w, int h) {
            int cabecera = 112;
            double ext = maxAbs * 1.25;
            double escala = 0.5 * Math.min(w, h - cabecera) / ext;
            int cx = w / 2;
            int cy = cabecera + (h - cabecera) / 2;
            double thetaMax = giro.get() * 2 * Math.PI;

            encabezado(g2, 24, "Solido de revolucion", titulo + "  |  giro alrededor del eje " + eje);
            ArrayList<String> tx = new ArrayList<>();
            ArrayList<Color> co = new ArrayList<>();
            ArrayList<Boolean> ca = new ArrayList<>();
            tx.add("Superficie de f(x)"); co.add(COL_F); ca.add(true);
            if (hayG) { tx.add("Superficie de g(x)"); co.add(COL_G); ca.add(true); }
            if (hayTapas) { tx.add("Caras planas"); co.add(COL_TAPA); ca.add(true); }
            tx.add("Eje de rotacion (" + eje + ")"); co.add(Tema.EJE); ca.add(false);
            boolean[] cajas = new boolean[ca.size()];
            for (int i = 0; i < cajas.length; i++) {
                cajas[i] = ca.get(i);
            }
            filaLeyenda(g2, w, 24, 78, tx.toArray(new String[0]), co.toArray(new Color[0]), cajas);

            g2.setFont(Tema.sans(Font.PLAIN, 12));
            g2.setColor(Tema.TEXTO_SUAVE);
            String ayuda = "Arrastra para girar la vista";
            g2.drawString(ayuda, w - 24 - g2.getFontMetrics().stringWidth(ayuda), 28);

            Composite previo = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive((float) Math.max(0, Math.min(1, aparece.get()))));

            
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(Tema.alfa(Tema.TEXTO_SUAVE, 120));
            double[][] extremos = {{ext, 0, 0}, {0, ext, 0}, {0, 0, ext}};
            String[] nombres = {"X", "Y", "Z"};
            for (int k = 0; k < 3; k++) {
                double[] neg = {-extremos[k][0], -extremos[k][1], -extremos[k][2]};
                linea(g2, neg, extremos[k], escala, cx, cy);
            }
            g2.setFont(Tema.mono(Font.BOLD, 13));
            for (int k = 0; k < 3; k++) {
                if ((eje == 'X' && k == 0) || (eje == 'Y' && k == 1)) {
                    continue;
                }
                double[] t = proy(extremos[k], escala, cx, cy);
                g2.setColor(Tema.TEXTO_SUAVE);
                g2.drawString(nombres[k], (int) t[0] + 6, (int) t[1] - 4);
            }

            
            ArrayList<Dibujo> lista = new ArrayList<>();
            double lx = 0.35, ly = 0.55, lz = 0.75;
            double lon = Math.sqrt(lx * lx + ly * ly + lz * lz);
            for (Cara cara : caras) {
                if (cara.thetaFin > thetaMax + 1e-9) {
                    continue;
                }
                double[][] q = new double[4][];
                double prof = 0;
                Dibujo d = new Dibujo();
                for (int i = 0; i < 4; i++) {
                    q[i] = proy(cara.p[i], escala, cx, cy);
                    d.xs[i] = (int) Math.round(q[i][0]);
                    d.ys[i] = (int) Math.round(q[i][1]);
                    prof += q[i][4];
                }
                d.prof = prof / 4;
                double ux = q[2][2] - q[0][2], uy = q[2][3] - q[0][3], uz = q[2][4] - q[0][4];
                double vx = q[3][2] - q[1][2], vy = q[3][3] - q[1][3], vz = q[3][4] - q[1][4];
                double nx = uy * vz - uz * vy;
                double ny = uz * vx - ux * vz;
                double nz = ux * vy - uy * vx;
                double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
                double luz = nl < 1e-12 ? 0.5 : Math.abs(nx * lx + ny * ly + nz * lz) / (nl * lon);
                double brillo = 0.42 + 0.58 * luz;
                d.color = new Color(
                    Math.min(255, (int) (cara.base.getRed() * brillo)),
                    Math.min(255, (int) (cara.base.getGreen() * brillo)),
                    Math.min(255, (int) (cara.base.getBlue() * brillo)));
                lista.add(d);
            }
            lista.sort((u, v) -> Double.compare(u.prof, v.prof));
            g2.setStroke(new BasicStroke(0.6f));
            for (Dibujo d : lista) {
                g2.setColor(d.color);
                g2.fillPolygon(d.xs, d.ys, 4);
                g2.setColor(Tema.alfa(Color.BLACK, 45));
                g2.drawPolygon(d.xs, d.ys, 4);
            }

            
            if (thetaMax < 2 * Math.PI - 1e-3 && thetaMax > 0) {
                int n = 40;
                Path2D.Double reg = new Path2D.Double();
                for (int i = 0; i <= n; i++) {
                    double x = a + (b - a) * i / n;
                    double[] c = par(true, x);
                    double[] s = proy(punto(c[0], c[1], thetaMax), escala, cx, cy);
                    if (i == 0) {
                        reg.moveTo(s[0], s[1]);
                    } else {
                        reg.lineTo(s[0], s[1]);
                    }
                }
                for (int i = n; i >= 0; i--) {
                    double x = a + (b - a) * i / n;
                    double[] c = par(false, x);
                    double[] s = proy(punto(c[0], c[1], thetaMax), escala, cx, cy);
                    reg.lineTo(s[0], s[1]);
                }
                reg.closePath();
                g2.setColor(Tema.alfa(Tema.CURVA_G, 150));
                g2.fill(reg);
                g2.setColor(Tema.CURVA_G);
                g2.setStroke(new BasicStroke(1.6f));
                g2.draw(reg);
            }

       
            double[] ini = eje == 'X' ? new double[]{-ext, 0, 0} : new double[]{0, -ext, 0};
            double[] fin = eje == 'X' ? new double[]{ext, 0, 0} : new double[]{0, ext, 0};
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Tema.EJE);
            linea(g2, ini, fin, escala, cx, cy);
            g2.setFont(Tema.mono(Font.PLAIN, 11));
            double pasoEje = paso(ext);
            for (double t = pasoEje; t <= ext + 1e-9; t += pasoEje) {
                double[] pt = eje == 'X' ? new double[]{t, 0, 0} : new double[]{0, t, 0};
                double[] s = proy(pt, escala, cx, cy);
                g2.setColor(Tema.EJE);
                g2.fillOval((int) s[0] - 3, (int) s[1] - 3, 6, 6);
                g2.setColor(Tema.mezcla(Tema.EJE, Tema.TEXTO, 0.35));
                g2.drawString(num(t), (int) s[0] + 6, (int) s[1] + 14);
            }
            double[] punta = proy(fin, escala, cx, cy);
            g2.setFont(Tema.mono(Font.BOLD, 13));
            g2.setColor(Tema.EJE);
            g2.drawString(eje + " (eje de rotacion)", (int) punta[0] + 6, (int) punta[1] - 6);
            g2.setComposite(previo);
        }
    }
}
