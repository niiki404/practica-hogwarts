package es.dam.hogwarts.persistencia;

import es.dam.hogwarts.modelo.Elemento;
import es.dam.hogwarts.modelo.EstadoElemento;
import es.dam.hogwarts.modelo.Zona;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * Persistencia del estado del castillo (XML) con DOM:
 * se carga el arbol completo en memoria, se navega y se modifican estados.
 *
 * Estructura:
 *   <castillo>
 *     <zona id="Z01" nombre="Exteriores del castillo">
 *       <elemento tipo="ANTORCHAS" estado="OPERATIVO"/>
 *       <sensores><sensor ref="1"/></sensores>
 *     </zona>
 *   </castillo>
 */

public class PersistenciaXml {
    /** Estado inicial del castillo al comenzar la vigilancia. */
    public List<Zona> zonasIniciales() {
        return List.of(
                new Zona("Z01", "Exteriores del castillo",
                        List.of(new Elemento("ANTORCHAS", EstadoElemento.OPERATIVO),
                                new Elemento("PERIMETRO", EstadoElemento.OPERATIVO)),
                        List.of(1, 2)),
                new Zona("Z02", "Cabana de Hagrid",
                        List.of(new Elemento("JAULA", EstadoElemento.CERRADA),
                                new Elemento("CRIATURA", EstadoElemento.DORMIDO)),
                        List.of(3)),
                new Zona("Z03", "Camara de los Secretos",
                        List.of(new Elemento("SELLO", EstadoElemento.SELLADA)),
                        List.of(4)),
                new Zona("Z04", "Torre de Gryffindor",
                        List.of(new Elemento("PUERTA_RETRATO", EstadoElemento.CERRADA)),
                        List.of(5)),
                new Zona("Z05", "Sala de los Menesteres",
                        List.of(new Elemento("MOBILIARIO", EstadoElemento.OPERATIVO)),
                        List.of(6)));
    }

    public void crearCastilloInicial(Path fichero) throws IOException {
        guardar(fichero, zonasIniciales());
    }

    /** Escribe la lista de zonas como XML (sirve para el castillo y para afectados.xml). */
    public void guardar(Path fichero, List<Zona> zonas) throws IOException {
        Rutas.prepararDirectorios(fichero.getParent());
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            Element raiz = doc.createElement("castillo");
            doc.appendChild(raiz);

            for (Zona zona : zonas) {
                Element eZona = doc.createElement("zona");
                eZona.setAttribute("id", zona.getId());
                eZona.setAttribute("nombre", zona.getNombre());

                for (Elemento elemento : zona.getElementos()) {
                    Element eElemento = doc.createElement("elemento");
                    eElemento.setAttribute("tipo", elemento.getTipo());
                    eElemento.setAttribute("estado", elemento.getEstado().name());
                    eZona.appendChild(eElemento);
                }

                Element eSensores = doc.createElement("sensores");
                for (Integer ref : zona.getSensores()) {
                    Element eSensor = doc.createElement("sensor");
                    eSensor.setAttribute("ref", String.valueOf(ref));
                    eSensores.appendChild(eSensor);
                }
                eZona.appendChild(eSensores);
                raiz.appendChild(eZona);
            }

            escribirDocumento(doc, fichero);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException("Error procesando el XML del castillo", e);
        }
    }

    /** Carga el XML completo en memoria y lo convierte en objetos del modelo. */
    public List<Zona> cargar(Path fichero) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(fichero.toFile());
            doc.getDocumentElement().normalize();

            List<Zona> zonas = new ArrayList<>();
            NodeList listaZonas = doc.getDocumentElement().getElementsByTagName("zona");
            for (int i = 0; i < listaZonas.getLength(); i++) {
                Element eZona = (Element) listaZonas.item(i);
                List<Elemento> elementos = new ArrayList<>();
                List<Integer> sensores = new ArrayList<>();

                NodeList hijos = eZona.getChildNodes();
                for (int j = 0; j < hijos.getLength(); j++) {
                    if (hijos.item(j) instanceof Element hijo) {
                        switch (hijo.getTagName()) {
                            case "elemento" -> elementos.add(new Elemento(
                                    hijo.getAttribute("tipo"),
                                    EstadoElemento.valueOf(hijo.getAttribute("estado"))));
                            case "sensores" -> {
                                NodeList refs = hijo.getElementsByTagName("sensor");
                                for (int k = 0; k < refs.getLength(); k++) {
                                    Element eSensor = (Element) refs.item(k);
                                    sensores.add(Integer.parseInt(eSensor.getAttribute("ref")));
                                }
                            }
                            default -> {
                                // Se ignoran nodos desconocidos
                            }
                        }
                    }
                }
                zonas.add(new Zona(eZona.getAttribute("id"), eZona.getAttribute("nombre"),
                        elementos, sensores));
            }
            return zonas;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Error leyendo el XML del castillo: " + fichero, e);
        }
    }

    /** Modifica el estado de un elemento de una zona y vuelve a guardar el XML. */
    public void cambiarEstado(Path fichero, String zonaId, String tipoElemento,
                              EstadoElemento nuevoEstado) throws IOException {
        List<Zona> zonas = cargar(fichero);
        boolean encontrado = false;
        for (Zona zona : zonas) {
            if (zona.getId().equals(zonaId)) {
                Optional<Elemento> elemento = zona.buscarElemento(tipoElemento);
                if (elemento.isPresent()) {
                    elemento.get().setEstado(nuevoEstado);
                    encontrado = true;
                }
            }
        }
        if (!encontrado) {
            throw new IOException("Elemento no encontrado: " + zonaId + "/" + tipoElemento);
        }
        guardar(fichero, zonas);
    }

    private void escribirDocumento(Document doc, Path fichero)
            throws TransformerException, IOException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(fichero.toFile()));
    }
}
