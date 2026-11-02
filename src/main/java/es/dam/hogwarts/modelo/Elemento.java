package es.dam.hogwarts.modelo;
/** Elemento estructural de una zona (puerta, sello, jaula...) con su estado. */
public class Elemento {
    private final String tipo;
    private EstadoElemento estado;

    public Elemento(String tipo, EstadoElemento estado) {
        this.tipo = tipo;
        this.estado = estado;
    }

    public String getTipo() {
        return tipo;
    }

    public EstadoElemento getEstado() {
        return estado;
    }

    public void setEstado(EstadoElemento estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return tipo + "=" + estado;
    }
}
