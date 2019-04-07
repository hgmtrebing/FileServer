
public class App {
	
	public static void main(String[] args) {
		
		if (args[0].equals("client")){
			Client c = new Client();

			if (args.length > 1) {
				String command = args[1];

				if (command.equals("upload")) {
					if (args.length > 3) {
						c.uploadFile(args[2], args[3]);
					}
				} else if (command.equals("download")) {
					if (args.length > 3) {
						c.downloadFile(args[2], args[3]);
					}
				} else if (command.equals("mkdir")) {
					if (args.length >2) {
						c.createDirectory(args[2]);
					}
				} else if (command.equals("rmdir")) {
					if (args.length >2) {
						c.removeDirectory(args[2]);
					}
				} else if (command.equals("rm")) {
					if (args.length > 2) {
						c.removeFile(args[2]);
					}
				} else if (command.equals("shutdown")) {
					c.shutdown();
				} else if (command.equals("ls")) {
					if (args.length > 2) {
						c.listContents(args[2]);
					}
				}
			}

		} else if (args[0].equals("server")) {
			if (args.length > 2 && args[1].equals("start")) {
				int portNumber = Integer.parseInt(args[2]);	
				Server s = new Server();
				s.start(portNumber);
			} else {
			//error
			System.err.println("Invalid arguments given to the server");
			}
		} else {

		}
	}
}
