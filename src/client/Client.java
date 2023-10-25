package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import logger.LoggerHandler;
import rmi.RMIServer;

/**
 * The Client class implements a simple RMI (Remote Method Invocation) client that allows the user to send
 * PUT, GET, or DELETE commands to an RMI server. It also supports reading commands from a file and provides
 * the option to exit the client application.
 */
public class Client extends AbstractClient {

  /**
   * The main method that starts the RMI client.
   *
   * @param args Command-line arguments for specifying the server host address and client port number.
   */
  private static final Logger logger = Logger.getLogger(Client.class.getName());

  static{
    LoggerHandler.initLogger(logger, "src/client/Client.log");
  }

  public static void main(String[] args) {
    try {
      // Extract server host address and client port number from command-line arguments.
      String serverHostAddress = args[0];
      int clientPortNumber = Integer.parseInt(args[1]);

      // Get the RMI registry and lookup the RMIServer stub.
      Registry RMIregistry = LocateRegistry.getRegistry(serverHostAddress, clientPortNumber);
      RMIServer RMIstub = (RMIServer) RMIregistry.lookup("RMIServer");

      // Display a message to indicate that the client is running.
      System.out.println(getCurrentTime() + " Client is running");
      System.out.println(getCurrentTime() + " Enter any one command: PUT/GET/DELETE/file");
      System.out.println(getCurrentTime() + " Enter 'exit' to quit");

      // Create a scanner to read user input.
      Scanner sc = new Scanner(System.in);
      while (true) {
        String clientMessage = sc.nextLine();

        // Process user input based on the entered command.
        switch (clientMessage.toUpperCase()) {
          case "FILE": {
            // Read commands from a file and send them to the server.
            File file = new File("commands.txt");
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
              String line;
              while ((line = reader.readLine()) != null) {
                String toServer = clientRead(line);
                String serverResponse = RMIstub.acceptRequest(toServer, "", serverHostAddress, String.valueOf(clientPortNumber));
                logger.log(Level.INFO, getCurrentTime() + " Received from server: " + serverResponse);
                System.out.println(getCurrentTime() + " Received from server: " + serverResponse);
              }
            }
            break;
          }
          case "EXIT": {
            // Exit the client application.
            logger.log(Level.WARNING, getCurrentTime() + " Client got disconnected");
            System.out.println(getCurrentTime() + " Client got disconnected");
            break;
          }
          default: {
            // Send the user's command to the server and display the server's response.
            String toServer = clientRead(clientMessage);
            String serverResponse = RMIstub.acceptRequest(toServer, "", serverHostAddress, String.valueOf(clientPortNumber));
            logger.log(Level.INFO, getCurrentTime() + " Received from server: " + serverResponse);
            System.out.println(getCurrentTime() + " Received from server: " + serverResponse);
          }
        }

        // Check if the user entered "exit" to quit the client application.
        if (clientMessage.equalsIgnoreCase("exit")) {
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}