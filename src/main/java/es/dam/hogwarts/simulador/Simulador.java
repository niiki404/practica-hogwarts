package es.dam.hogwarts.simulador;

import es.dam.hogwarts.excepciones.EventoCriticoException;
import es.dam.hogwarts.modelo.TipoIncidente;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Punto de entrada del SIMULADOR de vigilancia de Hogwarts.
 *
 * Uso:
 *   java ... Simulador                          -> modo normal (incidente aleatorio)
 *   java ... Simulador --incidente POLTERGEIST  -> modo prueba (incidente concreto)
 *
 * Orden conceptual obligatorio:
 *   operativa normal -> se escriben los ficheros -> aparece la anomalia ->
 *   los mismos ficheros la reflejan -> se detecta la situacion critica ->
 *   throw EventoCriticoException -> catch: log y cierre controlado.
 */
public class Simulador {

    private static final Logger log = LogManager.getLogger(Simulador.class);

    public static void main(String[] args) {
        TipoIncidente incidente = parsearIncidente(args);
        log.info("Arranca el simulador de vigilancia de Hogwarts");

        try (GestorOperativa operativa = new GestorOperativa(Path.of("datos"), incidente, 10, null)) {
            operativa.preparar();
            operativa.ejecutar();
        } catch (EventoCriticoException e) {
            // Al entrar aqui los datos forenses YA estan persistidos.
            // Este catch solo registra la excepcion y deja cerrar los recursos.
            log.error("Evento critico capturado: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error de E/S en el simulador", e);
        }
        log.info("Simulador finalizado de forma controlada");
    }

    static TipoIncidente parsearIncidente(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase("--incidente")) {
                return TipoIncidente.valueOf(args[i + 1].toUpperCase());
            }
        }
        // Modo normal: el incidente se elige al azar
        TipoIncidente[] posibles = Arrays.stream(TipoIncidente.values())
                .filter(t -> t != TipoIncidente.INDETERMINADO)
                .toArray(TipoIncidente[]::new);
        return posibles[new Random().nextInt(posibles.length)];
    }
}