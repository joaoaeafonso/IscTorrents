package connection.messages;

import connection.models.PeerInformation;
import files.models.FileSearchResult;

import java.util.List;

public class WordSearchMessage extends Message {

    private final String keyword;

    public WordSearchMessage(
            PeerInformation peerInformation,
            String keyWord,
            List<FileSearchResult> resultList)
    {
        super(peerInformation);
        this.keyword = keyWord;
    }

    public String getKeyword(){
        return this.keyword;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
