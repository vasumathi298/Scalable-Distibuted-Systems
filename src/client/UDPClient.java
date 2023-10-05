package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * The `UDPClient` class represents a client using the UDP protocol to communicate with a server.
 */
public class UDPClient implements Client {
    private DatagramSocket udpSocket;
    private InetAddress address;

    private String hostName;
    private int port;
    private byte[] dataGramPacket;

    private DatagramPacket packet;

    /**
     * Default constructor for the `UDPClient` class.
     */
    public UDPClient() {

    }

    /**
     * Opens a UDP connection to the specified hostname and port.
     *
     * @param hostname The hostname or IP address of the server to connect to.
     * @param port     The port number on which the server is listening.
     * @throws SocketException       If there is an issue with the socket connection.
     * @throws UnknownHostException If the hostname is not found.
     */
    @Override
    public void openConnection(String hostname, int port) throws SocketException, UnknownHostException {
        udpSocket = new DatagramSocket();
        this.hostName = hostname;
        this.port = port;
        address = InetAddress.getByName("localhost");
    }

    /**
     * Sends a request message over the UDP connection to the server.
     *
     * @param message The request message to send to the server.
     * @throws IOException If there is an issue with sending the request.
     */
    @Override
    public void sendRequest(String message) throws IOException {
        dataGramPacket = message.getBytes();
        packet = new DatagramPacket(dataGramPacket, dataGramPacket.length,
                InetAddress.getByName(this.hostName), this.port);
        udpSocket.send(packet);
    }

    /**
     * Receives a response message from the server over the UDP connection.
     *
     * @return A string containing the response received from the server.
     * @throws IOException If there is an issue with receiving the response.
     */
    @Override
    public String receiveResponse() throws IOException {
        dataGramPacket = new byte[5000];
        packet = new DatagramPacket(dataGramPacket, dataGramPacket.length);
        udpSocket.receive(packet);
        String received = new String(
                packet.getData(), 0, packet.getLength());
        System.out.println("Client is getting this:" + received);
        return received;
    }

    /**
     * Closes the UDP connection.
     */
    @Override
    public void closeConnection() {
        udpSocket.close();
    }
}
