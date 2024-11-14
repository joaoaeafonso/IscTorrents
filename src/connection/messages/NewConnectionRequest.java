package connection.messages;

import connection.models.PeerInformation;

public class NewConnectionRequest extends Message {

    public NewConnectionRequest(PeerInformation peerInformation){
        super(peerInformation);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
