package es.dam.hogwarts.persistencia;

import es.dam.hogwarts.modelo.Alerta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia del historico de operaciones (TXT).
 * Escritura: Java I/O clasico (BufferedWriter en modo append).
 * Lectura: puente InputStreamReader sobre Files.newInputStream (NIO.2), con UTF-8.
 */
public class PersistenciaTexto {
    private final Path fichero;

    public PersistenciaTexto(Path fichero) {
        this.fichero = fichero;
    }

    /** Añade una linea al final del historico. */
    public void escribirAlerta(Alerta alerta) throws IOException {
        Rutas.prepararDirectorios(fichero.getParent());
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fichero.toFile(), true), StandardCharsets.UTF_8))) {
            bw.write(alerta.toLinea());
            bw.newLine();
        }
    }

    /** Lee todo el historico. Fin de fichero: readLine() devuelve null. */
    public List<Alerta> leerAlertas() throws IOException {
        List<Alerta> alertas = new ArrayList<>();
        if (Files.notExists(fichero)) {
            return alertas;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Files.newInputStream(fichero), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.isBlank()) {
                    alertas.add(Alerta.desdeLinea(linea));
                }
            }
        }
        return alertas;
    }
}
