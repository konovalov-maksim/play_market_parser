package playMarketParser.gui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import playMarketParser.positionsChecker.PosChecker;
import playMarketParser.positionsChecker.Query;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class PosCheckerController implements Initializable, PosChecker.PosCheckCompleteListener {

    @FXML
    private Button addQueriesBtn;
    @FXML
    private Button importQueriesBtn;
    @FXML
    private TableView<Query> table;
    @FXML
    private TableColumn<Query, String> queryCol;
    @FXML
    private TableColumn<Query, String> posCol;

    private ObservableList<Query> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(1));
//        queryCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
//        queryCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
        table.setItems(queries);
    }

    @FXML
    private void addQueriesClick(ActionEvent event) {
        // Create the new dialog
        TextAreaDialog dialog = new TextAreaDialog("", "testLabel");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        // Show the dialog and capture the result.
        Optional result = dialog.showAndWait();

        // If the "Okay" button was clicked, the result will contain our String in the get() method
        if (result.isPresent()) {
            System.out.println(result.get());
//            queries.addAll(((String) result.get()).split("\\r?\\n"));
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                .forEachOrdered(s -> queries.add(new Query(s)));
        }
    }

    @FXML
    private void importQueriesClick(ActionEvent event) {

    }

    @FXML
    private void startPosChecking() {
        PosChecker posChecker = new PosChecker(queries, 5, 7, this);
        posChecker.start();
    }

    @Override
    public void onPosCheckingComplete(List<Query> processedQueries) {

    }
}
