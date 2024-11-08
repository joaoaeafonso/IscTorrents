package connection.messages;

import connection.models.PeerInformation;

public class NewConnectionResponse extends Message {

    public NewConnectionResponse(PeerInformation peerInformation){
        super(peerInformation);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
