import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class KeywordsXPathFinder extends BaseXPathFinder{
	@Override
	public List<String> generateDynamicXPaths(String param) {
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
	public void findBestXPath(String xmlFile, String pubIdType, Logger logger, String logFilePath) throws Exception {
		Document document = this.loadXmlDocument(xmlFile);
		//Crea un'istanza di XPath
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		// XPath migliore e valore massimo di corrispondenza
		String bestXPath = null;
		int maxMatchCount = 0;
			
		Map<String, Integer> expr2score = new HashMap<>();

		// Da aggiungere una mappa (?)
		
		// Prova diverse espressioni XPath dinamiche
		for (String dynamicXPath : this.generateDynamicXPaths(pubIdType)){
			// Compila l'espressione XPath
			XPathExpression expr = xpath.compile(dynamicXPath);

			// Esegui la query XPath sul documento
			//            Node articleIdNode = (Node) expr.evaluate(document, XPathConstants.NODE);
			
			
			NodeList keywordList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			
			
			
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				Node node = nodeList.item(i);
//				String extractedTag = serializeNodeToString(node);
//				System.out.println(extractedTag);
//			}
//
//			// Calcola il numero di corrispondenze
//			int matchCount = (articleIdNode != null) ? 1 : 0;
//
//			// Aggiorna se questa espressione è migliore della precedente
//			if (matchCount > maxMatchCount) {
//				maxMatchCount = matchCount;
//				bestXPath = dynamicXPath;
//			}
		}
	}
}