package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.AbstractClient;
import logger.LoggerHandler;
import rmi.RMIServer;

/**
 * An abstract class providing common functionality for implementing RMI (Remote Method Invocation) servers.
 * This class uses a ConcurrentHashMap to store key-value pairs and handles PUT, GET, and DELETE operations.
 */
public abstract class AbstractServer implements RMIServer {

  private static final Logger logger = Logger.getLogger(AbstractServer.class.getName());

  static{
    LoggerHandler.initLogger(logger, "src/server/Server.log");
  }

  /**
   * A ConcurrentHashMap used to store key-value pairs for server operations.
   */
  private static ConcurrentHashMap<String, String> concurrentMap = new ConcurrentHashMap<>();

  /**
   * Default constructor for the AbstractServer class. Initializes the ConcurrentHashMap with
   * sample key-value pairs.
   */
  public AbstractServer() {
    concurrentMap.put("a", "New York");
    concurrentMap.put("b", "Boston");
    concurrentMap.put("c", "Texas");
    concurrentMap.put("d", "California");
    concurrentMap.put("e", "Virginia");
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
  public synchronized String getOp(String key, String clientAddress, String clientPort) {
    long beginTime = System.currentTimeMillis();
    String[] msgChunk = key.split(" ");
    if (get(key) != null && msgChunk.length == 1) {
      String value = get(key);
      long endTime = System.currentTimeMillis();
      String ttlmsg = checkTimeToLive(beginTime, endTime);
      if (ttlmsg.equals("")) {
        logger.log(Level.INFO, getCurrentTime() + " Sent to client: GET operation with key: " + key + " gives value: " + value
                + " from " + clientAddress + ":" + clientPort );
        System.out.println(getCurrentTime() + " Sent to client: GET operation with key: " + key + " gives value: " + value
                + " from " + clientAddress + ":" + clientPort);
        return "GET operation with key: " + key + " gives value: " + value + " from " + clientAddress + ":" + clientPort;
      } else {
        return ttlmsg;
      }
    } else {
      logger.log(Level.WARNING, getCurrentTime() + " Sent to client: Invalid GET operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      System.out.println(getCurrentTime() + " Sent to client: Invalid GET operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return "Invalid GET operation received from client from " + clientAddress + ":" + clientPort;
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
        return "PUT operation with key: " + realKey + " and value: " + value + " completed"
                + " from " + clientAddress + ":" + clientPort;
      } else {
        return ttlmsg;
      }
    } else {
      logger.log(Level.WARNING, getCurrentTime() + " Sent to client: Invalid PUT operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      System.out.println(getCurrentTime() + " Sent to client: Invalid PUT operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return "Invalid PUT operation received from client from " + clientAddress + ":" + clientPort;
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
    if (endTime - startTime > 10) {
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
        serverResponse += getOp(key, clientAddress, clientPort);
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
}

