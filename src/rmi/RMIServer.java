package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * The RMIServer interface defines the remote methods that the RMI (Remote Method Invocation) server
 * should implement. It provides methods for basic key-value store operations (PUT, GET, DELETE),
 * handling client requests, checking time-to-live (TTL), and accepting and responding to client requests.
 */
public interface RMIServer extends Remote {

  /**
   * Stores a key-value pair in the server's data store.
   *
   * @param key   The key to store in the data store.
   * @param value The value associated with the key.
   * @throws RemoteException If a remote communication error occurs.
   */
  void put(String key, String value) throws RemoteException;

  /**
   * Retrieves the value associated with the provided key from the data store.
   *
   * @param key The key to look up in the data store.
   * @return The value associated with the key, or null if the key is not found.
   * @throws RemoteException If a remote communication error occurs.
   */
  String get(String key) throws RemoteException;

  /**
   * Deletes a key-value pair from the server's data store based on the provided key.
   *
   * @param key The key to delete from the data store.
   * @return True if the key was found and deleted; false if the key was not found.
   * @throws RemoteException If a remote communication error occurs.
   */
  boolean delete(String key) throws RemoteException;

  /**
   * Handles the DELETE operation received from a client and provides a response message.
   *
   * @param key           The key for the DELETE operation.
   * @param clientAddress The client's IP address.
   * @param clientPort    The client's port number.
   * @return A response message indicating the result of the DELETE operation.
   * @throws RemoteException If a remote communication error occurs.
   */
  String deleteOp(String key, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Handles the GET operation received from a client and provides a response message.
   *
   * @param key           The key for the GET operation.
   * @param clientAddress The client's IP address.
   * @param clientPort    The client's port number.
   * @return A response message indicating the result of the GET operation.
   * @throws RemoteException If a remote communication error occurs.
   */
  String getOp( String key, String clientAddress, String clientPort, boolean getFlag) throws RemoteException;

  /**
   * Handles the PUT operation received from a client and provides a response message.
   *
   * @param key           The key for the PUT operation.
   * @param clientAddress The client's IP address.
   * @param clientPort    The client's port number.
   * @return A response message indicating the result of the PUT operation.
   * @throws RemoteException If a remote communication error occurs.
   */
  String putOp(String key, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Accepts a client request, processes it, and returns a server response message.
   *
   * @param clientMessage   The message received from the client.
   * @param serverResponse  The current server response message.
   * @param clientAddress   The client's IP address.
   * @param clientPort      The client's port number.
   * @return The updated server response message after processing the client request.
   * @throws RemoteException If a remote communication error occurs.
   */
  String acceptRequest(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Checks the time elapsed between the start and end times and returns a TTL (Time-To-Live) message
   * if the request exceeded a certain threshold.
   *
   * @param startTime The start time of the request.
   * @param endTime   The end time of the request.
   * @return A TTL message if the request exceeded the specified time; otherwise, an empty string.
   * @throws RemoteException If a remote communication error occurs.
   */
  String checkTimeToLive(long startTime, long endTime) throws RemoteException;

  boolean prepare(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;
  String commit(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;
  void connectToCoordinator() throws RemoteException;
  String perform(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;

}