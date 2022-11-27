package shared;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class JsonLoader {
  public static MessageList loadFriendLog(int senderID, int receiverID) {
    int less = senderID < receiverID ? senderID : receiverID;
    int more = senderID > receiverID ? senderID : receiverID;
    String filename = "./src/Log/friend/" + less + "-" + more + ".json";
    Path path = Path.of(filename);
    if(!Files.exists(path)) {
      return null;
    }
    try {
      String jsonString = Files.readString(path);
      JSONArray jsonArray = JSON.parseArray(jsonString);
      MessageList messages = new MessageList();
      for(Object jsonObject : jsonArray) {
        MessageObject messageObject = JSON.parseObject(JSON.toJSONString(jsonObject), MessageObject.class);
        Message message = messageObject.getMessage();
        // BUG: message sender and receiver are not set
        messages.put(messageObject.getTimestamp(), message);
      }
      
      return messages;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static MessageList loadGroupLog(int groupID) {
    String filename = "./src/Log/group/" + groupID + ".json";
    Path path = Path.of(filename);
    if(!Files.exists(path)) {
      return null;
    }
    try {
      String jsonString = Files.readString(path);
      JSONArray jsonArray = JSON.parseArray(jsonString);
      MessageList messages = new MessageList();
      for(Object jsonObject : jsonArray) {
        MessageObject messageObject = JSON.parseObject(JSON.toJSONString(jsonObject), MessageObject.class);
        Message message = messageObject.getMessage();
        messages.put(messageObject.getTimestamp(), message);
      }
      return messages;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }


  public static void saveFriendLog(MessageList messages) {
    if(messages == null) {
      return;
    }
    Iterator<Message> iterator = messages.values().iterator();
    Message firstMessage = iterator.next();
    int senderID = firstMessage.getSender();
    int receiverID = firstMessage.getReceiver();
    int less = senderID < receiverID ? senderID : receiverID;
    int more = senderID > receiverID ? senderID : receiverID;
    // 自应知何处存放
    Path path = Path.of("./src/Log/" + firstMessage.getChannel() + "/" + less + "-" + more + ".json");
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

  public static void saveGroupLog(MessageList messages, int groupID) {
    if(messages == null) {
      return;
    }
    Iterator<Message> iterator = messages.values().iterator();
    Message firstMessage = iterator.next();
    Path path = Path.of("./src/Log/" + firstMessage.getChannel() + "/" + groupID + ".json");
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
    this.timestamp = timestamp;
    jsonObject.put("time", timestamp);
  }
  public Message getMessage() {
    return message;
  }
  public void setMessage(Message message) {
    this.message = message;
    jsonObject.put("message", message.getJson());
  }
  public String getJson() {
    return jsonObject.toString();
  }
}

