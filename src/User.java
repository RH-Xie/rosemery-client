import com.alibaba.fastjson.JSONObject;

public class User {
  private int id = 0;
  private String password = "";
  private String token = "";
  private String avatar = "";
  private String nickname = "";
  private String signature = "";
  
  JSONObject jsonObject = null;

  public User(int id, String password) {
    this.id = id;
    this.password = password;

    this.jsonObject = new JSONObject();
    this.jsonObject.put("id", id);
    this.jsonObject.put("password", password);
  }

  public int getId() {
    return this.id;
  }

  public String getAvatar() {
    return this.avatar;
  }

  public String getNickname() {
    return this.nickname;
  }

  public String getSignature() {
    return this.signature;
  }

  public String getPassword() {
    return this.password;
  }

  public String getJson() {
    return this.jsonObject.toString();
  }

  public String getToken() {
    return this.token;
  }

  // Setters
  public void setToken(String token) {
    this.token = token;
    this.jsonObject.put("token", token);
  }
}
class LoginMessage {
  public boolean isSuccess = false;
  public String token = "";
  private JSONObject jsonObject = null;

  LoginMessage() {
  }

  LoginMessage(boolean isSuccess, String token) {
    this.isSuccess = isSuccess;
    this.token = token;
    // new JSONObject() is necessary here because JSONObject.toString
    this.jsonObject = new JSONObject();
    this.jsonObject.put("isSuccess", this.isSuccess);
    this.jsonObject.put("token", this.token);
  }

  public boolean getIsSuccess() {
    return this.isSuccess;
  }

  public String getToken() {
    return this.token;
  }

  public String getJson() {
    return this.jsonObject.toString();
  }
}
class Friend {
  private int id = 0;
  private String nickname = "";
  private String avatar = "";
  private String signature = "";
  private JSONObject jsonObject = null;

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