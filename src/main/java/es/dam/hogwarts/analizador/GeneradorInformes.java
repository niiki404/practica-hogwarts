package es.dam.hogwarts.analizador;

import es.dam.hogwarts.modelo.RegistroSensor;
import es.dam.hogwarts.modelo.ResultadoAnalisis;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import es.dam.hogwarts.persistencia.PersistenciaXml;
import es.dam.hogwarts.persistencia.Rutas;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Genera las TRES salidas del analisis, cada una con un papel distinto:
 *   - informe.txt    : conclusion legible (NIO.2, Files.write)
 *   - anomalias.dat  : SOLO los registros anomalos usados como prueba
 *   - afectados.xml  : SOLO las zonas/elementos afectados o relevantes
 */
public class GeneradorInformes {

    private static final Logger log = LogManager.getLogger(GeneradorInformes.class);

    public void generar(ResultadoAnalisis resultado, Path dirSalida) throws IOException {
        Rutas.prepararDirectorios(dirSalida);

        // 1) Informe de texto
        List<String> lineas = new ArrayList<>();
        lineas.add("INFORME FORENSE - SISTEMA DE VIGILANCIA DE HOGWARTS");
        lineas.add("Fecha del analisis: " + LocalDateTime.now());
        lineas.add("");
        lineas.add("Incidente identificado: " + resultado.getTipoIncidente());
        lineas.add("Zona afectada: " + resultado.getZonaId() + " - " + resultado.getNombreZona());
        lineas.add("");
        lineas.add("Efectos detectados:");
        for (String efecto : resultado.getEfectos()) {
            lineas.add(" - " + efecto);
        }
        lineas.add("");
        lineas.add("Conclusion: " + resultado.getConclusion());
        lineas.add("");
        lineas.add("Registros anomalos utilizados como prueba: "
                + resultado.getAnomalias().size() + " (conservados en anomalias.dat)");
        Files.write(dirSalida.resolve(Rutas.FICHERO_INFORME), lineas, StandardCharsets.UTF_8);

        // 2) Binario con las pruebas: solo las lecturas anomalas
        Path ficheroAnomalias = dirSalida.resolve(Rutas.FICHERO_ANOMALIAS);
        Files.deleteIfExists(ficheroAnomalias);
        try (PersistenciaBinaria pb = new PersistenciaBinaria(ficheroAnomalias)) {
            for (RegistroSensor registro : resultado.getAnomalias()) {
                pb.insertar(registro);
            }
        }

        // 3) XML estructurado: solo las zonas afectadas
        new PersistenciaXml().guardar(dirSalida.resolve(Rutas.FICHERO_AFECTADOS),
                resultado.getZonasAfectadas());

        log.info("Tres salidas generadas en {}", dirSalida.toAbsolutePath());
    }
}