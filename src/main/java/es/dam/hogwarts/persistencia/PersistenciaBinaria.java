package es.dam.hogwarts.persistencia;

import es.dam.hogwarts.modelo.RegistroSensor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaBinaria implements AutoCloseable {
    /** int(4) + long(8) + double(8) + int(4) = 24 bytes */
    public static final int TAMANO_REGISTRO = 24;

    private final RandomAccessFile raf;

    public PersistenciaBinaria(Path fichero) throws IOException {
        Rutas.prepararDirectorios(fichero.getParent());
        if (Files.notExists(fichero)) {
            Files.createFile(fichero);
        }
        this.raf = new RandomAccessFile(fichero.toFile(), "rw");
    }

    /** ALTA: escribe un registro nuevo al final del fichero. */
    public void insertar(RegistroSensor registro) throws IOException {
        raf.seek(raf.length());
        escribirRegistro(registro);
    }

    /** CONSULTA DIRECTA: lee el registro n-esimo sin leer los anteriores. */
    public RegistroSensor leer(int indice) throws IOException {
        raf.seek((long) indice * TAMANO_REGISTRO);
        return new RegistroSensor(raf.readInt(), raf.readLong(), raf.readDouble(), raf.readInt());
    }

    /** MODIFICACION DIRECTA: sobrescribe solo ese registro. */
    public void modificar(int indice, RegistroSensor registro) throws IOException {
        raf.seek((long) indice * TAMANO_REGISTRO);
        escribirRegistro(registro);
    }

    public int numeroRegistros() throws IOException {
        return (int) (raf.length() / TAMANO_REGISTRO);
    }

    /** Recorrido completo (lo usa el analizador). */
    public List<RegistroSensor> leerTodos() throws IOException {
        List<RegistroSensor> registros = new ArrayList<>();
        for (int i = 0; i < numeroRegistros(); i++) {
            registros.add(leer(i));
        }
        return registros;
    }

    private void escribirRegistro(RegistroSensor r) throws IOException {
        raf.writeInt(r.getIdSensor());
        raf.writeLong(r.getTimestamp());
        raf.writeDouble(r.getValor());
        raf.writeInt(r.getNivelAlerta());
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
