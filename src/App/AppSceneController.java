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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Client.Client;
import Client.Client.ReceiveFileTask;

import java.awt.Desktop;
import java.beans.EventHandler;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import shared.Friend;
import shared.JsonLoader;
import shared.MakeFriends;
import shared.Message;
import shared.MessageList;
import shared.Util;

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

    @FXML
    private Button fileBtn;

    @FXML
    private Label talkingLabel;

    @FXML
    private ImageView addBtn;

    public ExecutorService pool;
    public Friend[] friendList = new MakeFriends().getFriends();
    // Request groupList from server
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
            // 设置选择栏的好友显示
              ImageView avatar = new ImageView(friend.getAvatar());
              avatar.setFitHeight(40);
              avatar.setFitWidth(40);
              
              setGraphic(avatar);
              setText(friend.getNickname());
          }
  
        }
      });


      
      System.out.println("Initializing " + location);
      groupListView.getItems().addAll(groupList);
      // Load the chat history
      friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        textFlow.getChildren().clear();
        currentFriend = newValue;
        talkingLabel.setText("与 " + newValue.getNickname() + " 聊天中");
        // Load from memory
        loadHistory();
      });

      // friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      //   textFlow.getChildren().clear();
      //   currentFriend = newValue;
      //   talkingLabel.setText("与 " + newValue.getNickname() + " 聊天中");
      //   // Load from memory
      //   loadHistory();
      // });

    }

    @Override
    public void finalize() throws IOException {
      saveChat();
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
      Platform.runLater(() -> {
        JsonLoader.saveToFile(friendChatList.get(currentFriend.getId()));
      });
    }
    
    @FXML
    void onAddClick(MouseEvent event) {
      System.out.println("Add friend");
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
          JsonLoader.saveToFile(friendChatList.get(currentFriend.getId()));
        });
      }
      else {
        System.out.println("No file selected");
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

    public void saveChat() {
      for(MessageList messageList : friendChatList.values()) {
        JsonLoader.saveToFile(messageList);
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
    }

    public void setFriendList(Friend[] friendList) {
      this.friendList = friendList;
    }

    public void setGroupList(String[] groupList) {
      this.groupList = groupList;
    }

    public void loadHistory() {
      if(currentFriend == null) {
        return;
      }
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
        MessageList messageList = JsonLoader.loadFromFile(client.getUserID(), currentFriend.getId());
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