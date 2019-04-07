
/* SWE 622 - Programming Assignment #1
 * Server Class
 * Created By: Harry Trebing - G00583550
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    private ServerSocket serverSocket;
    private String root;
    private boolean isRunning = false;
    private static final int dataSize = 1000;

    public Server (String root) {
        this.root = root;
    }

    public Server () {
        this(System.getProperty("user.dir"));
    }

    public int start(int port) {
        this.initialize(port);
        this.isRunning = true;

        while (this.isRunning) {
            try {
                Socket s = this.serverSocket.accept();
                FileManager fileManager = new FileManager(this.root);
                Messenger messenger = new Messenger (s);

                messenger.receiveMessage();

                if (messenger.getCommand() != Command.SHUTDOWN) {
                    new Thread(new DelegatorThread(messenger, fileManager)).start();
                } else {
                    this.isRunning = false;
                }
            } catch (Exception e) {
                return -1;
            }
        }
        try {
            this.serverSocket.close();
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }
    
    public void initialize (int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private abstract class GenericThread implements Runnable {

        Messenger messenger;
        FileManager fileManager;

		GenericThread (Messenger messenger, FileManager fileManager) {
            this.messenger = messenger;
            this.fileManager = fileManager;
		}
	}

	private class DelegatorThread extends GenericThread {
		private DelegatorThread (Messenger messenger, FileManager fileManager) {
			super(messenger, fileManager);
		}

		@Override
		public void run() {
			try {
                //this.messenger.receiveMessage();
                Protocol p = this.messenger.getProtocol();
                Command c = this.messenger.getCommand();

                //If client doesn't send proper REQUEST, send error and terminate thread
                if (p != Protocol.REQUEST) {
                    this.messenger.setProtocol(Protocol.ERROR_INVALID_PROTOCOL);
                    this.messenger.setMessage("INVALID PROTOCOL: " + p.toString() + ". Server Delegator Thread was expecting REQUEST protocol");
                    this.messenger.sendMessage();
                    return;
                }

                switch(c){
                    case UPLOAD_FILE:
                        new Thread(new UploaderThread(this.messenger, this.fileManager)).start();
                        break;
                    case DOWNLOAD_FILE:
                        new Thread(new DownloaderThread(this.messenger, this.fileManager)).start();
                        break;
                    case LIST_CONTENTS:
                        new Thread(new ListContentsThread(this.messenger, this.fileManager)).start();
                        break;
                    case CREATE_DIRECTORY:
                        new Thread(new CreateDirectoryThread(this.messenger, this.fileManager)).start();
                        break;
                    case REMOVE_DIRECTORY:
                        new Thread(new DeleteDirectoryThread(this.messenger, this.fileManager)).start();
                        break;
                    case REMOVE_FILE:
                        new Thread(new DeleteFileThread(this.messenger, this.fileManager)).start();
                        break;
                    default:
                        break;
                }

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class UploaderThread extends GenericThread{

		private UploaderThread (Messenger messenger, FileManager fileManager) {
            super(messenger, fileManager);
		}

		@Override
		public void run() {
            String[] args = this.messenger.getArguments();

            //Validate that arguments sent by client are not null and have a length greater than 0
            if (args == null || args.length < 6) {
                this.messenger.setProtocol(Protocol.ERROR_INVALID_ARGUMENTS);
                this.messenger.setMessage("Arugments sent to the server were invalid: " + Arrays.toString(args));
                this.messenger.sendMessage();
                return;
            }

            DataTransmitArgs cArgs = new DataTransmitArgs(args);
            DataTransmitArgs sArgs = new DataTransmitArgs(args);

            //Validate that directory doesn't already exist
            if (this.fileManager.isDirectory(cArgs.getServerFilePath())) {
                this.messenger.setProtocol(Protocol.ERROR_DIRECTORY_ALREADY_EXISTS);
                this.messenger.setMessage("Destination already exists as a directory on the Server: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            if (this.messenger.getProtocol() == Protocol.REQUEST) {
                if (this.fileManager.isFile(cArgs.getServerFilePath())){
                    this.fileManager.deleteFile(cArgs.getServerFilePath());
                }
                this.messenger.setProtocol(Protocol.ACKNOWLEDGE);
                this.messenger.setCommand(Command.UPLOAD_FILE);
                this.messenger.setMessage("Command Request Received");
                this.messenger.setArgument(cArgs.toArgs());
                this.messenger.sendMessage();
                this.messenger.receiveMessage();
            }

            while (this.messenger.getProtocol() == Protocol.TRANSMIT || this.messenger.getProtocol() == Protocol.RETRANSMIT) {
                cArgs = new DataTransmitArgs(this.messenger.getArguments());
                byte[] data = this.messenger.getData();

                this.fileManager.writeToFile(cArgs.getServerFilePath(), data);
                sArgs.setCurrentStart((int)this.fileManager.getFileSize(cArgs.getServerFilePath()));

                this.messenger.setProtocol(Protocol.RECEIPT);
                this.messenger.setCommand(Command.UPLOAD_FILE);
                this.messenger.setArgument(sArgs.toArgs());
                this.messenger.setMessage("Receipt of data thru byte " + sArgs.getCurrentStart() +
                " for the upload of " + sArgs.getClientFilePath() + " to " + sArgs.getServerFilePath());

                this.messenger.sendMessage();
                this.messenger.receiveMessage();
            }

            if (this.messenger.getProtocol() == Protocol.FINISH) {
                this.messenger.setProtocol(Protocol.SUCCESS);
                this.messenger.setCommand(Command.UPLOAD_FILE);
                this.messenger.setArgument(args);
                this.messenger.setMessage("File " + args[0] + " successfully uploaded");
            }
		}
    }

    private class DownloaderThread extends GenericThread {
        private DownloaderThread (Messenger messenger, FileManager fileManager) {
            super(messenger, fileManager);
        }

        @Override
        public void run() {
            byte[] file;
            DataTransmitArgs cArgs; 
            DataTransmitArgs sArgs; 
            String[] args = this.messenger.getArguments();

            //Validate that arguments sent by client are not null and have a length greater than 0
            if (args == null || args.length < 6) {
                this.messenger.setProtocol(Protocol.ERROR_INVALID_ARGUMENTS);
                this.messenger.setMessage("Arugments sent to the server were invalid: " + Arrays.toString(args));
                this.messenger.sendMessage();
                return;
            }

            cArgs = new DataTransmitArgs(args);
            sArgs = new DataTransmitArgs(args);

            //Validate that location is not a directory
            if (this.fileManager.isDirectory(cArgs.getServerFilePath())) {
                this.messenger.setProtocol(Protocol.ERROR_FILE_DOES_NOT_EXIST);
                this.messenger.setArgument(sArgs.toArgs());
                this.messenger.setMessage("Location exists as a directory on the Server: " + cArgs.getServerFilePath());
                this.messenger.sendMessage();
                return;
            }

            //Validate that the file exists
            if (!this.fileManager.isFile(cArgs.getServerFilePath())) {
                this.messenger.setProtocol(Protocol.ERROR_FILE_DOES_NOT_EXIST);
                this.messenger.setArgument(sArgs.toArgs());
                this.messenger.setMessage("File does not exist: " + cArgs.getServerFilePath());
                this.messenger.sendMessage();
                return;
            }

            file = this.fileManager.readFile(cArgs.getServerFilePath());
            sArgs.setEnd(file.length);

            if (this.messenger.getProtocol() == Protocol.REQUEST) {

                this.messenger.setProtocol(Protocol.ACKNOWLEDGE);
                this.messenger.setCommand(Command.DOWNLOAD_FILE);
                this.messenger.setMessage("Command Request Received");
                this.messenger.setArgument(sArgs.toArgs());
                this.messenger.sendMessage();
                //this.messenger.receiveMessage();
            }
        

            for (int i = 0; i < file.length; i+=dataSize) {

                sArgs.setCurrentStart(i);
                sArgs.setCurrentEnd((i+dataSize<file.length)?i+dataSize:file.length);

                byte[] chunk = Arrays.copyOfRange(file, sArgs.getCurrentStart(), sArgs.getCurrentEnd());

                this.messenger.setProtocol(Protocol.TRANSMIT);
                this.messenger.setCommand(Command.DOWNLOAD_FILE);
                this.messenger.setArgument(sArgs.toArgs());
                this.messenger.setData(chunk);

                this.messenger.sendMessage();
                this.messenger.receiveMessage();

                cArgs = new DataTransmitArgs(this.messenger.getArguments());

                //Should I keep this? Thread should terminate if response is invalid
                while (this.messenger.getProtocol() != Protocol.RECEIPT) {
                    this.messenger.setProtocol(Protocol.RETRANSMIT);
                    this.messenger.setCommand(Command.DOWNLOAD_FILE);
                    this.messenger.setArgument(sArgs.toArgs());
                    this.messenger.setData(chunk);
                    try {
                        this.messenger.receiveMessage();
                    } catch (Exception e) {
                        //indicates the client has stopped responding
                        return;
                    }
                }
            }

            this.messenger.setProtocol(Protocol.FINISH);
            this.messenger.setCommand(Command.UPLOAD_FILE);
            this.messenger.setArgument(sArgs.toArgs());
            this.messenger.sendMessage();

            this.messenger.close();
        }
    }

    private class CreateDirectoryThread extends GenericThread {

		private CreateDirectoryThread (Messenger messenger, FileManager fileManager){
            super(messenger, fileManager);
        }

        @Override
        public void run() {
            String[] args = this.messenger.getArguments();

            //Validate that arguments sent by client are not null and have a length greater than 0
            if (args == null || args.length < 1) {
                this.messenger.setProtocol(Protocol.ERROR_INVALID_ARGUMENTS);
                this.messenger.setMessage("Arugments sent to the server were invalid: " + args);
                this.messenger.sendMessage();
                return;
            }

            //Validate that directory doesn't already exist
            if (this.fileManager.isDirectory(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_DIRECTORY_ALREADY_EXISTS);
                this.messenger.setMessage("Directory already exists: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            //Validate that filepath doesn't already point to a file
            if (this.fileManager.isFile(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_FILE_ALREADY_EXISTS);
                this.messenger.setMessage("Location already exists as directory as a file: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.ACKNOWLEDGE);
            this.messenger.setCommand(Command.CREATE_DIRECTORY);
            this.messenger.setMessage("Command Request Received");
            this.messenger.sendMessage();

            boolean result = this.fileManager.createDirectory(args[0]);

            if (result == false) {
                this.messenger.setProtocol(Protocol.ERROR);
                this.messenger.setMessage("Directory was unable to be created " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.SUCCESS);
            this.messenger.setCommand(Command.CREATE_DIRECTORY);
            this.messenger.setMessage("Directory was created successfully: " + args[0]);
            this.messenger.sendMessage();
        }

    }

    private class DeleteDirectoryThread extends GenericThread {
        private DeleteDirectoryThread(Messenger messenger, FileManager fileManager){
            super(messenger, fileManager);
        }

        @Override
        public void run() {
            String[] args = this.messenger.getArguments();

            //Validate that arguments sent by client are not null and have a length greater than 0
            if (args == null || args.length < 1) {
                this.messenger.setProtocol(Protocol.ERROR_INVALID_ARGUMENTS);
                this.messenger.setMessage("Arugments sent to the server were invalid: " + args);
                this.messenger.sendMessage();
                return;
            }

            //Validate that directory DOES exist
            if (!this.fileManager.isDirectory(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_DIRECTORY_DOES_NOT_EXIST);
                this.messenger.setMessage("Directory does not exist on the server: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            if (!this.fileManager.isEmpty(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_DIRECTORY_NOT_EMPTY);
                this.messenger.setMessage("Directory is not empty: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.ACKNOWLEDGE);
            this.messenger.setCommand(Command.REMOVE_DIRECTORY);
            this.messenger.setMessage("Command Request Received");
            this.messenger.sendMessage();

            boolean result = this.fileManager.deleteDirectory(args[0]);

            if (result == false) {
                this.messenger.setProtocol(Protocol.ERROR);
                this.messenger.setMessage("Directory was unable to be deleted: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.SUCCESS);
            this.messenger.setCommand(Command.REMOVE_DIRECTORY);
            this.messenger.setMessage("Directory was deleted successfully: " + args[0]);
            this.messenger.sendMessage();
        }
    }

    private class DeleteFileThread extends GenericThread {
        private DeleteFileThread(Messenger messenger, FileManager fileManager) {
            super (messenger, fileManager);
        }

        @Override
        public void run() {
            String[] args = this.messenger.getArguments();

            //Validate that arguments sent by client are not null and have a length greater than 0
            if (args == null || args.length < 1) {
                this.messenger.setProtocol(Protocol.ERROR_INVALID_ARGUMENTS);
                this.messenger.setMessage("Arugments sent to the server were invalid: " + args);
                this.messenger.sendMessage();
                return;
            }

            if (this.fileManager.isDirectory(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_DIRECTORY_ALREADY_EXISTS);
                this.messenger.setMessage("Location exists as a directory on the server: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            //Validate that the file DOES exist
            if (!this.fileManager.isFile(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_FILE_DOES_NOT_EXIST);
                this.messenger.setMessage("File does not exist: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.ACKNOWLEDGE);
            this.messenger.setCommand(Command.REMOVE_FILE);
            this.messenger.setMessage("Command Request Received");
            this.messenger.sendMessage();

            boolean result = this.fileManager.deleteFile(args[0]);

            if (result == false) {
                this.messenger.setProtocol(Protocol.ERROR);
                this.messenger.setMessage("File was unable to be deleted: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.SUCCESS);
            this.messenger.setCommand(Command.REMOVE_DIRECTORY);
            this.messenger.setMessage("File was deleted successfully: " + args[0]);
            this.messenger.sendMessage();
        }
    }

    private class ListContentsThread extends GenericThread {
        private ListContentsThread(Messenger messenger, FileManager fileManager) {
            super(messenger, fileManager);
        }

        @Override
        public void run() {
            String[] args = this.messenger.getArguments();

            //Validate that arguments sent by client are not null and have a length greater than 0
            if (args == null || args.length < 1) {
                this.messenger.setProtocol(Protocol.ERROR_INVALID_ARGUMENTS);
                this.messenger.setMessage("Arugments sent to the server were invalid: " + args);
                this.messenger.sendMessage();
                return;
            }

            //Validate that directory DOES exist
            if (!this.fileManager.isDirectory(args[0])) {
                this.messenger.setProtocol(Protocol.ERROR_DIRECTORY_DOES_NOT_EXIST);
                this.messenger.setMessage("Directory does not exist: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.ACKNOWLEDGE);
            this.messenger.setCommand(Command.LIST_CONTENTS);
            this.messenger.setMessage("Command Request Received");
            this.messenger.sendMessage();

            String[] results = this.fileManager.listContents(args[0]);

            if (results == null) {
                this.messenger.setProtocol(Protocol.ERROR);
                this.messenger.setMessage("Error was encountered while listing the contents of directory: " + args[0]);
                this.messenger.sendMessage();
                return;
            }

            this.messenger.setProtocol(Protocol.SUCCESS);
            this.messenger.setCommand(Command.LIST_CONTENTS);
            this.messenger.setArgument(results);
            this.messenger.setMessage("Contents listed for directory: " + args[0]);
            this.messenger.sendMessage();
        }
    }
}