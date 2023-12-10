package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

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
    List<Integer> portNumbers= new ArrayList<>();
    portNumbers.add(Integer.parseInt(args[0]));
    portNumbers.add(Integer.parseInt(args[1]));
    portNumbers.add(Integer.parseInt(args[2]));
    portNumbers.add(Integer.parseInt(args[3]));
    portNumbers.add(Integer.parseInt(args[4]));


   AbstractServer[] paxosServer = new AbstractServer[5];

    try {
      for (int i = 0; i < 5; i++) {

        paxosServer[i] = new AbstractServer(portNumbers.get(i),5,  portNumbers.get(0));

        RMIServer paxosRMIServer = (RMIServer) UnicastRemoteObject.exportObject(paxosServer[i],portNumbers.get(i));

        Registry rmiRegistry = LocateRegistry.createRegistry(portNumbers.get(i));

        rmiRegistry.rebind("RMIServer"+portNumbers.get(i), paxosRMIServer);

        System.out.println("Server " + (i+1) + " is ready at port " + portNumbers.get(i));

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}