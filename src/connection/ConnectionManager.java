package connection;

import connection.messages.*;
import connection.models.MessageType;
import connection.models.PeerInformation;
import files.FileManager;
import files.models.FileSearchResult;
import gui.Gui;

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
        if(connectedPeers.contains(message.getPeerInformation())) {
            System.out.println(message.getPeerInformation()+" is already present in the connections.");
            return;
        }

        if( MessageType.CONNECTION_ACKNOWLEDGE == message.getMessageType() ) {
            connectedPeers.add(message.getPeerInformation());
            System.out.println("Connection established between "+this.information.toString()+" and "+message.getPeerInformation().toString()+".");
            return;
        }

        connectedPeers.add(message.getPeerInformation());
        System.out.println("Peer "+message.getPeerInformation().toString()+" added to Connections list.");

        NewConnectionRequest newRequest = new NewConnectionRequest(this.getInformation(), MessageType.CONNECTION_ACKNOWLEDGE);
        sendMessage(message.getPeerInformation(), newRequest);
    }

    @Override
    public void visit(WordSearchMessage message) {
        if( MessageType.WORD_SEARCH_MESSAGE_REQUEST == message.getMessageType() ) {
            List<FileSearchResult> allRelevantFiles = FileManager.getInstance().getAllRelevantFiles(message.getKeyword(), message);

            WordSearchMessage answer = new WordSearchMessage(this.information, MessageType.WORD_SEARCH_MESSAGE_RESPONSE, message.getKeyword(), allRelevantFiles);
            ConnectionManager.getInstance().sendMessage(message.getPeerInformation(), answer);
            return;
        }

        Gui.getInstance().updateResultList(message.getResultList());
    }

    @Override
    public void visit(FileBlockRequestMessage message) {
        //TODO
    }

}
