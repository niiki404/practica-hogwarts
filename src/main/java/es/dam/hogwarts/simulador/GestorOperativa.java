package es.dam.hogwarts.simulador;

import es.dam.hogwarts.excepciones.EventoCriticoException;
import es.dam.hogwarts.modelo.*;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import es.dam.hogwarts.persistencia.PersistenciaTexto;
import es.dam.hogwarts.persistencia.PersistenciaXml;
import es.dam.hogwarts.persistencia.Rutas;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Random;


/**
 * Operativa habitual del castillo, dividida en turnos.
 * En cada turno: historico (TXT) + mediciones normales (BIN) +
 * actualizacion del registro global por acceso directo.
 */
public class GestorOperativa implements AutoCloseable{
    private static final Logger log = LogManager.getLogger(GestorOperativa.class);

    private static final String[] EVENTOS_NORMALES = {
            "Ronda de prefetos sin novedad",
            "Apertura de puerta por un profesor",
            "Revision rutinaria de sensores",
            "Paso de alumnos entre clases"
    };

    private final Path dirDatos;
    private final TipoIncidente incidente;
    private final int turnos;
    private final Random random;

    private PersistenciaBinaria binaria;
    private PersistenciaTexto texto;
    private final PersistenciaXml xml = new PersistenciaXml();

    public GestorOperativa(Path dirDatos, TipoIncidente incidente, int turnos, Long semilla) {
        this.dirDatos = dirDatos;
        this.incidente = incidente;
        this.turnos = turnos;
        this.random = (semilla != null) ? new Random(semilla) : new Random();
    }

    /** Deja los tres ficheros listos en su estado inicial de la noche. */
    public void preparar() throws IOException {
        Rutas.prepararDirectorios(dirDatos);
        Files.deleteIfExists(dirDatos.resolve(Rutas.FICHERO_OPERACIONES));
        Files.deleteIfExists(dirDatos.resolve(Rutas.FICHERO_SENSORES));
        xml.crearCastilloInicial(dirDatos.resolve(Rutas.FICHERO_CASTILLO));

        texto = new PersistenciaTexto(dirDatos.resolve(Rutas.FICHERO_OPERACIONES));
        binaria = new PersistenciaBinaria(dirDatos.resolve(Rutas.FICHERO_SENSORES));

        // Registro 0: estado global del sistema. Se MODIFICARA por acceso directo.
        binaria.insertar(new RegistroSensor(0, System.currentTimeMillis(), 0, 0));
        log.info("Simulador preparado. Ficheros de datos en {}", dirDatos.toAbsolutePath());
    }

    public void ejecutar() throws IOException, EventoCriticoException {
        log.info("Comienza el turno de vigilancia ({} turnos)", turnos);
        int turnoIncidente = turnos / 2 + 1;
        GestorIncidentes gestorIncidentes =
                new GestorIncidentes(texto, binaria, xml, dirDatos.resolve(Rutas.FICHERO_CASTILLO));

        for (int turno = 1; turno <= turnos; turno++) {
            generarOperativaNormal(turno);

            if (turno == turnoIncidente) {
                gestorIncidentes.provocar(incidente);

                // Con la evidencia YA persistida, se evalua la criticidad
                log.warn("Anomalia detectada, evaluando criticidad...");
                RegistroSensor estadoGlobal = binaria.leer(0);
                if (estadoGlobal.getNivelAlerta() >= 2) {
                    throw new EventoCriticoException(
                            "Situacion critica confirmada en el turno " + turno
                                    + " (nivel de alerta " + estadoGlobal.getNivelAlerta() + ")");
                }
            }
        }
        log.info("Turno de vigilancia finalizado sin mas incidencias");
    }

    /** Un turno normal: TXT + BIN + update del registro 0 por acceso directo. */
    private void generarOperativaNormal(int turno) throws IOException {
        long ahora = System.currentTimeMillis();
        String zonaId = "Z0" + ((turno % 5) + 1);

        texto.escribirAlerta(new Alerta(LocalDateTime.now().toString(), zonaId, "OPERACION",
                EVENTOS_NORMALES[random.nextInt(EVENTOS_NORMALES.length)]));

        for (InfoSensor sensor : CatalogoSensores.todos()) {
            double valor = sensor.getMin()
                    + random.nextDouble() * (sensor.getMax() - sensor.getMin()) * 0.5;
            binaria.insertar(new RegistroSensor(sensor.getIdSensor(), ahora, redondear(valor), 0));
        }

        binaria.modificar(0, new RegistroSensor(0, ahora, turno, 0));
        log.debug("Turno {} completado", turno);
    }

    static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public void close() throws IOException {
        if (binaria != null) {
            binaria.close();
        }
    }

}
