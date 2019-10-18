package playMarketParser.gui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import playMarketParser.Global;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


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
//        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(1));
//        queryCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
//        table.setItems(queries);
    }

    @FXML
    private void showAddDlg(ActionEvent event) {
        // Create the new dialog
        TextAreaDialog dialog = new TextAreaDialog("", "testLabel");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        // Show the dialog and capture the result.
        Optional result = dialog.showAndWait();

        // If the "Okay" button was clicked, the result will contain our String in the get() method
        if (result.isPresent()) {
            System.out.println(result.get());
            List<String> inputQueries = Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .collect(Collectors.toList());
            queries.addAll(inputQueries);
        }
    }

    @FXML
    private void showImportDlg(ActionEvent event) {

    }


}
