package client;

import server.KVDataStore;
import server.Response;

import java.rmi.Naming;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.*; 

public class Client 
{ 

	private static KVDataStore keyValueDataStore;

	private String hostAddress;
	private int portNo;

	private BufferedReader inputReader =  new BufferedReader(new InputStreamReader(System.in));

	// Client log is stored in logs folder
	private Logger clientLogger = retrieveClientLogger("client.log");

	public Client(String hostAddress, int portNo)
	{ 
		this.hostAddress = hostAddress;
		this.portNo = portNo;
	} 

	public void launchClient() {
		while(true) {
			try
			{
				keyValueDataStore = (KVDataStore) Naming.lookup("//"+ hostAddress +":"+ portNo +"/Server");

				System.out.println("Remote connection established  [ host:"+ hostAddress +", port:"+ portNo +" ]");
				clientLogger.info("Remote connection established  [ host:"+ hostAddress +", port:"+ portNo +" ]");


				String instructionsToExecute ="";

				while (!instructionsToExecute.equals("exit"))
				{ 
					try
					{ 
						// Reads the request command from the user
						System.out.print("Enter request type (put/get/delete/exit): ");
						inputReader = new BufferedReader(new InputStreamReader(System.in));
						instructionsToExecute = inputReader.readLine().toLowerCase().trim();


						if(instructionsToExecute.equals("put")) {
							System.out.print("Enter the key to be stored: ");
							String keyToStore = inputReader.readLine();
							System.out.print("Enter value to be stored for the above key: ");
							String valueToTheKey = inputReader.readLine();
							clientLogger.info("Request Query [ipaddress=" + this.hostAddress + ", type=" + instructionsToExecute + ", key=" + keyToStore + ", value=" + valueToTheKey + "]");

							Response serverResponse = keyValueDataStore.putOperation(keyToStore, valueToTheKey);
							clientLogger.info(serverResponse.toString());
							System.out.println("Server Response Message: "+serverResponse.getResponseMessage());

						}
						else if(instructionsToExecute.equals("get")) {
							System.out.print("Enter the key to be retrieved: ");
							String key = inputReader.readLine();
							clientLogger.info("Request Query [ipaddress=" + this.hostAddress + ", type=" + instructionsToExecute + ", key=" + key + "]");

							Response serverResponse = keyValueDataStore.getOperation(key);
							clientLogger.info(serverResponse.toString());
							System.out.println("Response Message: "+serverResponse.getResponseMessage());
							System.out.println("Response result: "+serverResponse.getReturnValue());
						}
						else if(instructionsToExecute.equals("delete")) {
							System.out.print("Enter the key to be deleted: ");
							String key = inputReader.readLine();
							clientLogger.info("Request Query [ipaddress=" + this.hostAddress + ", type=" + instructionsToExecute + ", key=" + key + "]");

							Response serverResponse = keyValueDataStore.delete(key);
							clientLogger.info(serverResponse.toString());
							System.out.println("Response Message: "+serverResponse.getResponseMessage());
						}
					}
					catch(Exception e) {
						System.out.println("Request cannot be completed, trying to re-establish connection");
						clientLogger.log(Level.SEVERE, "Request cannot be completed", e);
						break;
					}

				}
				
				if(instructionsToExecute.equals("exit")) {
					break;
				}
			}
			catch (Exception e) {
				System.out.println("Remote connection failed, trying again in 5 seconds");
				clientLogger.log(Level.SEVERE, "Remote connection failed", e);
				
				
				// wait for 5 seconds before trying to re-establish connection
				try {
					Thread.sleep(5000);
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}
			}
		}

	}

	private Logger retrieveClientLogger(String clientLogFile) {
		Logger clientLogger = Logger.getLogger("client_log");
		clientLogger.setUseParentHandlers(false);

		FileHandler clientLogFileHandler;

		try {  
			File clientLog = new File(clientLogFile);
			if(!clientLog.exists()) {
				clientLog.createNewFile();
			}
			clientLogFileHandler = new FileHandler(clientLogFile,true);
			clientLogger.addHandler(clientLogFileHandler);
			SimpleFormatter logFileFormatter = new SimpleFormatter();
			clientLogFileHandler.setFormatter(logFileFormatter);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return clientLogger;
	}

	public static void main(String args[]) 
	{ 
		try {
			Client requestFromClient = new Client(args[0], Integer.parseInt(args[1]));
			requestFromClient.launchClient();
		} 
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Provie host name and port number as a part of command line arguments");
		}
		catch(NumberFormatException e) {
			System.out.println("Provide host name as a string and port number as a positive Integer");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	} 
} 

