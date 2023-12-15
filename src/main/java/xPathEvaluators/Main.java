package xPathEvaluators;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final int SAMPLE_SIZE = 100;
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/* Lanciare il Programma passando i seguenti parametri al main(): 
	 * [-xmlfiles XML_DIRECTORY] -> Path della Directory in cui ci sono i documenti .xml e i "parsing files"
	 * [-logdir LOG_DIRECTORY] -> Path della Directory in cui vengono scritti i file "log.txt"
	 */
	public static void main(String[] args) throws Exception {
		// Benchmark XPath extraction
		String logFilePath = null;
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
			
			case "-logdir":
				logFilePath = args[++i];
				final Path logDir = Paths.get(logFilePath);
				boolean usableLogDir = Files.isReadable(logDir);
				
				if (usableLogDir)
					break;
				else
					throw new NotDirectoryException("La Directory '" + logDir.toAbsolutePath() + "' non esiste oppure non è accessibile in lettura");
				
			default:
				throw new IllegalArgumentException("Parametro Sconosciuto " + args[i]);
			}
		}
		
		benchmarkXPathExtraction(directoryPath, logFilePath);

		// Generate JSON files
		//generateJsonFiles();
	}

	private static void benchmarkXPathExtraction(String directoryPath, String logFilePath) throws Exception {
		// Extract a random sample of files
		List<File> sampleFiles = FileUtil.getRandomSampleFromDirectory(directoryPath, SAMPLE_SIZE);
		
		Map<BaseXPathFinder, String> benchmark2log = new HashMap<>();
		
//		BaseXPathFinder dynamicXPathArticleId = new ArticleIdXPathFinder();
//		benchmark2log.put(dynamicXPathArticleId, logFilePath+"/logID.txt");
		
//		BaseXPathFinder dynamicXPathTitle = new TitleXPathFinder();
//		benchmark2log.put(dynamicXPathTitle, logFilePath+"/logTitle.txt");
		
//		BaseXPathFinder dynamicXPathKeywords = new KeywordsXPathFinder();
//		benchmark2log.put(dynamicXPathKeywords, logFilePath+"/logKeywords.txt");
		
//		BaseXPathFinder dynamicXPathTable = new TableXPathFinder();
//		benchmark2log.put(dynamicXPathTable, logFilePath+"/logTable.txt");
		
//		BaseXPathFinder dynamicXPathAbstract = new AbstractXPathFinder();
//		benchmark2log.put(dynamicXPathAbstract, logFilePath+"/logAbstract.txt");
		

		// For each file in the sample, find the best XPath expression and print the result
		for (BaseXPathFinder xPathFinder: benchmark2log.keySet()) {
			String specificLogPath = benchmark2log.get(xPathFinder);
			
			for (File file : sampleFiles) {
				logger.info(file.getName());
				Files.write(Paths.get(specificLogPath), String.format("File: %s\n", file.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
				// System.out.println("File selected: " + file.getName());
				String fileURI = file.toURI().toASCIIString();

				xPathFinder.findBestXPath(fileURI, logger, specificLogPath);

				Files.write(Paths.get(specificLogPath), String.format("\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
			}
			
			Files.write(Paths.get(specificLogPath), String.format("----------\nRisultati per %s\n", xPathFinder.toString()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
//				TableXPathFinder tf = (TableXPathFinder) xPathFinder;
//				Files.write(Paths.get(specificLogPath), String.format("Numero paragrafi che citano una tabella: %d, Numero citazioni riuscite (valutazione): %d\n", tf.getCitationsNumber() , tf.getPunteggioParagraphCitations()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
				
			Map<String, Integer> results = xPathFinder.getOrderedResults();
			for (Map.Entry<String, Integer> entry: results.entrySet()) {
				String expression = entry.getKey();
				Integer score = entry.getValue();
				Files.write(Paths.get(specificLogPath), String.format("XPath: %s -> Score = %d\n", expression, score).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
			}
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