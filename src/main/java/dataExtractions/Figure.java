package dataExtractions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringWriter;

public class Figure {

    public NodeList extractIDs(String xmlFile) throws Exception {
        Document document = this.loadXmlDocument(xmlFile);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expr = xpath.compile("//fig[@id]/@id");
        NodeList figuresID = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        return figuresID;
    }
    public String extractCaptions(String xmlFile, String figureId) throws Exception {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String xpathExpression = "//fig[@id='" + figureId + "']/caption";
        XPathExpression expr = xpath.compile(xpathExpression);
        NodeList captionNodes = (NodeList) expr.evaluate(this.loadXmlDocument(xmlFile), XPathConstants.NODESET);
        String extractedContent = "";
        String extractedContentNode = "";

        for(int i = 0; i<captionNodes.getLength(); i++) {
            Node node = captionNodes.item(i);
            if(node!=null){
                extractedContentNode = this.serializeNodeToString(node);
                extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?><caption>", "");
                extractedContentNode = extractedContentNode.replaceAll("</caption>", "");
                extractedContent += extractedContentNode;
            }
        }
        return extractedContent;
    }
    private String serializeNodeToString(Node node) {
        try {
            // Usa un Transformer per la serializzazione
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Configura l'output per indentare la stringa XML
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            // Crea un DOMSource dal nodo
            DOMSource source = new DOMSource(node);

            // Crea uno StringWriter per la stringa di output
            StringWriter stringWriter = new StringWriter();

            // Crea un StreamResult per la stringa di output
            StreamResult result = new StreamResult(stringWriter);

            // Esegue la trasformazione
            transformer.transform(source, result);

            // Restituisci la stringa risultante
            return stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public NodeList extractSources(String xmlFile) {
        return null;
    }
    public Document loadXmlDocument(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }
}
