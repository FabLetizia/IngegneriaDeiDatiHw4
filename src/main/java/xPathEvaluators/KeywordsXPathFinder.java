package xPathEvaluators;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class KeywordsXPathFinder extends BaseXPathFinder{
	private String bestXPath;
	public KeywordsXPathFinder(){
	}
	public KeywordsXPathFinder(String bestXPath){
		this.bestXPath = bestXPath;
	}
	public String getBestXPath(){
		return this.bestXPath;
	}
	@Override
	public List<String> generateDynamicXPaths() {
		List<String> xpaths = new ArrayList<>();
		xpaths.add("//kwd-group/kwd");
		xpaths.add("//kwd");
		return xpaths;
	}

	@Override
	public String extractValue(String xmlFile, String xpath) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);

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
		for (String dynamicXPath : this.generateDynamicXPaths()){
			
			if (this.expression2score.containsKey(dynamicXPath) == false)
				this.expression2score.put(dynamicXPath, 0);
			
			// Compila l'espressione XPath
			XPathExpression expr = xpath.compile(dynamicXPath);
			
			NodeList keywordList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			List<String> extractedContent = new ArrayList<>();
			
			if (keywordList != null) {
				for (int i = 0; i < keywordList.getLength(); i++) {
					extractedContent.add(keywordList.item(i).getTextContent());
				}
				
				if (extractedContent.size() == keywordList.getLength()) {
					Integer value = this.expression2score.get(dynamicXPath);
					this.expression2score.put(dynamicXPath, value+1);
				}
				
				logger.info("XPath: {}", dynamicXPath);
				logger.info("Extracted Value: {}", extractedContent.toString());
				saveLogToFile(logger, dynamicXPath, extractedContent.toString(), logFilePath);
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
	
	@Override
	public String toString() {
		return "KeywordXPathFinder";
	}
}