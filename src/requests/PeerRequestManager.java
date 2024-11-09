package requests;

import common.Pair;
import connection.ConnectionManager;
import connection.messages.*;
import connection.models.PeerInformation;
import downloads.DownloadTaskManager;
import files.FileManager;
import files.models.FileSearchResult;
import gui.Gui;

import java.util.List;
import java.util.concurrent.*;

public class PeerRequestManager {

    private static PeerRequestManager instance;
    private final List<FileSearchResult> responses = new CopyOnWriteArrayList<>();

    private CountDownLatch latch;

    private PeerRequestManager() { }

    public static synchronized void createInstance() {
        if(null == instance){
            instance = new PeerRequestManager();
        }
    }

    public static synchronized PeerRequestManager getInstance(){
        return instance;
    }

    public void peerFileBlockRequest(FileBlockRequestMessage message) {
        byte[] fileBlock = FileManager.getInstance().getFileBlock(message);

        FileBlockRequestMessageResponse response = new FileBlockRequestMessageResponse(
                ConnectionManager.getInstance().getInformation(),
                message.getFileHash(),
                message.getOffset(),
                fileBlock
        );

        ConnectionManager.getInstance().queueMessage(message.getPeerInformation(), response);
    }

    public void peerFileBlockResponse(FileBlockRequestMessageResponse message){
        DownloadTaskManager.getInstance().addResponse(message);
    }

    public void peerDownloadRequest(Pair<FileSearchResult, List<PeerInformation>> downloadInformation) {
        DownloadTaskManager.getInstance().startDownloadRequest(downloadInformation);
    }

    public void peerConnectionRequest(PeerInformation peer) {
        ConnectionManager instance = ConnectionManager.getInstance();
        NewConnectionRequest request = new NewConnectionRequest(instance.getInformation());
        instance.queueMessage(peer, request);
    }

    public void peerConnectionRequestResponse(PeerInformation peer) {
        ConnectionManager instance = ConnectionManager.getInstance();
        if(instance.getAllConnectedPeers().contains(peer)) {
            System.out.println(peer+" is already present in the connections.");
            return;
        }

        instance.addPeerToConnectedPeerList(peer);
        System.out.println("Peer "+peer+" added to Connections list.");

        NewConnectionResponse newRequest = new NewConnectionResponse(instance.getInformation());
        instance.queueMessage(peer, newRequest);
    }

    public void peerConnectionResponseAcknowledge(PeerInformation peer) {
        ConnectionManager instance = ConnectionManager.getInstance();
        instance.addPeerToConnectedPeerList(peer);
        System.out.println("Connection established between "+instance.getInformation().toString()+" and "+peer+".");
    }

    public void peerFileSearchRequest(WordSearchMessage message) {
        List<FileSearchResult> allRelevantFiles = FileManager.getInstance().getAllRelevantFiles(message.getKeyword(), message);

        WordSearchMessageResponse answer = new WordSearchMessageResponse(
                ConnectionManager.getInstance().getInformation(),
                message.getKeyword(),
                allRelevantFiles
        );

        ConnectionManager.getInstance().queueMessage(message.getPeerInformation(), answer);
    }

    public synchronized void peerFileWordSearchRequest(List<PeerInformation> peers, String keyword) {
        this.latch = new CountDownLatch(peers.size());
        ExecutorService executor = Executors.newFixedThreadPool(peers.size());

        for(PeerInformation peer: peers) {
            executor.submit(() -> {
                WordSearchMessage request = new WordSearchMessage(
                        ConnectionManager.getInstance().getInformation(),
                        keyword,
                        null
                );
                ConnectionManager.getInstance().queueMessage(peer, request);
            });
        }

        try {
            if(this.latch.await(30, TimeUnit.SECONDS)){
                System.out.println("Received all responses.");
            } else {
                System.out.println("Did not receive all the responses. Timeout occurred.");
            }

            Gui.getInstance().updateResultList(responses);
            responses.clear();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    public void addResponse(List<FileSearchResult> results) {
        responses.addAll(results);
        this.latch.countDown();
    }
}
