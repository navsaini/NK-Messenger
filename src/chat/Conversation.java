package chat;

public class Conversation {
	String sender;
	String receiver;
	
	public Conversation() {
		this.sender = "";
		this.receiver = "";
	}
	
	public Conversation(String sender, String receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}
}
