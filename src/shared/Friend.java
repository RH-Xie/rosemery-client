package shared;

import com.alibaba.fastjson.JSONObject;

public class Friend {
  private int id = 0;
  private String nickname = "";
  private String avatar =  "./avatar/1.jpg";
  private String signature = "";
  private JSONObject jsonObject = null;

  public Friend(User user) {
    this.id = user.getId();
    this.nickname = user.getNickname();
    this.avatar = user.getAvatar();
    this.signature = user.getSignature();
    this.jsonObject = new JSONObject();
    this.jsonObject.put("id", this.id);
    this.jsonObject.put("nickname", this.nickname);
    this.jsonObject.put("avatar", this.avatar);
    this.jsonObject.put("signature", this.signature);
  }

  public Friend(int id, String nickname, String avatar, String signature) {
    this.id = id;
    this.nickname = nickname;
    this.avatar = avatar;
    this.signature = signature;

    this.jsonObject = new JSONObject();
    this.jsonObject.put("id", id);
    this.jsonObject.put("nickname", nickname);
    this.jsonObject.put("avatar", avatar);
    this.jsonObject.put("signature", signature);
  }

  public int getId() {
    return this.id;
  }

  public String getNickname() {
    return this.nickname;
  }

  public String getAvatar() {
    return this.avatar;
  }

  public String getSignature() {
    return this.signature;
  }

  public String getJson() {
    return this.jsonObject.toString();
  }
  @Override
  public String toString() {
    return this.nickname;
  }
}
