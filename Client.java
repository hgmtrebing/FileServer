import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Client {
    private Messenger messenger;
    private FileManager fileManager;
    private static final int dataSize = 1000;

    public Client (String serverHostName, int serverPortNumber, String root) {
        this.messenger = new Messenger (serverHostName, serverPortNumber);
        this.fileManager = new FileManager(root);
    }

    public Client (String serverHostName, int serverPortNumber) {
        this(serverHostName, serverPortNumber, System.getProperty("user.dir"));
    }

    public Client (String root) {
        String[] serverAddr = System.getenv("PA1_SERVER").split(":");
        this.messenger = new Messenger(serverAddr[0], Integer.parseInt(serverAddr[1]));
        this.fileManager = new FileManager(root);
    }

    public Client() {
        String[] serverAddr = System.getenv("PA1_SERVER").split(":");
        this.messenger = new Messenger(serverAddr[0], Integer.parseInt(serverAddr[1]));
        this.fileManager = new FileManager (System.getProperty("user.dir"));
    }

    public void printOutput(String msg) {
        System.out.print(msg);
    }

    public void printError(String msg) {
        System.out.println (msg);
    }

    public int shutdown() {
        this.messenger.open();

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.SHUTDOWN);
        this.messenger.setMessage("Request to shutdown server");

        this.messenger.sendMessage();

        this.messenger.close();
        return 0;
    }

    public int uploadFile(String clientFilePath, String serverFilePath) {
        byte[] file = this.fileManager.readFile(clientFilePath);
        DataTransmitArgs cArgs = new DataTransmitArgs();
        DataTransmitArgs sArgs = new DataTransmitArgs();
        this.messenger.open();

        //Initialize DataTransmitArgs
        cArgs.setClientFilePath(clientFilePath);
        cArgs.setServerFilePath(serverFilePath);
        cArgs.setStart(0);
        cArgs.setCurrentStart(0);
        cArgs.setCurrentEnd(0);
        cArgs.setEnd(file.length);

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.UPLOAD_FILE);
        this.messenger.setMessage("Request to upload file " + serverFilePath);
        this.messenger.setArgument(cArgs.toArgs());
        this.messenger.setData(new byte[0]);

        this.messenger.sendMessage();
        this.messenger.receiveMessage();

        if (!this.serverAcknowledgedRequest()) {
            return -1;
        }

        for (int i = 0; i < file.length; i+=dataSize) {

            cArgs.setCurrentStart(i);
            cArgs.setCurrentEnd((i+dataSize<file.length)?i+dataSize:file.length);

            byte[] chunk = Arrays.copyOfRange(file, cArgs.getCurrentStart(), cArgs.getCurrentEnd());

            this.messenger.setProtocol(Protocol.TRANSMIT);
            this.messenger.setCommand(Command.UPLOAD_FILE);
            this.messenger.setArgument(cArgs.toArgs());
            this.messenger.setData(chunk);

            this.messenger.sendMessage();
            this.messenger.receiveMessage();
            sArgs = new DataTransmitArgs(this.messenger.getArguments());

            while (this.messenger.getProtocol() != Protocol.RECEIPT && !cArgs.equals(sArgs)) {
                this.messenger.close();
                this.messenger.open();
                this.messenger.setProtocol(Protocol.RETRANSMIT);
                this.messenger.setCommand(Command.UPLOAD_FILE);
                this.messenger.setArgument(cArgs.toArgs());
                this.messenger.setData(chunk);
            }
            this.printOutput("\r" + (cArgs.getCurrentEnd() / cArgs.getEnd()) + "% Uploaded");
        }

        this.messenger.setProtocol(Protocol.FINISH);
        this.messenger.setCommand(Command.UPLOAD_FILE);
        this.messenger.setArgument(cArgs.toArgs());
        this.messenger.sendMessage();
        this.printOutput("100% Uploaded");

        this.messenger.close();
        return 0;
    }

    public int downloadFile(String serverFilePath, String clientFilePath) {
        DataTransmitArgs cArgs = new DataTransmitArgs();
        DataTransmitArgs sArgs = new DataTransmitArgs();
        this.messenger.open();

        //Initialize DataTransmitArgs
        cArgs.setClientFilePath(clientFilePath);
        cArgs.setServerFilePath(serverFilePath);
        cArgs.setStart(0);
        cArgs.setCurrentStart(0);
        cArgs.setCurrentEnd(0);
        cArgs.setEnd(0);

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.DOWNLOAD_FILE);
        this.messenger.setMessage("Request to upload file " + serverFilePath);
        this.messenger.setArgument(cArgs.toArgs());
        this.messenger.setData(new byte[0]);

        this.messenger.sendMessage();
        this.messenger.receiveMessage();

        if (!this.serverAcknowledgedRequest()) {
            return -1;
        }

        //Delete File if it already exists
        if (this.fileManager.isFile(clientFilePath)) {
            this.fileManager.deleteFile(clientFilePath);
        }

        this.messenger.receiveMessage();

        //This won't execute until the server gets an answer
        while (this.messenger.getProtocol() == Protocol.TRANSMIT || this.messenger.getProtocol() == Protocol.RETRANSMIT) {
            sArgs = new DataTransmitArgs(this.messenger.getArguments());
            byte[] data = this.messenger.getData();

            //If the client sends a retransmit request
            if (this.messenger.getProtocol() == Protocol.RETRANSMIT) {
                //Truncate file (if necessary) and write to it
            }

            //If the client's request is different from what the server was expecting
            if (!sArgs.equals(cArgs)) {
                //send RETRANSMIT_REQUEST
                //Maybe make a while loop if the client sends multiple bad requests
                //Or a continue; clause
            }

            this.fileManager.writeToFile(clientFilePath, data);
            cArgs.setCurrentStart((int)this.fileManager.getFileSize(clientFilePath));

            this.messenger.setProtocol(Protocol.RECEIPT);
            this.messenger.setCommand(Command.DOWNLOAD_FILE);
            this.messenger.setArgument(cArgs.toArgs());
            this.messenger.setMessage("Receipt of data thru byte " + cArgs.getCurrentStart() +
            " for the download of " + serverFilePath + " to " + clientFilePath);

            this.messenger.sendMessage();
            this.messenger.receiveMessage();
        }

        if (this.messenger.getProtocol() == Protocol.FINISH) {
            this.messenger.setProtocol(Protocol.SUCCESS);
            this.messenger.setCommand(Command.UPLOAD_FILE);
            this.messenger.setArgument(cArgs.toArgs());
            this.messenger.setMessage("File " + serverFilePath + " successfully downloaded");
            this.messenger.sendMessage();
            return 0;
        }
        return -1;
    }

    public int removeFile(String filePath) {
        this.messenger.open();

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.REMOVE_FILE);
        this.messenger.setArgument(new String[]{filePath});
        this.messenger.setData(null);
        this.messenger.setMessage("Request to remove file: " + filePath);

        this.messenger.sendMessage();
        this.messenger.receiveMessage();

        if (!this.serverAcknowledgedRequest()) {
            return -1;
        }

        //Final confirmation from server on success or failure
        this.messenger.receiveMessage();
        int val = 0;
        Protocol p = this.messenger.getProtocol();
        if (p == Protocol.SUCCESS) {
            this.printOutput("File " + filePath + " was removed successfully");
        } else {
            this.printError (p.toString() + " ." + this.messenger.getMessage());
            val = -1;
        }

        this.messenger.close();
        return val;
    }

    public int createDirectory(String filePath) {
        this.messenger.open();

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.CREATE_DIRECTORY);
        this.messenger.setArgument(new String[]{filePath});
        this.messenger.setData(null);
        this.messenger.setMessage("Request to create directory: " + filePath);

        this.messenger.sendMessage();
        this.messenger.receiveMessage();

        if (!this.serverAcknowledgedRequest()) {
            return -1;
        }

        this.messenger.receiveMessage();
        int val = 0;
        Protocol p = this.messenger.getProtocol();
        if (p == Protocol.SUCCESS) {
            this.printOutput("Directory " + filePath + " creation was a success");
        } else {
            this.printError (p.toString() + " ." + this.messenger.getMessage());
            val = -1;
        }

        this.messenger.close();
        return val;
    }

    public int removeDirectory (String filePath) {
        this.messenger.open();

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.REMOVE_DIRECTORY);
        this.messenger.setArgument(new String[]{filePath});
        this.messenger.setData(null);
        this.messenger.setMessage("Request to remove directory: " + filePath);

        this.messenger.sendMessage();
        this.messenger.receiveMessage();

        if (!this.serverAcknowledgedRequest()) {
            return -1;
        }

        //This should be the final success message from the server
        this.messenger.receiveMessage();
        int val = 0;
        Protocol p = this.messenger.getProtocol();
        if (p == Protocol.SUCCESS) {
            this.printOutput("Directory " + filePath + " removal was a success");
        } else {
            this.printError (p.toString() + " ." + this.messenger.getMessage());
            val = -1;
        }

        this.messenger.close();
        return val;
    }

    public int listContents (String filePath) {
        this.messenger.open();

        this.messenger.setProtocol(Protocol.REQUEST);
        this.messenger.setCommand(Command.LIST_CONTENTS);
        this.messenger.setArgument(new String[]{filePath});
        this.messenger.setData(null);
        this.messenger.setMessage("Request to list the contents of directory " + filePath);

        this.messenger.sendMessage();
        this.messenger.receiveMessage();

        if (!this.serverAcknowledgedRequest()) {
            return -1;
        }

        //This should be the final success message from the server
        this.messenger.receiveMessage();
        int val = 0;
        Protocol p = this.messenger.getProtocol();
        if (p == Protocol.SUCCESS) {
            this.printOutput("Directory " + filePath + " contents: " + Arrays.toString(this.messenger.getArguments()));
        } else {
            this.printError (p.toString() + " ." + this.messenger.getMessage());
            val = -1;
        }

        this.messenger.close();
        return val;
    }

    private boolean serverAcknowledgedRequest() {
        Protocol p = this.messenger.getProtocol();
        if (p == null) {
            this.printError("Server did not acknowledge request");
            return false;
        } else if (p != Protocol.ACKNOWLEDGE) {
            this.printError (p.toString() + ": " + this.messenger.getMessage());
            return false;
        } else {
            return true;
        }
    }
}