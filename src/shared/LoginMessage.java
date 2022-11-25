package shared;
import com.alibaba.fastjson.JSONObject;

public class LoginMessage {
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