package connection;

import connection.messages.*;
import connection.models.PeerInformation;
import requests.PeerRequestManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static utils.TorrentsUtils.generateIdentifier;

public class ConnectionManager extends Thread implements MessageVisitor {

    PeerInformation information;

    private final int port;

    private ServerSocket serverSocket;
    private final List<PeerInformation> connectedPeers = new ArrayList<>();

    private static ConnectionManager instance;

    private ConnectionManager(String ipAddress, int port) {
        String id = generateIdentifier(ipAddress, port);
        this.information = new PeerInformation(ipAddress, port, id);
        this.port = port;
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

    public synchronized List<PeerInformation> getAllConnectedPeers(){
        return this.connectedPeers;
    }

    public synchronized void addPeerToConnectedPeerList(PeerInformation peer){
        this.connectedPeers.add(peer);
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

    public void sendMessage(PeerInformation peerInfo, Message message) {
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
    public void visit(WordSearchMessage message) {
        PeerRequestManager.getInstance().peerFileSearchRequest(message);
    }

    @Override
    public void visit(WordSearchMessageResponse message) {
        PeerRequestManager.getInstance().addResponse(message.getResultList());
    }

    @Override
    public void visit(FileBlockRequestMessage message) {
        //TODO
    }

    @Override
    public void visit(FileBlockRequestMessageResponse message) {
        //TODO
    }

}
