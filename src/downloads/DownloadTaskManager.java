package downloads;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.messages.FileBlockRequestMessageResponse;
import connection.models.PeerInformation;
import files.models.FileSearchResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class DownloadTaskManager {

    private static DownloadTaskManager instance;
    public static int BLOCK_SIZE = 10240;
    public static int MAX_NUM_THREADS = 5;

    private CountDownLatch latch;
    private final List<FileBlockRequestMessageResponse> responses = new CopyOnWriteArrayList<>();

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
        int totalFileBlockMessages = fileBlockRequestMessages.size();
        HashMap<PeerInformation, List<FileBlockRequestMessage>> downloadInfo =
                handleFileBlockDistributionPerPeer(fileBlockRequestMessages, downloadInformation.getSecond());
        sendFileBlockMessage(downloadInfo, totalFileBlockMessages);
    }

    private synchronized void sendFileBlockMessage(
            HashMap<PeerInformation, List<FileBlockRequestMessage>> requestInformation,
            int totalFileBlocks
    ){
        this.latch = new CountDownLatch(totalFileBlocks);
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUM_THREADS);

        Instant start = Instant.now();
        for(Map.Entry<PeerInformation, List<FileBlockRequestMessage>> entry: requestInformation.entrySet() ) {
            for(FileBlockRequestMessage message: entry.getValue()) {
                executor.submit(() -> ConnectionManager.getInstance().queueMessage(entry.getKey(), message));
            }
        }

        try {
            if(this.latch.await(90, TimeUnit.SECONDS)) {
                System.out.println("Received all responses.");

                //TODO(joaoaeafonso): add mechanism to put file back together and store it in memory

                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                System.out.println("TEMPO DO DOWNLOAD -> "+timeElapsed);
            } else {
                System.out.println("Did not receive all the responses. Timeout occurred. Cannot save file.");
            }

            responses.clear();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    public void addResponse(FileBlockRequestMessageResponse fileBlock) {
        responses.add(fileBlock);
        this.latch.countDown();
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
