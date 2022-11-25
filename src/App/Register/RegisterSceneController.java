package App.Register;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import App.App;
import Client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import shared.Message;

public class RegisterSceneController {

    private Client client = null;

    @FXML
    private TextField nicknameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private TextField userIDInput;

    @FXML
    private ImageView registerBtn;

    @FXML
    void onRegisterClick(MouseEvent event) {
      System.out.println("注册：" + userIDInput.getText() + " " + passwordInput.getText() + " " + nicknameInput.getText());
      this.client = new Client();
      if(this.client == null) {
        System.out.println("Client 实例为空，请重启");
      }
      try {
        Message message = new Message();
        message.setContent(userIDInput.getText() + " " + passwordInput.getText() + " " + nicknameInput.getText());
        String response = client.register(message);
        // 等待回应
        System.out.println("收到回应：" + response);
        if(response.equals("done")) {
          Alert alert = new Alert(AlertType.CONFIRMATION);
          alert.setContentText("注册成功");
          alert.show();
          App.RegisterStage.close();
        }
        else if(response.equals("conflict")) {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setContentText("用户ID已存在");
          alert.show();
        }
        else if(response.equals("error")) {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setContentText("密文格式错误");
          alert.show();
        }
        // 出现在这下面的都是未完善你的类型
        else {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setContentText("未知错误");
          alert.show();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

}
