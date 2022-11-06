import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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
    private VBox textFlow;

    @FXML
    private Pane titlePane;

    @FXML
    private Button sendBtn;

    @FXML
    private ImageView closeImg;

    public ExecutorService pool;
    public Friend[] friendList = new MakeFriends().getFriends();
    public String[] groupList = {"群聊1", "群聊2", "群聊3", "群聊4"};
    public ConcurrentHashMap<Integer, MessageList> friendChatList = new ConcurrentHashMap<Integer, MessageList>();

    public Friend currentFriend = null;

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
        currentFriend = newValue;
        // Load from memory
        if (friendChatList.containsKey(currentFriend.getId())) {
          MessageList messageList = friendChatList.get(currentFriend.getId());
          Util.sortByKey(messageList);
        }
        else {
          MessageList messageList = JsonLoader.loadFromFile(client.getUserID(), currentFriend.getId());
          if(messageList == null) {
            System.out.println("打开了未曾聊天的用户: " + currentFriend.getId());
            textFlow.getChildren().addAll(new Label("你们已经是好友啦！\n"));
            return;
          }
          // Load from disk
          friendChatList.put(currentFriend.getId(), messageList);
          messageList = friendChatList.get(currentFriend.getId());
          Util.sortByKey(messageList);
        }
      });

    }

    @Override
    public void finalize() throws IOException {
      for(MessageList messageList : friendChatList.values()) {
        JsonLoader.saveToFile(messageList);
      }
    }

    @FXML
    void onSendMessage(ActionEvent event) {
      if(currentFriend == null || inputTextArea.getText().equals("")) {
        System.out.println("No friend selected / No message input");
        return;
      }
      Message message = new Message();
      message.setTime(System.currentTimeMillis());
      message.setType("text");
      message.setSender(client.getUserID());
      message.setReceiver(currentFriend.getId());
      message.setContent(inputTextArea.getText());
      message.setChannel("friend");
      // Add new message to memery
      if(friendChatList.containsKey(currentFriend.getId())) {
        friendChatList.get(currentFriend.getId()).put(message.getTime(), message);
      }
      else {
        MessageList messageList = new MessageList();
        messageList.put(message.getTime(), message);
        friendChatList.put(currentFriend.getId(), messageList);
      }
      // "[" + new Date(message.getTime()) + "]" + 
      Platform.runLater(() -> {
        appendText(message);
      });
      System.out.println("Message: " + textFlow.getChildren().size());
      inputTextArea.clear();
      try {
        SendMessageTask sendMessageTask = new SendMessageTask(message);
        pool.submit(sendMessageTask);
      } catch (IOException e) {
        e.printStackTrace();
      }
      Platform.runLater(() -> {
        JsonLoader.saveToFile(friendChatList.get(currentFriend.getId()));
      });
    }
    
    @FXML
    void onClickTest(ActionEvent event) {
      System.out.println(inputTextArea.getText());
      // System.out.println(JsonLoader.loadFromFile("./src/Log/Friend/tst.json"));
    }

    @FXML
    void onCloseClick(MouseEvent event) {
      Platform.exit();
    }

    
    @FXML
    void onEnterReleased(KeyEvent event) {
      if(event.getCode() == KeyCode.ENTER) {
        onSendMessage(null);
      }
    }

    public boolean setClient(Client client) {
      this.client = client;
      Runnable listenTask = () -> {
        client.listenMessage();
      };
      new Thread(listenTask).start();
      return true;
    }

    public void addMessage(Message message) {

      if(message.getChannel().equals("friend")) {
        long timestamp = message.getTime();
        int senderID = message.getSender();
        if(friendChatList.containsKey(senderID)) {
          friendChatList.get(senderID).put(timestamp, message);
        }
        else {
          MessageList messageList = new MessageList();
          messageList.put(timestamp, message);
          friendChatList.put(senderID, messageList);
        }
        if(currentFriend != null && currentFriend.getId() == senderID) {
          Platform.runLater(() -> {
            appendText(message);
          });
        }
      }
    }

    public void setFriendList(Friend[] friendList) {
      this.friendList = friendList;
    }

    public void setGroupList(String[] groupList) {
      this.groupList = groupList;
    }

    public void appendText(Message message) {
      if(message.getType().equals("text")) {
        Date date = new Date(message.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
         Label label = new Label("[" + dateString + "]"  + message.getSender() + ": \n" + message.getContent() + "\n");

        if(message.getSender() == client.getUserID()) {
          label.setAlignment(Pos.CENTER_RIGHT);
          label.setTextFill(Color.BLUE);
        }
        else {
          label.setTextFill(Color.BLACK);
        }
        textFlow.getChildren().addAll(label);
      }
    }

    class SendMessageTask implements Callable<Void> {
      private Message message;
      // private DataInputStream inputFromServer;
      private DataOutputStream outputToServer;

      SendMessageTask(Message message) throws IOException {
        this.message = message;
        // this.inputFromServer = client.getInputFromServer();
        this.outputToServer = client.getOutputToServer();
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