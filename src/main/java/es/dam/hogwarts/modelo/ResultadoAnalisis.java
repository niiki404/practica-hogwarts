package es.dam.hogwarts.modelo;

import java.util.List;

/**
 * Resultado del analisis forense: que incidente encaja con la evidencia,
 * en que zona, que efectos se han detectado, que conclusion se extrae,
 * y los datos concretos que lo justifican (para generar las salidas).
 */
public class ResultadoAnalisis {

    private final TipoIncidente tipoIncidente;
    private final String zonaId;
    private final String nombreZona;
    private final List<String> efectos;
    private final String conclusion;
    private final List<RegistroSensor> anomalias;
    private final List<Zona> zonasAfectadas;

    public ResultadoAnalisis(TipoIncidente tipoIncidente, String zonaId, String nombreZona,
                             List<String> efectos, String conclusion,
                             List<RegistroSensor> anomalias, List<Zona> zonasAfectadas) {
        this.tipoIncidente = tipoIncidente;
        this.zonaId = zonaId;
        this.nombreZona = nombreZona;
        this.efectos = efectos;
        this.conclusion = conclusion;
        this.anomalias = anomalias;
        this.zonasAfectadas = zonasAfectadas;
    }

    public TipoIncidente getTipoIncidente() {
        return tipoIncidente;
    }

    public String getZonaId() {
        return zonaId;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public List<String> getEfectos() {
        return efectos;
    }

    public String getConclusion() {
        return conclusion;
    }

    public List<RegistroSensor> getAnomalias() {
        return anomalias;
    }

    public List<Zona> getZonasAfectadas() {
        return zonasAfectadas;
    }
}