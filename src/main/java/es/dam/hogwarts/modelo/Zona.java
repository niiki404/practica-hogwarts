package es.dam.hogwarts.modelo;

import java.util.List;
import java.util.Optional;

/** Zona del castillo: su id relaciona los tres ficheros (TXT, BIN y XML). */
public class Zona {
    private final String id;
    private final String nombre;
    private final List<Elemento> elementos;
    private final List<Integer> sensores;

    public Zona(String id, String nombre, List<Elemento> elementos, List<Integer> sensores) {
        this.id = id;
        this.nombre = nombre;
        this.elementos = elementos;
        this.sensores = sensores;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Elemento> getElementos() {
        return elementos;
    }

    public List<Integer> getSensores() {
        return sensores;
    }

    public Optional<Elemento> buscarElemento(String tipo) {
        return elementos.stream().filter(e -> e.getTipo().equals(tipo)).findFirst();
    }

    @Override
    public String toString() {
        return id + " (" + nombre + ") " + elementos;
    }
}
