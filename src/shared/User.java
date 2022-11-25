package shared;
import java.util.HashMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class User {
  private int id = 0;
  private String password = "";
  private String token = "";
  private String avatar =  "./avatar/1.jpg";
  private String nickname = "";
  private String signature = "";
  private HashMap<Integer, Group> groupList = null;
  
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

  public HashMap<Integer, Group> getGroupList() {
    return this.groupList;
  }

  // Setters
  public void setToken(String token) {
    this.token = token;
    this.jsonObject.put("token", token);
  }

  public User setGroupList(HashMap<Integer, Group> groupList) {
    this.groupList = groupList;
    JSONArray jsonArray = new JSONArray();
    for(Group group : groupList.values()) {
      jsonArray.add(group.getJson());
    }
    this.jsonObject.put("groupList", this.groupList);
    return this;
  }

  public User setAvatar(String avatar) {
    this.avatar = avatar;
    this.jsonObject.put("avatar", avatar);
    return this;
  }

  public User setNickname(String nickname) {
    this.nickname = nickname;
    this.jsonObject.put("nickname", nickname);
    return this;
  } 
}