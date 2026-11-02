package es.dam.hogwarts;

import es.dam.hogwarts.modelo.EstadoElemento;
import es.dam.hogwarts.modelo.Zona;
import es.dam.hogwarts.persistencia.PersistenciaXml;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Procesamiento correcto del XML con DOM: crear, cargar y modificar estados. */
class PersistenciaXmlTest {

    @TempDir
    Path temp;

    @Test
    void crearCargarYModificar() throws IOException {
        PersistenciaXml xml = new PersistenciaXml();
        Path fichero = temp.resolve("castillo.xml");

        xml.crearCastilloInicial(fichero);
        List<Zona> zonas = xml.cargar(fichero);
        assertEquals(5, zonas.size());
        assertEquals("Z01", zonas.get(0).getId());

        xml.cambiarEstado(fichero, "Z03", "SELLO", EstadoElemento.DEBILITADO);

        List<Zona> trasCambio = xml.cargar(fichero);
        Zona z03 = trasCambio.stream()
                .filter(z -> z.getId().equals("Z03"))
                .findFirst()
                .orElseThrow();
        assertEquals(EstadoElemento.DEBILITADO,
                z03.buscarElemento("SELLO").orElseThrow().getEstado());
    }
}