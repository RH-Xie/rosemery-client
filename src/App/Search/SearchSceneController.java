package App.Search;

import java.net.Socket;
import java.util.concurrent.Callable;

import Client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import shared.Message;

public class SearchSceneController {
    private long timestamp = 0;
    private Client client;

    @FXML
    private RadioButton friendRadio;

    @FXML
    private RadioButton groupRadio;

    @FXML
    private ListView<?> resultListView;

    @FXML
    private TextField searchInput;

    @FXML
    private ToggleGroup type;

    @FXML
    void onEnterPressed(ActionEvent event) {
      if (searchInput.getText().length() == 0) {
        return;
      }
      // 防抖
      if(System.currentTimeMillis() - timestamp < 500) {
        return;
      }
      timestamp = System.currentTimeMillis();

      if (friendRadio.isSelected()) {
        System.out.println("Find Friend");

      }

    }

    public void setClient(Client client) {
      this.client = client;
    }

    class FindById implements Callable<Void> {
      private Message message;

      @Override
      public Void call() throws Exception {
        // TODO 11.26接着完善
        return null;
      }

    } 
  }
