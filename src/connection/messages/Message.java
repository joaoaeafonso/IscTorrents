package connection.messages;

import connection.models.MessageType;
import connection.models.PeerInformation;

import java.io.Serializable;

public abstract class Message  implements Serializable {
    private final PeerInformation sender;
    private final MessageType type;

    public Message(PeerInformation sender, MessageType type) {
        this.sender = sender;
        this.type = type;
    }

    public PeerInformation getPeerInformation() {
        return sender;
    }

    public MessageType getMessageType() {
        return type;
    }

    public abstract void accept(MessageVisitor visitor);

    @Override
    public String toString() {
        return "From: "+this.sender.toString() + " of Type: " + this.type.toString()+".";
    }

}
