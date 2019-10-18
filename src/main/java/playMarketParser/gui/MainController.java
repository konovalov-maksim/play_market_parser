package playMarketParser.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML
    private Button addQueriesBtn;
    @FXML
    private Button importQueriesBtn;
    @FXML
    private TableView<String> table;
    @FXML
    private TableColumn<String, String> queryCol;

    private ObservableList<String> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
