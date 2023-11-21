package coordinator;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Coordinator extends Remote {
  String prepareTransaction(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;
  void connectParticipants() throws RemoteException;
}