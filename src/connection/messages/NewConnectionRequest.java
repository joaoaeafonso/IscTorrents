package connection.messages;

import connection.models.MessageType;
import connection.models.PeerInformation;

public class NewConnectionRequest extends Message {

    public NewConnectionRequest(PeerInformation peerInformation, MessageType messageType){
        super(peerInformation, messageType);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
