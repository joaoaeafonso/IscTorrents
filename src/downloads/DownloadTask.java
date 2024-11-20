package downloads;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.FileBlockRequestMessage;
import connection.messages.FileBlockRequestMessageResponse;
import connection.models.PeerInformation;
import files.FileManager;
import files.models.FileSearchResult;
import gui.Gui;
import requests.PeerRequestManager;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static downloads.DownloadTaskManager.BLOCK_SIZE;
import static downloads.DownloadTaskManager.MAX_NUM_THREADS;

public class DownloadTask implements Runnable {

    private final String downloadId;
    private final Pair<FileSearchResult, List<PeerInformation>> downloadInformation;
    private final BlockingQueue<FileBlockRequestMessage> pendingRequestsQueue = new LinkedBlockingQueue<>();
    private final List<FileBlockRequestMessageResponse> responses = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<PeerInformation, AtomicInteger> downloadInformationMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<FileBlockRequestMessageResponse>> responseFutures = new ConcurrentHashMap<>();
    private CountDownLatch latch;

    public DownloadTask(Pair<FileSearchResult, List<PeerInformation>> downloadInformation) {
        this.downloadId = UUID.randomUUID().toString();
        this.downloadInformation = downloadInformation;
    }

    @Override
    public void run() {
        List<FileBlockRequestMessage> fileBlockRequestMessages = createFileBlockRequestList(downloadInformation.getFirst());
        this.latch = new CountDownLatch(fileBlockRequestMessages.size());
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(downloadInformation.getSecond().size(), MAX_NUM_THREADS));

        for (PeerInformation peer : downloadInformation.getSecond()) {
            downloadInformationMap.put(peer, new AtomicInteger(0));
        }

        PeerRequestManager.getInstance().registerDownloadTask(downloadId, this);

        Instant start = Instant.now();
        pendingRequestsQueue.addAll(fileBlockRequestMessages);

        for (PeerInformation peer : downloadInformation.getSecond()) {
            executor.submit(() -> processMessagesForPeer(peer));
        }

        try {
            if (latch.await(90, TimeUnit.SECONDS)) {
                System.out.println("Received all responses.");
                FileManager.getInstance().assembleReceivedFile(responses, downloadInformation.getFirst().getFileName());

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
            PeerRequestManager.getInstance().unregisterDownloadTask(downloadId);
            FileManager.getInstance().updateAvailableFilesInDirectory();
        }
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
                    fileSearchInformation.getFileName(),
                    this.downloadId);

            blockRequestList.add(blockRequestMessage);
            offset += length;
        }

        return blockRequestList;
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
}
