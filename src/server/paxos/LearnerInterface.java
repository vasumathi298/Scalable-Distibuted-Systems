package server.paxos;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The LearnerInterface represents a remote interface that defines
 * the learning process in the Paxos consensus algorithm. It contains
 * the learning method to acknowledge an accepted proposal.
 */
public interface LearnerInterface extends Remote {
  /**
   * The learn method is used to inform the Learner of an accepted proposal.
   *
   * @param proposalId The unique identifier for the proposal.
   * @param acceptedValue The value that has been accepted.
   * @throws RemoteException If a remote invocation error occurs.
   */
  void learn(int proposalId, Object acceptedValue) throws RemoteException, NotBoundException;

  /**
   * To set the proposal value that is agreed upon.
   * @param proposalValue
   * @throws RemoteException
   */
  void setProposalValue(Object proposalValue) throws RemoteException;
}