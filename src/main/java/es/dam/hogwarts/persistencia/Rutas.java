package es.dam.hogwarts.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Nombres de fichero y utilidades de directorios.
 * Rutas SIEMPRE relativas para que el proyecto funcione en cualquier equipo.
 */
public class Rutas {
    public static final String FICHERO_OPERACIONES = "operaciones.txt";
    public static final String FICHERO_SENSORES = "sensores.dat";
    public static final String FICHERO_CASTILLO = "castillo.xml";
    public static final String FICHERO_INFORME = "informe.txt";
    public static final String FICHERO_ANOMALIAS = "anomalias.dat";
    public static final String FICHERO_AFECTADOS = "afectados.xml";

    private Rutas() {
    }

    /** Crea los directorios necesarios antes de usarlos (NIO.2). */
    public static void prepararDirectorios(Path... directorios) throws IOException {
        for (Path directorio : directorios) {
            if (directorio != null) {
                Files.createDirectories(directorio);
            }
        }
    }
}
