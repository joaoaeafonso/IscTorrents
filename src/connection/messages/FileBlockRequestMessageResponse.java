package connection.messages;

import connection.models.PeerInformation;

public class FileBlockRequestMessageResponse extends Message {
    private final String fileHash;
    private final long offset;
    private final byte[] data;

    public FileBlockRequestMessageResponse(PeerInformation sender, String fileHash, long offset, byte[] data) {
        super(sender);
        this.fileHash = fileHash;
        this.offset = offset;
        this.data = data;
    }

    public String getFileHash() {
        return fileHash;
    }

    public long getOffset() {
        return offset;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
