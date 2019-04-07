import java.io.Serializable;

public class Message implements Serializable{
    private final Protocol protocol;
    private final Command command;
    private final String[] arguments;
    private final byte[] data;
    private final String message;

    public Message (Protocol protocol, Command command, String[] arguments, byte[] data, String message) {
        this.protocol = protocol;
        this.command = command;
        this.arguments = arguments;
        this.data = data;
        this.message = message;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public Command getCommand() {
        return this.command;
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getMessage() {
        return this.message;
    }
}