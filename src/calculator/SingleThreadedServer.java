package calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a single-threaded TCP server. It is able to interact
 * with only one client at the time. If a client tries to connect when
 * the server is busy with another one, it will have to wait.
 * 
 * @author Olivier Liechti
 */
public class SingleThreadedServer {
		
	final static Logger LOG = Logger.getLogger(SingleThreadedServer.class.getName());
	
	int port;
	
	/**
	 * Constructor
	 * @param port the port to listen on 
	 */
	public SingleThreadedServer(int port) {
		this.port = port;
	}
	
	/**
	 * This method initiates the process. The server creates a socket and binds
	 * it to the previously specified port. It then waits for clients in a infinite
	 * loop. When a client arrives, the server will read its input line by line
	 * and send back the data converted to uppercase. This will continue until
	 * the client sends the "BYE" command.
	 */
	public void serveClients() {
		ServerSocket serverSocket;
		Socket clientSocket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
			return;
		}
				
		while (true) {
			try {
				
				LOG.log(Level.INFO, "Waiting (blocking) for a new client on port {0}", port);				
				clientSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream());
				String line;
				boolean shouldRun = true;
				
				out.println("Welcome to the Single-Threaded Server.\nSend me text lines and conclude with the BYE command.");
				out.flush();
				LOG.info("Reading until client sends BYE or closes the connection...");		
				out.println("What computation do you want ?");
				out.flush();
				while ( (shouldRun) && (line = in.readLine()) != null ) {
					if (line.equalsIgnoreCase("bye")) {
						shouldRun = false;
						continue;
					}

					line = line.replace(" ", "");
					
			        String[] strNumber = line.split("[/*+-]"); 
			        String[] strOperation = line.split("[0-9]");
					
					if(strNumber.length != strOperation.length) {
						out.println("Seule les nombres et les caracteres suivants sont autorise [*/+-]");
						out.println("What computation do you want ?");
						out.flush();
						continue;
					}
					
					try 
					{
						float result = Float.parseFloat(strNumber[0]);
						
						for(int i = 1; i < strNumber.length; ++i) {
						
							switch(strOperation[i]) {
							case "+":
								result += Float.parseFloat(strNumber[i]);
								break;
							case "-":
								result -= Float.parseFloat(strNumber[i]);
								break;
							case "*":
								result *= Float.parseFloat(strNumber[i]);
								break;
							case "/":
								result /= Float.parseFloat(strNumber[i]);
								break;
							default:
								out.println("Seule les operateurs suivants sont autorise [*/+-]");
								break;
							}
						}
						
						out.println(line + " = " + result);
					} catch (NumberFormatException nfe) {
						LOG.info("Erreur : " + nfe.getMessage());
						out.println("Erreur interne");
					}
					// pour permettre que le message s'affiche en premier
					out.println("What computation do you want ?");
					out.flush();
				}
				
				LOG.info("Cleaning up resources...");				
				clientSocket.close();
				in.close();
				out.close();
				
			} catch (IOException ex) {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ex1) {
						LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
					}
				}
				if (out != null) {
					out.close();
				}
				if (clientSocket != null) {
					try {
						clientSocket.close();
					} catch (IOException ex1) {
						LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
					}
				}
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}	
	}
}
