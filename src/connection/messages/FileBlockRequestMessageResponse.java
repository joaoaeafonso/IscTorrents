package connection.messages;

import connection.models.PeerInformation;

public class FileBlockRequestMessageResponse extends Message {

    private final String downloadId;
    private final String fileHash;
    private final String correspondingRequestMessageId;
    private final long offset;
    private final byte[] data;

    public FileBlockRequestMessageResponse(PeerInformation sender, String downloadId, String requestMessageId, String fileHash, long offset, byte[] data) {
        super(sender);
        this.downloadId = downloadId;
        this.correspondingRequestMessageId = requestMessageId;
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

    public String getCorrespondingRequestMessageId() {
        return correspondingRequestMessageId;
    }

    public String getDownloadId() {
        return downloadId;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
