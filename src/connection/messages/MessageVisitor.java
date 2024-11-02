package connection.messages;

public interface MessageVisitor {
    void visit(NewConnectionRequest message);
    void visit(WordSearchMessage message);
}
