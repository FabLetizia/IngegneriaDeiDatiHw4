package dataExtractions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import netscape.javascript.JSObject;

public class Table {

	private String tableId;
	private String body;
	private String caption;
	private List<String> captionCitations;
	private List<String> foots;
	private List<Paragraph> paragraphs;
	private List<Cell> cells;

	public JSONObject getTable(String xmlFile, String tableId) throws Exception {
		this.tableId = tableId;
		this.body = this.extractBody(xmlFile);
		this.caption = this.extractCaption(xmlFile);
		this.captionCitations = this.extractCaptionCitations(xmlFile);
		this.foots = this.extractFoots(xmlFile);
//		this.paragraphs = this.extractParagraphs(xmlFile);
		
		JSONObject jsonTable = new JSONObject();
        jsonTable.put("table_id", this.tableId);
        jsonTable.put("body", this.body);
        jsonTable.put("caption", this.caption);
        
        JSONArray jsonCaptionCitations = new JSONArray(this.captionCitations);
        JSONArray jsonFoots = new JSONArray(this.foots);
        
        jsonTable.put("caption_citations", jsonCaptionCitations);
        jsonTable.put("foots", jsonFoots);

		return jsonTable;

	}

	private String extractCaption(String xmlFile) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//table-wrap[@id='" + this.tableId + "']/caption";
		XPathExpression expr = xpath.compile(xpathExpression);
		
		NodeList captionNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";
		
		for(int i = 0; i<captionNodes.getLength(); i++) {
			Node node = captionNodes.item(i);
			if(node!=null){
				extractedContentNode = this.serializeNodeToString(node);
				extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?><caption>", "");
				extractedContentNode = extractedContentNode.replaceAll("</caption>", "");
				//captions.add(extractedContentNode);
				extractedContent += extractedContentNode;
			}
		}
		
		return extractedContent;
	}

	private List<String> extractFoots(String xmlFile) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		//table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/thead | //table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/tbody
		String xpathExpression = "//table-wrap[@id='" + this.tableId + "']/table-wrap-foot//p";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList footsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";
		List<String> foots = new ArrayList<String>();

		for(int i = 0; i<footsNodes.getLength(); i++) {
			Node node = footsNodes.item(i);
			if(node!=null){
				extractedContentNode = this.serializeNodeToString(node);
				extractedContent = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				if(extractedContent.isEmpty()) {
					foots.add(extractedContent);
				}
			}
		}
		
		return foots;
	}

	private List<String> extractCaptionCitations(String xmlFile) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//table-wrap[@id='" + this.tableId + "']/caption/xref[@ref-type='bibr']/@rid";
		XPathExpression expr = xpath.compile(xpathExpression);
		
		NodeList citationsCaptionNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		List<String> citations = new ArrayList<String>();
		for(int i = 0; i<citationsCaptionNodes.getLength(); i++) {
			Node node = citationsCaptionNodes.item(i);
			if(node!=null) {
				extractedContent = this.extractBibr(node.getTextContent(),document);
				if(extractedContent.isEmpty()) {
					citations.add(extractedContent);
				}
			}
		}
		return citations;
	}

	private String extractBibr(String textContent, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//ref[@id='" + textContent + "']";
		XPathExpression expr = xpath.compile(xpathExpression);
		String extractContent = "";
		
		Node citationNode = (Node) expr.evaluate(document, XPathConstants.NODE);
		if(citationNode != null) {
			extractContent = this.serializeNodeToString(citationNode);
			extractContent = extractContent.replaceAll("<\\?xml.*\\?>", "");
		}
		
		return extractContent;
	}

	private String extractBody(String xmlFile) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//table-wrap[@id='" + this.tableId + "']/table/thead | //table-wrap[@id='" + this.tableId + "']/table/tbody";
		XPathExpression expr = xpath.compile(xpathExpression);

		NodeList bodyNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";

		for(int i = 0; i<bodyNodes.getLength(); i++) {
			Node node = bodyNodes.item(i);
			if(node!=null){
				extractedContentNode = this.serializeNodeToString(node);
				extractedContent += extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
			}
		}
		return extractedContent;
	}

	public Document loadXmlDocument(String xmlFile) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(xmlFile);
	}

	private String serializeNodeToString(Node node) {
		try {
			// Usa un Transformer per la serializzazione
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Configura l'output per indentare la stringa XML
			transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");

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
			return "";
		}
	}

}
