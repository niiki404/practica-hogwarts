package es.dam.hogwarts;

import es.dam.hogwarts.analizador.AnalizadorForense;
import es.dam.hogwarts.excepciones.EventoCriticoException;
import es.dam.hogwarts.modelo.ResultadoAnalisis;
import es.dam.hogwarts.modelo.TipoIncidente;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import es.dam.hogwarts.simulador.GestorOperativa;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integracion: recorre el flujo completo
 * datos -> incidente -> persistencia -> analisis -> salidas.
 */
class IntegracionTest {

    @TempDir
    Path temp;

    @Test
    void flujoCompletoSimuladorAnalizador() throws IOException {
        Path datos = temp.resolve("datos");
        Path salida = temp.resolve("salida");

        // 1. Simulacion con incidente controlado
        try (GestorOperativa operativa =
                     new GestorOperativa(datos, TipoIncidente.FUGA_CRIATURA, 10, 3L)) {
            operativa.preparar();
            try {
                operativa.ejecutar();
            } catch (EventoCriticoException esperada) {
                // Esperada: la fuga de Buckbeak es critica
            }
        }

        // Los tres ficheros de datos se han generado
        assertTrue(Files.exists(datos.resolve("operaciones.txt")));
        assertTrue(Files.exists(datos.resolve("sensores.dat")));
        assertTrue(Files.exists(datos.resolve("castillo.xml")));

        // 2. Analisis forense independiente
        ResultadoAnalisis resultado = new AnalizadorForense(datos, salida).analizar();
        assertEquals(TipoIncidente.FUGA_CRIATURA, resultado.getTipoIncidente());

        // 3. Las tres salidas existen y contienen lo esperado
        assertTrue(Files.exists(salida.resolve("informe.txt")));
        assertTrue(Files.exists(salida.resolve("anomalias.dat")));
        assertTrue(Files.exists(salida.resolve("afectados.xml")));

        String informe = Files.readString(salida.resolve("informe.txt"));
        assertTrue(informe.contains("FUGA_CRIATURA"));

        try (PersistenciaBinaria pb =
                     new PersistenciaBinaria(salida.resolve("anomalias.dat"))) {
            assertTrue(pb.numeroRegistros() > 0);
        }

        String afectados = Files.readString(salida.resolve("afectados.xml"));
        assertTrue(afectados.contains("Z02"));
        assertTrue(afectados.contains("FUGADA"));
    }
}