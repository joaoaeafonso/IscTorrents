import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<File> readFiles(File dir) {
        List<File> musicFileList = new ArrayList<>();

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((_, nome) ->
                    nome.toLowerCase().endsWith(".mp3") ||
                    nome.toLowerCase().endsWith(".wav") ||
                    nome.toLowerCase().endsWith(".flac") ||
                    nome.toLowerCase().endsWith(".aac") ||
                    nome.toLowerCase().endsWith(".ogg") ||
                    nome.toLowerCase().endsWith(".mp4"));

            if (files != null) {
                musicFileList.addAll(Arrays.asList(files));
            }
        } else {
            System.err.println("A diretoria especificada não é válida.");
        }

        return musicFileList;
    }

}
