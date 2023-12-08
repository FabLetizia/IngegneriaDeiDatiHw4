import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TableIdXPathFinder extends BaseXPathFinder{
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
					extractedContent += tableIdList.item(i).getTextContent()+" ";
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

	@Override
	public String toString() {
		return "TableIdXPathFinder";
	}

}
