package App;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.event.ChangeListener;

import Client.Client;
import Client.Client.ReceiveFileTask;

import java.awt.Desktop;


import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import shared.Friend;
import shared.Group;
import shared.JsonLoader;
import shared.MakeFriends;
import shared.Message;
import shared.MessageList;
import shared.Util;

public class AppSceneController implements Initializable{

    private Client client = null;
    private double xOffset;
    private double yOffset;
    private Stage appStage = App.getAppStage();

    @FXML
    private ListView<Friend> friendListView;

    @FXML
    private ListView<Group> groupListView;

    @FXML
    private TabPane tabpane;
    
    @FXML
    private TextArea inputTextArea;

    @FXML
    private VBox textFlow;

    @FXML
    private Pane titlePane;

    @FXML
    private AnchorPane topPane;

    @FXML
    private Button sendBtn;

    @FXML
    private ImageView closeImg;

    @FXML
    private Button fileBtn;

    @FXML
    private Label talkingLabel;

    @FXML
    private ImageView addBtn;
    
    @FXML
    private Tab friendTab;

    @FXML
    private Tab groupTab;

    public ExecutorService pool;
    public Set<Friend> friendList = new HashSet<>();
    // Request groupList from server
    public Set<Group> groupList = new HashSet<>();
    public ConcurrentHashMap<Integer, MessageList> friendChatList = new ConcurrentHashMap<Integer, MessageList>();
    public ConcurrentHashMap<Integer, MessageList> groupChatList = new ConcurrentHashMap<Integer, MessageList>();
    public Friend currentFriend = null;
    public Group currentGroup = null;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
      System.out.println("client: " + client);
      System.out.println("Friends: " + friendList.size());
      this.pool = Executors.newFixedThreadPool(20);
      friendListView.getItems().addAll(friendList);
      // Cutomize the friend list view cells
      friendListView.setCellFactory(param -> new ListCell<Friend>() {
        @Override
        protected void updateItem(Friend friend, boolean empty) {
          super.updateItem(friend, empty);

          if (empty || friend == null || friend.getNickname() == null) {
              setText(null);
          } else {
            // 设置选择栏的好友显示
              ImageView avatar = new ImageView(friend.getAvatar());
              avatar.setFitHeight(40);
              avatar.setFitWidth(40);
              
              setGraphic(avatar);
              setText(friend.getNickname());
          }
  
        }
      });

      groupListView.getItems().addAll(groupList);
      // Cutomize the friend list view cells
      groupListView.setCellFactory(param -> new ListCell<Group>() {
        @Override
        protected void updateItem(Group group, boolean empty) {
          super.updateItem(group, empty);

          if (empty || group == null || group.getName() == null) {
              setText(null);
          } else {
            // 设置选择栏的好友显示
              ImageView avatar = new ImageView("./avatar/1.jpg");
              avatar.setFitHeight(40);
              avatar.setFitWidth(40);
              setGraphic(avatar);
              setText(group.getName());
          }
  
        }
      });



      
      System.out.println("Initializing " + location);
      // Load the chat history
      friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if(newValue == null) return;
        textFlow.getChildren().clear();
        currentFriend = newValue;
        currentGroup = null;
        talkingLabel.setText("与 " + newValue.getNickname() + " 聊天中");
        Platform.runLater(() -> {
          groupListView.getSelectionModel().clearSelection();
        });
        // Load from memory
        loadHistory();
      });

      groupListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if(newValue == null) return;
        textFlow.getChildren().clear();
        currentGroup = newValue;
        currentFriend = null;
        talkingLabel.setText(newValue.getName() + " (" + newValue.getMember().size() + "人)");
        // Load from memory
        Platform.runLater(() -> {
          friendListView.getSelectionModel().clearSelection();
        });
        loadHistory();
      });
      
      // 窗口拖拽
      topPane.setOnMousePressed(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            xOffset = appStage.getX() - event.getScreenX();
            yOffset = appStage.getY() - event.getScreenY();
        }
      });
      topPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          appStage.setX(event.getScreenX() + xOffset);
          appStage.setY(event.getScreenY() + yOffset);
        }
      });
    }

    @Override
    public void finalize() throws IOException {
      saveChat();
    }

    @FXML
    void onSendMessage(ActionEvent event) {
      if((currentFriend == null && currentGroup == null) || inputTextArea.getText().equals("")) {
        System.out.println("No friend or group selected / No message input");
        return;
      }
      Message message = new Message();
      message.setTime(System.currentTimeMillis());
      message.setType("text");
      message.setSender(client.getUserID());
      message.setContent(inputTextArea.getText());
      if(currentFriend != null) {
        message.setReceiver(currentFriend.getId());
        message.setChannel("friend");
        // Add new message to memory
        if(friendChatList.containsKey(currentFriend.getId())) {
          friendChatList.get(currentFriend.getId()).put(message.getTime(), message);
        }
        else {
          MessageList messageList = new MessageList();
          messageList.put(message.getTime(), message);
          friendChatList.put(currentFriend.getId(), messageList);
        }
      }
      else if(currentGroup != null) {
        message.setReceiver(currentGroup.getGroupID());
        message.setChannel("group");

        // Add new message to memory
        if(groupChatList.containsKey(currentGroup.getGroupID())) {
          groupChatList.get(currentGroup.getGroupID()).put(message.getTime(), message);
        }
        else {
          MessageList messageList = new MessageList();
          messageList.put(message.getTime(), message);
          groupChatList.put(currentGroup.getGroupID(), messageList);
        }
      }


      // "[" + new Date(message.getTime()) + "]" + 
      Platform.runLater(() -> {
        addText(message);
      });
      System.out.println("Message: " + textFlow.getChildren().size());
      inputTextArea.clear();
      try {
        SendMessageTask sendMessageTask = new SendMessageTask(message);
        pool.submit(sendMessageTask);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if(currentFriend != null) {
        Platform.runLater(() -> {
          JsonLoader.saveFriendLog(friendChatList.get(currentFriend.getId()));
        });
      }
      else if(currentGroup != null) {
        Platform.runLater(() -> {
          JsonLoader.saveGroupLog(groupChatList.get(currentGroup.getGroupID()), currentGroup.getGroupID());
        });
      }
    }
    
    @FXML
    void onAddClick(MouseEvent event) {
      App.enterSearch(client);
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

    @FXML
    void onFileBtnClick(ActionEvent event) {
      if(currentFriend == null) {
        System.out.println("No friend selected");
        return;
      }
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Resource File");
      File file = fileChooser.showOpenDialog(App.getAppStage());
      if(file != null) {
        System.out.println(file.getAbsolutePath());
        Message message = new Message();
        message.setTime(System.currentTimeMillis());
        message.setType("file");
        message.setSender(client.getUserID());
        message.setReceiver(currentFriend.getId());
        message.setContent(file.getName());
        message.setChannel("friend");
        // 创建文件传输任务，但并不是立刻开始传输（isReady被设置后才能传）
        client.createFileTask(message, file);
        if(friendChatList.containsKey(currentFriend.getId())) {
          friendChatList.get(currentFriend.getId()).put(message.getTime(), message);
        }
        else {
          MessageList messageList = new MessageList();
          messageList.put(message.getTime(), message);
          friendChatList.put(currentFriend.getId(), messageList);
        }
  
        Platform.runLater(() -> {
          addFile(message, file);
          JsonLoader.saveFriendLog(friendChatList.get(currentFriend.getId()));
        });
      }
      else {
        System.out.println("No file selected");
      }
    }

    public boolean setClient(Client client) {
      this.client = client;
      // 读取好友列表、更新
      friendList = this.client.getFriends();
      updateFriends();
      // 读取群组列表、更新
      groupList = this.client.getGroups();
      updateGroups();
      Runnable listenTask = () -> {
        client.listenMessage();
      };
      new Thread(listenTask).start();
      return true;
    }

    public void saveChat() {
      for(MessageList messageList : friendChatList.values()) {
        JsonLoader.saveFriendLog(messageList);
      }
      for(MessageList messageList : groupChatList.values()) {
        Message firstMessage = messageList.values().iterator().next();
        int groupID = firstMessage.getGroupID();
        JsonLoader.saveGroupLog(messageList, groupID);
      }
    }
    public void addHistory(Message message) {
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
      }
      else if(message.getChannel().equals("group")) {
        long timestamp = message.getTime();
        int groupID = message.getGroupID();
        if(groupChatList.containsKey(groupID)) {
          groupChatList.get(groupID).put(timestamp, message);
        }
        else {
          MessageList messageList = new MessageList();
          messageList.put(timestamp, message);
          groupChatList.put(groupID, messageList);
        }
      }
    }

    public void setFriendList(Set<Friend> friendList) {
      this.friendList = friendList;
    }

    public void setGroupList(Set<Group> groupList) {
      this.groupList = groupList;
    }

    public void addFriend(Friend friend) {
      friendList.add(friend);
      client.saveFriends();
      Platform.runLater(() -> {
        friendListView.getItems().add(friend);
      });
    }

    public void addGroup(Group group) {
      groupList.add(group);
      client.saveGroups();
      Platform.runLater(() -> {
        groupListView.getItems().add(group);
      });
    }

    public void updateFriends() {
      Platform.runLater(() -> {
        friendListView.getItems().clear();
        friendListView.getItems().addAll(friendList);
      });
    }

    public void updateGroups() {
      Platform.runLater(() -> {
        groupListView.getItems().clear();
        groupListView.getItems().addAll(groupList);
      });
    }

    public void loadHistory() {
      if(currentFriend != null) {
        if(currentFriend.getId() == client.getUserID()) {
          textFlow.getChildren().addAll(new Label("你们已经是好友啦！\n"));
          return;
        }
        if (friendChatList.containsKey(currentFriend.getId())) {
          MessageList messageList = friendChatList.get(currentFriend.getId());
          Util.sortByKey(messageList);
          System.out.println("内存：" + messageList);
        }
        else {
          MessageList messageList = JsonLoader.loadFriendLog(client.getUserID(), currentFriend.getId());
          if(messageList == null) {
            System.out.println("打开了未曾聊天的用户: " + currentFriend.getId());
            textFlow.getChildren().addAll(new Label("你们已经是好友啦！\n"));
            return;
          }
          else {
            // Load from disk
            friendChatList.put(currentFriend.getId(), messageList);
            messageList = friendChatList.get(currentFriend.getId());
            Util.sortByKey(messageList);
            System.out.println("硬盘：" + messageList);
          }
  
        }
  
      }

      else if(currentGroup != null) {
        if (groupChatList.containsKey(currentGroup.getGroupID())) {
          MessageList messageList = groupChatList.get(currentGroup.getGroupID());
          Util.sortByKey(messageList);
          System.out.println("内存：" + messageList);
        }
        else {
          MessageList messageList = JsonLoader.loadGroupLog(currentGroup.getGroupID());
          if(messageList == null) {
            System.out.println("打开了未曾聊天的群组: " + currentGroup.getGroupID());
            textFlow.getChildren().addAll(new Label("欢迎加入新群聊\n"));
            return;
          }
          else {
            // Load from disk
            groupChatList.put(currentGroup.getGroupID(), messageList);
            messageList = groupChatList.get(currentGroup.getGroupID());
            Util.sortByKey(messageList);
            System.out.println("硬盘：" + messageList);
          }
  
        }
      }
    }

    public void addText(Message message) {
      addHistory(message);
      if(message.getType().equals("text")) {
        Date date = new Date(message.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        Label label = new Label("【" + message.getSender() + "】"  + dateString + " \n" + message.getContent());
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

    public void addFile(Message message, File localFile) {
      System.out.println("消息类型：" + message.getType());
      addHistory(message);
      if(message.getSender() != currentFriend.getId() && message.getSender() != client.getUserID()) {
        // 既不是别人发的，也不是我发的，故不显示
        return;
      }
      int userID = client.getUserID();
      // 接收者的逻辑
      if(userID == message.getReceiver()) {
        Date date = new Date(message.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        Label textLabel = new Label("【" + message.getSender() + "】"  + dateString + ": \n");
        Label fileLabel = new Label(message.getContent());
        ImageView imageView = new ImageView("./icon/file.png");
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        fileLabel.setGraphic(imageView);
        fileLabel.setGraphicTextGap(5);
        fileLabel.setCursor(Cursor.HAND);
        fileLabel.setOnMouseClicked((MouseEvent mouse) -> {
          File file = new File("./src/tempFile/" + message.getContent());
          if(!file.exists()) {
            fileLabel.setText(message.getContent() + "(传输中)");
            // 问题出在这
            Client.ReceiveFileTask receiveFileTask = client.createReceiveFileTask(message, file);
            Runnable waitTask = () -> {
              while(!receiveFileTask.isDone()) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              }
              Platform.runLater(() -> {
                fileLabel.setText(message.getContent() + "(传输完成)");
                imageView.setImage(new Image("./icon/file_done.png"));
                fileLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, fileLabel.getOnMouseClicked());
                fileLabel.setOnMouseClicked((MouseEvent open)->{
                  try {
                    Desktop.getDesktop().open(file);
                  } catch (IOException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("文件可能已被删除");
                    e.printStackTrace();
                  }
                });
              });
              return;
            };
            new Thread(waitTask).start();
          }
          else {
            Platform.runLater(() -> {
              fileLabel.setText(message.getContent() + "(传输完成)");
              imageView.setImage(new Image("./icon/file_done.png"));
              fileLabel.setOnMouseClicked((MouseEvent notused)->{
                try {
                  Desktop.getDesktop().open(file);
                } catch (IOException e) {
                  Alert alert = new Alert(AlertType.ERROR);
                  alert.setTitle("错误");
                  alert.setHeaderText("文件可能已被删除");
                  e.printStackTrace();
                }
              });
            });  
          }
            
        });
        if(new File("./src/tempFile/" + message.getContent()).exists()) {
          fileLabel.setText(message.getContent() + "(传输完成)");
          imageView.setImage(new Image("./icon/file_done.png"));
          fileLabel.setOnMouseClicked((MouseEvent mouse)->{
            try {
              Desktop.getDesktop().open(new File("./src/tempFile/" + message.getContent()));
            } catch (IOException e) {
              Alert alert = new Alert(AlertType.ERROR);
              alert.setTitle("错误");
              alert.setHeaderText("文件可能已被删除");
              e.printStackTrace();
            }
          });
        }
        else {
          fileLabel.setText(message.getContent() + "(点击接收)");
        }
        if(message.getSender() == client.getUserID()) {
          fileLabel.setAlignment(Pos.CENTER_RIGHT);
          fileLabel.setTextFill(Color.BLUE);
        }
        else {
          fileLabel.setTextFill(Color.BLACK);
        }
        System.out.println("add file");
        textFlow.getChildren().addAll(textLabel, fileLabel);
      }
      // 发送者的逻辑
      else {
        
        Date date = new Date(message.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        Label textLabel = new Label("【" + message.getSender() + "】"  + dateString + ": \n");
        Label imageLabel = new Label("[" + dateString + "]"  + message.getSender() + ": \n");
        ImageView imageView = new ImageView("./icon/file_done.png");
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        imageLabel.setCursor(Cursor.HAND);
        imageLabel.setGraphic(imageView);
        imageLabel.setGraphicTextGap(5);
        imageLabel.setText(message.getContent());
        imageLabel.setOnMouseClicked((MouseEvent mouse)->{
          try {
            // 临时处理：没有指定则从tempFile提取
            // 后续计划：已有文件存储在json中，没有则从服务器下载
            if(localFile == null) {
              File tempPath = new File("./src/tempFile/" + message.getContent());
              Desktop.getDesktop().open(tempPath);
            }
            else {
              Desktop.getDesktop().open(localFile);
            }
          } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("文件可能已被删除");
            e.printStackTrace();
          }
        });
        if(message.getSender() == client.getUserID()) {
          imageLabel.setAlignment(Pos.CENTER_RIGHT);
          textLabel.setTextFill(Color.BLUE);
          imageLabel.setTextFill(Color.BLUE);
        }
        else {
          textLabel.setTextFill(Color.BLACK);
        }
        System.out.println("add file");
        textFlow.getChildren().addAll(textLabel, imageLabel);
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