package es.dam.hogwarts.excepciones;

/**
 * Indica que una anomalia ha alcanzado una situacion critica.
 * Cuando se lanza, los datos del incidente YA estan persistidos.
 */
public class EventoCriticoException extends Exception {

    private static final long serialVersionUID = 1L;

    public EventoCriticoException(String mensaje) {
        super(mensaje);
    }

    public EventoCriticoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
