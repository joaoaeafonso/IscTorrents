package connection;

import connection.messages.Message;
import connection.messages.MessageVisitor;
import connection.messages.NewConnectionRequest;
import connection.models.PeerInformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager implements MessageVisitor {

    PeerInformation information;

    private final int port;

    private ServerSocket serverSocket;
    private List<NewConnectionRequest> connectedPeers = new ArrayList<>();

    public ConnectionManager(String ipAddress, int port, String id) {
        this.information = new PeerInformation(ipAddress, port, id);
        this.port = port;
    }

    public void receiveMessage(Message message) {
        message.accept(this);
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

    public void sendMessage(PeerInformation peerInfo, Message message) {
        try (Socket socket = new Socket(peerInfo.getIpAddress(), peerInfo.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(message);
            out.flush();
            System.out.println("Message sent to");
        } catch (IOException ex) {
            System.err.println("Exception occurred. Cause: "+ex.getCause()+", Message: "+ex.getMessage());
        }
    }

    @Override
    public void visit(NewConnectionRequest message) {
        //TODO
    }
}
