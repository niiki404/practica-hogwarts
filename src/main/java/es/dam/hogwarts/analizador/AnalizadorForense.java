package es.dam.hogwarts.analizador;

import es.dam.hogwarts.modelo.Alerta;
import es.dam.hogwarts.modelo.RegistroSensor;
import es.dam.hogwarts.modelo.ResultadoAnalisis;
import es.dam.hogwarts.modelo.Zona;
import es.dam.hogwarts.persistencia.PersistenciaBinaria;
import es.dam.hogwarts.persistencia.PersistenciaTexto;
import es.dam.hogwarts.persistencia.PersistenciaXml;
import es.dam.hogwarts.persistencia.Rutas;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Punto de entrada del ANALIZADOR FORENSE.
 * Programa independiente: trabaja UNICAMENTE con los ficheros de datos
 * generados por el simulador. No recibe el tipo de incidente por ningun
 * parametro ni objeto compartido: lo deduce cruzando TXT + BIN + XML.
 *
 * Uso:
 *   java ... AnalizadorForense [dirDatos] [dirSalida]
 *   (por defecto: datos  y  salida)
 */
public class AnalizadorForense {

    private static final Logger log = LogManager.getLogger(AnalizadorForense.class);

    private final Path dirDatos;
    private final Path dirSalida;

    public AnalizadorForense(Path dirDatos, Path dirSalida) {
        this.dirDatos = dirDatos;
        this.dirSalida = dirSalida;
    }

    public ResultadoAnalisis analizar() throws IOException {
        log.info("Inicio del analisis forense sobre {}", dirDatos.toAbsolutePath());

        List<Alerta> alertas =
                new PersistenciaTexto(dirDatos.resolve(Rutas.FICHERO_OPERACIONES)).leerAlertas();

        List<RegistroSensor> registros;
        try (PersistenciaBinaria pb =
                     new PersistenciaBinaria(dirDatos.resolve(Rutas.FICHERO_SENSORES))) {
            registros = pb.leerTodos();
        }

        List<Zona> zonas =
                new PersistenciaXml().cargar(dirDatos.resolve(Rutas.FICHERO_CASTILLO));

        log.info("Procesados {} eventos, {} mediciones y {} zonas",
                alertas.size(), registros.size(), zonas.size());

        ResultadoAnalisis resultado = new MotorReglas().analizar(alertas, registros, zonas);
        new GeneradorInformes().generar(resultado, dirSalida);

        log.info("Analisis completado: {}", resultado.getTipoIncidente());
        return resultado;
    }

    public static void main(String[] args) {
        Path dirDatos = args.length > 0 ? Path.of(args[0]) : Path.of("datos");
        Path dirSalida = args.length > 1 ? Path.of(args[1]) : Path.of("salida");
        try {
            ResultadoAnalisis resultado = new AnalizadorForense(dirDatos, dirSalida).analizar();
            System.out.println("Incidente identificado: " + resultado.getTipoIncidente());
            System.out.println("Conclusion: " + resultado.getConclusion());
        } catch (IOException e) {
            log.error("Error durante el analisis forense", e);
        }
    }
}