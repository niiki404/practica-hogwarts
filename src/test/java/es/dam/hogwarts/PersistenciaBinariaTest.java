package es.dam.hogwarts;

import es.dam.hogwarts.modelo.RegistroSensor;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Alta, lectura directa y modificacion de registros con RandomAccessFile. */
class PersistenciaBinariaTest {

    @TempDir
    Path temp;

    @Test
    void altaLecturaDirectaYModificacion() throws IOException {
        try (PersistenciaBinaria pb = new PersistenciaBinaria(temp.resolve("sensores.dat"))) {
            pb.insertar(new RegistroSensor(0, 1000L, 0, 0));
            pb.insertar(new RegistroSensor(1, 1001L, 42.5, 0));
            pb.insertar(new RegistroSensor(2, 1002L, 10.0, 0));
            assertEquals(3, pb.numeroRegistros());

            // Lectura directa del segundo registro (sin recorrer el primero)
            RegistroSensor leido = pb.leer(1);
            assertEquals(1, leido.getIdSensor());
            assertEquals(42.5, leido.getValor(), 0.001);

            // Modificacion por acceso directo, sin reescribir el fichero
            pb.modificar(1, new RegistroSensor(1, 2000L, 99.9, 2));
            RegistroSensor modificado = pb.leer(1);
            assertEquals(99.9, modificado.getValor(), 0.001);
            assertEquals(2, modificado.getNivelAlerta());

            // Los demas registros no se han tocado
            assertEquals(0, pb.leer(0).getIdSensor());
            assertEquals(10.0, pb.leer(2).getValor(), 0.001);
        }
    }
}