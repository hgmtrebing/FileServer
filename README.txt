Programming Assignment #1
File Sharing System (FSS) Using Only Java Sockets
SWE 622 - Spring 2019
Created by: Harry Geoffrey Trebing - G00583550

Instructions to Run and Test Assignment

    1. Begin by initializing the server.
            java -jar pa1.jar server start <portnumber> &

    2. Wait at least 1 second before attempting to connect with the server, to ensure that the server is
    properly initialized.

    3. Export an environmental variable called PA1_SERVER, containing the hostname and port number of the
    server, delimited by a colon. This is required so that the client can find the server.

    4. Issue commands to the client by using the following syntax:
            java -jar pa1.jar client <command> <arguments> 
        
        Note that this syntax automatically instantiates a new client, and connects it to the server, before
        performing the desired command. Users do not need to manually instantiate/connect the client.

    5. Once finished issuing commands, be sure to shutdown the server:
            java -jar pa1.jar client shutdown

        Doing this frees up the port on the server's computer, as the server will remain active until it is
        shut down. 


Important Details

    1. The file system root for BOTH the client and the server is automatically set to whatever directory
    they were started in.

    2. All errors encountered by the client return a non-zero error code and have an error message written
    to System.err. This includes both internal errors and errors returned by the server.

    3. By default, the client expects that an environmental variable called PA1_SERVER has been set, 
    containing the hostname and portnumber of the server, delimited by a colon. If PA1_SERVER has not been
    set correctly, the client will not be able to establish a connection with the server.

    4. The main method for my File Server resides in App.java. To invoke it correctly, please use either of
    the follow syntaxes:
        - java -jar pa1.jar <client/server> <command> <arguments>
        - java -cp pa1.jar App <client/server> <command> <arguments>

    5. My Client accepts the following commands:
        
        a. 'upload' - Uploads a file to the server. Invoked through the following syntax:
                java -jar pa1.jar client upload <path_on_client> <path_on_server>

        b. 'download' - Downloads a file from the server. Invoked through the following syntax: 
                java -jar pa1.jar client download <path_on_server> <path_on_client>

        c. 'dir' - lists the contents of a directory on the server. Invoked through the following syntax:
                java -jar pa1.jar client dir <path_on_server> 

        d. 'rm' - Removes a file listed on the server. Invoked through the following syntax:
                java -jar pa1.jar client rm <path_to_file_on_server>

        e. 'rmdir' - Removes a directory listed on the server. Invoked through the following syntax:
                java -jar pa1.jar client rmdir <path_to_directory_on_server>

        f. 'mkdir' - Creates a directory on the server. Invoked through the following syntax:
                java -jar pa1.jar client mkdir <new_path_to_directory_on_server>

        g. 'shutdown' - Shuts down the server. Invoked through the following syntax:
                java -jar pa1.jar client shutdown