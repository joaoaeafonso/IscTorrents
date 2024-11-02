package connection.messages;

import connection.models.MessageType;
import connection.models.PeerInformation;

public class FileBlockRequestMessage extends Message {

    private String fileHash;
    private long offset;
    private long length;


    public FileBlockRequestMessage(PeerInformation sender, MessageType type, String hash, long offset, long length) {
        super(sender, type);
        this.fileHash = hash;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
