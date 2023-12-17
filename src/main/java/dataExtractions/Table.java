package dataExtractions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Table {

	public List<Map<String, Object>> extractParagraphs(String xmlFile, String tableId) throws Exception {
		List<Map<String, Object>> paragraphs = new ArrayList<>();

		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//xref[@ref-type='table' and @rid='" + tableId + "']/..";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList citationsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContentNode = "";

		for(int i = 0; i<citationsNodes.getLength(); i++) {
			Node node = citationsNodes.item(i);
			if(node!=null){
				LinkedHashMap<String,Object> contentParagraph = new LinkedHashMap<>();

				extractedContentNode = this.serializeNodeToString(node);
				extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				contentParagraph.put("text",extractedContentNode);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document paragraphDocument = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(extractedContentNode)));
				contentParagraph.put("citations", this.extractParagraphCitations(xmlFile,paragraphDocument));

				paragraphs.add(contentParagraph);
			}
		}

		return paragraphs;
	}

	public Set<String> extractParagraphCitations(String xmlFile,Document document) throws Exception {
		Set<String> citations = new HashSet<>();
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//xref[@ref-type='bibr']/@rid";
		XPathExpression expr = xpath.compile(xpathExpression);

		NodeList paragraphsCitationsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

		for (int i = 0; i < paragraphsCitationsNodes.getLength(); i++) {
			Node node = paragraphsCitationsNodes.item(i);
			if (node != null) {
				String extractedContent = this.extractBibr(node.getTextContent(), xmlFile);
				if (!extractedContent.isEmpty())
					citations.add(extractedContent);
			}
		}
		return citations;
	}

	private String extractBibr(String textContent, String xmlFile) throws XPathExpressionException, Exception {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//ref[@id='" + textContent + "']";
		XPathExpression expr = xpath.compile(xpathExpression);
		String extractContent = "";

		Node citationNode = (Node) expr.evaluate(this.loadXmlDocument(xmlFile), XPathConstants.NODE);
		if(citationNode != null) {
			extractContent = this.serializeNodeToString(citationNode);
			extractContent = extractContent.replaceAll("<\\?xml.*\\?>", "");
		}
		return extractContent;
	}

	public List<Map<String, Object>> extractCells(String xmlFile, String tableId) throws Exception {
		List<Map<String, Object>> cells = new ArrayList<>();

		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//table-wrap[@id='" + tableId + "']//td";
		XPathExpression expr = xpath.compile(xpathExpression);

		NodeList contentCellsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		Set<String> cellValues = new HashSet<String>();

		for(int i = 0; i<contentCellsNodes.getLength(); i++) {
			Node node = contentCellsNodes.item(i);
			if(node!=null && node.getTextContent() != ""){
				cellValues.add(node.getTextContent());
			}
		}

		for (String c: cellValues) {			
			Map<String, Object> cell = new LinkedHashMap<>();
			cell.put("content", c);
			cell.put("cited_in", this.extractCellCitedIn(c, document));
			
			cells.add(cell);
		}

		return cells;

	}

	public List<String> extractCellCitedIn(String term, Document document) throws Exception {
		List<String> citedIn = new ArrayList<>();
		term = term.replaceAll("'", "");

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//p[contains(.,'" + term + "')] | //p[contains(.,'" + term.toLowerCase() + "')] | "
				+ "//p[contains(.,'" + term.toUpperCase() + "')] ";
		XPathExpression expr = xpath.compile(xpathExpression);

		NodeList citationsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";

		for(int i = 0; i < citationsNodes.getLength(); i++) {
			Node node = citationsNodes.item(i);
			if (node != null) {
				extractedContentNode = this.serializeNodeToString(node);
				extractedContent = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				citedIn.add(extractedContent);
			}
		}

		return citedIn;
	}

	public String extractCaption(String xmlFile, String tableId) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//table-wrap[@id='" + tableId + "']/caption";
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
				extractedContent += extractedContentNode;
			}
		}

		return extractedContent;
	}

	public List<String> extractFoots(String xmlFile, String tableId) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		//table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/thead | //table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/tbody
		String xpathExpression = "//table-wrap[@id='" + tableId + "']/table-wrap-foot//p";
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

	public List<String> extractCaptionCitations(String xmlFile, String tableId) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//table-wrap[@id='" + tableId + "']/caption/xref[@ref-type='bibr']/@rid";
		XPathExpression expr = xpath.compile(xpathExpression);

		NodeList citationsCaptionNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		List<String> citations = new ArrayList<String>();
		for(int i = 0; i<citationsCaptionNodes.getLength(); i++) {
			Node node = citationsCaptionNodes.item(i);
			if(node!=null) {
				extractedContent = this.extractBibr(node.getTextContent(),document);
				if(!extractedContent.isEmpty()) {
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

	public String extractBody(String xmlFile, String tableId) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		String xpathExpression = "//table-wrap[@id='" + tableId + "']/table/thead | //table-wrap[@id='" + tableId + "']/table/tbody";
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

	public NodeList extractIDs(String xmlFile) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		XPathExpression expr = xpath.compile("//table-wrap[@id]/@id");
		NodeList tablesID = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		return tablesID;
	}

}
