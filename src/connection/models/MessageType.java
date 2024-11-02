package connection.models;

public enum MessageType {
    CONNECTION_REQUEST,
    CONNECTION_ACKNOWLEDGE,
    INFORMATION_REQUEST;

    @Override
    public String toString() {
        return switch (this) {
            case CONNECTION_REQUEST -> "CONNECTION_REQUEST";
            case CONNECTION_ACKNOWLEDGE -> "CONNECTION_ACKNOWLEDGE";
            case INFORMATION_REQUEST -> "INFORMATION_REQUEST";
        };
    }
}
