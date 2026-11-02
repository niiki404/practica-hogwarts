package es.dam.hogwarts;

import es.dam.hogwarts.modelo.Alerta;
import es.dam.hogwarts.persistencia.PersistenciaTexto;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Escritura y recuperacion correcta de informacion textual. */
class PersistenciaTextoTest {

    @TempDir
    Path temp;

    @Test
    void escribeYLeeAlertas() throws IOException {
        Path fichero = temp.resolve("operaciones.txt");
        PersistenciaTexto persistencia = new PersistenciaTexto(fichero);

        persistencia.escribirAlerta(
                new Alerta("2026-11-03T18:00:00", "Z01", "ALERTA_METEOROLOGICA", "Viento fuerte"));
        persistencia.escribirAlerta(
                new Alerta("2026-11-03T18:01:00", "Z04", "OPERACION", "Ronda sin novedad"));

        List<Alerta> leidas = persistencia.leerAlertas();
        assertEquals(2, leidas.size());
        assertEquals("Z01", leidas.get(0).getZonaId());
        assertEquals("ALERTA_METEOROLOGICA", leidas.get(0).getTipo());
        assertEquals("Ronda sin novedad", leidas.get(1).getDetalle());
    }
}