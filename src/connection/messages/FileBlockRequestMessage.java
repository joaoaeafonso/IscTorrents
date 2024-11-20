package connection.messages;

import connection.models.PeerInformation;

public class FileBlockRequestMessage extends Message {

    private final String downloadId;
    private final String fileHash;
    private final String fileName;
    private final long offset;
    private final long length;

    public FileBlockRequestMessage(PeerInformation sender, String hash, long offset, long length, String fileName, String downloadId) {
        super(sender);
        this.downloadId = downloadId;
        this.fileHash = hash;
        this.fileName = fileName;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    public String getFileHash() {
        return fileHash;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadId() {
        return downloadId;
    }

    @Override
    public String toString() {
        return "FileBlockRequestMessage: [hash: "+this.fileHash+"], [offset: "+this.offset+"], [length: "+this.length+"]";
    }
}
