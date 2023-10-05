package client;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This interface defines the contract for a generic client that can establish a connection,
 * send requests, receive responses, and close the connection.
 */
public interface Client {

  /**
   * Opens a connection to the specified hostname and port.
   *
   * @param hostname The hostname or IP address of the server to connect to.
   * @param port     The port number on which the server is listening.
   * @throws SocketException       If there is an issue with the socket connection.
   * @throws UnknownHostException If the hostname is not found.
   */
  void openConnection(String hostname, int port) throws SocketException, UnknownHostException;

  /**
   * Sends a request message to the connected server.
   *
   * @param message The request message to send to the server.
   * @throws IOException If there is an issue with sending the request.
   */
  void sendRequest(String message) throws IOException;

  /**
   * Receives a response message from the connected server.
   *
   * @return A string containing the response received from the server.
   * @throws IOException If there is an issue with receiving the response.
   */
  String receiveResponse() throws IOException;

  /**
   * Closes the connection to the server.
   * This method should be called when the client is done communicating with the server.
   */
  void closeConnection();
}
