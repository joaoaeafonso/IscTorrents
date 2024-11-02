package connection.models;

import java.io.Serializable;

public class PeerInformation  implements Serializable {

    private final String ipAddress;
    private final String identifier;

    private final int port;

    public PeerInformation(String ipAddress, int port, String id) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.identifier = id;
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

    @Override
    public String toString() {
        return "PeerInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", identifier='" + identifier + '\'' +
                '}';
    }

}
