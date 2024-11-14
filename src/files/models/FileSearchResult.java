package files.models;

import connection.messages.WordSearchMessage;
import connection.models.PeerInformation;

import java.io.Serializable;

public class FileSearchResult implements Serializable {

    private WordSearchMessage relatedMessage;
    private String hash;
    private long fileSize;
    private String fileName;
    private PeerInformation peerInformation;

    public FileSearchResult(
            WordSearchMessage relatedMessage,
            String hash,
            long fileSize,
            String fileName,
            PeerInformation peerInformation) {
        this.relatedMessage = relatedMessage;
        this.hash = hash;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.peerInformation = peerInformation;
    }

    public WordSearchMessage getRelatedMessage() {
        return relatedMessage;
    }

    public void setRelatedMessage(WordSearchMessage relatedMessage) {
        this.relatedMessage = relatedMessage;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public PeerInformation getPeerInformation() {
        return peerInformation;
    }

    public void setPeerInformation(PeerInformation peerInformation) {
        this.peerInformation = peerInformation;
    }
}
