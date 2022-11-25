package shared;
import java.util.concurrent.ConcurrentHashMap;

public class MessageList extends ConcurrentHashMap<Long, Message> {
  public MessageList() {
    super();
  }
}