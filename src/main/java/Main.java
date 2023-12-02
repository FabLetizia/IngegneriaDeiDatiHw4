import java.io.File;
import java.util.List;

public class Main {
    private static final String DIRECTORY_PATH = "/Users/alessandropesare/xml_files";
    private static final int SAMPLE_SIZE = 10;

    public static void main(String[] args) throws Exception {
        // Benchmark XPath extraction
        benchmarkXPathExtraction();

        // Generate JSON files
        //generateJsonFiles();
    }

    private static void benchmarkXPathExtraction() throws Exception {
        BaseXPathFinder dynamicXPath = new ArticleIdXPathFinder();

        // Extract a random sample of files
        List<File> sampleFiles = FileUtil.getRandomSampleFromDirectory(DIRECTORY_PATH, SAMPLE_SIZE);

        // For each file in the sample, find the best XPath expression and print the result
        for (File file : sampleFiles) {
            System.out.println("File selected: " + file.getName());

            String bestXPath = dynamicXPath.findBestXPath(file.getAbsolutePath(), "pmc");
            System.out.println("Best XPath: " + bestXPath);
            System.out.println("Extracted Value: " + dynamicXPath.extractValue(file.getAbsolutePath(), bestXPath));
            System.out.println();
        }
    }

    //TO-DO (L'idea è di non caricarsi tutti i file xml in memoria ma analizzarli come stream)
    /*private static void generateJsonFiles() throws Exception {
        BaseXPathFinder dynamicXPath = new ArticleIdXPathFinder();

        // Get a list of all XML files in the directory
        File[] allXmlFiles = new File(DIRECTORY_PATH).listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        // Process one XML file at a time and generate the corresponding JSON file
        if (allXmlFiles != null) {
            Arrays.stream(allXmlFiles)
                    .limit(SAMPLE_SIZE) // Limit the number of files processed to the sample size
                    .forEach(xmlFile -> {
                        try {
                            System.out.println("Generating JSON for: " + xmlFile.getName());

                            // Use dynamicXPath or another suitable XPathFinder to extract structured information
                            // For simplicity, let's assume a method extractStructuredInfo() is available
                            String structuredInfo = dynamicXPath.extractStructuredInfo(xmlFile.getAbsolutePath());

                            // Generate JSON file with the structured information
                            String jsonFileName = xmlFile.getName().replace(".xml", ".json");
                            String jsonFilePath = DIRECTORY_PATH + File.separator + jsonFileName;
                            JsonUtil.writeJsonFile(jsonFilePath, structuredInfo);

                            System.out.println("JSON File generated: " + jsonFilePath);
                            System.out.println();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }*/
}
