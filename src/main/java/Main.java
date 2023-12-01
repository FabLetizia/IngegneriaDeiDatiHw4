import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        // Directory contenente i file XML
        String directoryPath = "/Users/alessandropesare/xml_files";

        // Numero di file da selezionare casualmente
        int sampleSize = 10; // Puoi modificare questo valore in base alle tue esigenze

        // Estrai casualmente un campione di file
        List<File> sampleFiles = FileUtil.getRandomSampleFromDirectory(directoryPath, sampleSize);


        // Per ogni file nel campione, trova la migliore espressione XPath e stampa il risultato
        for (File file : sampleFiles) {
            System.out.println("File selezionato: " + file.getName());

            // Usa la classe DynamicXPath per trovare la migliore espressione XPath per poi estrarre il valore
            String bestXPath = DynamicXPath.findBestXPath(file.getAbsolutePath(), "pmc");
            System.out.println("Migliore XPath: " + bestXPath);
            System.out.println("Valore estratto: " + DynamicXPath.extractValue(file.getAbsolutePath(), bestXPath));
            System.out.println();
        }
    }
}