package App.Search;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import App.App;
import App.AppSceneController;
import Client.Client;
import Client.Client.FindTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import shared.Friend;
import shared.Group;
import shared.Message;

public class SearchSceneController {
    private long timestamp = 0;
    private Client client;

    @FXML
    private RadioButton friendRadio;

    @FXML
    private RadioButton groupRadio;

    @FXML
    private TextField searchInput;

    @FXML
    private ToggleGroup type;

    @FXML
    void onEnterPressed(ActionEvent event) {
      if (searchInput.getText().length() == 0) {
        return;
      }
      // 节流
      if(System.currentTimeMillis() - timestamp < 500) {
        return;
      }
      timestamp = System.currentTimeMillis();
      Message message = new Message();
      message.setSender(client.getUserID());
      message.setContent(searchInput.getText());
      message.setTime(System.currentTimeMillis());

      if (friendRadio.isSelected()) {
        System.out.println("Find Friend");
        message.setType("friend");
      }
      else if (groupRadio.isSelected()) {
        System.out.println("Find Group");
        message.setType("group");
      }
      FindTask task = client.createFindTask(message);
      // 等待搜索
      while(!task.isDone()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      responseToSearch(message, task);
    }

    public void setClient(Client client) {
      this.client = client;
    }

    public void responseToSearch(Message message, FindTask task) {
      // 
      if(message.getType().equals("friend")) {
        if(task.getResult() == -1) {
          Alert a = new Alert(Alert.AlertType.ERROR);
          a.setContentText("查无此人");
          a.setTitle("搜索失败");
          a.show();
          return;
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("已添加");
        a.setTitle("搜索成功"); 
        a.show();
        AppSceneController app = App.getAppSceneController();
        Friend friend = task.getFriend();
        System.out.println(friend.getJson());
        app.addFriend(friend);
      }
      else if(message.getType().equals("group")) {
        if(task.getResult() == -1) {
          Alert a = new Alert(Alert.AlertType.INFORMATION);
          a.setContentText("新建群聊");
          a.setTitle("已创建新群聊");
          a.show();
          AppSceneController app = App.getAppSceneController();
          Group group = task.getGroup();
          app.addGroup(group);
          return;
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("已添加");
        a.setTitle("搜索成功"); 
        a.show();
        AppSceneController app = App.getAppSceneController();
        Group group = task.getGroup();
        System.out.println(group.getJson());
        app.addGroup(group);
      }

    }
  }
