package connection.messages;

import connection.models.PeerInformation;

import java.io.Serializable;
import java.util.UUID;

public abstract class Message  implements Serializable {
    private final PeerInformation sender;
    private final String uniqueId;

    public Message(PeerInformation sender) {
        this.sender = sender;
        this.uniqueId = UUID.randomUUID().toString();
    }

    public PeerInformation getPeerInformation() {
        return sender;
    }

    public String getMessageId() {
        return this.uniqueId;
    }

    public abstract void accept(MessageVisitor visitor);

    @Override
    public String toString() {
        return "From: "+this.sender.toString()+".";
    }

}
