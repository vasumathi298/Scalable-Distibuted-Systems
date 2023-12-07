package server;

import paxos.PaxosAccepted;
import paxos.PaxosPromise;
import client.Request;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

// RMI Interface
public interface KVDataStore extends Remote{
	public Response putOperation(String key, String value) throws RemoteException;
	public Response getOperation(String key) throws RemoteException;
	public Response deleteOperation(String key) throws RemoteException;
	public HashMap<String, String> getKeyValueHashMap() throws RemoteException;
	public String getServerID() throws RemoteException;
	public PaxosPromise prepare(long proposalNumber) throws RemoteException;
	public PaxosAccepted accept(long proposalNumber, Request value) throws RemoteException;
	public void callLearner(PaxosAccepted accepted) throws RemoteException;
	public void registerServer(String currentServerID, KVDataStore server) throws RemoteException, AlreadyBoundException;
}

