package dataExtractions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TableManagement {
	
	public JSONArray extractTables(String xmlFile) throws Exception {
        Document document = this.loadXmlDocument(xmlFile);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expr = xpath.compile("//table-wrap[@id]/@id");
        NodeList tableList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        
        JSONArray jsonTables = new JSONArray();
        for(int i = 0; i<tableList.getLength();i++) {
        	Node node = tableList.item(i);
        	if(node != null) {
        		Table table = new Table();
        		jsonTables.put(table.getTable(xmlFile, tableList.item(i).getTextContent())); 
        	}
        }
        return jsonTables;
    }
    public Document loadXmlDocument(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }
}
