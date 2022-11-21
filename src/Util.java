import java.util.TreeMap;

import javafx.application.Platform;

public class Util {
  public static MessageList sortByKey(MessageList messageList) {
    TreeMap<Long, Message> treeMap = new TreeMap<Long, Message>(messageList);
    MessageList result = new MessageList();
    System.out.println("sortByKey");
    for (Long key : treeMap.keySet()) {
      Message message = messageList.get(key);
      result.put(key, treeMap.get(key));
      AppSceneController controller = App.getAppSceneController();
      if(message.getType().equals("text") ) {
        Platform.runLater(() -> {
          controller.addText(message);
        });
      }
      if(message.getType().equals("file")) {
        Platform.runLater(() -> {
          controller.addFile(message, null);
        });
      }
    }
    return result;
  }

}
