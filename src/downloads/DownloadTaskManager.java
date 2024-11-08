package downloads;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.models.PeerInformation;
import files.models.FileSearchResult;

import java.util.ArrayList;
import java.util.List;


public class DownloadTaskManager {

    private static DownloadTaskManager instance;
    public static int BLOCK_SIZE = 10240;

    private DownloadTaskManager() { }

    public static synchronized void createInstance() {
        if(null == instance){
            instance = new DownloadTaskManager();
        }
    }

    public static synchronized DownloadTaskManager getInstance(){
        return instance;
    }

    public synchronized void startDownloadRequest(Pair<FileSearchResult, List<PeerInformation>> downloadInformation) {
        List<FileBlockRequestMessage> fileBlockRequestMessages = createFileBlockRequestList(downloadInformation.getFirst());

        System.out.println("SIZE = "+fileBlockRequestMessages.size());
        //TODO
    }

    private List<FileBlockRequestMessage> createFileBlockRequestList(FileSearchResult fileSearchInformation) {
        List<FileBlockRequestMessage> blockRequestList = new ArrayList<>();

        long fileSize = fileSearchInformation.getFileSize();
        long offset = 0;

        while ( offset < fileSize ) {
            long length = Math.min(BLOCK_SIZE, fileSize - offset);

            FileBlockRequestMessage blockRequestMessage = new FileBlockRequestMessage(
                    ConnectionManager.getInstance().getInformation(),
                    fileSearchInformation.getHash(),
                    offset,
                    length);

            blockRequestList.add(blockRequestMessage);
            offset += length;
        }

        return blockRequestList;
    }

}
