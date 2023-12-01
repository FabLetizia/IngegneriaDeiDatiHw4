import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FileUtil {

    public static List<File> getRandomSampleFromDirectory(String directoryPath, int sampleSize) {
        List<File> allFiles = getAllFilesInDirectory(directoryPath);

        if (sampleSize >= allFiles.size()) {
            return allFiles; // Se il campione richiesto è maggiore o uguale al numero totale di file, restituisci tutti i file.
        }

        // Usa un seme casuale per ottenere risultati diversi ad ogni esecuzione
        long seed = System.nanoTime();
        Collections.shuffle(allFiles, new Random(seed));

        // Estrai il campione
        return allFiles.subList(0, sampleSize);
    }

    private static List<File> getAllFilesInDirectory(String directoryPath) {
        List<File> files = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            Collections.addAll(files, fileList);
        }
        return files;
    }
}