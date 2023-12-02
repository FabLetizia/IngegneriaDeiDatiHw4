public interface XPathFinder {
    public String findBestXPath(String xmlFile, String param) throws Exception;
    public String extractValue(String xmlFile, String xpath) throws Exception;
}