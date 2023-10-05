package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * The `UDPServer` class represents a server implementation using the UDP protocol.
 */
public class UDPServer implements Server {
    private DatagramSocket udpSocket;
    private int clientPort;
    private byte[] dataGramPacket = new byte[5000];
    private InetAddress address;

    /**
     * Default constructor for the `UDPServer` class.
     *
     * @throws SocketException If there is an issue with socket creation.
     */
    public UDPServer() throws SocketException {
    }

    /**
     * Opens a UDP connection on the specified port.
     *
     * @param port The port number on which to open the connection.
     * @return A placeholder string (null in this case).
     * @throws IOException If there is an issue with opening the connection.
     */
    @Override
    public String openConnection(int port) throws IOException {
        udpSocket = new DatagramSocket(port);
        return null;
    }

    /**
     * Sends a response message to the connected client over the UDP connection.
     *
     * @param message The response message to send to the client.
     * @throws IOException If there is an issue with sending the response.
     */
    @Override
    public void sendResponse(String message) throws IOException {
        byte[] newSendingMessageBytes = new byte[5000];
        newSendingMessageBytes = message.getBytes();
        DatagramPacket packet = new DatagramPacket(newSendingMessageBytes, newSendingMessageBytes.length,
                address, clientPort);
        System.out.println(new String(packet.getData(), StandardCharsets.UTF_8) + "server sending this:");
        udpSocket.send(packet);
    }

    /**
     * Receives a request message from the connected client over the UDP connection.
     *
     * @return A string containing the request message received from the client.
     * @throws IOException If there is an issue with receiving the request.
     */
    @Override
    public String receiveRequest() throws IOException {
        DatagramPacket packet
                = new DatagramPacket(dataGramPacket, dataGramPacket.length);
        udpSocket.receive(packet);
        address = packet.getAddress();
        clientPort = packet.getPort();
        String received
                = new String(packet.getData(), 0, packet.getLength());
        System.out.println(received + "server side:");
        return received;
    }

    /**
     * Closes the UDP connection.
     *
     * @throws IOException If there is an issue with closing the connection.
     */
    @Override
    public void closeConnection() throws IOException {
        udpSocket.close();
    }

    /**
     * Retrieves the IP address of the connected client.
     *
     * @return A string representing the IP address of the client.
     */
    @Override
    public String getClientIp() {
        return address.toString();
    }
}
