import org.slf4j.Logger;

public interface XPathFinder {
    public void findBestXPath(String xmlFile, Logger logger, String logFilePath) throws Exception;
    public String extractValue(String xmlFile, String xpath) throws Exception;
}