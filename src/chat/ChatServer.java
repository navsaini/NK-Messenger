package chat;

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// hello

public class ChatServer extends Observable {
	private ArrayList<PrintWriter> clientOutputStreams;
	static int num = 1;
	
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
			ClientHandler c = new ClientHandler(clientSocket);
			c.setName("" + num);
			num++;
			Thread t = new Thread(c);
			t.start();
			this.addObserver(writer);
			System.out.println("got a connection");
		}
	}
		
	class ClientHandler implements Runnable {
		private BufferedReader reader;
		String name;
		
		public ClientHandler(Socket clientSocket) throws IOException {
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
		
		public String setName(String name) {
			this.name = name;
			return name;
		}
		
		// reads messages sent to server
		@Override
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("server read with the following count: " + ChatClient.count);
					ChatClient.count++;
					setChanged();
					notifyObservers(name + ": " + message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


