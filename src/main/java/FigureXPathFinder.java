import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FigureXPathFinder extends BaseXPathFinder{
	private int figureNumber = 0;
	private int captionNumber = 0;
	private int sourceNumber = 0;
	private int citationsNumber = 0;
	private int paragraphsNumber = 0;
	
	@Override
	public List<String> generateDynamicXPaths() {
		List<String> xpaths = new ArrayList<>();
		xpaths.add("//fig[@id]/@id");

		//xpaths.add("//id");
		// Aggiungi altre espressioni XPath secondo necessità
		return xpaths;
	}

	@Override
	public String extractValue(String xmlFile, String xpath) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);
		// Esegui la query XPath sul documento
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath expr = xPathFactory.newXPath();
		return expr.evaluate(xpath, document);
	}

	@Override
	public void findBestXPath(String xmlFile, Logger logger, String logFilePath) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);
		//Crea un'istanza di XPath
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		

		// Prova diverse espressioni XPath dinamiche
		for (String dynamicXPath : this.generateDynamicXPaths()) {

			// New
			if (this.expression2score.containsKey(dynamicXPath) == false)
				this.expression2score.put(dynamicXPath, 0);

			// Compila l'espressione XPath
			XPathExpression expr = xpath.compile(dynamicXPath);

			// Esegui la query XPath sul documento
			NodeList figureNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			String extractedContent = "";

			for(int i = 0; i<figureNodes.getLength(); i++) {
				Node node = figureNodes.item(i);
				if(node!=null){
					extractedContent += node.getTextContent() + " ";
					this.figureNumber++;
//					this.extractCaption(logger, logFilePath, node.getTextContent(), document);
//					this.extractSource(logger, logFilePath, node.getTextContent(), document);
					this.extractCitedInParagraphs(logger, logFilePath, node.getTextContent(), document);
					
				}
			}

			Integer value = this.expression2score.get(dynamicXPath);
			this.expression2score.put(dynamicXPath, value+1);

			logger.info("XPath: {}", dynamicXPath);
			logger.info("Extracted Value: {}", extractedContent);
			saveLogToFile(logger, dynamicXPath, extractedContent, logFilePath);
		}
	}

	private void saveLogToFile(Logger logger, String dynamicXPath, String extractedContent, String logFilePath) {
		try {
			Files.write(Paths.get(logFilePath), String.format("XPath: %s, Extracted Value: %s\n", dynamicXPath, extractedContent).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("Errore durante il salvataggio del log su file", e);
		}
	}
	
	private String serializeNodeToString(Node node) {
		try {
			// Usa un Transformer per la serializzazione
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Configura l'output per indentare la stringa XML
			transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");

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
	
	private void extractCaption(Logger logger, String logFilePath, String figureId, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		
//		String xpathExpression = "//fig[@id='" + figureId + "']/caption/p | //fig[@id='" + figureId + "']/caption/title ";
		String xpathExpression = "//fig[@id='" + figureId + "']/caption";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList captionNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";
		
		if(captionNodes.getLength()>0) {
			this.captionNumber++;
		}
		
		for(int i = 0; i<captionNodes.getLength(); i++) {
			Node node = captionNodes.item(i);
			if(node!=null){
				extractedContentNode = this.serializeNodeToString(node);
				extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?><caption>", "");
				extractedContentNode = extractedContentNode.replaceAll("</caption>", "");				
				extractedContent += extractedContentNode;
			}
		}
		
		
		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	

	}

	
	private void extractSource(Logger logger, String logFilePath, String figureId, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		
		String xpathExpression = "//fig[@id='"+ figureId +"']/graphic/@*[local-name()='href']";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList sourceNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		
		if(sourceNodes.getLength()>0) {
			this.sourceNumber++;
		}
		
		for(int i = 0; i<sourceNodes.getLength(); i++) {
			Node node = sourceNodes.item(i);
			if(node!=null){ 
				extractedContent += node.getTextContent() + " ";
			}
		}
		
		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	

	}

	/* forse questo da rifare */
	
	private void extractCitedInParagraphs(Logger logger, String logFilePath, String figureId, Document document) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		
		String xpathExpression = "//xref[@ref-type='fig' and @rid='" + figureId + "']/..";
//		String xpathExpression = "//p[xref[@ref-type='fig' and @rid='"+ figureId + "']]";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList citationsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";
		List<String> paragraphs = new ArrayList<String>();
		
		for(int i = 0; i<citationsNodes.getLength(); i++) {
			Node node = citationsNodes.item(i);
			if(node!=null){ 
				this.citationsNumber++;
				extractedContentNode = this.serializeNodeToString(node);
				extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				paragraphs.add(extractedContentNode);
				if(extractedContentNode.startsWith("<p")) {
					this.paragraphsNumber++;
				}
				extractedContent += extractedContentNode + "\n"; 
			}
		}
		
		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
		
		for(String paragraph: paragraphs) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document paragraphDocument = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(paragraph)));
			this.extractCitationsParagraphs(logger, logFilePath, paragraphDocument);
		}
	}
	
	private void extractCitationsParagraphs(Logger logger, String logFilePath, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		
		String xpathExpression = "//xref[@ref-type='bibr']/@rid";
//		String xpathExpression = "//p[xref[@ref-type='fig' and @rid='"+ figureId + "']]";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList citationsParagraphsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		
		for(int i = 0; i<citationsParagraphsNodes.getLength(); i++) {
			Node node = citationsParagraphsNodes.item(i);
			if(node!=null){ 
				extractedContent += node.getTextContent() + " "; 
			}
		}
		
		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
	}
	
	
	public int getFigureNumber() {
		return figureNumber;
	}

	public int getCaptionNumber() {
		return captionNumber;
	}
	
	public int getSourceNumber() {
		return sourceNumber;
	}

	public int getCitationsNumber() {
		return citationsNumber;
	}


	public int getParagraphsNumber() {
		return paragraphsNumber;
	}


}
