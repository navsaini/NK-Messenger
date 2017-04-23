package chat;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
	private JTextArea incoming; 
	private JTextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	
	static int count = 0;
	
	public static void main(String[] args) {
		try {
			new ChatClient().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception {
		initView();
		setUpNetworking();
	}

	private void initView() {
		JFrame frame = new JFrame("Ludicrously Simple Chat Client"); 
		JPanel mainPanel = new JPanel(); 
		incoming = new JTextArea(15, 50); 
		incoming.setLineWrap(true); 
		incoming.setWrapStyleWord(true); 
		incoming.setEditable(false); 
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		outgoing = new JTextField(20); 
		JButton sendButton = new JButton("Send"); 
		sendButton.addActionListener(new SendButtonListener()); 
		mainPanel.add(qScroller); 
		mainPanel.add(outgoing); 
		mainPanel.add(sendButton); 
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel); 
		frame.setSize(650, 500); 
		frame.setVisible(true);
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 3000);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new ClientObserver(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			System.out.println("action performed: " + count);
			count++;
			writer.println(outgoing.getText());
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}

	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("incoming append count: " + count);
					count++;
					incoming.append(message + "\n");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
}
