import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

public abstract class BaseXPathFinder implements XPathFinder{
    public Document loadXmlDocument(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }
    public abstract List<String> generateDynamicXPaths(String param);

    public abstract String extractValue(String xmlFile, String xpath) throws Exception;

    public abstract String findBestXPath(String xmlFile,String pubIdType) throws Exception;
}
