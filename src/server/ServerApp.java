package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import logger.LoggerHandler;

/**
 * The `ServerApp` class represents a server application that communicates with clients using TCP or UDP protocols.
 */
public class ServerApp {
  private static Map<String, Integer> keyStore = new HashMap<String, Integer>();
  private static Server socketProtocolConnection = null;

  private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

  static {
	  keyStore.put("a", 4);
	  keyStore.put("b", 6);
	  keyStore.put("c", 8);
	  keyStore.put("e", 9);
	  keyStore.put("f", 16);
  }
  /**
   * Default constructor for the `ServerApp` class.
   */
  ServerApp() {

  }

  /**
   * Handles and logs an invalid command.
   *
   * @param message The message indicating the invalid command.
   * @throws IOException If there is an issue with sending the response.
   */
  private static void invalidCommandMode(String message) throws IOException {
    System.out.println(message);
    socketProtocolConnection.sendResponse(message);
    logger.log(Level.SEVERE, message);
  }

  /**
   * Logs an exception along with a message.
   *
   * @param e       The exception that occurred.
   * @param message The message to log along with the exception.
   * @throws IOException If there is an issue with sending the response.
   */
  private static void logException(Exception e, String message) throws IOException {
    System.out.println(message);
    socketProtocolConnection.sendResponse(message);
    logger.log(Level.SEVERE, message);
  }

  /**
   * Logs a message at INFO level.
   *
   * @param msg The message to log.
   */
  private static void logMessage(String msg) {
    System.out.println("Successfully performed operation. " + msg);
    logger.log(Level.INFO, msg);
  }

  /**
   * The main method of the server application. It initializes the server, handles client requests, and logs messages.
   *
   * @param args Command-line arguments. Expected argument is [port].
   * @throws IOException If there is an issue with I/O operations.
   */
  public static void main(String args[]) throws IOException {
    // Initialize the logger.
    LoggerHandler.initLogger(logger, "src/server/Server.log");

    if (args.length == 1) {
      int port = Integer.valueOf(args[0]);
      Scanner sc = new Scanner(System.in);
      System.out.println("Enter protocol mode of the server.");
      String protocolType = sc.nextLine();
      String inetAddress = "";

      if (protocolType.equals("TCP")) {
        socketProtocolConnection = new TCPServer();
        socketProtocolConnection.openConnection(port);
      } else if (protocolType.equals("UDP")) {
        socketProtocolConnection = new UDPServer();
        socketProtocolConnection.openConnection(port);
      }

      while (true) {
        if (protocolType.equals("TCP") || protocolType.equals("UDP")) {
          String receivedMsg = socketProtocolConnection.receiveRequest();
          logger.log(Level.INFO, receivedMsg);

          if (receivedMsg != null) {
            String[] instructions = receivedMsg.split(" ");
            inetAddress = socketProtocolConnection.getClientIp();

            if (instructions.length == 3 || instructions.length == 4) {
              if (instructions[0].equals("PUT") && instructions.length == 4) {
                try {
                  keyStore.put(instructions[1], Integer.valueOf(instructions[2]));
                  String logDataMessage = "Put operation success";
                  logMessage(logDataMessage + " packet_id: " + instructions[3] + " InetAddress: " + inetAddress + " port: " + port);
                  socketProtocolConnection.sendResponse(logDataMessage + " " + String.valueOf(instructions[3]));

                } catch (Exception e) {
                  logException(e, "Put operation terminated with exception, packet_id: " +
                      String.valueOf(instructions[3]));
                }
              } else if (instructions[0].equals("GET") && instructions.length == 3) {
                try {
                  int keyValue = keyStore.get(instructions[1]);
                  String logDataMessage = "Get operation success";
                  logMessage(logDataMessage + " packet_id: " + instructions[2] + " InetAddress: " + inetAddress + " port: " + port);
                  socketProtocolConnection.sendResponse(String.valueOf(keyValue) + " " + String.valueOf(instructions[2]));
                } catch (Exception e) {
                  logException(e, "Get operation terminated with exception, packet_id: " +
                      String.valueOf(instructions[2]));
                }
              } else if (instructions[0].equals("DELETE") && instructions.length == 3) {

                try {
                  if (keyStore.containsKey(instructions[1])) {
                    keyStore.remove(instructions[1]);
                    String logDataMessage = "Delete operation success";
                    logMessage(logDataMessage + " packet_id: " + instructions[2] + " InetAddress: " + inetAddress + " port: " + port);
                    socketProtocolConnection.sendResponse(String.valueOf(logDataMessage) + " " + String.valueOf(instructions[2]));
                  } else {
                    invalidCommandMode("Invalid operation provided by user. #" + receivedMsg.split("#")[1]);
                  }
                } catch (Exception e) {
                  logException(e, "Delete operation terminated with exception, packet_id: " +
                      String.valueOf(instructions[2]));
                }
              } else {
                invalidCommandMode("Invalid operation provided by user. #" + receivedMsg.split("#")[1]);
              }
            } else {
              invalidCommandMode("Invalid operation provided by user. #" + receivedMsg.split("#")[1]);
            }
          }
        } else {
          System.out.println("Invalid protocol Mode");
          break;
        }
      }
    } else {
      System.out.println("Server not started.");
    }
    socketProtocolConnection.closeConnection();
  }
}
