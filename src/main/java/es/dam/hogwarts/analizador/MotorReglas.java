package es.dam.hogwarts.analizador;

import es.dam.hogwarts.modelo.Alerta;
import es.dam.hogwarts.modelo.CatalogoSensores;
import es.dam.hogwarts.modelo.EstadoElemento;
import es.dam.hogwarts.modelo.InfoSensor;
import es.dam.hogwarts.modelo.RegistroSensor;
import es.dam.hogwarts.modelo.ResultadoAnalisis;
import es.dam.hogwarts.modelo.TipoIncidente;
import es.dam.hogwarts.modelo.Zona;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Motor de reglas del analizador forense.
 * Cada regla cruza las TRES fuentes de datos: un tipo de alerta del TXT,
 * lecturas anomalas de un sensor del BIN y un estado concreto de un
 * elemento del XML. Solo si las tres encajan se declara el incidente.
 */
public class MotorReglas {

    /** Estados que se consideran "normales"; cualquier otro marca una zona afectada. */
    private static final Set<EstadoElemento> ESTADOS_NORMALES = EnumSet.of(
            EstadoElemento.OPERATIVO, EstadoElemento.CERRADA,
            EstadoElemento.SELLADA, EstadoElemento.DORMIDO);

    private record Regla(TipoIncidente tipo, String tipoAlerta, int idSensor,
                         String tipoElemento, EstadoElemento estadoEsperado, String zonaId,
                         List<String> efectos, String conclusion) {
    }

    private static final List<Regla> REGLAS = List.of(
            new Regla(TipoIncidente.TORMENTA_MAGICA, "ALERTA_METEOROLOGICA", 1,
                    "ANTORCHAS", EstadoElemento.APAGADO, "Z01",
                    List.of("Antorchas de la explanada apagadas",
                            "Viento muy por encima del rango habitual"),
                    "Una tormenta magica ha azotado los exteriores; revisar el alumbrado exterior."),
            new Regla(TipoIncidente.DEMENTORES_PERIMETRO, "ALERTA_TERMICA", 2,
                    "PERIMETRO", EstadoElemento.COMPROMETIDO, "Z01",
                    List.of("Temperatura exterior anormalmente baja",
                            "Perimetro magico comprometido"),
                    "Presencia de dementores en los limites del castillo; reforzar el perimetro."),
            new Regla(TipoIncidente.FUGA_CRIATURA, "AVISO_JAULA", 3,
                    "CRIATURA", EstadoElemento.FUGADA, "Z02",
                    List.of("Jaula abierta", "Criatura fugada", "Presion de la jaula a cero"),
                    "Buckbeak se ha escapado de su jaula; activar el protocolo de busqueda."),
            new Regla(TipoIncidente.FALLO_CONTENCION_CAMARA, "AVISO_RUIDOS", 4,
                    "SELLO", EstadoElemento.DEBILITADO, "Z03",
                    List.of("Sello de la Camara debilitado", "Vibraciones anomalas"),
                    "El sello de contencion de la Camara de los Secretos esta fallando."),
            new Regla(TipoIncidente.INTRUSO_NO_AUTORIZADO, "AVISO_PRESENCIA", 5,
                    "PUERTA_RETRATO", EstadoElemento.FORZADA, "Z04",
                    List.of("Puerta del retrato forzada", "Presencia detectada fuera de horario"),
                    "Hay un intruso en la Torre de Gryffindor; revisar el Mapa del Merodeador."),
            new Regla(TipoIncidente.POLTERGEIST, "AVISO_OBJETOS", 6,
                    "MOBILIARIO", EstadoElemento.CAOTICO, "Z05",
                    List.of("Mobiliario en estado caotico", "Nivel magico residual erratico"),
                    "Peeves esta campando a sus anchas en la Sala de los Menesteres."));

    public ResultadoAnalisis analizar(List<Alerta> alertas,
                                      List<RegistroSensor> registros,
                                      List<Zona> zonas) {
        // 1. Anomalias del BIN segun los rangos del catalogo (no segun el simulador)
        List<RegistroSensor> anomalias = new ArrayList<>();
        for (RegistroSensor registro : registros) {
            InfoSensor info = CatalogoSensores.porId(registro.getIdSensor());
            if (info != null && info.esAnomalo(registro.getValor())) {
                anomalias.add(registro);
            }
        }

        // 2. Zonas del XML con algun elemento en estado no normal
        List<Zona> afectadas = zonas.stream()
                .filter(z -> z.getElementos().stream()
                        .anyMatch(e -> !ESTADOS_NORMALES.contains(e.getEstado())))
                .toList();

        // 3. Reglas: solo si coinciden las tres fuentes
        for (Regla regla : REGLAS) {
            if (cumple(regla, alertas, anomalias, zonas)) {
                Zona zona = zonas.stream()
                        .filter(z -> z.getId().equals(regla.zonaId()))
                        .findFirst()
                        .orElse(null);
                return new ResultadoAnalisis(regla.tipo(), regla.zonaId(),
                        zona != null ? zona.getNombre() : regla.zonaId(),
                        regla.efectos(), regla.conclusion(), anomalias, afectadas);
            }
        }

        return new ResultadoAnalisis(TipoIncidente.INDETERMINADO, "-", "-", List.of(),
                "La evidencia disponible no encaja con ningun incidente conocido.",
                anomalias, afectadas);
    }

    private boolean cumple(Regla regla, List<Alerta> alertas,
                           List<RegistroSensor> anomalias, List<Zona> zonas) {
        boolean txt = alertas.stream().anyMatch(a ->
                a.getTipo().equals(regla.tipoAlerta()) && a.getZonaId().equals(regla.zonaId()));
        boolean bin = anomalias.stream().anyMatch(r -> r.getIdSensor() == regla.idSensor());
        boolean xml = zonas.stream().anyMatch(z ->
                z.getId().equals(regla.zonaId())
                        && z.buscarElemento(regla.tipoElemento())
                        .map(e -> e.getEstado() == regla.estadoEsperado())
                        .orElse(false));
        return txt && bin && xml;
    }
}