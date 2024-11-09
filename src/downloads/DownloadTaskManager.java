package downloads;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.models.PeerInformation;
import files.models.FileSearchResult;

import java.util.ArrayList;
import java.util.HashMap;
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
        HashMap<PeerInformation, List<FileBlockRequestMessage>> downloadInfo = handleFileBlockDistributionPerPeer(fileBlockRequestMessages, downloadInformation.getSecond());

        //TODO
    }

    private HashMap<PeerInformation, List<FileBlockRequestMessage>> handleFileBlockDistributionPerPeer(List<FileBlockRequestMessage> fileBlockRequestMessages, List<PeerInformation> peers) {
        HashMap<PeerInformation, List<FileBlockRequestMessage>> downloadInformation = new HashMap<>();
        int totalBlocks = fileBlockRequestMessages.size();
        int totalPeers = peers.size();

        if(totalBlocks == 0 || totalPeers == 0) {
            System.out.println("Invalid number of blocks or Peers. Won't proceed with download request.");
            return downloadInformation;
        }

        int blocksPerPeer = totalBlocks / totalPeers;
        int remainingBlocks = totalBlocks % totalPeers;
        int currentIndex = 0;

        for(PeerInformation peer: peers) {
            int blocksToAssign = blocksPerPeer + (remainingBlocks > 0 ? 1 : 0);
            if(remainingBlocks > 0) {
                remainingBlocks--;
            }

            List<FileBlockRequestMessage> blocksForPeer = fileBlockRequestMessages.subList(currentIndex, currentIndex + blocksToAssign);
            currentIndex += blocksToAssign;

            downloadInformation.put(peer, blocksForPeer);
        }

        return downloadInformation;
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
