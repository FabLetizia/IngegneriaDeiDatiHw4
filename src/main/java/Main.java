import java.io.File;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
	private static final int SAMPLE_SIZE = 1000;

	/* Lanciare il Programma passando i seguenti parametri al main(): 
	 * [-xmlfiles XML_DIRECTORY] -> Path della Directory in cui ci sono i documenti .xml e i "parsing files"
	 */
	public static void main(String[] args) throws Exception {
		// Benchmark XPath extraction
		String directoryPath = null;

		for (int i=0; i < args.length; i++) {
			switch(args[i]) {
			case "-xmldocs":
				directoryPath = args[++i];
				final Path docsDir = Paths.get(directoryPath);
				boolean usableDocsDir = Files.isReadable(docsDir);
				
				if (usableDocsDir)
					break;
				else
					throw new NotDirectoryException("La Directory '" + docsDir.toAbsolutePath() + "' non esiste oppure non è accessibile in lettura");
			
			default:
				throw new IllegalArgumentException("Parametro Sconosciuto " + args[i]);
			}
		}

		benchmarkXPathExtraction(directoryPath);

		// Generate JSON files
		//generateJsonFiles();
	}

	private static void benchmarkXPathExtraction(String directoryPath) throws Exception {
		BaseXPathFinder dynamicXPath = new ArticleIdXPathFinder();

		// Extract a random sample of files
		List<File> sampleFiles = FileUtil.getRandomSampleFromDirectory(directoryPath, SAMPLE_SIZE);

		// For each file in the sample, find the best XPath expression and print the result
		for (File file : sampleFiles) {
			System.out.println("File selected: " + file.getName());
			String fileURI = file.toURI().toASCIIString();

			String bestXPath = dynamicXPath.findBestXPath(fileURI, "pmc");
			System.out.println("Best XPath: " + bestXPath);
			System.out.println("Extracted Value: " + dynamicXPath.extractValue(fileURI, bestXPath));
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