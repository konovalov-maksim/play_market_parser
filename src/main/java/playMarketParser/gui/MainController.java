package playMarketParser.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import playMarketParser.Global;

import java.io.IOException;
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

    @FXML
    private void openPrefs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/prefs.fxml"), Global.getBundle());
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("appName");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
