import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class JsonLoader {
  private JSONObject jsonObject = null;
  public JsonLoader() {
    this.jsonObject = new JSONObject();
  }
  public JsonLoader(JSONObject jsonObject) {
    this.jsonObject = jsonObject;
  }
  public JSONObject getJsonObject() {
    return this.jsonObject;
  }
  public String getJson() {
    return this.jsonObject.toString();
  }
  public void setJsonObject(JSONObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public static MessageList loadFromFile(int senderID, int receiverID) {
    int less = senderID < receiverID ? senderID : receiverID;
    int more = senderID > receiverID ? senderID : receiverID;
    String filename = "./src/Log/Friend/" + less + "-" + more + ".json";
    Path path = Path.of(filename);
    if(!Files.exists(path)) {
      return null;
    }
    try {
      String jsonString = Files.readString(path);
      JSONArray jsonArray = JSON.parseArray(jsonString);
      MessageList messages = new MessageList();
      for(Object jsonObject : jsonArray) {
        MessageObject messageObject = JSON.parseObject(jsonObject.toString(), MessageObject.class);
        Message message = messageObject.getMessage();
        messages.put(messageObject.getTimestamp(), message);
        System.out.println("Loading " + JSON.toJSONString(jsonObject));
      }
      
      return messages;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static void saveToFile(MessageList messages) {
    if(messages == null) {
      return;
    }
    Iterator<Message> iterator = messages.values().iterator();
    Message firstMessage = iterator.next();
    // 自应知何处存放
    Path path = Path.of(firstMessage.getSender() + "-" + firstMessage.getReceiver() + ".json");
    if(!Files.exists(path)) {
      try {
        Files.createFile(path);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    JSONArray jsonArray = new JSONArray();
    for(Message message : messages.values()) {
      MessageObject messageObject = new MessageObject();
      messageObject.setMessage(message);
      messageObject.setTimestamp(message.getTime());
      jsonArray.add(messageObject);
    }
    try {
      Files.writeString(path, jsonArray.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

class MessageObject {
  private long timestamp = 0;
  private Message message = null;
  private JSONObject jsonObject = null;

  MessageObject() {
    this.jsonObject = new JSONObject();
    this.message = new Message();
  }

  public long getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(long timestamp) {
    jsonObject.put("time", timestamp);
    this.timestamp = timestamp;
  }
  public Message getMessage() {
    return message;
  }
  public void setMessage(Message message) {
    jsonObject.put("message", message.getJson());
    this.message = message;
  }
  public String getJson() {
    return jsonObject.toString();
  }
}

class MessageList extends ConcurrentHashMap<Long, Message> {
  public MessageList() {
    super();
  }
}