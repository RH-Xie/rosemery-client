import java.util.concurrent.ConcurrentHashMap;

public class Chat {
  private ConcurrentHashMap<Long, Message> messages = new ConcurrentHashMap<Long, Message>();
  private String type = "Friend";
  private int senderID = 0;
  private int receiverID = 0;
  Chat() {

  }
  public Message getMessage(Long timestamp) {
    return messages.get(timestamp);
  }
  public int getReceiverID(Long timestamp) {
    return receiverID;
  }
  public int getSenderID(Long timestamp) {
    return senderID;
  }
  public String getType() {
    return type;
  }

  public void setReceiverID(int receiverID) {
    this.receiverID = receiverID;
  }
  public void setSenderID(int senderID) {
    this.senderID = senderID;
  }
  public void setType(String type) {
    this.type = type;
  }
  

  public boolean addMessage(Message message) {
    if(messages == null) {
      return false;
    }
    messages.put(message.getTime(), message);
    return true;
  }

  public void loadMessages() {
    int less = senderID < receiverID ? senderID : receiverID;
    int more = senderID > receiverID ? senderID : receiverID;
    JsonLoader.loadFromFile("./src/Log/" + type + "/" + less + "-" + more + ".json");
  }
}
