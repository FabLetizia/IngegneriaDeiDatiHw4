package xPathEvaluators;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dataExtractions.Figure;
import dataExtractions.Keywords;
import dataExtractions.Table;

public class Main {
	private static final int SAMPLE_SIZE = 100;
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/* Lanciare il Programma passando i seguenti parametri al main(): 
	 * [-xmlfiles XML_DIRECTORY] -> Path della Directory in cui ci sono i documenti .xml e i "parsing files"
	 * [-logdir LOG_DIRECTORY] -> Path della Directory in cui vengono scritti i file "log.txt"
	 * [-jsondir JSON_DIRECTORY] -> Path della Directory in cui vengono inseriti i file ".json" con le informazioni estratte
	 */
	public static void main(String[] args) throws Exception {
		// Benchmark XPath extraction
		String logFilePath = null;
		String directoryPath = null;
		String jsonPath = null;

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

			case "-jsondir":
				jsonPath = args[++i];
				final Path jsonDir = Paths.get(jsonPath);
				boolean usableJsonDir = Files.isReadable(jsonDir);

				if (usableJsonDir)
					break;
				else
					throw new NotDirectoryException("La Directory '" + jsonDir.toAbsolutePath() + "' non esiste oppure non è accessibile in lettura");

			default:
				throw new IllegalArgumentException("Parametro Sconosciuto " + args[i]);
			}
		}

		benchmarkXPathExtraction(directoryPath, logFilePath);

		// Generate JSON files
		generateJsonFiles(directoryPath, jsonPath);
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
	private static void generateJsonFiles(String directoryPath,String jsonPath) throws Exception{
		// definiamo tutte le Xpath dinamiche che abbiamo testato in precedenza e utilizziamole per l'estrazione
		BaseXPathFinder dynamicXPathArticleID = new ArticleIdXPathFinder("//article-meta/article-id[@pub-id-type='pmc']");
		BaseXPathFinder dynamicXPathTitle = new TitleXPathFinder("//title-group/article-title");
		BaseXPathFinder dynamicXPathAbstract = new AbstractXPathFinder("//abstract");
		Keywords keywordsExtractor = new Keywords("//kwd");
		Table tableExtractor = new Table();
		Figure figureExtractor = new Figure();

		// Get a list of all XML files in the directory
		List<File> allXmlFiles = FileUtil.getAllXMLFilesInDirectory(directoryPath);

		int numThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

		// Process one XML file at a time and generate the corresponding JSON file
		if (allXmlFiles != null) {
			allXmlFiles.stream().forEach(xmlFile -> {
				executorService.submit(() -> {
					File currentJson = new File(jsonPath + "/" + xmlFile.getName().replaceAll(".xml", ".json"));
					if (currentJson.exists()) {
						System.out.println("Skipping " + xmlFile.getName() + ", his JSON has already been created");
						return;
					}
					try {
						// Use dynamicXPath or another suitable XPathFinder to extract structured information
						// For simplicity, let's assume a method extractStructuredInfo() is available
						Map<String, Object> jsonMap = new LinkedHashMap<>();
						Map<String, Object> contentMap = new LinkedHashMap<>();
						
						String structuredInfoArticleId = dynamicXPathArticleID.extractValue(xmlFile.toURI().toASCIIString(),dynamicXPathArticleID.getBestXPath());
						jsonMap.put("pmcid", structuredInfoArticleId);
						
						String structuredInfoTitle = dynamicXPathTitle.extractValue(xmlFile.toURI().toASCIIString(),dynamicXPathTitle.getBestXPath());
						contentMap.put("title", structuredInfoTitle);
						String structuredInfoAbstract = dynamicXPathAbstract.extractValue(xmlFile.toURI().toASCIIString(),dynamicXPathAbstract.getBestXPath());
						contentMap.put("abstract", structuredInfoAbstract);
						
						List<String> keywords = keywordsExtractor.extractKeywords(xmlFile.toURI().toASCIIString());
						contentMap.put("keywords", keywords);
						
						List<Map<String, Object>> tables = new ArrayList<>();
						
						NodeList tableIDs = tableExtractor.extractIDs(xmlFile.toURI().toASCIIString());
						
						for (int i = 0; i < tableIDs.getLength(); i++) {
							Map<String, Object> tableObject = new LinkedHashMap<>();
							
							tableObject.put("table_id", tableIDs.item(i).getTextContent());
							tableObject.put("body", tableExtractor.extractBody(xmlFile.toURI().toASCIIString(), tableIDs.item(i).getTextContent()));
							tableObject.put("caption", tableExtractor.extractCaption(xmlFile.toURI().toASCIIString(), tableIDs.item(i).getTextContent()));
							tableObject.put("caption_citations", tableExtractor.extractCaptionCitations(xmlFile.toURI().toASCIIString(), tableIDs.item(i).getTextContent()));
							tableObject.put("foots", tableExtractor.extractFoots(xmlFile.toURI().toASCIIString(), tableIDs.item(i).getTextContent()));
							tableObject.put("paragraphs", tableExtractor.extractParagraphs(xmlFile.toURI().toASCIIString(), tableIDs.item(i).getTextContent()));
							tableObject.put("cells", tableExtractor.extractCells(xmlFile.toURI().toASCIIString(), tableIDs.item(i).getTextContent()));
							
							tables.add(tableObject);
						}
						
						contentMap.put("tables", tables);
						
						List<Map<String, Object>> figures = new ArrayList<>();
						
						NodeList figureIDs = figureExtractor.extractIDs(xmlFile.toURI().toASCIIString());
						
						for(int i = 0; i<figureIDs.getLength(); i++){
							Map<String, Object> figureObject = new LinkedHashMap<>();
			
							figureObject.put("fig_id", figureIDs.item(i).getTextContent());
							figureObject.put("src", figureExtractor.extractSources(xmlFile.toURI().toASCIIString(), figureIDs.item(i).getTextContent(), xmlFile.getName()));
							figureObject.put("caption", figureExtractor.extractCaptions(xmlFile.toURI().toASCIIString(),figureIDs.item(i).getTextContent()));
							figureObject.put("caption_citations", figureExtractor.extractCaptionCitations(xmlFile.toURI().toASCIIString(), figureIDs.item(i).getTextContent()));
							figureObject.put("paragraphs", figureExtractor.extractParagraphs(xmlFile.toURI().toASCIIString(), figureIDs.item(i).getTextContent()));
							
							figures.add(figureObject);
						}
						
						contentMap.put("figures", figures);
						
						jsonMap.put("content", contentMap);

						String filePath = jsonPath;
						String filename = xmlFile.getName();
						filename = filename.replace(".xml",".json");
						filename = filename.replace("PMC", "pmcid_");
						
						ObjectMapper mapper = new ObjectMapper();
						mapper.enable(SerializationFeature.INDENT_OUTPUT);
						
						try {
							mapper.writeValue(new File(filePath + "/" + filename), jsonMap);
							System.out.println("Generated JSON for: " + xmlFile.getName());
						}
						catch (Exception e) {
							e.printStackTrace();
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			});
		}
	}
}