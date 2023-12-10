package server;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import logger.LoggerHandler;
import rmi.RMIServer;
import server.paxos.AcceptorInterface;
import server.paxos.LearnerInterface;
import server.paxos.ProposerInterface;

/**
 * Abstract server class that contains the implementation of the hashmap functions.
 */
public class AbstractServer extends RemoteObject implements ProposerInterface, AcceptorInterface, LearnerInterface,RMIServer,
        Serializable {
  // Map to store, get and delete key/value pairs.
  private ConcurrentHashMap<String, String> concurrentMap = new ConcurrentHashMap<>();
  private int numServers;
  private int basePort;
  private int serverId;
  private boolean isPromised;
  private int currentSequenceNumber;
  private int proposedSequenceNumber = 0;
  private Object acceptedValue;
  private String response;
  private List<RMIServer> acceptors;

  private static final Logger logger = Logger.getLogger(AbstractServer.class.getName());

  static{
    LoggerHandler.initLogger(logger, "src/server/Server.log");
  }


  public AbstractServer(){
    // empty default constructor
  }

  /**
   * Constructor to create a Server instance.
   * @param serverId The unique ID of this server.
   * @param numServers The total number of servers in the system.
   */
  public AbstractServer(int serverId, int numServers, int basePort) {
    this.basePort = basePort;
    this.numServers = numServers;
    this.serverId = serverId;
    this.currentSequenceNumber = -1;
    this.isPromised = false;
    this.acceptors = new ArrayList<>();
    this.response = "";
    concurrentMap.put("1","NY");
    concurrentMap.put("2","Boston");
    concurrentMap.put("3","Virginia");
    concurrentMap.put("4","Houston");
    concurrentMap.put("5","Texas");
  }
  public synchronized void proposeOperation(String clientMessage, String serverResponse,
                                            String clientAddress, String clientPort) throws RemoteException, NotBoundException {
    this.acceptors = new ArrayList<>();
    int proposalId = generateProposalId();
    propose(proposalId, new Operation(clientMessage,serverResponse,clientAddress,clientPort));
  }
  public synchronized int prepare(int proposalId) throws RemoteException {
    /**
     * Below three lines is for simulating a failure in one of the acceptors during the prepare stage.
     *         this.simulateFailure();
     *         if(this.isPromised==false)
     *             return 0;
     */
    if(proposalId>this.currentSequenceNumber){
      this.currentSequenceNumber = proposalId;
      this.isPromised = true;
      System.out.println(getCurrentTime()+" Promise received from port number: "+ this.serverId);
      return 1;
    }
    System.out.println(getCurrentTime()+" Promise not received from port number: "+ this.serverId);
    return 0;
  }
  public synchronized String accept(int proposalId, Object proposalValue) throws RemoteException, NotBoundException {
    // Implement Paxos accept logic here
    System.out.println(getCurrentTime()+" Accepting the operation in all the replicas");
    for(int i = 0;i<acceptors.size();i++){
      acceptors.get(i).learn(proposalId,proposalValue);
    }
    return acceptors.get(0).recieveResponse();
  }

  @Override
  public synchronized void propose(int proposalId, Object proposalValue) throws RemoteException, NotBoundException {
    // Implement Paxos propose logic here
    System.out.println(getCurrentTime()+" Sending the proposal to all replicas");
    int numberOfPromises = 0;
    for(int serverPort = this.basePort;serverPort<this.basePort+this.numServers;serverPort++){
      if(serverPort!=this.serverId){
        Registry registry = LocateRegistry.getRegistry("localhost", serverPort);
        RMIServer process = (RMIServer) registry.lookup("RMIServer"+serverPort);
        this.acceptors.add(process);
        numberOfPromises+=process.prepare(proposalId);
      }
    }
    if(numberOfPromises<numServers/2){
      System.out.println(getCurrentTime()+" Operation aborted due to no majority");
    }
    else{
      System.out.println(getCurrentTime()+" Majority achieved");
      this.response = accept(proposalId,proposalValue);
      this.response = this.response.substring(0,response.length()-4) + serverId;
    }

  }
  public synchronized void learn(int proposalId, Object acceptedValue) throws RemoteException, NotBoundException {
    this.setProposalValue(acceptedValue);
    this.acceptRequest(((Operation) this.acceptedValue).clientMessage, ((Operation) this.acceptedValue).serverResponse,
            ((Operation) this.acceptedValue).clientAddress, String.valueOf(this.serverId));
  }

  public synchronized void setProposalValue(Object acceptedValue) throws RemoteException {
    this.acceptedValue = acceptedValue;
  }


  /**
   * Generates a unique proposal ID.
   * @return A unique proposal ID.
   */
  private int generateProposalId() {
    // Placeholder code to generate a unique proposal ID
    this.proposedSequenceNumber+=1;
    return this.proposedSequenceNumber;
  }
  /**
   * Implementation of the PUT operation. Stores the given key-value pair in the ConcurrentHashMap.
   *
   * @param key   The key to store in the map.
   * @param value The value associated with the key.
   */
  public synchronized void put(String key, String value) {
    concurrentMap.put(key, value);
  }

  /**
   * Implementation of the GET operation. Retrieves the value associated with the provided key.
   *
   * @param key The key to look up in the map.
   * @return The value associated with the key, or null if the key is not found.
   */
  public synchronized String get(String key) {
    return concurrentMap.get(key);
  }

  /**
   * Implementation of the DELETE operation. Removes the key-value pair associated with the given key.
   *
   * @param key The key to delete from the map.
   * @return True if the key was found and deleted; false if the key was not found.
   */
  public synchronized boolean delete(String key) {
    if (concurrentMap.containsKey(key)) {
      concurrentMap.remove(key);
      return true;
    }
    return false;
  }

  /**
   * Gets the current system time of the server in the "yyyy-MM-dd HH:mm:ss.SSS" format.
   *
   * @return The current time as a formatted string.
   */
  public synchronized static String getCurrentTime() {
    LocalDateTime nowDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    return nowDateTime.format(formatter);
  }
  /**
   * Handles the DELETE operation received from a client and provides a response message.
   *
   * @param key           The key for the DELETE operation.
   * @param clientAddress The client's IP address.
   * @param clientPort    The client's port number.
   * @return A response message indicating the result of the DELETE operation.
   */
  public synchronized String deleteOp(String key, String clientAddress, String clientPort) {
    long beginTime = System.currentTimeMillis();
    String[] msgchunk = key.split(" ");
    if (delete(key) && msgchunk.length == 1) {
      long endTime = System.currentTimeMillis();
      String ttlmsg = checkTimeToLive(beginTime, endTime);
      if (ttlmsg.equals("")) {
        System.out.println(getCurrentTime() + " Sent to client: DELETE operation with key: " + key + " completed"
                + " from " + clientAddress + ":" + clientPort);
        logger.log(Level.INFO, getCurrentTime() + " Sent to client: DELETE operation with key: " + key + " completed"
                + " from " + clientAddress + ":" + clientPort);
        return "DELETE operation with key: " + key + " completed from " + clientAddress + ":" + clientPort;
      } else {
        return ttlmsg;
      }
    } else {
      logger.log(Level.WARNING, getCurrentTime() + " Sent to client: Invalid DELETE operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      System.out.println(getCurrentTime() + " Sent to client: Invalid DELETE operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return "Invalid DELETE operation received from client from " + clientAddress + ":" + clientPort;
    }
  }

  /**
   * Handles the GET operation received from a client and provides a response message.
   *
   * @param key           The key for the GET operation.
   * @param clientAddress The client's IP address.
   * @param clientPort    The client's port number.
   * @return A response message indicating the result of the GET operation.
   */
  public synchronized String getOp( String key, String clientAddress, String clientPort, boolean getFlag) {
    long startTime = System.currentTimeMillis();
    String[] validChunks = key.split(" ");
    // If valid, then send confirmation message to client.
    if (getFlag == true) {
      if (get(key) != null && validChunks.length == 1) {
        String value = get(key);
        long endTime = System.currentTimeMillis();
        String timeOutMessage = checkTimeToLive(startTime, endTime);
        if (timeOutMessage.equals("")) {
          System.out.println(getCurrentTime() + " Sent to client:" + " GET operation with key: " + key + " gives value: " + value
                  + " from " + clientAddress + ":" + clientPort);
          return getCurrentTime()+" GET operation with key: " + key + " gives value: " + value;
        } else {
          return timeOutMessage;
        }
      } else {
        System.out.println(getCurrentTime() + " Sent to client:" + " Invalid GET operation received from client"
                + " from " + clientAddress + ":" + clientPort);
        return getCurrentTime()+" Invalid GET operation received from client";
      }
    }
    else {
      if(validChunks.length==1){
        return get(key);
      }
      else if(validChunks.length==2){
        return get(validChunks[0]);
      }
      else {
        return null;
      }
    }
  }

  /**
   * Helper function to invoke the PUT operation of the map and check if it can be performed using the provided arguments.
   *
   * @param key           The key for the PUT operation.
   * @param clientAddress The client's IP address.
   * @param clientPort    The client's port number.
   * @return A response message indicating the result of the PUT operation.
   */
  public synchronized String putOp(String key, String clientAddress, String clientPort) {
    long startTime = System.currentTimeMillis();
    String[] validChunks = key.split(" ");
    String realKey = key.split(" ", 2)[0];
    String value = key.split(" ", 2)[1];
    if (!realKey.equals("") && !value.equals("") && validChunks.length == 2) {
      put(realKey, value);
      long endTime = System.currentTimeMillis();
      String ttlmsg = checkTimeToLive(startTime, endTime);
      if (ttlmsg.equals("")) {
        logger.log(Level.INFO, getCurrentTime() + " Sent to client: PUT operation with key: " + realKey + " and value: " + value + " completed"
                + " from " + clientAddress + ":" + clientPort);
        System.out.println(getCurrentTime() + " Sent to client: PUT operation with key: " + realKey + " and value: " + value + " completed"
                + " from " + clientAddress + ":" + clientPort);
        return getCurrentTime()+"PUT operation with key: " + realKey + " and value: " + value + " completed"
                + " from " + clientAddress + ":" + clientPort;
      } else {
        return ttlmsg;
      }
    } else {
      logger.log(Level.WARNING, getCurrentTime() + " Sent to client: Invalid PUT operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      System.out.println(getCurrentTime() + " Sent to client: Invalid PUT operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return getCurrentTime()+"Invalid PUT operation received from client from " + clientAddress + ":" + clientPort;
    }
  }

  /**
   * Checks the time elapsed between the start and end times and returns a TTL message if the request exceeded 10 ms.
   *
   * @param startTime The start time of the request.
   * @param endTime   The end time of the request.
   * @return A TTL message if the request exceeded the specified time; otherwise, an empty string.
   */
  public synchronized String checkTimeToLive(long startTime, long endTime) {
    if (endTime - startTime > 5000) {
      return getCurrentTime() + " Request Timed out with request taking: " +
              (endTime - startTime) + " ms to process!";
    }
    return "";
  }
  /**
   * Accepts a client request, processes it, and returns a server response message.
   *
   * @param clientMessage   The message received from the client.
   * @param serverResponse  The current server response message.
   * @param clientAddress   The client's IP address.
   * @param clientPort      The client's port number.
   * @return The updated server response message after processing the client request.
   */
  public synchronized String acceptRequest(String clientMessage, String serverResponse, String clientAddress, String clientPort) {
    String operation = clientMessage.split(" ", 2)[0];
    String key = clientMessage.split(" ", 2)[1];
    switch (operation) {
      case "PUT": {
        serverResponse += putOp(key, clientAddress, clientPort);
        break;
      }
      case "GET": {
        serverResponse += getOp(key, clientAddress, clientPort,true);
        break;
      }
      case "DELETE": {
        serverResponse += deleteOp(key, clientAddress, clientPort);
        break;
      }
      default: {
        serverResponse = clientMessage;
        logger.log(Level.INFO, getCurrentTime() + " Sent to client: " + serverResponse + " from " + clientAddress + ":" + clientPort);
        System.out.println(getCurrentTime() + " Sent to client: " + serverResponse + " from " + clientAddress + ":" + clientPort);
      }
    }
    return serverResponse;
  }

  public synchronized String recieveResponse() throws RemoteException {
    return this.response;
  }
  public void simulateFailure() {
    Random random = new Random();

    while (true) {
      try {
        // Simulate random failures by sleeping for a random period
        Thread.sleep(random.nextInt(5000) + 3000); // Sleep for 3 to 8 seconds

        // Simulate restarting the acceptor thread
        isPromised = false;
        System.out.println("Acceptor thread restarted.");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  private static class Operation implements Serializable{
    String clientMessage;
    String serverResponse;
    String clientAddress;
    String clientPort;
    Operation(String clientMessage, String serverResponse, String clientAddress, String clientPort){
      this.clientMessage = clientMessage;
      this.serverResponse = serverResponse;
      this.clientAddress = clientAddress;
      this.clientPort = clientPort;
    }
  }
}