import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.alibaba.fastjson.JSON;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Client {
  private Socket socket = null;
  private User user = null;
  private String host = "localhost";
  private int port = 8000;
  private DataInputStream inputFromServer;
  private DataOutputStream outputToServer;

  Client() {
    try {
      this.socket = new Socket(this.host, this.port);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      Alert alert = new Alert(AlertType.ERROR);
      alert.setContentText("服务器未启动");
      alert.show();
    } catch (IOException e) {
      e.printStackTrace();
      Alert alert = new Alert(AlertType.ERROR);
      alert.setContentText("端口关闭");
      alert.show();
    }
  }
  Client(Client client) {
    this.user = client.user;
    this.host = client.host;
    this.port = client.port;
    this.socket = client.socket;
  }

  public void stop() {
    try {
      socket.close();
    } catch (IOException e) {
    }
  }

  public LoginMessage login() {
    if (this.socket == null) {
      return null;
    }
    LoginMessage result = null;
    try {
      this.inputFromServer = new DataInputStream(socket.getInputStream());
      this.outputToServer = new DataOutputStream(socket.getOutputStream());
      outputToServer.writeUTF("login");
      outputToServer.writeUTF(user.getJson());
      String loginMessageJsonString = this.inputFromServer.readUTF();
      System.out.println("loginMessageJsonString" + loginMessageJsonString);
      result = JSON.parseObject(loginMessageJsonString, LoginMessage.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  public void listenMessage() {
    if (this.socket == null) {
      return;
    }
    try {
      while(true) {
        this.inputFromServer = new DataInputStream(socket.getInputStream());
        String operation = this.inputFromServer.readUTF();
          if(operation.equals("reponseMessage")) {
            String messageJsonString = this.inputFromServer.readUTF();
            Message message = JSON.parseObject(messageJsonString, Message.class);
            if(message.getChannel().equals("friend")) {
              // Receive new message from friend
              AppSceneController appSceneController = App.getAppSceneController();
              appSceneController.appendText(message.getSender() + " " + message.getTime() + " " + message.getContent());
              appSceneController.addMessage(message);
            }
            else {
              System.out.println("未知的消息频道");
            }
          }
          else {
            System.out.println("未实现的消息类型");
          }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setUser(int id, String password) {
    this.user = new User(id, password);
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public Socket getSocket() {
    return this.socket;
  }

  public int getUserID() {
    return this.user.getId();
  }
}