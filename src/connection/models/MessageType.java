package connection.models;

public enum MessageType {
    CONNECTION_REQUEST,
    CONNECTION_ACKNOWLEDGE,
    WORD_SEARCH_MESSAGE_REQUEST,
    WORD_SEARCH_MESSAGE_RESPONSE;

    @Override
    public String toString() {
        return switch (this) {
            case CONNECTION_REQUEST -> "CONNECTION_REQUEST";
            case CONNECTION_ACKNOWLEDGE -> "CONNECTION_ACKNOWLEDGE";
            case WORD_SEARCH_MESSAGE_REQUEST -> "WORD_SEARCH_MESSAGE_REQUEST";
            case WORD_SEARCH_MESSAGE_RESPONSE -> "WORD_SEARCH_MESSAGE_RESPONSE";
        };
    }
}
