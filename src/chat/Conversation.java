package chat;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
	String sender;
	List<String> receiver;
	
	public Conversation() {
		this.receiver = new ArrayList<String>();
		this.sender = "";
		this.receiver.add("");
	}
	
	public Conversation(String sender, String receiver) {
		this.receiver = new ArrayList<String>();
		this.sender = sender;
		this.receiver.add(receiver);
	}
	
	@Override
	public boolean equals (Object other) {
		if (other instanceof Conversation) {
			Conversation conv = (Conversation) other;
			return conv.sender.equals(this.sender) && conv.receiver.equals(this.receiver);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ("sender: " + this.sender + ", receiver: " + this.receiver);
	}
}
