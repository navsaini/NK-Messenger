package chat;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
	String sender;
	List<String> receivers;
	String conversationText;
	
	public Conversation() {
		this.receivers = new ArrayList<String>();
		this.sender = "";
		this.receivers.add("");
		this.conversationText = "";
	}
	
	public Conversation(String sender, String receiver) {
		this.receivers = new ArrayList<String>();
		this.sender = sender;
		this.receivers.add(receiver);
		this.conversationText = "";
	}
	
	public Conversation(String sender, List<String> receiver) {
		this.receivers = receiver;
		this.sender = sender;
		this.conversationText = "";
	}
	
	public void addReceiver(String receiver) {
		this.receivers.add(receiver);
	}
	
	public void addTextToConv(String text) {
		conversationText += text + "\n";
	}
	
	@Override
	public boolean equals (Object other) {
		if (other instanceof Conversation) {
			Conversation conv = (Conversation) other;
			return conv.sender.equals(this.sender) && conv.receivers.equals(this.receivers);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ("sender: " + this.sender + ", receiver: " + this.receivers);
	}
}
