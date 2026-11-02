package es.dam.hogwarts.modelo;
/**
 * Registro de tamaño fijo (24 bytes) del fichero sensores.dat:
 * idSensor int(4) + timestamp long(8) + valor double(8) + nivelAlerta int(4).
 * nivelAlerta: 0 = normal, 1 = aviso, 2 = critico.
 * El registro con idSensor = 0 guarda el estado global del sistema.
 */
public class Alerta {
    private final String timestamp;
    private final String zonaId;
    private final String tipo;
    private final String detalle;

    public Alerta(String timestamp, String zonaId, String tipo, String detalle) {
        this.timestamp = timestamp;
        this.zonaId = zonaId;
        this.tipo = tipo;
        this.detalle = detalle;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getZonaId() {
        return zonaId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDetalle() {
        return detalle;
    }

    public String toLinea() {
        return timestamp + ";" + zonaId + ";" + tipo + ";" + detalle;
    }

    public static Alerta desdeLinea(String linea) {
        String[] campos = linea.split(";", 4);
        if (campos.length < 4) {
            throw new IllegalArgumentException("Linea de alerta mal formada: " + linea);
        }
        return new Alerta(campos[0], campos[1], campos[2], campos[3]);
    }

    @Override
    public String toString() {
        return toLinea();
    }
}
