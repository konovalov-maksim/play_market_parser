package playMarketParser.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import static playMarketParser.Global.showAlert;


public class PosCheckerController implements Initializable, PosChecker.PosCheckListener {

    @FXML private Button addQueriesBtn;
    @FXML private Button importQueriesBtn;
    @FXML private Button startBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearBtn;
    @FXML private Button abortBtn;
    @FXML private CheckBox titleFirstChb;
    @FXML private TextField appUrlTf;
    @FXML private Label processedQueriesCntLbl;
    @FXML private Label allQueriesCntLbl;
    @FXML private VBox rootPane;
    @FXML private TableView<Query> table;
    @FXML private TableColumn<Query, String> queryCol;
    @FXML private TableColumn<Query, String> pseudoPosCol;
    @FXML private TableColumn<Query, String> realPosCol;

    private PosChecker posChecker;
    private ResourceBundle rb;
    private Prefs prefs;

    private ObservableList<Query> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();
        prefs = new Prefs();
        enableReadyMode();

        appUrlTf.setText(prefs.getString("pos_app_url"));
        titleFirstChb.setSelected(prefs.getBoolean("title_first"));

        //Таблица
        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        pseudoPosCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        realPosCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        queryCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        pseudoPosCol.setCellValueFactory(new PropertyValueFactory<>("pseudoPosString"));
        realPosCol.setCellValueFactory(new PropertyValueFactory<>("realPosString"));
        table.setItems(queries);

        //Подписи
        refreshProcessedCnt(0);
        allQueriesCntLbl.textProperty().bind(Bindings.size(queries).asString());
    }

    @FXML
    private void addQueries() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterQueries"), rb.getString("addingQueries"), "");

        Optional result = dialog.showAndWait();
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("txtDescr"), "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("csvDescr"), "*.csv"));
        fileChooser.setInitialDirectory(Global.getInitDir(prefs, "input_path"));
        File inputFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (inputFile == null) return;
        prefs.put("input_path", inputFile.getParentFile().toString());
        prefs.put("title_first", titleFirstChb.isSelected());

        try (Stream<String> lines = Files.lines(inputFile.toPath(), StandardCharsets.UTF_8)) {
            lines.skip(titleFirstChb.isSelected() ? 1 : 0)
                    .distinct()
                    .forEachOrdered(r -> queries.add(new Query(r)));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("unableToReadFile"));
        }
    }

    @FXML
    private void startPosChecking() {
        if (queries.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noQueries"));
            return;
        }
        if (appUrlTf.getText().length() == 0) {
            showAlert(rb.getString("error"), rb.getString("noAppUrl"));
            return;
        }
        for (Query query : queries) query.clearPositions();
        table.refresh();

        String appId = appUrlTf.getText().replaceAll(".*id=", "");
        prefs.put("pos_app_url", appUrlTf.getText());

        posChecker = new PosChecker(appId, queries, prefs.getInt("pos_threads_cnt"),
                prefs.getInt("pos_checks_cnt"),this);

        enableLoadingMode();
        posChecker.start();
    }

    @FXML
    private void abortPosChecking() {
        posChecker.abort();
    }

    @FXML
    private void exportResults(){
        if (queries == null || queries.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noResults"));
            return;
        }

        String curDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(rb.getString("outPositions") + " " + curDate);
        fileChooser.setInitialDirectory(Global.getInitDir(prefs, "output_path"));
        File outputFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;
        prefs.put("output_path", outputFile.getParentFile().toString());


        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile))) {
            //Указываем кодировку файла UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');

            //Добавляем заголовок
            String firstRow = rb.getString("query") + Global.CSV_DELIMITER + rb.getString("finalPos") + " " + curDate + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (Query query : queries)
                newContent.add(query.getFullRowText() + Global.CSV_DELIMITER + query.getRealPos());
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(rb.getString("saved"), rb.getString("fileSaved"));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("fileNotSaved"));
        }

    }

    @FXML
    private void clearQueries() {
        table.getItems().clear();
        enableReadyMode();
    }

    private void refreshProcessedCnt(int processed) {
        Platform.runLater(() -> processedQueriesCntLbl.setText(rb.getString("processedQueries") + " " + processed + "/"));
    }

    private void enableReadyMode() {
        addQueriesBtn.setDisable(false);
        importQueriesBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(true);
        startBtn.setManaged(true);
        abortBtn.setManaged(false);
    }

    private void enableLoadingMode() {
        addQueriesBtn.setDisable(true);
        importQueriesBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(true);
        startBtn.setManaged(false);
        abortBtn.setManaged(true);
    }

    private void enableCompleteMode() {
        addQueriesBtn.setDisable(false);
        importQueriesBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(false);
        startBtn.setManaged(true);
        abortBtn.setManaged(false);
    }

    @Override
    public void onPositionChecked() {
        table.refresh();
        Platform.runLater(() -> processedQueriesCntLbl.setText(rb.getString("processedQueries") + ": "
                + posChecker.getProcessedQueriesCount() + "/"));
    }

    @Override
    public void onAllPositionsChecked() {
        enableCompleteMode();
    }

}
