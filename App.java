/* SWE 622 - Programming Assignment #1
 * App Class
 * Created By: Harry Trebing - G00583550
 */

public class App {
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("No arguments detected");	
			return;
		}	

		if (args[0].equals("client")){
			Client c = new Client();
			c.initialize();

			if (args.length > 1) {
				String command = args[1];

				if (command.equals("upload")) {
					if (args.length > 3) {
						c.uploadFile(args[2], args[3]);
					} else {
						System.out.println("Invalid number of arguments given to 'client upload' command");
					}
				} else if (command.equals("download")) {
					if (args.length > 3) {
						c.downloadFile(args[2], args[3]);
					} else {
						System.out.println("Invalid number of arguments given to 'client download' command");
					}
				} else if (command.equals("mkdir")) {
					if (args.length >2) {
						c.createDirectory(args[2]);
					} else {
						System.out.println("Invalid number of arguments given to 'client mkdir' command");
					}
				} else if (command.equals("rmdir")) {
					if (args.length >2) {
						c.removeDirectory(args[2]);
					} else {
						System.out.println("Invalid number of arguments given to 'client rmdir' command");
					}
				} else if (command.equals("rm")) {
					if (args.length > 2) {
						c.removeFile(args[2]);
					} else {
						System.out.println("Invalid number of arguments given to 'client rm' command");
					}
				} else if (command.equals("shutdown")) {
					c.shutdown();
				} else if (command.equals("dir")) {
					if (args.length > 2) {
						c.listContents(args[2]);
					} else {
						System.out.println ("Invalid number of arguments given to 'client dir' command");
					}
				} else {
					System.out.println("Invalid command given to client");
				}
			}

		} else if (args[0].equals("server")) {
			if (args.length > 2 && args[1].equals("start")) {
				int portNumber;
				try {
					portNumber = Integer.parseInt(args[2]);	
				} catch (Exception e) {
					System.out.println ("Invalid port number given to server");
					return;
				}
				Server s = new Server();
				s.start(portNumber);
			} else {
			System.out.println("'server' can only accept 'start' command, with a port number");
			}
		} else {
			System.out.println("First argument must be either 'client' or 'server'");
		}
	}
}
