package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The `TCPClient` class represents a client using the TCP protocol to communicate with a server.
 */
public class TCPClient implements Client {
    private Socket socketPort = null;
    private DataInputStream clientIn = null;
    private DataOutputStream clientOut = null;

    /**
     * Default constructor for the `TCPClient` class.
     */
    public TCPClient() {

    }

    /**
     * Opens a TCP connection to the specified hostname and port.
     *
     * @param hostname The hostname or IP address of the server to connect to.
     * @param port     The port number on which the server is listening.
     */
    @Override
    public void openConnection(String hostname, int port) {
        try {
            socketPort = new Socket(hostname, port);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a request message over the TCP connection to the server.
     *
     * @param message The request message to send to the server.
     * @throws IOException If there is an issue with sending the request.
     */
    @Override
    public void sendRequest(String message) throws IOException {
        System.out.println("Client Connected");
        clientOut = new DataOutputStream(
                socketPort.getOutputStream());
        clientOut.writeUTF(message);
    }

    /**
     * Receives a response message from the server over the TCP connection.
     *
     * @return A string containing the response received from the server.
     * @throws IOException If there is an issue with receiving the response.
     */
    @Override
    public String receiveResponse() throws IOException {
        clientIn = new DataInputStream(socketPort.getInputStream());
        return clientIn.readUTF();
    }

    /**
     * Closes the TCP connection, input stream, and output stream.
     */
    @Override
    public void closeConnection() {
        try {
            clientIn.close();
            clientOut.close();
            socketPort.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
