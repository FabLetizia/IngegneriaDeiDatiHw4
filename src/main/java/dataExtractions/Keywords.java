package dataExtractions;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class Keywords {

    private String bestXPath;

    public Keywords(){
    }
    public Keywords(String bestXPath){
        this.bestXPath = bestXPath;
    }
    public String getBestXPath() {
        return bestXPath;
    }
    public NodeList extractKeywords(String xmlFile) throws Exception {
        Document document = this.loadXmlDocument(xmlFile);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expr = xpath.compile(this.getBestXPath());
        NodeList keywordList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        return keywordList;
    }
    public Document loadXmlDocument(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }
}
