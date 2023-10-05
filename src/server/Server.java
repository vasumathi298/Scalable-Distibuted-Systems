package server;

import java.io.IOException;

/**
 * The `Server` interface defines the contract for a generic server that can open connections,
 * send responses, receive requests, close connections, and retrieve client IP information.
 */
public interface Server {

    /**
     * Opens a connection on the specified port and returns a message indicating the success or failure of the operation.
     *
     * @param port The port number on which to open the connection.
     * @return A message indicating the success or failure of the connection operation.
     * @throws IOException If there is an issue with opening the connection.
     */
    String openConnection(int port) throws IOException;

    /**
     * Sends a response message to the connected client.
     *
     * @param message The response message to send to the client.
     * @throws IOException If there is an issue with sending the response.
     */
    void sendResponse(String message) throws IOException;

    /**
     * Receives a request message from the connected client.
     *
     * @return A string containing the request message received from the client.
     * @throws IOException If there is an issue with receiving the request.
     */
    String receiveRequest() throws IOException;

    /**
     * Closes the connection to the client.
     *
     * @throws IOException If there is an issue with closing the connection.
     */
    void closeConnection() throws IOException;

    /**
     * Retrieves the IP address of the connected client.
     *
     * @return A string representing the IP address of the client.
     */
    String getClientIp();
}
