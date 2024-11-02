package connection;

import connection.messages.Message;
import connection.messages.MessageVisitor;
import connection.messages.NewConnectionRequest;
import connection.messages.WordSearchMessage;
import connection.models.MessageType;
import connection.models.PeerInformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConnectionManager implements MessageVisitor {

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

    public static synchronized  ConnectionManager getInstance() {
        return instance;
    }

    public static synchronized  void createInstance(String ipAddress, int port) {
        if( null == instance ) {
            instance = new ConnectionManager(ipAddress, port);
        }
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(this.port);
            System.out.println(this.information.toString());

            while( !Thread.interrupted() ) {
                System.out.println("Waiting for connections...");
                Socket socket = serverSocket.accept();
                new Thread(() -> listenForMessages(socket)).start();
            }

        } catch (IOException ex) {
            System.err.println("Exception occurred. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
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
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception occurred. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }
    }

    private String generateIdentifier(String ipAddress, int port) {
        String data = ipAddress + port;
        return Integer.toHexString(Objects.hash(data));
    }

    public void sendMessage(PeerInformation peerInfo, Message message) {
        try (Socket socket = new Socket(peerInfo.getIpAddress(), peerInfo.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(message);
            out.flush();
        } catch (IOException ex) {
            System.err.println("Exception occurred. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }
    }

    @Override
    public void visit(NewConnectionRequest message) {
        if(connectedPeers.contains(message.getPeerInformation())) {
            System.out.println(message.getPeerInformation()+" is already present in the connections.");
            return;
        }

        connectedPeers.add(message.getPeerInformation());

        NewConnectionRequest ackMessage = new NewConnectionRequest(this.information, MessageType.CONNECTION_ACKNOWLEDGE);
        System.out.println("Sending message "+ ackMessage);
        sendMessage(message.getPeerInformation(), ackMessage);
    }

    @Override
    public void visit(WordSearchMessage message) {
        //TODO
    }
}
