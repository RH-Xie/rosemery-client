import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class LoginSceneController {

    private Client client = null;

    @FXML
    private TextField idTextField;

    @FXML
    private ImageView loginBtn;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Label registerLabel;
    
    @FXML
    void onLoginBtnClick(MouseEvent event) {
      System.out.println(passwordTextField.getText());
      this.client = new Client();
      if(this.client == null) {
        System.out.println("Client 实例为空，请重启");
      }
      client.setUser(Integer.parseInt(idTextField.getText()), passwordTextField.getText());
      LoginMessage loginMessage = client.login();
      if(loginMessage == null) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText("连接失败");
        alert.show();
      } else {
        if((loginMessage.isSuccess)) {
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.setContentText("登录成功");
          App.enterApp("999", this.client);
          alert.show();
          App.LoginStage.close();
        } else {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setContentText("登录失败");
          alert.show();
        }
      }
    }

    @FXML
    void onRegisterBtnClick(MouseEvent event) {
      App.enterRegister();
    }
  }
