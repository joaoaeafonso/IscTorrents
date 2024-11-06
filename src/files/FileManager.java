package files;

import connection.messages.WordSearchMessage;
import files.models.FileSearchResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {

    public static int BLOCK_SIZE = 10240;

    private static FileManager instance;
    private final File actuatingDirectory;

    private FileManager(File actuatingDirectory) {
        this.actuatingDirectory = actuatingDirectory;
    }

    public static synchronized  FileManager getInstance() {
        return instance;
    }

    public static synchronized  void createInstance(File dir){
        if( null == instance ){
            instance = new FileManager(dir);
        }
    }

    public synchronized List<FileSearchResult> getAllRelevantFiles(String keyword, WordSearchMessage message){
        List<FileSearchResult> resultList = new ArrayList<>();
        List<File> allFiles = readFiles();

        if(keyword.isEmpty()) {
            return resultList;
        }

        for(File file: allFiles) {
            if( file.getName().toLowerCase().contains(keyword) ) {

                resultList.add(new FileSearchResult(
                        message,
                        getFileHash(file),
                        file.length(),
                        file.getName(),
                        message.getPeerInformation()
                ));
            }
        }

        if( resultList.isEmpty() ) {
            return null;
        }

        return resultList;
    }

    private String getFileHash(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesRead = -1;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
            fis.close();

            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            System.err.println("Exception occurred. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
            return "";
        }
    }

    private synchronized List<File> readFiles() {
        List<File> musicFileList = new ArrayList<>();

        File[] files = actuatingDirectory.listFiles((_, nome) ->
                nome.toLowerCase().endsWith(".mp3") ||
                        nome.toLowerCase().endsWith(".wav") ||
                        nome.toLowerCase().endsWith(".flac") ||
                        nome.toLowerCase().endsWith(".aac") ||
                        nome.toLowerCase().endsWith(".ogg") ||
                        nome.toLowerCase().endsWith(".mp4"));

        if (files != null) {
            musicFileList.addAll(Arrays.asList(files));
        }

        return musicFileList;
    }


}
