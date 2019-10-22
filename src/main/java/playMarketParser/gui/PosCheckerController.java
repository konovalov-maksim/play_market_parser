package playMarketParser.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import playMarketParser.Global;
import playMarketParser.Prefs;
import playMarketParser.positionsChecker.PosChecker;
import playMarketParser.positionsChecker.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;


public class PosCheckerController implements Initializable, PosChecker.PosCheckCompleteListener {

    @FXML private Button addQueriesBtn;
    @FXML private Button importQueriesBtn;
    @FXML private Button startBtn;
    @FXML private TextField appUrlTf;
    @FXML private CheckBox titleFirstChb;
    @FXML private VBox rootPane;
    @FXML private TableView<Query> table;
    @FXML private TableColumn<Query, String> queryCol;
    @FXML private TableColumn<Query, String> posCol;

    private ResourceBundle bundle;
    private Prefs prefs;

    private ObservableList<Query> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = Global.getBundle();
        prefs = new Prefs();

        appUrlTf.setText(prefs.getString("pos_app_url"));

        //Таблица
        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(0.8));
        posCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        queryCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        posCol.setCellValueFactory(new PropertyValueFactory<>("realPosString"));
        table.setItems(queries);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void addQueries(ActionEvent event) {
        // Create the new dialog
        TextAreaDialog dialog = new TextAreaDialog("", bundle.getString("enterQueries"));
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        // Show the dialog and capture the result.
        Optional result = dialog.showAndWait();

        // If the "Okay" button was clicked, the result will contain our String in the get() method
        if (result.isPresent()) {
            System.out.println(result.get());
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> queries.add(new Query(s)));
        }
    }

    @FXML
    private void importQueries() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("txtDescr"), "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("csvDescr"), "*.csv"));
        fileChooser.setInitialDirectory(Global.getInitDir(prefs, "inputPath"));
        File inputFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (inputFile == null) return;
        prefs.put("inputPath", inputFile.getParentFile().toString());


        try (Stream<String> lines = Files.lines(inputFile.toPath(), StandardCharsets.UTF_8)) {
            lines.skip(titleFirstChb.isSelected() ? 1 : 0)
                    .distinct()
                    .forEachOrdered(r -> queries.add(new Query(r)));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(bundle.getString("error"), bundle.getString("unableToReadFile"));
        }
    }

    @FXML
    private void startPosChecking() {
        if (queries.size() == 0) {
            showAlert(bundle.getString("error"), bundle.getString("noQueries"));
            return;
        }
        if (appUrlTf.getText().length() == 0) {
            showAlert(bundle.getString("error"), bundle.getString("noAppUrl"));
            return;
        }
        String appId = appUrlTf.getText().replaceAll(".*id=", "");
        //https://play.google.com/store/apps/details?id=com.appgrade.cinemaguru
        prefs.put("pos_app_url", appUrlTf.getText());

        PosChecker posChecker = new PosChecker(appId, queries, 5, 7, this);
        posChecker.start();
    }

    @FXML
    private void exportResults(ActionEvent event){
        if (queries == null || queries.size() == 0) {
            showAlert(bundle.getString("error"), bundle.getString("noResults"));
            return;
        }

        String curDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(bundle.getString("outPositions") + " " + curDate);
        fileChooser.setInitialDirectory(Global.getInitDir(prefs, "outputPath"));
        File outputFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;
        prefs.put("outputPath", outputFile.getParentFile().toString());


        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile))) {
            //Указываем кодировку файла UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');

            //Добавляем заголовок
            String firstRow = bundle.getString("query") + Global.CSV_DELIMITER + bundle.getString("position") + " " +
                        new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis())) + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (Query query : queries)
                newContent.add(query.getFullRowText() + Global.CSV_DELIMITER + query.getRealPos());
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(bundle.getString("saved"), bundle.getString("fileSaved"));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showAlert(bundle.getString("error"), bundle.getString("fileNotSaved"));
        }

    }

    @Override
    public void onPosCheckingComplete(List<Query> processedQueries) {
        queries = FXCollections.observableArrayList(processedQueries);
        table.refresh();
    }

}
