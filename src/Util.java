import java.util.TreeMap;

public class Util {
  public static MessageList sortByKey(MessageList messageList) {
    TreeMap<Long, Message> treeMap = new TreeMap<Long, Message>(messageList);
    MessageList result = new MessageList();
    for (Long key : treeMap.keySet()) {
      Message message = messageList.get(key);
      result.put(key, treeMap.get(key));
      AppSceneController controller = App.getAppSceneController();
      controller.appendText(message);
    }
    return result;
  }

}
