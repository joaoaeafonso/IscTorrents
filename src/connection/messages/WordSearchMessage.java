package connection.messages;

import connection.models.MessageType;
import connection.models.PeerInformation;
import files.models.FileSearchResult;

import java.util.List;

public class WordSearchMessage extends Message {

    private final String keyword;
    private final List<FileSearchResult> resultList;

    public WordSearchMessage(
            PeerInformation peerInformation,
            MessageType messageType,
            String keyWord,
            List<FileSearchResult> resultList)
    {
        super(peerInformation, messageType);
        this.keyword = keyWord;
        this.resultList = resultList;
    }

    public String getKeyword(){
        return this.keyword;
    }

    public List<FileSearchResult> getResultList(){
        return this.resultList;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
