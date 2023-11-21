package coordinator;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import rmi.RMIServer;

public class CoordinatorImpl extends UnicastRemoteObject implements Coordinator{

  private List<RMIServer> participants;
  private List<String> participantHosts;
  private List<Integer> participantPorts;
  public CoordinatorImpl(List<String> participantHosts, List<Integer> participantPorts) throws RemoteException {
    this.participantHosts = participantHosts;
    this.participantPorts = participantPorts;
  }

  public void connectParticipants() throws RemoteException{
    participants = new ArrayList<>();
    for (int i = 0; i < participantHosts.size(); i++) {
      try {
        Registry registry = LocateRegistry.getRegistry(participantHosts.get(i), participantPorts.get(i));
        RMIServer participant = (RMIServer) registry.lookup("RMIServer");
        participants.add(participant);
      } catch (Exception e) {
        throw new RemoteException("Unable to connect to participant", e);
      }
    }
  }

  public synchronized String prepareTransaction(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException {
    for (int i = 0; i < participantHosts.size(); i++) {
      int rmiPort = participantPorts.get(i);
      if (Integer.valueOf(clientPort)==rmiPort)
        continue;
      if (!participants.get(i).prepare(clientMessage,serverResponse,clientAddress,clientPort)) {
        return "Abort";
      }
    }
    String response = "";
    for (int i = 0; i < participantHosts.size(); i++) {
      int rmiPort = participantPorts.get(i);
      if (Integer.valueOf(clientPort)==rmiPort)
        continue;
      response =  participants.get(i).commit(clientMessage,serverResponse,clientAddress,String.valueOf(participantPorts.get(i)));
    }
    return response;
  }
}