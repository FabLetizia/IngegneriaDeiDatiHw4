package xPathEvaluators;

import org.slf4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

public abstract class BaseXPathFinder implements XPathFinder{
	
	protected Map<String, Integer> expression2score = new HashMap<>();

    public abstract String getBestXPath();

    public Document loadXmlDocument(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }
    public abstract List<String> generateDynamicXPaths();

    public abstract String extractValue(String xmlFile, String xpath) throws Exception;

    public abstract void findBestXPath(String xmlFile, Logger logger, String logFilePath) throws Exception;
    
    public Map<String, Integer> getOrderedResults() {
    	List<Map.Entry<String, Integer>> entryList = new ArrayList<>(this.expression2score.entrySet());
    	entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    	
    	Map<String, Integer> orderedMap = new LinkedHashMap<>();
    	for (Map.Entry<String, Integer> entry: entryList) {
    		orderedMap.put(entry.getKey(), entry.getValue());
    	}
    	
    	return orderedMap;
    }
}
