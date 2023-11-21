package server;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import coordinator.Coordinator;
import coordinator.CoordinatorImpl;
import logger.LoggerHandler;
import rmi.RMIServer;


/**
 * The Server class represents the RMI (Remote Method Invocation) server application.
 * It provides the main method to start the server and make it available for remote method invocations.
 */
public class Server  {

  private static final Logger logger = Logger.getLogger(Server.class.getName());

  static{
    LoggerHandler.initLogger(logger, "src/server/Server.log");
  }
  /**
   * The main method that starts the RMI server.
   *
   * @param args Command-line arguments for specifying the server's port number.
   */
  public static void main(String[] args) throws RemoteException, AlreadyBoundException {
    // Port number is taken from the terminal argument.
    List<Integer> portNumbers= new ArrayList<>();
    // Extract server host address and client port number from command-line arguments.
    portNumbers.add(Integer.parseInt(args[0]));
    portNumbers.add(Integer.parseInt(args[1]));
    portNumbers.add(Integer.parseInt(args[2]));
    portNumbers.add(Integer.parseInt(args[3]));
    portNumbers.add(Integer.parseInt(args[4]));

    int serverPort = Integer.parseInt(args[0])-1;
    Coordinator coordinator = new CoordinatorImpl(
            Arrays.asList("localhost", "localhost", "localhost", "localhost", "localhost"), portNumbers);

    Registry coordinatorRegistry = LocateRegistry.createRegistry(serverPort);
    coordinatorRegistry.bind("Coordinator", coordinator);
    System.out.println("Coordinator is listening on port " + serverPort);

    try {
      // Start the participants
      for (int i = 0; i < 5; i++) {
        RMIServer participant = new AbstractServer("localhost", serverPort);
        int port = serverPort + i +1;
        Registry participantRegistry = LocateRegistry.createRegistry(port);
        participantRegistry.bind("RMIServer", participant);
        participant.connectToCoordinator();
        System.out.println("Participant is listening on port " + port);
      }
      coordinator.connectParticipants();
      System.out.println("Servers are ready");
     } catch (Exception e) {
      e.printStackTrace();
    }
  }
}