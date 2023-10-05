Assumptions:

Key Format: Keys entered for the data store should be strings and should not contain any spaces.

Value Format: Values entered for the data store should be integers.

Timeout: The client expects a response from the server within 5 seconds. If no response is received within this time, the client considers the request as unacknowledged.

Unique Message ID: The client sends a unique message ID, which is appended to the request message. This is used to handle unacknowledged packets.

Server Response Format: The server responds to the request with a unique client ID sent by the client. This client ID is appended to the response, followed by #.

Communication Protocol: Both client and server communication should use the same protocol, i.e., either TCP or UDP.

Command Case: All user input commands should be in uppercase (PUT/DELETE/GET). The server will respond with "Invalid command" if the user enters a command in lowercase.

Log Sharing: Assumption is that same log files are used for both mode of communication and the log is persisted in proper format as mentioned in the requirements.

Starting the Server Application:

To start the server application, follow these steps:

Command-line Arguments: Provide the following command-line argument:
<port_number>: The port number on which the server will listen for incoming client requests.
Example:

java ServerApp 8080
This command starts the server on port 8080, listening for client connections.

Starting the Client Application:

To start the client application, follow these steps:

Command-line Arguments: Provide the following command-line arguments:
<host_name>: The hostname or IP address of the server to connect to.
<port_number>: The port number on which the server is listening.
Example:


java ClientApp localhost 8080
This command starts the client application and connects it to the server running on the local machine at port 8080. The client can then interact with the server using the specified protocol (TCP or UDP) and adhere to the assumptions mentioned earlier.

Please replace <host_name> and <port_number> with the actual values you need for your specific setup.


Log Sample
Client.log
[2023-10-04 18:00:12.952] INFO - PUT a 78 1696456808742
[2023-10-04 18:00:13.217] INFO - Put operation success #1696456808742
[2023-10-04 18:00:31.155] INFO - GET a 1696456813218
[2023-10-04 18:00:31.162] INFO - 78 #1696456813218

Server.log
[2023-10-04 18:00:13.157] INFO - PUT a 78 #1696456808742
[2023-10-04 18:00:13.215] INFO - Put operation successpacket_id: #1696456808742 InetAddress: /127.0.0.1 port: 1234
[2023-10-04 18:00:31.158] INFO - GET a #1696456813218
[2023-10-04 18:00:31.160] INFO - Get operation successpacket_id: #1696456813218 InetAddress: /127.0.0.1 port: 1234
