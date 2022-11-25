package App.Search;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class SearchSceneController {

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

}
