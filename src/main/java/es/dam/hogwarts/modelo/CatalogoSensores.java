package es.dam.hogwarts.modelo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Catalogo de sensores magicos del castillo. */
public class CatalogoSensores {
    private static final Map<Integer, InfoSensor> SENSORES = new LinkedHashMap<>();

    static {
        registrar(new InfoSensor(1, "Z01", "viento (km/h)", 0, 60));
        registrar(new InfoSensor(2, "Z01", "temperatura exterior (C)", -5, 35));
        registrar(new InfoSensor(3, "Z02", "presion de la jaula (u.m.)", 20, 80));
        registrar(new InfoSensor(4, "Z03", "vibraciones (0-100)", 0, 40));
        registrar(new InfoSensor(5, "Z04", "presencia (personas)", 0, 2));
        registrar(new InfoSensor(6, "Z05", "nivel magico residual", 0, 200));
    }

    private CatalogoSensores() {
    }

    private static void registrar(InfoSensor info) {
        SENSORES.put(info.getIdSensor(), info);
    }

    public static InfoSensor porId(int idSensor) {
        return SENSORES.get(idSensor);
    }

    public static Collection<InfoSensor> todos() {
        return SENSORES.values();
    }

}
