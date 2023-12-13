package xPathEvaluators;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AbstractXPathFinder extends BaseXPathFinder{
	@Override
	public List<String> generateDynamicXPaths() {
		// Implementa la logica per generare espressioni XPath dinamiche basate sul tuo caso specifico
		List<String> xpaths = new ArrayList<>();
//		xpaths.add("//abstract/p | //abstract/sec");
		xpaths.add("//abstract");

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
			NodeList abstractNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			String extractedContent = "";
			String extractedContentNode = "";

			for(int i = 0; i<abstractNodes.getLength(); i++) {
				Node node = abstractNodes.item(i);
				if(node!=null){
					extractedContentNode = this.serializeNodeToString(node);
					extractedContentNode = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
					extractedContentNode = extractedContentNode.replaceAll("<abstract[^>]*>", "");
					extractedContentNode = extractedContentNode.replaceAll("</abstract>", "");
					extractedContent += extractedContentNode;
					
					Integer value = this.expression2score.get(dynamicXPath);
					this.expression2score.put(dynamicXPath, value+1);
				}
			}


			//				if(extractedContent.startsWith("<p>")){
			//					Integer value = this.expression2score.get(dynamicXPath);
			//					this.expression2score.put(dynamicXPath, value+1);
			//				}

			

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

	// Metodo per serializzare un nodo in una stringa XML
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
}
