package connection.messages;

import connection.models.MessageType;
import connection.models.PeerInformation;

public class WordSearchMessage extends Message {

    public WordSearchMessage(PeerInformation peerInformation, MessageType messageType){
        super(peerInformation, messageType);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
