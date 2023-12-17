package dataExtractions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    public List<String> extractKeywords(String xmlFile) throws Exception {
    	List<String> keywords = new ArrayList<>();
    	
        Document document = this.loadXmlDocument(xmlFile);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expr = xpath.compile(this.getBestXPath());
        NodeList keywordList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        
        for(int i = 0; i < keywordList.getLength(); i++) {
        	Node node = keywordList.item(i);
        	if (node != null)
        		keywords.add(node.getTextContent());
        }
        
        return keywords;
    }
    
    public Document loadXmlDocument(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }
}
