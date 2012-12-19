package com.TournamentBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private static ServerThread st;
    private static IRCClient ircClient;
    
	public SocketServer (IRCClient ircClient) {
		SocketServer.ircClient = ircClient;
		st = new ServerThread();
		new Thread(st).start();
	}
	
	public static class ServerThread implements Runnable {
		ServerThread() {
		}
		
		public void run() {
			ServerSocket server = null;
			try {
				server = new ServerSocket(2000);
			} catch (IOException e) {
			}
			
			boolean listening = true;
			
			while (listening) { /* Forever! */
				try {
					Client client = new Client(server.accept()); //Get the connected client.
					ClientProcessor cp = new ClientProcessor(client);  //run the clientprocessor class in a new thread - this proccesses the client's IO
					new Thread(cp).start();
				} catch (IOException e) {
				}
			}
		}
	} //endServerThread
	
	private static class Client extends Socket { //the conneted client.
		private Socket socket;
	   	
		Client(Socket socket) {
			this.socket = socket;
		}
	}
	
	private static class ClientProcessor implements Runnable {
		private Client client;
		private PrintWriter out;
		private BufferedReader in;
		private String message;

		ClientProcessor(Client client) {
			this.client = client;
			
			try {
				out = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(
								client.socket.getOutputStream())), true);
				in = new BufferedReader(new InputStreamReader(
						client.socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        		
		public void run() {
			try {
				if (out != null) {
					out.print("Welcome client!\n"); //this is just debug.
					out.flush();
				}
				
				do { //This can be indefinite, but it's not with the current setup because the server always closes the connection after the first message.
					try {	
						if (in != null && (message = in.readLine()) != null) {
							//System.out.println (message);	 //DEBUG
							String[] params = message.split (" ");
							
							if (params[0].equals ("WIN")) {
								//WIN HANDLER
								ircClient.win(params[1]); //let the irc client know who has won.
							}
							
							out.close();
							in.close();
							client.close();
							break;
						}
					} catch (Exception e) {
						//System.out.println (e);
						out.close();
						in.close();
						client.close();
						break;
					}
				} while (message != null && !message.equals("bye"));
				
			} catch (IOException e) {
				//System.out.println (e);
				out.close();
				try {
					in.close();
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
