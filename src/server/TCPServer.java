package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The `TCPServer` class represents a server implementation using the TCP protocol.
 */
public class TCPServer implements Server {

    private Socket socketPort = null;
    private ServerSocket serverSocket = null;
    private DataInputStream serverIn = null;
    private DataOutputStream serverOut = null;

    /**
     * Default constructor for the `TCPServer` class.
     */
    public TCPServer() {

    }

    /**
     * Opens a TCP connection on the specified port and accepts a client connection.
     *
     * @param port The port number on which to open the connection.
     * @return The IP address of the connected client.
     * @throws IOException If there is an issue with opening the connection or accepting the client.
     */
    @Override
    public String openConnection(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        socketPort = serverSocket.accept();
        System.out.println("Server connected and accepted");
        return socketPort.getInetAddress().toString();
    }

    /**
     * Sends a response message to the connected client over the TCP connection.
     *
     * @param message The response message to send to the client.
     * @throws IOException If there is an issue with sending the response.
     */
    @Override
    public void sendResponse(String message) throws IOException {
        serverOut = new DataOutputStream((socketPort.getOutputStream()));
        serverOut.writeUTF(message);
    }

    /**
     * Receives a request message from the connected client over the TCP connection.
     *
     * @return A string containing the request message received from the client.
     * @throws IOException If there is an issue with receiving the request.
     */
    @Override
    public String receiveRequest() throws IOException {
        serverIn = new DataInputStream((socketPort.getInputStream()));
        String line = serverIn.readUTF();
        return line;
    }

    /**
     * Closes the TCP connection to the client.
     *
     * @throws IOException If there is an issue with closing the connection.
     */
    @Override
    public void closeConnection() throws IOException {
        serverIn.close();
        serverOut.close();
        socketPort.close();
    }

    /**
     * Retrieves the IP address of the connected client.
     *
     * @return A string representing the IP address of the client.
     */
    @Override
    public String getClientIp() {
        return socketPort.getInetAddress().toString();
    }
}
