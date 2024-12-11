package downloads;

import common.Pair;
import connection.models.PeerInformation;
import files.models.FileSearchResult;

import java.util.*;


public class DownloadTaskManager {

    private static DownloadTaskManager instance;
    public static int BLOCK_SIZE = 10240;
    public static int MAX_NUM_THREADS = 5;

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
        DownloadTask downloadTask = new DownloadTask(downloadInformation);
        new Thread(downloadTask).start();
    }

}
