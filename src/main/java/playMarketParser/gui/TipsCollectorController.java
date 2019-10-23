package playMarketParser.gui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import playMarketParser.tipsCollector.Query;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class TipsCollectorController implements Initializable {

    @FXML private Button addQueriesBtn;
    @FXML private Button importQueriesBtn;
    @FXML private Button clearBtn;
    @FXML private Button exportBtn;
    @FXML private Button startBtn;
    @FXML private Button abortBtn;
    @FXML private CheckBox titleFirstChb;
    @FXML private TableView<String> inputTable;
    @FXML private TableColumn inputQueryCol;
    @FXML private TableView<Query> outputTable;
    @FXML private TableColumn outputQueryCol;
    @FXML private TableColumn tipCol;

    private ObservableList<String> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void addQueriesClick(ActionEvent event) {

    }

    @FXML
    private void importQueries() {

    }

    @FXML
    private void addQueries() {

    }

    @FXML
    private void clearQueries() {

    }

    @FXML
    private void exportResults() {

    }

    @FXML
    private void startTipsCollecting() {

    }

    @FXML
    private void abortTipsCollecting() {

    }
}
