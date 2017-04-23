package chat;

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer extends Observable {
	private ArrayList<PrintWriter> clientOutputStreams;
	
	public static void main(String[] args) {
		try {
			new ChatServer().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception {
		setUpNetworking();
	}
	
	private void setUpNetworking() throws Exception {
		clientOutputStreams = new ArrayList<PrintWriter>();
		ServerSocket serverSock = new ServerSocket(3000);
		
		while (true) {
			Socket clientSocket = serverSock.accept();
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket));
			t.start();
			this.addObserver(writer);
			System.out.println("got a connection");
		}
	}
		
	class ClientHandler implements Runnable {
		private BufferedReader reader;
		
		public ClientHandler(Socket clientSocket) throws IOException {
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("server read with the following count: " + ChatClient.count);
					ChatClient.count++;
					setChanged();
					notifyObservers(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


