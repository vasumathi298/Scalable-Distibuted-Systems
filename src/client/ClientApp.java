package client;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import logger.LoggerHandler;

/**
 * The `ClientApp` class represents a client application that communicates with a server using TCP or UDP protocols.
 */
public class ClientApp {
  private static Client socketProtocolConnection = null;
  private static final Logger logger = Logger.getLogger(ClientApp.class.getName());

  /**
   * Constructor for the `ClientApp` class.
   */
  ClientApp() {

  }

  /**
   * Logs a message indicating that the client is sending a message.
   *
   * @param message The message being sent by the client.
   */
  private static void logClientSendingMessage(String message) {
    System.out.println("Client sending this: " + message);
    logger.log(Level.INFO, message);
  }

  /**
   * Logs a message indicating that the client has received a response from the server.
   *
   * @param message   The response message received from the server.
   * @param sentTime  The timestamp when the original message was sent.
   */
  private static void logClientReceivingMessage(String message, long sentTime) {
    String[] messages = message.split("#");
  
    if (messages.length == 2) {
      if (Long.valueOf(messages[1]).equals(sentTime)) {
        System.out.println("Operation performed and received from: " + message);
        logger.log(Level.INFO, message);
      } else {
        System.out.println("Unrequested datagram packet received from server: " + message);
        logger.log(Level.INFO, message);
      }
    } else {
      System.out.println("Invalid response received from server: " + message);
      logger.log(Level.INFO, message);
    }
  }

  /**
   * The main method of the client application. It initializes the client, sends requests to the server,
   * and receives responses.
   *
   * @param args Command-line arguments. Expected arguments are [host] [port].
   * @throws IOException          If there is an issue with I/O operations.
   * @throws InterruptedException If the thread is interrupted.
   */
  public static void main(String args[]) throws IOException, InterruptedException {
    LoggerHandler.initLogger(logger, "src/client/Client.log");
    if (args.length == 2) {
      int port = Integer.valueOf(args[1]);
      String host = (args[0]);
      Scanner sc = new Scanner(System.in);
      System.out.println("Enter protocol mode of the client.");
      String protocolType = sc.nextLine();
      if (protocolType.equals("TCP")) {
        socketProtocolConnection = new TCPClient();
        socketProtocolConnection.openConnection(host, port);
      } else if (protocolType.equals("UDP")) {
        socketProtocolConnection = new UDPClient();
        socketProtocolConnection.openConnection(host, port);
      }
      while (true) {
        if (protocolType.equals("TCP") || protocolType.equals("UDP")) {
          long currentTime = System.currentTimeMillis();
          System.out.println("Enter operation to perform and operands.");
          String clientCommandMsg = sc.nextLine();
          if (clientCommandMsg != null && clientCommandMsg.length() < 80) {
            logClientSendingMessage(clientCommandMsg + " " + String.valueOf(currentTime));
            socketProtocolConnection.sendRequest(clientCommandMsg + " #" + String.valueOf(currentTime));
            long timeToLive = 5000;
            while (timeToLive > 0) {
              String response = socketProtocolConnection.receiveResponse();
              logClientReceivingMessage(response, currentTime);
              if (response == null) {
                timeToLive -= 1000;
              } else {
                break;
              }
            }
          } else {
            System.out.println("Enter a non-empty command/ Enter message in less than 80 characters");
          }

        } else {
          System.out.println("Invalid protocol Mode");
          break;
        }
      }
    } else {
      System.out.println("Client was provided with bad args.");
    }
    socketProtocolConnection.closeConnection();
  }
}
