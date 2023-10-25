package client;

import logger.LoggerHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class containing common implementations for both TCP and UDP clients.
 * This abstract class provides utility methods for checking the validity of operations,
 * reading client instructions, and getting the current time.
 */
public abstract class AbstractClient {

  private static final Logger logger = Logger.getLogger(AbstractClient.class.getName());

  static{
    LoggerHandler.initLogger(logger, "src/client/Client.log");
  }

  /**
   * Checks the validity of an operation based on the provided content.
   *
   * @param content The content to check for validity.
   * @return True if the operation is valid (PUT, GET, or DELETE); otherwise, false.
   */
  protected synchronized static boolean checkOperationValidity(String content) {
    String operation = content.split(" ", 2)[0];
    return operation.equals("PUT") || operation.equals("GET") || operation.equals("DELETE");
  }

  /**
   * Reads a client instruction and logs it with a timestamp.
   *
   * @param instruction The client instruction to read.
   * @return The same instruction if it is valid; otherwise, a message indicating a malformed request.
   */
  protected synchronized static String clientRead(String instruction) {
    if (checkOperationValidity(instruction)) {
      String message = getCurrentTime() + " " + "Sent to server: " + instruction;
      logger.log(Level.INFO,message);
      System.out.println(message);
      return instruction;
    } else {
      String message = getCurrentTime() + " " + "Received malformed request of length: " +
              instruction.length();
      logger.log(Level.WARNING,message);
      System.out.println(message);
      return "Received malformed request of length: " + instruction.length();
    }
  }

  /**
   * Gets the current time in the "yyyy-MM-dd HH:mm:ss.SSS" format.
   *
   * @return The current time as a formatted string.
   */
  protected synchronized static String getCurrentTime() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    return currentDateTime.format(formatter);
  }
}
