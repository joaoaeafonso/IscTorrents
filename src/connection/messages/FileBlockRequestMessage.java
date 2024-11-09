package connection.messages;

import connection.models.PeerInformation;

public class FileBlockRequestMessage extends Message {

    private String fileHash;
    private String fileName;
    private long offset;
    private long length;

    public FileBlockRequestMessage(PeerInformation sender, String hash, long offset, long length, String fileName) {
        super(sender);
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

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "FileBlockRequestMessage: [hash: "+this.fileHash+"], [offset: "+this.offset+"], [length: "+this.length+"]";
    }

}
