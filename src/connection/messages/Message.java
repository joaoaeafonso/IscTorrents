package connection.messages;

import connection.models.PeerInformation;

import java.io.Serializable;

public abstract class Message  implements Serializable {
    private final PeerInformation sender;

    public Message(PeerInformation sender) {
        this.sender = sender;
    }

    public PeerInformation getPeerInformation() {
        return sender;
    }


    public abstract void accept(MessageVisitor visitor);

    @Override
    public String toString() {
        return "From: "+this.sender.toString()+".";
    }

}
