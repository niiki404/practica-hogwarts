package es.dam.hogwarts.modelo;
/** Metadatos de un sensor: que mide y cual es su rango habitual. */
public class InfoSensor {
    private final int idSensor;
    private final String zonaId;
    private final String descripcion;
    private final double min;
    private final double max;

    public InfoSensor(int idSensor, String zonaId, String descripcion, double min, double max) {
        this.idSensor = idSensor;
        this.zonaId = zonaId;
        this.descripcion = descripcion;
        this.min = min;
        this.max = max;
    }

    public int getIdSensor() {
        return idSensor;
    }

    public String getZonaId() {
        return zonaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean esAnomalo(double valor) {
        return valor < min || valor > max;
    }

}
