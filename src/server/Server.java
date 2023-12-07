package server;

import paxos.PaxosAccepted;
import paxos.PaxosPromise;
import client.Request;

import java.net.*;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.*; 
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Server extends UnicastRemoteObject implements KVDataStore
{ 

	private static final long serialVersionUID = 1L;

	private String serverID;

	// Stores all the data sent from the client
	private HashMap<String,String> keyValueHashMap = new HashMap<String, String>();

	// Server log is stored in logs folder
	public Logger serverLogger;

	private Registry rmiRegistry;

	private int portNo;

	private long previousSequenceNumber;

	private Request previousTransactionValue;

	private long latestLearnedVal;

	private long randAcceptFailNo = 81l;

	private int paxosMaximumRetries = 3;

	protected Server(String serverID, Registry registry, int portNo) throws RemoteException {
		super();
		this.serverID = serverID;
		this.rmiRegistry = registry;
		this.serverLogger = getServerLogger("server.log");
		this.portNo = portNo;
	}

	public void registerServer(String currServerID, KVDataStore server) throws RemoteException{
		this.rmiRegistry.rebind(currServerID, server);
		this.serverLogger.info("New server that got registered is : "+currServerID);
	}

	public String getServerID() throws RemoteException{
		return serverID;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public HashMap<String, String> getKeyValueHashMap() throws RemoteException {
		return keyValueHashMap;
	}

	public void setKeyValueHashMap(HashMap<String, String> keyValueHashMap) {
		this.keyValueHashMap = keyValueHashMap;
	}


	public synchronized Response getOperation(String key) throws RemoteException {
		serverLogger.info("Request Query [type=" + "get" + ", key=" + key + "]");

		Response serverResponse = new Response();
		serverResponse.setResponseType("get");

		if(!keyValueHashMap.containsKey(key)){
			serverResponse.setReturnValue(null);
			serverResponse.setResponseMessage("The requested key "+key+" doesn't  exist in the storage");
		}
		else {
			String val = keyValueHashMap.get(key);
			serverResponse.setReturnValue(val);
			serverResponse.setResponseMessage("Key was retrieved successfully from HashMap Storage");
		}	

		serverLogger.info(serverResponse.toString());
		return serverResponse;
	}


	public Response putOperation(String keyToStore, String valueToStore) throws RemoteException {
		serverLogger.info("Request Query [type=" + "put" + ", key=" + keyToStore + ", value=" + valueToStore + "]");
		Request clientRequest = new Request();
		clientRequest.setRequestType("put");
		clientRequest.setKeyToSend(keyToStore);
		clientRequest.setValueRetrieved(valueToStore);

		serverLogger.info("Invoking Proposer");

		Response serverResponse = new Response();
		serverResponse.setResponseType("put");
		serverResponse.setReturnValue(null);

		try {
			callProposer(clientRequest);
			serverResponse.setResponseMessage("Successfully inserted the entry in the datastore");
		}
		catch(TimeoutException e) {
			serverResponse.setResponseMessage("Request timed out");
		}

		serverLogger.info(serverResponse.toString());
		return serverResponse;
	}

	public Response deleteOperation(String keyToDelete) throws RemoteException {
		serverLogger.info("Request Query [type=" + "delete" + ", key=" + keyToDelete + "]");
		Request deleteRequest = new Request();
		deleteRequest.setRequestType("delete");
		deleteRequest.setKeyToSend(keyToDelete);
		deleteRequest.setValueRetrieved(null);

		serverLogger.info("Invoking Proposer");
		Response serverResponse = new Response();
		serverResponse.setResponseType("delete");
		serverResponse.setReturnValue(null);

		try{
			callProposer(deleteRequest);
			serverResponse.setResponseMessage("Successfully deleted the entry from the datastore");
		}
		catch(TimeoutException e) {
			serverResponse.setResponseMessage("Request timed out");
		}

		serverLogger.info(serverResponse.toString());
		return serverResponse;

	}

	public void callProposer(Request transaction) throws AccessException, RemoteException, TimeoutException {

		boolean ifRoundFailed = true;
		int retryNumber = 1;

		while(ifRoundFailed){
			if(retryNumber > this.paxosMaximumRetries) {
				throw new TimeoutException();
			}
			retryNumber++;

			serverLogger.info("Fresh Paxos Round has been instantiated");

			long sequenceNumber = System.currentTimeMillis();
			serverLogger.info("Fresh Sequence number is "+sequenceNumber);

			List<PaxosPromise> serverPromises = new ArrayList<PaxosPromise>();
			for(String aServer: this.rmiRegistry.list()) {
				try {
					serverLogger.info("Sending prepare to server: "+aServer);
					KVDataStore server = (KVDataStore) this.rmiRegistry.lookup(aServer);
					PaxosPromise promise = server.prepare(sequenceNumber);
					serverLogger.info("Received promise from server");
					promise.setServerID(aServer);
					serverPromises.add(promise);
				}
				catch(RemoteException re) {
					serverLogger.info("Received denial");
					continue;
				} catch (NotBoundException e) {
					e.printStackTrace();
				}

			}

			if( serverPromises.size() <= this.rmiRegistry.list().length / 2) {
				try {
					serverLogger.info("Majority of acceptors didn't promise, restarting paxos run in 2 seconds");
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					serverLogger.log(Level.SEVERE, "Interrupted Exception", e);
				}
				continue;
			}

			serverLogger.info("Majority of acceptors promised");

			long maximum = 0l;
			Request requestValue = transaction;

			for(PaxosPromise aServerPromise : serverPromises) {
				if((aServerPromise.getPreviousSequenceNumber() != 0) && (aServerPromise.getPreviousSequenceNumber() > maximum)) {
					maximum = aServerPromise.getPreviousSequenceNumber();
					requestValue = aServerPromise.getPreviousTransactionValue();
				}
			}

			serverLogger.info("Value for accept: "+requestValue.toString());
			List<PaxosAccepted> acceptedPromises = new ArrayList<PaxosAccepted>();

			for(PaxosPromise promise : serverPromises) {
				try {
					serverLogger.info("Sending accept to server: "+promise.getServerID());
					String aServer = promise.getServerID();
					KVDataStore server = (KVDataStore) this.rmiRegistry.lookup(aServer);
					PaxosAccepted acceptedMessage = server.accept(sequenceNumber, requestValue);
					acceptedMessage.setServerID(promise.getServerID());
					acceptedPromises.add(acceptedMessage);
					serverLogger.info("Received accept");
				}
				catch(RemoteException re) {
					serverLogger.info("Received reject");
					continue;
				} catch (NotBoundException e) {
					serverLogger.log(Level.SEVERE, "Not Bound Exception", e);
				}
			}

			if( acceptedPromises.size() <= this.rmiRegistry.list().length / 2) {
				try {
					serverLogger.info("Majority of acceptors didn't accept, restarting paxos run in 2 seconds");
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					serverLogger.log(Level.SEVERE, "Interrupted Exception", e);
				}
				continue;
			}

			serverLogger.info("Majority of acceptors accepted");

			serverLogger.info("Invoking Learners");
			for(PaxosAccepted accepted: acceptedPromises) {
				try {
					serverLogger.info("Invoking learner: "+accepted.getServerID());
					KVDataStore server = (KVDataStore) this.rmiRegistry.lookup(accepted.getServerID());
					server.callLearner(accepted);
					serverLogger.info("Learner was able to successfully learn");
				}
				catch(RemoteException re) {
					serverLogger.info("Learner failed");
					continue;
				} catch (NotBoundException e) {
					serverLogger.log(Level.SEVERE, "Not Bound Exception", e);
				}
			}

			serverLogger.info("Learning job finished");

			ifRoundFailed = false;
		}
		serverLogger.info("Paxos round ended");
	}



	public PaxosPromise prepare(long sequenceNumber) throws RemoteException {
		if(sequenceNumber % this.randAcceptFailNo == 0l) {
			serverLogger.info("Acceptor failed at random time as per configuration");
			throw new RemoteException();
		}

		if(sequenceNumber<=this.previousSequenceNumber) {
			serverLogger.info("Prepare request Declined as previous proposal number("+this.previousSequenceNumber +") is greater than new proposal number("+sequenceNumber+")");
			throw new RemoteException();
		}

		PaxosPromise paxosPromise = new PaxosPromise();
		paxosPromise.setSequenceNumber(sequenceNumber);
		paxosPromise.setPreviousSequenceNumber(this.previousSequenceNumber);
		paxosPromise.setPreviousTransactionValue(previousTransactionValue);

		serverLogger.info("Promising for proposal number: "+sequenceNumber);
		return paxosPromise;
	}



	public PaxosAccepted accept(long sequenceNumber, Request requestValue) throws RemoteException {
		if(sequenceNumber % this.randAcceptFailNo == 0l) {
			serverLogger.info("Acceptor failed at random time as per configuration");
			throw new RemoteException();
		}

		if(sequenceNumber<this.previousSequenceNumber) {
			serverLogger.info("Accept request Declined as new proposal number("+sequenceNumber+") is less than previous proposal numberr("+this.previousSequenceNumber +")");
			throw new RemoteException();
		}

		serverLogger.info("Accept request confirmed for transaction: "+requestValue.toString());

		PaxosAccepted paxosAccepted = new PaxosAccepted();
		paxosAccepted.storeSequenceNumber(sequenceNumber);
		paxosAccepted.setTransactionValue(requestValue);

		return paxosAccepted;
	}

	public synchronized void callLearner(PaxosAccepted accepted) throws RemoteException{
		serverLogger.info("Learner instantiated");

		if(this.latestLearnedVal == accepted.findSequenceNumber()) {
			serverLogger.info("Aborting learning, value is already learned");
			throw new RemoteException();
		}

		if(accepted.getServerID() == this.serverID) {
			serverLogger.info("Erasing previous proposal number and accepted value");
			this.previousSequenceNumber = 0;
			this.previousTransactionValue = null;
		}

		Request clientRequest = accepted.getTransactionValue();

		if(clientRequest.getRequestType().equals("put")) {
			this.keyValueHashMap.put(clientRequest.getKeyToSend(), clientRequest.getValueRetrieved());
		}
		else if(clientRequest.getRequestType().equals("delete")){
			this.keyValueHashMap.remove(clientRequest.getKeyToSend());
		}
		this.latestLearnedVal = accepted.findSequenceNumber();
		serverLogger.info("Learned a new value: "+clientRequest.toString());
	}


	private static Logger getServerLogger(String serverLogFile) {
		Logger logger = Logger.getLogger("server_log");  
		FileHandler serverFileHandler;

		try {  
			File serverLog = new File(serverLogFile);
			if(!serverLog.exists()) {
				serverLog.createNewFile();
			}
			serverFileHandler = new FileHandler(serverLogFile,true);
			logger.addHandler(serverFileHandler);
			SimpleFormatter serverLogFormatted = new SimpleFormatter();
			serverFileHandler.setFormatter(serverLogFormatted);

		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return logger;
	}


	public static String generateServerID(int portNo) {
		String serverID = null;
		try {
			InetAddress IP = InetAddress.getLocalHost();
			serverID = IP.getHostAddress()+"_"+String.valueOf(portNo);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return serverID;
	}

	public static void main(String args[]) 
	{ 
		try {
			int portNumber = Integer.parseInt(args[0]);
			Registry rmiRegistery = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			String currentServerID = generateServerID(portNumber);
			Server server = new Server(currentServerID, rmiRegistery, portNumber);

			rmiRegistery.rebind("Server", server);

			server.serverLogger.info("Server started");

			InputStream inputStream = new FileInputStream("resources/config.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			String[] paxosNodes = properties.getProperty("paxos.nodes").split(",");

			boolean isRegistrySuccessful = false;
			server.serverLogger.info("Server trying to connect to a cluster");

			for(String aPaxosNode : paxosNodes)
			{
				try {
					String[] nodes = aPaxosNode.split(":");
					String paxosNodeIPAddress = nodes[0];
					int paxosNodePortNo = Integer.parseInt(nodes[1]);

					Registry paxosRegistry = LocateRegistry.getRegistry(paxosNodeIPAddress, paxosNodePortNo);

					for(String paxosServer : paxosRegistry.list()) {
						try {
							KVDataStore registeredPaxosServer = (KVDataStore)paxosRegistry.lookup(paxosServer);
							if(!currentServerID.equals(registeredPaxosServer.getServerID())) {
								isRegistrySuccessful = true;
								server.setKeyValueHashMap(registeredPaxosServer.getKeyValueHashMap());
								registeredPaxosServer.registerServer(currentServerID, server);
								server.serverLogger.info("Registered current server with server: "+registeredPaxosServer.getServerID());
								rmiRegistery.bind(registeredPaxosServer.getServerID(), registeredPaxosServer);
								server.serverLogger.info("Registered server: "+registeredPaxosServer.getServerID()+" with current server" );
							}
						}
						catch(ConnectException e) {
							continue;
						}
					}
					if(isRegistrySuccessful==true) break;
				}
				catch(Exception e){
					continue;
				}
			}

			if(!isRegistrySuccessful) {
				server.serverLogger.info("Could not connect to any cluster, acting as a standalone cluster");
			}
			else {
				server.serverLogger.info("Connected to a cluster");
			}

		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Please provide port as command line argument");
			System.exit(0);
		}
		catch(NumberFormatException e) {
			System.out.println("Please provide port as command line argument");
			System.exit(0);
		}
		catch(ConnectException e) {
			System.err.println("Could not connect to Master Server: " + e);
			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Server exception: " + e);
			System.exit(0);
		}
	}
} 