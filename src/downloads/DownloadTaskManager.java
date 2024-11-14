package downloads;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.messages.FileBlockRequestMessageResponse;
import connection.models.PeerInformation;
import files.FileManager;
import files.models.FileSearchResult;
import gui.Gui;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class DownloadTaskManager {

    private static DownloadTaskManager instance;
    public static int BLOCK_SIZE = 10240;
    public static int MAX_NUM_THREADS = 5;

    private CountDownLatch latch;
    private final List<FileBlockRequestMessageResponse> responses = Collections.synchronizedList(new ArrayList<>());

    private final BlockingQueue<FileBlockRequestMessage> pendingRequestsQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, CompletableFuture<FileBlockRequestMessageResponse>> responseFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PeerInformation, AtomicInteger> downloadInformationMap = new ConcurrentHashMap<>();

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
        sendFileBlockMessage(fileBlockRequestMessages, downloadInformation.getSecond(), downloadInformation.getFirst().getFileName());
    }

    private void sendFileBlockMessage(
            List<FileBlockRequestMessage> fileBlockRequestMessageList,
            List<PeerInformation> peerInformationList,
            String fileName) {
        this.latch = new CountDownLatch(fileBlockRequestMessageList.size());
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(peerInformationList.size(), MAX_NUM_THREADS));

        for (PeerInformation peer : peerInformationList) {
            downloadInformationMap.put(peer, new AtomicInteger(0));
        }

        Instant start = Instant.now();
        this.pendingRequestsQueue.addAll(fileBlockRequestMessageList);

        for(PeerInformation peer: peerInformationList) {
            executor.submit( () -> processMessagesForPeer(peer) );
        }

        try {
            if(this.latch.await(90, TimeUnit.SECONDS)) {
                System.out.println("Received all responses.");

                FileManager.getInstance().assembleReceivedFile(responses, fileName);

                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                Gui.getInstance().handleDownloadFinished(downloadInformationMap, timeElapsed.getSeconds());
            } else {
                System.out.println("Did not receive all the responses. Timeout occurred. Cannot save file.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            responses.clear();
            downloadInformationMap.clear();
            pendingRequestsQueue.clear();
            responseFutures.clear();
            FileManager.getInstance().updateAvailableFilesInDirectory();
        }
    }

    private void processMessagesForPeer(PeerInformation peer) {
        try {
            while(!Thread.interrupted()) {
                FileBlockRequestMessage message = pendingRequestsQueue.take();

                downloadInformationMap.get(peer).incrementAndGet();

                CompletableFuture<FileBlockRequestMessageResponse> future = new CompletableFuture<>();
                responseFutures.put(message.getMessageId(), future);

                ConnectionManager.getInstance().queueMessage(peer, message);

                try {

                    FileBlockRequestMessageResponse response = future.get(30, TimeUnit.SECONDS);

                    synchronized (responses) {
                        responses.add(response);
                    }

                    this.latch.countDown();

                } catch (ExecutionException | TimeoutException ex) {
                    // Makes sense to retry the message sending ?
                } finally {
                    responseFutures.remove(message.getMessageId());
                }
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void addResponse(FileBlockRequestMessageResponse fileBlockResponse) {
        String messageID = fileBlockResponse.getCorrespondingRequestMessageId();

        CompletableFuture<FileBlockRequestMessageResponse> future = responseFutures.get(messageID);
        if (future != null) {
            future.complete(fileBlockResponse);
        } else {
            System.out.println("No pending request found for message ID: " + messageID);
        }
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
                    length,
                    fileSearchInformation.getFileName());

            blockRequestList.add(blockRequestMessage);
            offset += length;
        }

        return blockRequestList;
    }

}
