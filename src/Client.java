import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Client {
  private Socket socket = null;
  private User user = null;
  private String host = "localhost";
  private int port = 8000;
  private DataInputStream inputFromServer;
  private DataOutputStream outputToServer;
  public ExecutorService pool;
  private Stack<Callable<Void>> tasks;
  private Stack<Callable<Void>> receiveTasks;

  Client() {
    this.pool = Executors.newFixedThreadPool(20);
    this.tasks = new Stack<Callable<Void>>();
    this.receiveTasks = new Stack<Callable<Void>>();
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
            if(message.getType().equals("file")) {
              Platform.runLater(() -> {
                appSceneController.addMessage(message);
                appSceneController.addFile(message);
              });
            }
            if(message.getType().equals("text")) {
              Platform.runLater(() -> {
                appSceneController.addMessage(message);
              });
            }
          } else {
            System.out.println("未知的消息频道（未完善群聊）");
          }
        }
        // 文件接受。客户端请求文件，服务端得知，返回信号
        else if (operation.equals("fileFromServer")) {
          System.out.println("fileFromServer");
          String messageJsonString = this.inputFromServer.readUTF();
          Message message = JSON.parseObject(messageJsonString, Message.class);
          Platform.runLater(() -> {
            appSceneController.addMessage(message);
            appSceneController.addFile(message);
          });
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

  public void createFileTask(Message message, File file) {
    
    try {

      SendFileTask sendFileTask = new SendFileTask(message, file);
      this.pool.submit(sendFileTask);
      this.tasks.push(sendFileTask);
      System.out.println("压栈：" + tasks.size());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean createReceiveFileTask(Message message, File file) {
    try {
      ReceiveFileTask receiveFileTask = new ReceiveFileTask(message, file);
      this.pool.submit(receiveFileTask);
      this.receiveTasks.push(receiveFileTask);
      System.out.println("【收文件】压栈：" + receiveTasks.size());
      return receiveFileTask.isDone();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
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
    public Void call() throws Exception{
      try {
        outputToServer.writeUTF("file");
        outputToServer.writeUTF(this.message.getJson());
        Thread.sleep(1000);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        int length = 0;
        long progress = 0;
        // while(!isReady) {
        //   System.out.println("【" + user.getId() +  "】等待服务端准备好接收文件");
        //   Thread.sleep(1000);
        // }
        // System.out.println("ready是否成功？"+ isReady);
        // outputToServer.writeLong(file.length());
        Socket temp_socket = new Socket(host, 8001);
        DataOutputStream temp_outputToServer = new DataOutputStream(temp_socket.getOutputStream());
        Thread.sleep(1000);
        while((length = fis.read(bytes, 0, bytes.length)) != -1) {
            System.out.println("【" + user.getId() + "】文件发送length: " + length);
            temp_outputToServer.write(bytes, 0, length);
            progress += length;
            System.out.println("| " + (100*progress/file.length()) + "% |");
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

  class ReceiveFileTask implements Callable<Void> {
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
    public Void call() throws Exception{
      try {
        // 等待服务端ServerSocket的accept
        Thread.sleep(1000);
        Socket temp_socket = new Socket(host, 8002);
        DataInputStream input = new DataInputStream(temp_socket.getInputStream());
        String fileName = message.getContent();
        File directory = new File("./src/tempFile");
        if(!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int length = 0;
        byte[] bytes = new byte[1024];
        while((length = input.read(bytes, 0, bytes.length)) != -1) {
          fileOutputStream.write(bytes, 0, length);
          fileOutputStream.flush();
        }
        this.isDone = true;
        fileOutputStream.close();
        input.close();
        temp_socket.close();
      } catch (Exception e) {
        System.out.println("【" + user.getId() + "】文件接收失败");
        // TODO: handle exception
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


}