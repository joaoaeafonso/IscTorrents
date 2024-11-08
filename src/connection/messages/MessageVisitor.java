package connection.messages;

public interface MessageVisitor {
    void visit(NewConnectionRequest message);
    void visit(NewConnectionResponse message);
    void visit(WordSearchMessage message);
    void visit(WordSearchMessageResponse message);
    void visit(FileBlockRequestMessage message);
}
