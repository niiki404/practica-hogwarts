package es.dam.hogwarts.simulador;

import es.dam.hogwarts.modelo.Alerta;
import es.dam.hogwarts.modelo.EstadoElemento;
import es.dam.hogwarts.modelo.RegistroSensor;
import es.dam.hogwarts.modelo.TipoIncidente;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import es.dam.hogwarts.persistencia.PersistenciaTexto;
import es.dam.hogwarts.persistencia.PersistenciaXml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;



/**
 * Produce las consecuencias de cada incidente y las persiste con los
 * MISMOS mecanismos que la operativa normal:
 * TXT (sintoma) + BIN (mediciones anomalas) + XML (estados afectados).
 */

public class GestorIncidentes {
    private static final Logger log = LogManager.getLogger(GestorIncidentes.class);

    private final PersistenciaTexto texto;
    private final PersistenciaBinaria binaria;
    private final PersistenciaXml xml;
    private final Path ficheroCastillo;

    public GestorIncidentes(PersistenciaTexto texto, PersistenciaBinaria binaria,
                            PersistenciaXml xml, Path ficheroCastillo) {
        this.texto = texto;
        this.binaria = binaria;
        this.xml = xml;
        this.ficheroCastillo = ficheroCastillo;
    }

    public void provocar(TipoIncidente tipo) throws IOException {
        long ahora = System.currentTimeMillis();
        String ts = LocalDateTime.now().toString();

        switch (tipo) {
            case TORMENTA_MAGICA -> {
                alerta(ts, "Z01", "ALERTA_METEOROLOGICA",
                        "Viento huracanado y lluvia torrencial sobre la explanada");
                medir(1, 187.55, ahora);
                medir(1, 203.21, ahora + 1);
                xml.cambiarEstado(ficheroCastillo, "Z01", "ANTORCHAS", EstadoElemento.APAGADO);
            }
            case DEMENTORES_PERIMETRO -> {
                alerta(ts, "Z01", "ALERTA_TERMICA",
                        "Descenso brusco y sobrenatural de la temperatura en los limites");
                medir(2, -15.30, ahora);
                medir(2, -12.85, ahora + 1);
                xml.cambiarEstado(ficheroCastillo, "Z01", "PERIMETRO", EstadoElemento.COMPROMETIDO);
            }
            case FUGA_CRIATURA -> {
                alerta(ts, "Z02", "AVISO_JAULA",
                        "La jaula de Buckbeak aparece abierta y vacia");
                medir(3, 0.0, ahora);
                medir(3, 0.0, ahora + 1);
                xml.cambiarEstado(ficheroCastillo, "Z02", "JAULA", EstadoElemento.ABIERTA);
                xml.cambiarEstado(ficheroCastillo, "Z02", "CRIATURA", EstadoElemento.FUGADA);
            }
            case FALLO_CONTENCION_CAMARA -> {
                alerta(ts, "Z03", "AVISO_RUIDOS",
                        "Golpes y siseos tras la entrada de la Camara de los Secretos");
                medir(4, 95.02, ahora);
                medir(4, 88.47, ahora + 1);
                xml.cambiarEstado(ficheroCastillo, "Z03", "SELLO", EstadoElemento.DEBILITADO);
            }
            case INTRUSO_NO_AUTORIZADO -> {
                alerta(ts, "Z04", "AVISO_PRESENCIA",
                        "Movimiento detectado en la torre fuera del horario permitido");
                medir(5, 4.0, ahora);
                medir(5, 3.0, ahora + 1);
                xml.cambiarEstado(ficheroCastillo, "Z04", "PUERTA_RETRATO", EstadoElemento.FORZADA);
            }
            case POLTERGEIST -> {
                alerta(ts, "Z05", "AVISO_OBJETOS",
                        "El mobiliario de la Sala de los Menesteres se mueve solo");
                medir(6, 450.30, ahora);
                medir(6, 382.10, ahora + 1);
                xml.cambiarEstado(ficheroCastillo, "Z05", "MOBILIARIO", EstadoElemento.CAOTICO);
            }
            default -> throw new IllegalArgumentException("Incidente no provocable: " + tipo);
        }

        // Nivel global critico: MODIFICACION por acceso directo del registro 0
        binaria.modificar(0, new RegistroSensor(0, ahora, 0, 2));
        log.warn("Evidencias del incidente persistidas en TXT, BIN y XML");
    }

    private void alerta(String ts, String zonaId, String tipoAlerta, String detalle) throws IOException {
        texto.escribirAlerta(new Alerta(ts, zonaId, tipoAlerta, detalle));
    }

    private void medir(int idSensor, double valor, long ts) throws IOException {
        binaria.insertar(new RegistroSensor(idSensor, ts, valor, 2));
    }
}
