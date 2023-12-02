import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

public class DynamicXPath {

    public static String findBestXPath(String xmlFile, String pubIdType) throws Exception {
        // Carica il documento XML dal file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(xmlFile);  Non funziona (da capire perché) -> richiede i file nella Cartella dei "docs"
        Document document = parseXmlFile(xmlFile); 	   // Funziona -> richiede i file nella Cartella del "progetto"

        // Crea un'istanza di XPath
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        // XPath migliore e valore massimo di corrispondenza
        String bestXPath = null;
        int maxMatchCount = 0;

        // Prova diverse espressioni XPath dinamiche
        for (String dynamicXPath : generateDynamicXPaths(pubIdType)) {
            // Compila l'espressione XPath
            XPathExpression expr = xpath.compile(dynamicXPath);

            // Esegui la query XPath sul documento
            Node articleIdNode = (Node) expr.evaluate(document, XPathConstants.NODE);

            // Calcola il numero di corrispondenze
            int matchCount = (articleIdNode != null) ? 1 : 0;

            // Aggiorna se questa espressione è migliore della precedente
            if (matchCount > maxMatchCount) {
                maxMatchCount = matchCount;
                bestXPath = dynamicXPath;
            }
        }

        return bestXPath;
    }

    public static String extractValue(String xmlFile, String xpath) throws Exception {
        // Carica il documento XML dal file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(xmlFile); 	Non funziona (da capire perché) -> richiede i file nella Cartella dei "docs"
        Document document = parseXmlFile(xmlFile);		// Funziona -> richiede i file nella Cartella del "progetto"

        // Esegui la query XPath sul documento
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath expr = xPathFactory.newXPath();
        return expr.evaluate(xpath, document);
    }

    private static Document parseXmlFile(String xmlFile) throws Exception {
        InputSource inputSource = new InputSource(new java.io.FileReader(xmlFile));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputSource);
    }

    private static List<String> generateDynamicXPaths(String pubIdType) {
        // Implementa la logica per generare espressioni XPath dinamiche basate sul tuo caso specifico
        List<String> xpaths = new ArrayList<>();
        xpaths.add("//article-meta/article-id[@pub-id-type='" + pubIdType + "']");
        // Aggiungi altre espressioni XPath secondo necessità
        return xpaths;
    }
}