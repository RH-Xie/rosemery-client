import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;

public final class MakeFriends implements Initializable{
  public Friend[] friends;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // TODO Auto-generated method stub

  }

  public Friend[] getFriends() {
    friends = new Friend[10];
    for (int i = 0; i < 10; i++) {
      friends[i] = new Friend(i, "nickname" + i, "avatar" + i, "signature" + i);
    }
    System.out.println(this.friends.length);
    return this.friends;
  }
}
