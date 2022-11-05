import com.alibaba.fastjson.JSONObject;

public class Message {
  private long time;
  private String type;
  private String channel;
  private String content;
  private int sender;
  private int receiver;
  private JSONObject jsonObject = null;

  Message() {
    this.time = 0;
    this.type = "text";
    this.content = "";
    this.sender = 0;
    this.receiver = 0;
    this.channel = "friend";
    this.jsonObject = new JSONObject();
  }

  public long getTime() {
    return this.time;
  }

  public String getType() {
    return this.type;
  }

  public String getContent() {
    return this.content;
  }

  public int getSender() {
    return this.sender;
  }

  public int getReceiver() {
    return this.receiver;
  }

  public String getChannel() {
    return this.channel;
  }

  public String getJson() {
    return this.jsonObject.toString();
  }

  @Override
  public String toString() {
    return this.type;
  }

  public void setTime(long time) {
    this.time = time;
    this.jsonObject.put("time", time);
  }

  public void setType(String type) {
    this.type = type;
    this.jsonObject.put("type", type);
  }

  public void setContent(String content) {
    this.content = content;
    this.jsonObject.put("content", content);
  }

  public void setSender(int sender) {
    this.sender = sender;
    this.jsonObject.put("sender", sender);
  }

  public void setReceiver(int receiver) {
    this.receiver = receiver;
    this.jsonObject.put("receiver", receiver);
  }

  public void setChannel(String channel) {
    this.channel = channel;
    this.jsonObject.put("channel", channel);
  }
}
