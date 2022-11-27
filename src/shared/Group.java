package shared;
import java.util.HashMap;
import java.util.HashSet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Group {
  private int groupID = 0;
  private int ownerID = 0;
  private HashSet<Friend> member = new HashSet<Friend>();
  private int state = 0;
  private String name = "";
  private JSONObject jsonObject = null;

  public Group(int groupID, int ownerID, String name, HashSet<Friend> member, int state) {
    this.groupID = groupID;
    this.ownerID = ownerID;
    this.name = name;
    this.member = member;
    this.state = state;

    this.jsonObject = new JSONObject();
    this.jsonObject.put("groupID", groupID);
    this.jsonObject.put("ownerID", ownerID);
    this.jsonObject.put("name", name);
    this.jsonObject.put("member", member);
    this.jsonObject.put("state", state);
  }

  // public Group(int groupID, int state) {
  //   jsonObject = new JSONObject();
  //   this.groupID = groupID;
  //   this.state = state;
  // }

  public Group() {
    this.jsonObject = new JSONObject();
  }

  public int getGroupID() {
    return groupID;
  }
  public void setGroupID(int groupID) {
    this.groupID = groupID;
    this.jsonObject.put("groupID", groupID);
  }
  public int getState() {
    return state;
  }
  public void setState(int state) {
    this.state = state;
    this.jsonObject.put("state", state);
  }
  public String getJson() {
    return jsonObject.toString();
  }
  public void addMember(Friend friend) {
    member.add(friend);
  }
  public void removeMember(Friend friend) {
    member.remove(friend);
  }
  public HashSet<Friend> getMember() {
    return member;
  }
  public int getOwnerID() {
    return ownerID;
  }
  public void setOwnerID(int ownerID) {
    this.ownerID = ownerID;
    this.jsonObject.put("ownerID", ownerID);
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
    this.jsonObject.put("name", name);
  }
}

class GroupList{
  private HashMap<Integer, Group> groupList = new HashMap<Integer, Group>();
  private JSONObject jsonObject;
  public void addGroup(Group group) {
    groupList.put(group.getGroupID(), group);
    updateGroup();
  }
  public void removeGroup(Group group) {
    groupList.remove(group.getGroupID());
    updateGroup();
  }
  public HashMap<Integer, Group> getGroupList() {
    return groupList;
  }

  public Group getGroup(int groupID) {
    return groupList.get(groupID);
  }

  private void updateGroup() {
    JSONArray array = new JSONArray();
    // Update the json object
    for(Group g: groupList.values()) {
      array.add(g.getJson());
    }
    this.jsonObject.put("groupList", array);
  }
}