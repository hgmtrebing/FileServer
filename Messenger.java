
/* SWE 622 - Programming Assignment #1
 * Messenger Class
 * Created By: Harry Trebing - G00583550
 */

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Messenger {

    private Socket socket;
    private SocketAddress addr;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean isOpen;

    private Message mostRecentMessage;

    private Protocol protocol;
    private Command command;
    private String[] args;
    private byte[] data;
    private String message;

    public Messenger (String hostName, int portNumber) {
        this.addr = new InetSocketAddress(hostName, portNumber);
    }

    public Messenger (SocketAddress addr) {
        this.addr = addr;
    }

    public Messenger (Socket socket) {
        try{
            this.socket = socket;
            this.addr = socket.getLocalSocketAddress();
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.isOpen = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    //This may throw an exception - think of how to handle
    public Messenger (int portNumber) {
        this.addr = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), portNumber);
    }
    */

    public void open() {
        try {
            this.socket = new Socket();
            this.socket.connect(addr);
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
            this.isOpen = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            this.socket.close();
            this.in = null;
            this.out = null;
            this.isOpen = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage() {
        try {
            Message m = new Message (this.protocol, this.command, this.args, this.data, this.message);
            this.out.writeObject(m);
            this.out.flush();
            this.resetMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage() {
        try {
            this.mostRecentMessage = (Message) this.in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setProtocol (Protocol p) {
        this.protocol = p;
    }

    public void setCommand (Command c) {
        this.command = c;
    }

    public void setArgument(String[] args) {
        this.args = args;
    }

    public void setData (byte[] data) {
        this.data = data;
    }

    public void setMessage (String message) {
        this.message = message;
    }

    public void resetMessage () {
        this.setProtocol(null);
        this.setCommand(null);
        this.setArgument(null);
        this.setData(null);
        this.setMessage(null);
    }

    public Protocol getProtocol () {
        return mostRecentMessage.getProtocol();
    }

    public Command getCommand() {
        return this.mostRecentMessage.getCommand();
    }

    public String[] getArguments() {
        return this.mostRecentMessage.getArguments();
    }

    public byte[] getData() {
        return this.mostRecentMessage.getData();
    }

    public String getMessage() {
        return this.mostRecentMessage.getMessage();
    }
}