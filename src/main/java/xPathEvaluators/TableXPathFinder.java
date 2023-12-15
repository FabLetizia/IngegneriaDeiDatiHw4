package xPathEvaluators;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class TableXPathFinder extends BaseXPathFinder{

	private int tableNumber = 0;
	private int bodyNumber = 0;
	private int captionNumber = 0; 
	private int footsNumber = 0;
	private int citationsNumber = 0;
	private int paragraphsNumber = 0;
	private int punteggioParagraphCitations = 0;
	private int numberTableWithCells = 0;
	private int punteggioCaptionCitations = 0;

	
	public int getPunteggioCaptionCitations() {
		return punteggioCaptionCitations;
	}

	public int getPunteggioParagraphCitations() {
		return punteggioParagraphCitations;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public int getBodyNumber() {
		return bodyNumber;
	}

	public int getCaptionNumber() {
		return captionNumber;
	}

	public int getFootsNumber() {
		return footsNumber;
	}

	public int getCitationsNumber() {
		return citationsNumber;
	}

	public int getParagraphsNumber() {
		return paragraphsNumber;
	}

	public int getNumberTableWithCells() {
		return numberTableWithCells;
	}


	/*
    Ricerca della migliore xpath per l'estrazione delle table id
	 */
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
			NodeList tableIdList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			String extractedContent = "";

			if(tableIdList!=null){
				for(int i =0; i<tableIdList.getLength(); i++) {
					Node node = tableIdList.item(i);
					this.tableNumber++;
					extractedContent += node.getTextContent()+" ";
					//					this.extractBody(logger, logFilePath, node.getTextContent(), document);
					this.extractCaption(logger, logFilePath, node.getTextContent(), document);
					//					this.extractFoots(logger, logFilePath, node.getTextContent(), document);
					//					this.extractTextParagraphs(logger, logFilePath, node.getTextContent(), document);
					//					this.extractContentCells(logger, logFilePath, node.getTextContent(), document);
				}

				if(extractedContent.contains(" ") && (extractedContent.contains("t") || extractedContent.contains("T"))){
					Integer value = this.expression2score.get(dynamicXPath);
					this.expression2score.put(dynamicXPath, value+1);
				}

				logger.info("XPath: {}", dynamicXPath);
				logger.info("Extracted Value: {}", extractedContent);
				saveLogToFile(logger, dynamicXPath, extractedContent, logFilePath);
			}
		}
	}

	private void saveLogToFile(Logger logger, String dynamicXPath, String extractedContent, String logFilePath) {
		try {
			Files.write(Paths.get(logFilePath), String.format("XPath: %s, Extracted Value: %s\n", dynamicXPath, extractedContent).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("Errore durante il salvataggio del log su file", e);
		}

	}

	/*
    Generazione delle espressioni xpath candidate
	 */
	@Override
	public List<String> generateDynamicXPaths() {
		// Implementa la logica per generare espressioni XPath dinamiche basate sul tuo caso specifico
		List<String> xpaths = new ArrayList<>();
		xpaths.add("//table-wrap[@id]/@id");
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

	private void extractBody(Logger logger, String logFilePath, String tableId, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		//table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/thead | //table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/tbody
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
		if(extractedContent.startsWith("<thead") || extractedContent.contains("</tbody>")) {
			this.bodyNumber++;
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
	}



	private void extractCaption(Logger logger, String logFilePath, String tableId, Document document) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		//table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/thead | //table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/tbody
		//		String xpathExpression = "//table-wrap[@id='" + tableId + "']/caption/p | //table-wrap[@id='" + tableId + "']/caption/title";
		String xpathExpression = "//table-wrap[@id='" + tableId + "']/caption";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList captionNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";
		/* IMPORTANTE: La caption è unica --> se mai ce ne fosse più di una la mettiamo tutta insieme in extractedContent */
		// List<String> captions = new ArrayList<String>();

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

		if(!extractedContent.startsWith("<caption")) {
			this.captionNumber++;
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	

		this.extractCitationsCaption(logger, logFilePath, tableId, document, extractedContent);


	}

	private void extractCitationsCaption(Logger logger, String logFilePath, String tableID, Document document, String captionText) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//table-wrap[@id='" + tableID + "']/caption/xref[@ref-type='bibr']/@rid";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList citationsCaptionNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		int cont = 0;

		for(int i = 0; i<citationsCaptionNodes.getLength(); i++) {
			Node node = citationsCaptionNodes.item(i);
			if(node!=null){ 
				extractedContent += node.getTextContent() + " "; 
			}

			if(captionText.contains(node.getTextContent())) {
				cont++;
			}
		}
		// se l'xpath funziona perfetta: punteggioParagraphCitations = tableNum
		if(cont == citationsCaptionNodes.getLength()) {
			this.punteggioCaptionCitations++;
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
	}

	private void extractFoots(Logger logger, String logFilePath, String tableId, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		//table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/thead | //table-wrap[@id='table_id']/table[@frame='hsides' and @rules='groups']/tbody
		String xpathExpression = "//table-wrap[@id='" + tableId + "']/table-wrap-foot//p";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList footsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";

		//sui primi 100 articoli, su 414 tabelle totali abbiamo verificato che 274 hanno il foot con xpath //table-wrap[@id='Table_ID']/table-wrap-foot
		//e abbiamo verificato se c'era almeno un p con xpath  //table-wrap[@id='Table_ID']/table-wrap-foot//p
		//ottenendo lo stesso risultato

		if(footsNodes.getLength()==0) {
			this.footsNumber++;
		}

		for(int i = 0; i<footsNodes.getLength(); i++) {
			Node node = footsNodes.item(i);
			if(node!=null){
				extractedContentNode = this.serializeNodeToString(node);
				extractedContent += extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
			}
		}
		if(extractedContent.startsWith("<p")) {
			this.footsNumber++;
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
	}

	private void extractTextParagraphs(Logger logger, String logFilePath, String tableId, Document document) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//xref[@ref-type='table' and @rid='"+ tableId + "']/..";
		//		String xpathExpression = "//p[xref[@ref-type='fig' and @rid='"+ figureId + "']]";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList textParagraphsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";
		List<String> paragraphs = new ArrayList<String>();

		for(int i = 0; i<textParagraphsNodes.getLength(); i++) {
			Node node = textParagraphsNodes.item(i);
			if(node!=null){ 
				this.citationsNumber++;
				extractedContentNode = this.serializeNodeToString(node);
				extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				if(extractedContentNode.startsWith("<p")) {
					this.paragraphsNumber++;
				}
				extractedContent += extractedContentNode;

				paragraphs.add(extractedContentNode);

			}
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);

		for(String p : paragraphs) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document paragraphDocument = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(p)));
			this.extractCitationsParagraph(logger, logFilePath, tableId, paragraphDocument, p);
		}

	}

	private void extractCitationsParagraph(Logger logger, String logFilePath, String tableId, Document document, String textParagraphs) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//xref[@ref-type='bibr']/@rid";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList citationsParagraphNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		int cont = 0;

		for(int i = 0; i<citationsParagraphNodes.getLength(); i++) {
			Node node = citationsParagraphNodes.item(i);
			if(node!=null){ 
				extractedContent += node.getTextContent() + " "; 
			}

			if(textParagraphs.contains(node.getTextContent())) {
				cont++;
			}
		}
		// se l'xpath funziona perfetta: punteggioParagraphCitations = num paragrafi (citationNumbers)
		if(cont == citationsParagraphNodes.getLength()) {
			this.punteggioParagraphCitations++;
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
	}

	private void extractContentCells(Logger logger, String logFilePath, String tableId, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		String xpathExpression = "//table-wrap[@id='" + tableId + "']//td";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList contentCellsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "\n";
		Set<String> cellValues = new HashSet<String>();

		if(contentCellsNodes.getLength()>0) {
			// se l'xpath funziona questo num = num di tabelle
			this.numberTableWithCells++;
		}

		for(int i = 0; i<contentCellsNodes.getLength(); i++) {
			Node node = contentCellsNodes.item(i);
			if(node!=null){ 
				cellValues.add(node.getTextContent());
				extractedContent += node.getTextContent() + "\n"; 
			}
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);

		for(String value: cellValues) {
			this.extractCitedInCells(logger, logFilePath, value, document);
		}
	}

	/* vai a cercare per ogni valore di una cella tutti i paragrafi in cui è contenuto */
	private void extractCitedInCells(Logger logger, String logFilePath, String term, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		//DA RIVEDERE !!!!!!!!
		term = term.replaceAll("'", "");

		String xpathExpression = "//p[contains(.,'" + term + "')]";
		XPathExpression expr = xpath.compile(xpathExpression);
		NodeList citationsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";

		for(int i = 0; i<citationsNodes.getLength(); i++) {
			Node node = citationsNodes.item(i);
			if(node!=null){ 
				extractedContentNode = this.serializeNodeToString(node);
				extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				extractedContent += extractedContentNode;
			}
		}

		logger.info("XPath: {}", xpathExpression);
		logger.info("Extracted Value: {}", extractedContent);
		saveLogToFile(logger, xpathExpression, extractedContent, logFilePath);	
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

	@Override
	public String toString() {
		return "TableIdXPathFinder";
	}

}
