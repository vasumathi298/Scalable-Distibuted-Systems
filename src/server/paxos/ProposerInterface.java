package server.paxos;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The ProposerInterface provides a remote method to initiate a proposal in the Paxos consensus algorithm.
 * It is part of the Paxos distributed consensus protocol, representing the proposing role.
 */
public interface ProposerInterface extends Remote {

  /**
   * Initiates a proposal with the given proposal ID and value.
   *
   * @param proposalId The unique identifier for the proposal.
   * @param proposalValue The value being proposed.
   * @throws RemoteException If a remote invocation error occurs.
   */
  void propose(int proposalId, Object proposalValue) throws RemoteException, NotBoundException;

  /**
   * Method called by the client to initiate the proposal and setting the operation
   * @param clientMessage
   * @param serverResponse
   * @param clientAddress
   * @param clientPort
   * @throws RemoteException
   * @throws NotBoundException
   */
  void proposeOperation(String clientMessage, String serverResponse,
                        String clientAddress, String clientPort) throws RemoteException, NotBoundException;
}