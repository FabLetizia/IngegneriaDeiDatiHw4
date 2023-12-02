import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        // Directory contenente i file XML
        String directoryPath = "E:/Riccardo/Università/Anno V/I SEMESTRE/Ingegneria dei Dati - 6 CFU/Homework/HW4/hw4-privato/docs";

        // Numero di file da selezionare casualmente
        int sampleSize = 50; // Puoi modificare questo valore in base alle tue esigenze

        // Estrai casualmente un campione di file
        List<File> sampleFiles = FileUtil.getRandomSampleFromDirectory(directoryPath, sampleSize);


        // Per ogni file nel campione, trova la migliore espressione XPath e stampa il risultato
        for (File file : sampleFiles) {
            System.out.println("File selezionato: " + file.getName());

            // Usa la classe DynamicXPath per trovare la migliore espressione XPath per poi estrarre il valore
            String bestXPath = DynamicXPath.findBestXPath(file.toURI(), "pmc");
            System.out.println("Migliore XPath: " + bestXPath);
            System.out.println("Valore estratto: " + DynamicXPath.extractValue(file.toURI(), bestXPath));
            System.out.println();
        }
    }
}