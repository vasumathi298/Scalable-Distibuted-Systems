package server;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import logger.LoggerHandler;
import rmi.RMIServer;


/**
 * The Server class represents the RMI (Remote Method Invocation) server application.
 * It provides the main method to start the server and make it available for remote method invocations.
 */
public class Server extends AbstractServer {

  private static final Logger logger = Logger.getLogger(Server.class.getName());

  static{
    LoggerHandler.initLogger(logger, "src/server/Server.log");
  }
  /**
   * The main method that starts the RMI server.
   *
   * @param args Command-line arguments for specifying the server's port number.
   */
  public static void main(String[] args) {
    // Port number is taken from the terminal argument.
    int portNumber = Integer.parseInt(args[0]);
    Server obj = new Server();

    // Starting the server.
    try {
      RMIServer skeleton = (RMIServer) UnicastRemoteObject.exportObject(obj, 0);
      Registry registry = LocateRegistry.createRegistry(portNumber);
      registry.rebind("RMIServer", skeleton);
      logger.log(Level.INFO, getCurrentTime() + " Server is listening on port " + portNumber);
      System.out.println(getCurrentTime() + " Server is listening on port " + portNumber);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
