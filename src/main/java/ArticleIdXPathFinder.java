import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

public class ArticleIdXPathFinder extends BaseXPathFinder{
    /*
    Ricerca della migliore xpath per l'estrazione dell'article id
     */
    @Override
    public String findBestXPath(String xmlFile,String pubIdType) throws Exception {

        Document document = this.loadXmlDocument(xmlFile);
        //Crea un'istanza di XPath
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        // XPath migliore e valore massimo di corrispondenza
        String bestXPath = null;
        // se tutte gli article-id fossero vuoti il miglior punteggio è 0 (se la xpath funziona)
        int maxMatchCount = -1;

        // Prova diverse espressioni XPath dinamiche
        for (String dynamicXPath : this.generateDynamicXPaths(pubIdType)){
            // Compila l'espressione XPath
            XPathExpression expr = xpath.compile(dynamicXPath);

            // Esegui la query XPath sul documento
            Node articleIdNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            int matchCount = 0;
            if(articleIdNode!=null){
                if(articleIdNode.getTextContent().length()>3 && articleIdNode.getTextContent().startsWith("PMC")){
                    matchCount++;
                }
            }
            // Aggiorna se questa espressione è migliore della precedente
            if (matchCount > maxMatchCount) {
                maxMatchCount = matchCount;
                bestXPath = dynamicXPath;
            }
        }

        return bestXPath;
    }
    /*
    Generazione delle espressioni xpath candidate
     */
    @Override
    public List<String> generateDynamicXPaths(String pubIdType) {
        // Implementa la logica per generare espressioni XPath dinamiche basate sul tuo caso specifico
        List<String> xpaths = new ArrayList<>();
        xpaths.add("//article-meta/article-id[@pub-id-type='" + pubIdType + "']");
        // Aggiungi altre espressioni XPath secondo necessità
        return xpaths;
    }

    /*
    Estrazione del valore ricercato data la espressione xpath
     */
    @Override
    public String extractValue(String xmlFile, String xpath) throws Exception {
        Document document = this.loadXmlDocument(xmlFile);
        // Esegui la query XPath sul documento
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath expr = xPathFactory.newXPath();
        return expr.evaluate(xpath, document);
    }

}