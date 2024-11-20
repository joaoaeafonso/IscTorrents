package connection.messages;

import connection.models.PeerInformation;

public class NewDisconnectionRequest extends Message {

    public NewDisconnectionRequest(PeerInformation peerInformation){
        super(peerInformation);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
