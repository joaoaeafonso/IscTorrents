package connection;

import common.Pair;
import connection.messages.*;
import connection.models.PeerInformation;
import downloads.DownloadTaskManager;
import requests.PeerRequestManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static utils.TorrentsUtils.generateIdentifier;

public class ConnectionManager extends Thread implements MessageVisitor {

    PeerInformation information;

    private final int port;

    private ServerSocket serverSocket;
    private final Map<String, PeerInformation> connectedPeers = new HashMap<>();

    private final BlockingQueue<Pair<PeerInformation, Message>> messageQueue;
    private final ScheduledExecutorService scheduler;

    private static ConnectionManager instance;

    private ConnectionManager(String ipAddress, int port) {
        String id = generateIdentifier(ipAddress, port);
        this.information = new PeerInformation(ipAddress, port, id);
        this.port = port;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(DownloadTaskManager.MAX_NUM_THREADS);

        startSendingMessages();
    }

    @Override
    public void run() {
        startServer();
    }

    public static synchronized  ConnectionManager getInstance() {
        return instance;
    }

    public static synchronized  void createInstance(String ipAddress, int port) {
        if( null == instance ) {
            instance = new ConnectionManager(ipAddress, port);
        }
    }

    public synchronized PeerInformation getInformation(){
        return this.information;
    }

    public synchronized Map<String, PeerInformation> getAllConnectedPeers(){
        return this.connectedPeers;
    }

    public synchronized void addPeerToConnectedPeerList(PeerInformation peer){
        this.connectedPeers.put(peer.getIdentifier(), peer);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(this.port);
            System.out.println(this.information.toString());

            while( !Thread.interrupted() ) {
                System.out.println("Waiting for connections...");
                Socket socket = serverSocket.accept();
                new Thread(() -> listenForMessages(socket)).start();
            }

        } catch (IOException ex) {
            System.err.println("Exception occurred startServer. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }
    }

    private void receiveMessage(Message message) {
        message.accept(this);
    }

    private void listenForMessages(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Message message = (Message) in.readObject();
                receiveMessage(message);
            }
        } catch (EOFException _) {

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception occurred listenForMessages. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }
    }

    private void startSendingMessages() {
        scheduler.scheduleAtFixedRate( () -> {
            try {
                Pair<PeerInformation, Message> pair = messageQueue.poll();
                if (pair != null) {
                    sendMessage(pair.getFirst(), pair.getSecond());
                }
            } catch (Exception ex) {
                System.err.println("Exception occurred listenForMessages. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
            }
        },  0, 10, TimeUnit.MILLISECONDS );
    }

    public void queueMessage(PeerInformation peerInformation, Message message) {
        if(messageQueue.offer(new Pair<>(peerInformation, message))) {
            System.out.println("Message "+message+" successfully added to queue.");
        } else {
            System.err.println("Error adding message "+message+" to queue.");
        }
    }

    private void sendMessage(PeerInformation peerInfo, Message message) {
        try (Socket socket = new Socket(peerInfo.getIpAddress(), peerInfo.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(message);
            out.flush();
        } catch (IOException ex) {
            System.err.println("Exception occurred sendMessage. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }
    }

    @Override
    public void visit(NewConnectionRequest message) {
        PeerRequestManager.getInstance().peerConnectionRequestResponse(message.getPeerInformation());
    }

    @Override
    public void visit(NewConnectionResponse message) {
        PeerRequestManager.getInstance().peerConnectionResponseAcknowledge(message.getPeerInformation());
    }

    @Override
    public void visit(NewDisconnectionRequest message) {
        System.out.println("Received a disconnect request from Peer = "+message.getPeerInformation());

        if(this.connectedPeers.get(message.getPeerInformation().getIdentifier()) != null) {
            this.connectedPeers.remove(message.getPeerInformation().getIdentifier());
        }
    }

    @Override
    public void visit(WordSearchMessage message) {
        if(!isValidSender(message.getPeerInformation())){
            return;
        }
        PeerRequestManager.getInstance().peerFileSearchRequest(message);
    }

    @Override
    public void visit(WordSearchMessageResponse message) {
        if(!isValidSender(message.getPeerInformation())){
            return;
        }
        PeerRequestManager.getInstance().addResponse(message.getResultList());
    }

    @Override
    public void visit(FileBlockRequestMessage message) {
        if(!isValidSender(message.getPeerInformation())){
            return;
        }
        PeerRequestManager.getInstance().peerFileBlockRequest(message);
    }

    @Override
    public void visit(FileBlockRequestMessageResponse message) {
        if(!isValidSender(message.getPeerInformation())){
            return;
        }
        PeerRequestManager.getInstance().peerFileBlockResponse(message);
    }

    private synchronized boolean isValidSender(PeerInformation peerInformation){
        if(!connectedPeers.containsKey(peerInformation.getIdentifier())){
            System.err.println("Message received from Peer ["+peerInformation+"]. " +
                    "Peers is not part of the established connections. Ignoring message.");
            return false;
        }
        return true;
    }
}
