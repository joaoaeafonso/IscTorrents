package connection.models;

import java.util.Objects;

public class PeerInformation {

    private final String ipAddress;
    private final String identifier;

    private final int port;

    public PeerInformation(String ipAddress, int port, String id) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.identifier = generateIdentifier(id, ipAddress, port);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getIdentifier() {
        return identifier;
    }

    private String generateIdentifier(String id, String ipAddress, int port) {
        String data = id + ipAddress + port;
        return Integer.toHexString(Objects.hash(data));
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", identifier='" + identifier + '\'' +
                '}';
    }

}
