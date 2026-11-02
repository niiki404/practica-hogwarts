package es.dam.hogwarts.modelo;
/**
 * Registro de tamaño fijo (24 bytes) del fichero sensores.dat:
 * idSensor int(4) + timestamp long(8) + valor double(8) + nivelAlerta int(4).
 * nivelAlerta: 0 = normal, 1 = aviso, 2 = critico.
 * El registro con idSensor = 0 guarda el estado global del sistema.
 */
public class RegistroSensor {
    private final int idSensor;
    private final long timestamp;
    private final double valor;
    private final int nivelAlerta;

    public RegistroSensor(int idSensor, long timestamp, double valor, int nivelAlerta) {
        this.idSensor = idSensor;
        this.timestamp = timestamp;
        this.valor = valor;
        this.nivelAlerta = nivelAlerta;
    }

    public int getIdSensor() {
        return idSensor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValor() {
        return valor;
    }

    public int getNivelAlerta() {
        return nivelAlerta;
    }

    @Override
    public String toString() {
        return "sensor=" + idSensor + " ts=" + timestamp + " valor=" + valor + " nivel=" + nivelAlerta;
    }
}
