import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AppSceneController implements Initializable{

    private Client client = null;

    @FXML
    private ListView<Friend> friendListView;

    @FXML
    private ListView<String> groupListView;

    @FXML
    private TabPane tabPane;
    
    @FXML
    private TextArea inputTextArea;

    @FXML
    private TextFlow textFlow;

    @FXML
    private Pane titlePane;

    @FXML
    private Button sendBtn;

    @FXML
    private Button testBtn;

    ExecutorService pool;
    Friend[] friendList = new MakeFriends().getFriends();
    String[] groupList = {"群聊1", "群聊2", "群聊3", "群聊4"};
    ConcurrentHashMap<Integer, MessageList> friendChatList = new ConcurrentHashMap<Integer, MessageList>();

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
      System.out.println("Friends: " + friendList.length);
      this.pool = Executors.newFixedThreadPool(20);
      friendListView.getItems().addAll(friendList);
      friendListView.setCellFactory(param -> new ListCell<Friend>() {
        @Override
        protected void updateItem(Friend friend, boolean empty) {
          super.updateItem(friend, empty);

          if (empty || friend == null || friend.getNickname() == null) {
              setText(null);
          } else {
              setText(friend.getNickname());
          }
  
        }
      });

      System.out.println("Initializing " + location);
      groupListView.getItems().addAll(groupList);

      friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        textFlow.getChildren().clear();
        // Load from memory
        if (friendChatList.containsKey(newValue.getId())) {
          for(Message message : friendChatList.get(newValue.getId()).values()) {
            Text text = new Text("[" + message.getTime() + "]" + message.getSender() + ": " + message.getContent());
            textFlow.getChildren().add(text);
          }
        }
        else {
          friendChatList.put(newValue.getId(), new MessageList());
          System.out.println("打开了未曾聊天的用户: " + newValue.getId());
        }
      });
    }

    @FXML
    void onSendMessage(ActionEvent event) {
      Friend friend = friendListView.getSelectionModel().getSelectedItem();
      Message message = new Message();
      message.setTime(System.currentTimeMillis());
      message.setType("text");
      message.setSender(client.getUserID());
      message.setReceiver(friendListView.getSelectionModel().selectedItemProperty().getValue().getId());
      message.setContent(inputTextArea.getText());
      message.setChannel("friend");
      friendChatList.get(friend.getId()).put(message.getTime(), message);
      Text text = new Text("[" + message.getTime() + "]" + message.getSender() + ": " + message.getContent());
      textFlow.getChildren().add(text);
      inputTextArea.clear();
      SendMessageTask sendMessageTask;
      try {
        sendMessageTask = new SendMessageTask(message);
        pool.submit(sendMessageTask);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    @FXML
    void onClickTest(ActionEvent event) {
      System.out.println(inputTextArea.getText());
      // System.out.println(JsonLoader.loadFromFile("./src/Log/Friend/tst.json"));
    }

    public boolean setClient(Client client) {
      this.client = client;
      return true;
    }

    public void addMessage(Message message) {
      friendChatList.get(message.getSender()).put(message.getTime(), message);
    }

    public void setFriendList(Friend[] friendList) {
      this.friendList = friendList;
    }

    public void setGroupList(String[] groupList) {
      this.groupList = groupList;
    }

    public void appendText(String text) {
      Text t = new Text(text);
      textFlow.getChildren().add(t);
    }

    class SendMessageTask implements Callable<Void> {
      private Message message;
      private DataInputStream inputFromServer;
      private DataOutputStream outputToServer;

      SendMessageTask(Message message) throws IOException {
        this.message = message;
      }

      @Override
      public Void call() throws Exception{
        try {
          inputTextArea.setText("");
          outputToServer.writeUTF("message");
          outputToServer.writeUTF(this.message.getJson());
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    }
}