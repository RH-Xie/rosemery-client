package Client;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import App.App;
import App.AppSceneController;
import App.Search.SearchSceneController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import shared.Friend;
import shared.Group;
import shared.LoginMessage;
import shared.Message;
import shared.User;

public class Client {
  private Socket socket = null;
  private User user = null;
  private String host = "localhost";
  private int port = 8000;
  private long HEART_BEAT = 5000;
  private DataInputStream inputFromServer;
  private DataOutputStream outputToServer;
  public ExecutorService pool;
  private Stack<Callable<Void>> tasks;
  private Set<Friend> friends = new HashSet<>();
  private Set<Group> groups = new HashSet<>();

  public Client() {
    this.pool = Executors.newFixedThreadPool(20);
    this.tasks = new Stack<Callable<Void>>();
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

      // 开始心跳
      Runnable heartBeatTask = () -> {
        while (true) {
          try {
            Thread.sleep(HEART_BEAT);
            
            // 与服务器保持心跳
            this.outputToServer.writeUTF("heartBeat");
            Message message = new Message();
            message.setTime(System.currentTimeMillis())
            .setType("heartBeat")
            .setSender(user.getId())
            .setContent("OK");
            this.outputToServer.writeUTF(message.getJson());
            SimpleDateFormat formatter = new SimpleDateFormat ("[HH:mm:ss]");
            String dateString = formatter.format(new Date());
            System.out.println(dateString+  user.getId() + ": 发送心跳");
            // 更新本地
            AppSceneController controller = App.getAppSceneController();
            // controller.saveChat();
            // 保存好友列表和群聊列表
            saveFriends();
            saveGroups();

          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      };
      new Thread(heartBeatTask).start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  public String register(Message message) {
    if (this.socket == null) {
      return "localerror";
    }
    String result = "networkerror";
    try {
      this.inputFromServer = new DataInputStream(socket.getInputStream());
      this.outputToServer = new DataOutputStream(socket.getOutputStream());
      outputToServer.writeUTF("register");
      outputToServer.writeUTF(message.getJson());
      result = this.inputFromServer.readUTF();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  public void listenMessage() {
    if (this.socket == null) {
      System.out.println("socket is null");
      return;
    }
    try {
      while (true) {
        this.inputFromServer = new DataInputStream(socket.getInputStream());
        String operation = this.inputFromServer.readUTF();
        AppSceneController appSceneController = App.getAppSceneController();
        // 消息响应
        if (operation.equals("reponseMessage")) {
          String messageJsonString = this.inputFromServer.readUTF();
          Message message = JSON.parseObject(messageJsonString, Message.class);
          if (message.getChannel().equals("friend")) {
            // Receive new message from friend
            System.out.println("进入成功");
            if (message.getType().equals("file")) {
              Platform.runLater(() -> {
                appSceneController.addFile(message, null);
              });
            }
            if (message.getType().equals("text")) {
              Platform.runLater(() -> {
                appSceneController.addText(message);
              });
            }
            if(message.getType().equals("search")) {
              // 27号，在这里添加搜索的响应，包括前面的弹窗提示，都要纳入这后面更好
              // System.out.println("【查找】成功响应，但暂无作用");
              String friendJson = inputFromServer.readUTF();
              Friend friend = JSON.parseObject(friendJson, Friend.class);
              FindTask findTask = (FindTask)tasks.pop();
              findTask.setFriend(friend);
              findTask.setDone();
              if(!message.getContent().equals("null")) {
                findTask.setResult(Integer.parseInt(message.getContent()));
              }
              // 否则保持-1
            }
          } 
          else if (message.getChannel().equals("group")) {
            System.out.println("回应");
            // Receive new message from group
            if (message.getType().equals("file")) {
              Platform.runLater(() -> {
                appSceneController.addFile(message, null);
              });
            }
            else if (message.getType().equals("text")) {
              Platform.runLater(() -> {
                appSceneController.addText(message);
              });
            }
            else if (message.getType().equals("search")) {
              // 27号，在这里添加搜索的响应，包括前面的弹窗提示，都要纳入这后面更好
              System.out.println("【查找】成功响应");
              String groupJson = inputFromServer.readUTF();
              Group group = JSON.parseObject(groupJson, Group.class);
              System.out.println("json: " + groupJson);
              System.out.println("object: " + group.getJson());
              FindTask findTask = (FindTask)tasks.pop();
              findTask.setGroup(group);
              findTask.setDone();
              if(!message.getContent().equals("null")) {
                findTask.setResult(Integer.parseInt(message.getContent()));
              }
            }
          }
          
          else {
            System.out.println("未知的消息频道（未完善群聊）");
          }
        }
        // 文件接受。客户端请求文件，服务端得知，返回信号
        else if (operation.equals("fileFromServer")) {
          System.out.println("fileFromServer");
          String messageJsonString = this.inputFromServer.readUTF();
          Message message = JSON.parseObject(messageJsonString, Message.class);
          // Platform.runLater(() -> {
          //   appSceneController.addFile(message, null);
          // });
        }

        else {
          System.out.println("未实现的消息类型：" + operation);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setUser(int id, String password) {
    this.user = new User(id, password);
  }

  public void setUser(User user) {
    this.user = user;
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

  public DataInputStream getInputFromServer() {
    return this.inputFromServer;
  }

  public DataOutputStream getOutputToServer() {
    return this.outputToServer;
  }

  public void saveFriends() {
    try {
      File file = new File("./src/data/friends.json");
      if (!file.exists()) {
        file.createNewFile();
      }
      JSONArray array = new JSONArray();
      for(Friend friend : this.friends) {
        array.add(friend);
      }
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
      dataOutputStream.writeUTF(array.toString());
      dataOutputStream.close();
      fileOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadFriends() {
    try {
      File file = new File("./src/data/friends.json");
      if (!file.exists()) {
        file.createNewFile();
      }
      FileInputStream fileInputStream = new FileInputStream(file);
      DataInputStream dataInputStream = new DataInputStream(fileInputStream);
      String json = dataInputStream.readUTF();
      JSONArray jsonArray = JSON.parseArray(json);
      System.out.println("读取好友");
      System.out.println("长度：" + jsonArray.size());
      for(Object o : jsonArray) {
        Friend friend = JSON.parseObject(JSON.toJSONString(o), Friend.class);
        friends.add(friend);
        System.out.println("好友：" + friend.getJson());
      }
      dataInputStream.close();
      fileInputStream.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  public Set<Friend> getFriends() {
    loadFriends();
    return this.friends;
  }

  public void setFriends(Set<Friend> friends) {
    this.friends = friends;
  }

  public void addFriend(Friend friend) {
    this.friends.add(friend);
  }

  public void removeFriend(Friend friend) {
    this.friends.remove(friend);
  }

  public void saveGroups() {
    try {
      File file = new File("./src/data/groups.json");
      if (!file.exists()) {
        file.createNewFile();
      }
      JSONArray array = new JSONArray();
      for(Group g : groups) {
        array.add(g);
      }
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
      dataOutputStream.writeUTF(array.toString());
      dataOutputStream.close();
      fileOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadGroups() {
    // outputToServer.writeUTF();
    // 后续可以完善直接请求服务器的，因为存在本地的群聊信息可能不是最新的
    try {
      File file = new File("./src/data/groups.json");
      if (!file.exists()) {
        file.createNewFile();
      }
      FileInputStream fileInputStream = new FileInputStream(file);
      DataInputStream dataInputStream = new DataInputStream(fileInputStream);
      String json = dataInputStream.readUTF();
      JSONArray jsonArray = JSON.parseArray(json);
      for(Object o : jsonArray) {
        Group u = JSON.parseObject(JSON.toJSONString(o), Group.class);
        groups.add(u);
      }
      dataInputStream.close();
      fileInputStream.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Set<Group> getGroups() {
    loadGroups();
    return this.groups;
  }

  public void setGroups(Set<Group> groups) {
    this.groups = groups;
  }

  public void addGroup(Group group) {
    this.groups.add(group);
  }

  public void removeGroup(Group group) {
    this.groups.remove(group);
  }

  public void createFileTask(Message message, File file) {

    try {

      SendFileTask sendFileTask = new SendFileTask(message, file);
      this.pool.submit(sendFileTask);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public ReceiveFileTask createReceiveFileTask(Message message, File file) {
    try {
      ReceiveFileTask receiveFileTask = new ReceiveFileTask(message, file);
      this.pool.submit(receiveFileTask);
      return receiveFileTask;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public FindTask createFindTask(Message message) {
    FindTask findTask = new FindTask(message);
    this.pool.submit(findTask);
    return findTask;
  }

  class SendFileTask implements Callable<Void> {
    private Message message;
    // private DataInputStream inputFromServer;
    private File file;
    private boolean isReady;

    SendFileTask(Message message, File file) throws IOException {
      this.message = message;
      // this.inputFromServer = client.getInputFromServer();
      this.file = file;
      this.isReady = false;
    }

    @Override
    public Void call() throws Exception {
      try {
        outputToServer.writeUTF("file");
        outputToServer.writeUTF(this.message.getJson());
        Thread.sleep(1000);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        int length = 0;
        long progress = 0;
        // while(!isReady) {
        // System.out.println("【" + user.getId() + "】等待服务端准备好接收文件");
        // Thread.sleep(1000);
        // }
        // System.out.println("ready是否成功？"+ isReady);
        // outputToServer.writeLong(file.length());
        Socket temp_socket = new Socket(host, 8001);
        DataOutputStream temp_outputToServer = new DataOutputStream(temp_socket.getOutputStream());
        Thread.sleep(1000);
        while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
          System.out.println("【" + user.getId() + "】文件发送length: " + length);
          temp_outputToServer.write(bytes, 0, length);
          progress += length;
          System.out.println("| " + (100 * progress / file.length()) + "% |");
        }
        System.out.println("文件发送完成");
        fis.close();
        temp_outputToServer.close();
        temp_socket.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    public boolean isReady() {
      return isReady;
    }

    public void setReady(boolean isReady) {
      this.isReady = isReady;
    }
  }

  public class ReceiveFileTask implements Callable<Void> {
    private Message message;
    // private DataInputStream inputFromServer;
    private boolean isDone;
    private File file;

    ReceiveFileTask(Message message, File file) throws IOException {
      this.isDone = false;
      this.message = message;
      // this.inputFromServer = client.getInputFromServer();
      this.file = file;
    }

    @Override
    public Void call() throws Exception {
      try {
        // 发送指令
        outputToServer.writeUTF("requireFile");
        outputToServer.writeUTF(this.message.getJson());
        outputToServer.writeUTF(file.getName());
        // 等待服务端ServerSocket的accept
        Thread.sleep(1000);
        Socket temp_socket = new Socket(host, 8002);
        DataInputStream input = new DataInputStream(temp_socket.getInputStream());
        String fileName = message.getContent();
        File directory = new File("./src/tempFile");
        if (!directory.exists()) {
          directory.mkdir();
        }
        File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int length = 0;
        byte[] bytes = new byte[1024];
        while ((length = input.read(bytes, 0, bytes.length)) != -1) {
          fileOutputStream.write(bytes, 0, length);
          fileOutputStream.flush();
        }
        this.isDone = true;
        fileOutputStream.close();
        input.close();
        temp_socket.close();
        System.out.println("【" + user.getId() + "】文件接收完成");
      } catch (Exception e) {
        System.out.println("【" + user.getId() + "】文件接收失败");
        e.printStackTrace();
      }
      return null;
    }

    public boolean isDone() {
      return this.isDone;
    }

    public File getFile() {
      return this.file;
    }
  }

  public class FindTask implements Callable<Void> {
    private Message message;
    private boolean isDone;
    private int result;
    private Friend friend;
    private Group group;

    public FindTask(Message message) {
      this.message = message;
      this.isDone = false;
      this.result = -1;
    }

    @Override
    public Void call() throws Exception {
      try {
        System.out.println("【" + user.getId() + "】开始查找");
        // 发送指令
        outputToServer.writeUTF("search");
        // 发送搜索字段
        outputToServer.writeUTF(message.getJson());
        tasks.push(this);
        System.out.println("压栈：" + tasks.size());
      }catch(Exception e) {
        System.out.println("FindTask 错误");
        e.printStackTrace();
      }
      return null;
    }

    public boolean isDone() {
      return this.isDone;
    }

    public void setDone() {
      this.isDone = true;
    }

    public void setFriend(Friend friend) {
      this.friend = friend;
    }

    public Friend getFriend() {
      return this.friend;
    }

    public void setGroup(Group group) {
      this.group = group;
    }

    public Group getGroup() {
      loadGroups();
      return this.group;
    }

    public void setResult(int result) {
      this.result = result;
    }

    public int getResult() {
      return this.result;
    }
  }
}