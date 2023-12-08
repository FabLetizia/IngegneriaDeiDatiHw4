import java.util.List;

import org.slf4j.Logger;

public class TableContentXPathFinder extends BaseXPathFinder{
    @Override
    public List<String> generateDynamicXPaths(String param) {
        return null;
    }

    @Override
    public String extractValue(String xmlFile, String xpath) throws Exception {
        return null;
    }

    @Override
    public void findBestXPath(String xmlFile, String pubIdType, Logger logger, String logFilePath) throws Exception {
    }
}
