package es.dam.hogwarts;

import es.dam.hogwarts.excepciones.EventoCriticoException;
import es.dam.hogwarts.modelo.Alerta;
import es.dam.hogwarts.modelo.RegistroSensor;
import es.dam.hogwarts.modelo.TipoIncidente;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import es.dam.hogwarts.persistencia.PersistenciaTexto;
import es.dam.hogwarts.simulador.GestorOperativa;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Provocacion controlada de una situacion critica: se comprueba la
 * excepcion esperada Y que la evidencia ya estaba persistida antes
 * de entrar en el catch.
 */
class ExcepcionTest {

    @TempDir
    Path temp;

    @Test
    void incidenteCriticoLanzaExcepcionTrasPersistir() throws IOException {
        try (GestorOperativa operativa =
                     new GestorOperativa(temp, TipoIncidente.DEMENTORES_PERIMETRO, 6, 42L)) {
            operativa.preparar();
            assertThrows(EventoCriticoException.class, operativa::ejecutar);
        }

        // La evidencia forense ya existia en los ficheros al capturar la excepcion
        List<Alerta> alertas =
                new PersistenciaTexto(temp.resolve("operaciones.txt")).leerAlertas();
        assertTrue(alertas.stream().anyMatch(a -> a.getTipo().equals("ALERTA_TERMICA")));

        try (PersistenciaBinaria pb = new PersistenciaBinaria(temp.resolve("sensores.dat"))) {
            List<RegistroSensor> registros = pb.leerTodos();
            assertTrue(registros.stream().anyMatch(r -> r.getNivelAlerta() == 2));
            // El registro global (posicion 0) quedo marcado como critico
            assertTrue(pb.leer(0).getNivelAlerta() >= 2);
        }
    }
}