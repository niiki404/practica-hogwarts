package es.dam.hogwarts;

import es.dam.hogwarts.analizador.AnalizadorForense;
import es.dam.hogwarts.excepciones.EventoCriticoException;
import es.dam.hogwarts.modelo.ResultadoAnalisis;
import es.dam.hogwarts.modelo.TipoIncidente;
import es.dam.hogwarts.simulador.GestorOperativa;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * El analizador identifica correctamente CADA tipo de incidente definido,
 * leyendo unicamente los ficheros de datos (prueba parametrizada).
 */
class AnalizadorParametrizadoTest {

    @TempDir
    Path temp;

    @ParameterizedTest
    @EnumSource(value = TipoIncidente.class, names = {
            "TORMENTA_MAGICA", "FUGA_CRIATURA", "INTRUSO_NO_AUTORIZADO",
            "FALLO_CONTENCION_CAMARA", "DEMENTORES_PERIMETRO", "POLTERGEIST"})
    void elAnalizadorIdentificaCadaIncidente(TipoIncidente tipo) throws IOException {
        Path datos = temp.resolve("datos_" + tipo);
        Path salida = temp.resolve("salida_" + tipo);

        try (GestorOperativa operativa = new GestorOperativa(datos, tipo, 6, 7L)) {
            operativa.preparar();
            try {
                operativa.ejecutar();
            } catch (EventoCriticoException esperada) {
                // El incidente siempre acaba en situacion critica: es lo esperado
            }
        }

        ResultadoAnalisis resultado = new AnalizadorForense(datos, salida).analizar();
        assertEquals(tipo, resultado.getTipoIncidente());
    }
}