package files.models;

import connection.messages.WordSearchMessage;
import connection.models.PeerInformation;

public class FileSearchResult {

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

}
