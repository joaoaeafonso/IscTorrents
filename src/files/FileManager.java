package files;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.messages.WordSearchMessage;
import files.models.FileSearchResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FileManager {

    private static FileManager instance;
    private final File actuatingDirectory;

    private Map<String, File> filesInDirMap;

    private FileManager(File actuatingDirectory) {
        this.actuatingDirectory = actuatingDirectory;
        getAllFilesInActuatingDir();
    }

    public static synchronized  FileManager getInstance() {
        return instance;
    }

    public static synchronized  void createInstance(File dir){
        if( null == instance ){
            instance = new FileManager(dir);
        }
    }

    public synchronized byte[] getFileBlock(FileBlockRequestMessage message) {
        String fileHash = message.getFileHash();
        long offset = message.getOffset();
        int length = (int) message.getLength();
        byte[] data = new byte[length];

        File file = this.filesInDirMap.get(fileHash);

        try(FileInputStream fileInputStream = new FileInputStream(file)) {

            if (fileInputStream.skip(offset) != offset) {
                System.out.println("Error skipping until offset byte. Cannot proceed.");
                return null;
            }

            int bytesRead = 0;
            while (bytesRead < length) {
                int result = fileInputStream.read(data, bytesRead, length - bytesRead);
                if (result == -1) {
                    break;
                }
                bytesRead += result;
            }

            if (bytesRead < length) {
                byte[] adjustedData = new byte[bytesRead];
                System.arraycopy(data, 0, adjustedData, 0, bytesRead);
                data = adjustedData;
            }

            return data;

        } catch (IOException ex) {
            System.err.println("Exception occurred sendMessage. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }

        return null;
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
                        ConnectionManager.getInstance().getInformation()
                ));
            }
        }

        return resultList;
    }

    private String getFileHash(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesRead;

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

    private synchronized void getAllFilesInActuatingDir() {
        List<File> allFiles = readFiles();
        Map<String, File> pairMap = new HashMap<>();

        for(File file: allFiles){
            String hash = getFileHash(file);
            pairMap.put(hash, file);
        }

        this.filesInDirMap = pairMap;
    }

}
