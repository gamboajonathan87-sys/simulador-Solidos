import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class App {

    // Problema seleccionado (define cuales son f y g)
    static int problema = 0;

    static final String[] NOMBRES = {
        "Ejercicio del informe: f(x) = x + 3, g(x) = x^2 + 1",
        "Configuracion 1: f(x) = raiz(x), g(x) = 0",
        "Configuracion 2: f(x) = raiz(x), g(x) = x^2",
        "Configuracion 3: f(x) = 2x, g(x) = x^2"
    };

    
    static double f(double x) {
        switch (problema) {
            case 0: return x + 3;
            case 1: return Math.sqrt(x);
            case 2: return Math.sqrt(x);
            default: return 2 * x;
        }
    }

    static double g(double x) {
        switch (problema) {
            case 0: return x * x + 1;
            case 1: return 0;
            case 2: return x * x;
            default: return x * x;
        }
    }

    
    static double integrando(double x, String metodo) {
        if (metodo.equals("disco")) {
            return Math.PI * f(x) * f(x);
        } else if (metodo.equals("arandelas")) {
            return Math.PI * (f(x) * f(x) - g(x) * g(x));
        } else {
            return 2 * Math.PI * x * (f(x) - g(x));
        }
    }

    
    static double simpson(double a, double b, int n, String metodo) {
        double h = (b - a) / n;
        double suma = integrando(a, metodo) + integrando(b, metodo);

        for (int i = 1; i < n; i++) {
            double x = a + i * h;
            if (i % 2 == 0) {
                suma += 2 * integrando(x, metodo);
            } else {
                suma += 4 * integrando(x, metodo);
            }
        }
        return suma * h / 3;
    }

   
    static String textoIntegral(String metodo) {
        if (metodo.equals("disco")) {
            return "V = pi * integral de [f(x)]^2 dx";
        } else if (metodo.equals("arandelas")) {
            return "V = pi * integral de ([f(x)]^2 - [g(x)]^2) dx";
        } else {
            return "V = 2*pi * integral de x*(f(x) - g(x)) dx";
        }
    }

    // Volumen acumulado desde a hasta x
    static double volumenAcumulado(double a, double x, String metodo) {
        if (x <= a) {
            return 0;
        }
        return simpson(a, x, 2000, metodo);
    }

    // Exporta los datos a un archivo CSV
    static void exportarCSV(String archivo, double a, double b, String metodo, int puntos) throws IOException {
        try (PrintWriter pw = new PrintWriter(archivo)) {
            pw.println("x,f(x),g(x),Vacum(x)");
            for (int i = 0; i <= puntos; i++) {
                double x = a + (b - a) * i / puntos;
                pw.println(String.format(Locale.US, "%.6f,%.6f,%.6f,%.6f",
                        x, f(x), g(x), volumenAcumulado(a, x, metodo)));
            }
        }
    }

    // Valida los datos y devuelve la lista de errores (vacia si todo esta bien)
    static ArrayList<String> validar(double a, double b, String metodo, char eje) {
        ArrayList<String> errores = new ArrayList<>();

        if (a >= b) {
            errores.add("Se debe cumplir a < b.");
        }

        boolean metodoValido = metodo.equals("disco") || metodo.equals("arandelas") || metodo.equals("capas");
        boolean comboOk = (metodo.equals("disco") && eje == 'X')
                || (metodo.equals("arandelas") && eje == 'X')
                || (metodo.equals("capas") && eje == 'Y');

        if (!metodoValido) {
            errores.add("Metodo no reconocido. Usa: disco, arandelas o capas.");
        } else if (!comboOk) {
            errores.add("Combinacion metodo-eje no permitida (disco y arandelas con eje X, capas con eje Y).");
        }

        if (a < b && metodoValido) {
            int n = 2000;
            double tol = 1e-9;
            boolean definida = true;
            boolean fMayor = true;
            boolean gNoNegativa = true;
            boolean gCero = true;

            for (int i = 0; i <= n; i++) {
                double x = a + (b - a) * i / n;
                double fx = f(x);
                double gx = g(x);

                if (Double.isNaN(fx) || Double.isNaN(gx) || Double.isInfinite(fx) || Double.isInfinite(gx)) {
                    if (definida) {
                        errores.add("f(x) o g(x) no esta definida en x = " + x);
                        definida = false;
                    }
                    continue;
                }

                if (fx - gx < -tol && fMayor) {
                    errores.add("f(x) < g(x) en x = " + x);
                    fMayor = false;
                }

                if ((metodo.equals("disco") || metodo.equals("arandelas")) && gx < -tol && gNoNegativa) {
                    errores.add("g(x) < 0 en x = " + x);
                    gNoNegativa = false;
                }

                if (metodo.equals("disco") && Math.abs(gx) > tol && gCero) {
                    errores.add("El metodo del disco requiere g(x) = 0. Para dos curvas usa arandelas.");
                    gCero = false;
                }
            }

            if (metodo.equals("capas") && a < 0) {
                errores.add("Para capas cilindricas se requiere 0 <= a.");
            }
        }

        return errores;
    }

    
    static double leerNumero(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        String texto = sc.nextLine().trim().replace(',', '.');
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Simulador de solidos de revolucion ===");

        // 1. Elegir el problema
        System.out.println("Elige el problema:");
        for (int i = 0; i < NOMBRES.length; i++) {
            System.out.println("  " + i + ") " + NOMBRES[i]);
        }
        double opcion = leerNumero(sc, "Opcion: ");
        if (Double.isNaN(opcion) || opcion < 0 || opcion >= NOMBRES.length) {
            System.out.println("ERROR: opcion no valida.");
            return;
        }
        problema = (int) opcion;

        // 2. Pedir los datos
        double a = leerNumero(sc, "Limite a: ");
        double b = leerNumero(sc, "Limite b: ");
        if (Double.isNaN(a) || Double.isNaN(b)) {
            System.out.println("ERROR: a y b deben ser numeros.");
            return;
        }

        System.out.print("Metodo (disco / arandelas / capas): ");
        String metodo = sc.nextLine().trim().toLowerCase();

        System.out.print("Eje de rotacion (X / Y): ");
        String ejeTxt = sc.nextLine().trim().toUpperCase();
        char eje = ejeTxt.isEmpty() ? ' ' : ejeTxt.charAt(0);

        // 3. Validar
        ArrayList<String> errores = validar(a, b, metodo, eje);
        if (!errores.isEmpty()) {
            for (String error : errores) {
                System.out.println("ERROR: " + error);
            }
            System.out.println("Corrige los datos antes de calcular.");
            return;
        }

        // 4. Calcular
        System.out.println("Datos validos.");
        System.out.println("Problema: " + NOMBRES[problema]);
        System.out.println("Metodo: " + metodo + " | Eje: " + eje);
        System.out.println("Integral: " + textoIntegral(metodo));
        System.out.println("Intervalo: [" + a + ", " + b + "]");

        double volumen = simpson(a, b, 2000, metodo);
        System.out.println("Volumen = " + String.format(Locale.US, "%.6f", volumen) + " unidades cubicas");

        // 5. Comparar con el resultado manual
        double manual = leerNumero(sc, "Ingresa tu resultado manual (unidades cubicas): ");
        if (Double.isNaN(manual)) {
            System.out.println("ERROR: el resultado manual debe ser un numero.");
        } else {
            double diferencia = Math.abs(manual - volumen) / Math.abs(volumen) * 100;
            System.out.println("Resultado manual:    " + String.format(Locale.US, "%.6f", manual));
            System.out.println("Resultado simulador: " + String.format(Locale.US, "%.6f", volumen));
            System.out.println("Diferencia porcentual: " + String.format(Locale.US, "%.4f", diferencia) + " %");

            if (diferencia < 1.0) {
                System.out.println("CONCORDANCIA: los resultados coinciden.");
            } else {
                System.out.println("NO HAY CONCORDANCIA: revisa tu procedimiento manual.");
            }
        }

        // 6. Volumen acumulado y exportacion
        System.out.println("Vacum(b) = " + String.format(Locale.US, "%.6f", volumenAcumulado(a, b, metodo)));
        try {
            exportarCSV("resultados.csv", a, b, metodo, 100);
            System.out.println("Datos exportados a resultados.csv");
        } catch (IOException e) {
            System.out.println("ERROR al exportar: " + e.getMessage());
        }
    }
}