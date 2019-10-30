package playMarketParser.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import playMarketParser.Global;
import playMarketParser.Prefs;
import playMarketParser.positionsChecker.PosChecker;
import playMarketParser.positionsChecker.Query;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static playMarketParser.Global.showAlert;


public class PosCheckerController implements Initializable, PosChecker.PosCheckListener {

    @FXML private Button addQueriesBtn;
    @FXML private Button importQueriesBtn;
    @FXML private Button startBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private CheckBox titleFirstChb;
    @FXML private CheckBox savePrevResultsChb;
    @FXML private TextField appUrlTf;
    @FXML private Label queriesCntLbl;
    @FXML private Label progLbl;
    @FXML private ProgressBar progBar;
    @FXML private TableView<Query> table;
    @FXML private TableColumn<Query, String> queryCol;
    @FXML private TableColumn<Query, String> pseudoPosCol;
    @FXML private TableColumn<Query, String> realPosCol;
    @FXML private VBox rootPane;

    private MenuItem removeItem;
    private PosChecker posChecker;
    private ResourceBundle rb;

    private String titleRow = "";

    private ObservableList<Query> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();

        appUrlTf.setText(Prefs.getString("pos_app_url"));
        titleFirstChb.setSelected(Prefs.getBoolean("title_first"));

        //Tables
        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        pseudoPosCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        realPosCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        queryCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        pseudoPosCol.setCellValueFactory(new PropertyValueFactory<>("pseudoPosString"));
        realPosCol.setCellValueFactory(new PropertyValueFactory<>("realPosString"));
        table.setItems(queries);

        //Context menu
        removeItem = new MenuItem(rb.getString("removeQuery"));
        ImageView delIcon = new ImageView("/images/icons/delete.png");
        delIcon.setFitHeight(20);
        delIcon.setFitWidth(20);
        removeItem.setGraphic(delIcon);
        removeItem.setOnAction(e -> table.getItems().remove(table.getSelectionModel().getSelectedItem()));
        ContextMenu rowMenu = new ContextMenu();
        rowMenu.getItems().add(removeItem);
        table.setRowFactory(c -> {
            TableRow<Query> row = new TableRow<>();
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            return row;
        });

        //Labels
        queriesCntLbl.textProperty().bind(Bindings.size(queries).asString());

        enableReadyMode();
    }

    @FXML
    private void addQueries() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterQueries"), rb.getString("addingQueries"), "");

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            queries.clear();
            savePrevResultsChb.setSelected(false);
            savePrevResultsChb.setDisable(true);
            System.out.println(result.get());
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> queries.add(new Query(s)));
            enableReadyMode();
        }
    }

    @FXML
    private void importQueries() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("csvDescr"), "*.csv"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("txtDescr"), "*.txt"));
        fileChooser.setInitialDirectory(Global.getInitDir("input_path"));
        File inputFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (inputFile == null) return;
        Prefs.put("input_path", inputFile.getParentFile().toString());
        Prefs.put("title_first", titleFirstChb.isSelected());

        enableReadyMode();
        queries.clear();
        try {
            List<String> lines = new LinkedList<>(Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8));
            if (titleFirstChb.isSelected()) {
                titleRow = lines.get(0);
                lines.remove(0);
            }
            boolean manyColumns = lines.size() > 0 && lines.get(0).contains(Global.CSV_DELIMITER);
            savePrevResultsChb.setSelected(manyColumns);
            savePrevResultsChb.setDisable(!manyColumns);
            lines.stream().distinct().map(Query::new).forEachOrdered(q -> queries.add(q));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("unableToReadFile"));
        }
    }

    @FXML
    private void start() {
        if (queries.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noQueries"));
            return;
        }
        if (appUrlTf.getText().length() == 0) {
            showAlert(rb.getString("error"), rb.getString("noAppUrl"));
            return;
        }
        for (Query query : queries) query.reset();
        table.refresh();

        String appId = appUrlTf.getText().replaceAll(".*id=", "");
        Prefs.put("pos_app_url", appUrlTf.getText());

        posChecker = new PosChecker(appId, queries, Prefs.getInt("pos_threads_cnt"),
                Prefs.getInt("pos_checks_cnt"), this);

        enableLoadingMode();
        posChecker.start();
    }

    @FXML
    private void pause() {
        posChecker.pause();
    }

    @FXML
    private void resume() {
        enableLoadingMode();
        posChecker.start();
    }

    @FXML
    private void stop() {
        posChecker.stop();
    }

    @FXML
    private void exportResults() {
        if (queries == null || queries.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noResults"));
            return;
        }

        String curDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(rb.getString("outPositions") + " " + curDate);
        fileChooser.setInitialDirectory(Global.getInitDir("output_path"));
        File outputFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;
        if (!outputFile.getParentFile().canWrite()) {
            showAlert(rb.getString("error"), rb.getString("cantWrite"));
            return;
        }
        Prefs.put("output_path", outputFile.getParentFile().toString());

        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile))) {
            //Указываем кодировку файла UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');

            //Добавляем заголовок
            String firstRow = (savePrevResultsChb.isSelected() ? titleRow : rb.getString("query"))
                    + Global.CSV_DELIMITER + rb.getString("finalPos") + " " + curDate + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (Query query : queries)
                newContent.add((savePrevResultsChb.isSelected() ? query.getFullRowText() : query.getText())
                                + Global.CSV_DELIMITER + query.getRealPos());
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(rb.getString("saved"), rb.getString("fileSaved"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("alreadyUsing"));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("fileNotSaved"));
        }
    }

    @FXML
    private void clearQueries() {
        table.getItems().clear();
        enableReadyMode();
    }

    private void enableReadyMode() {
        addQueriesBtn.setDisable(false);
        importQueriesBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(true);
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
        resumeBtn.setDisable(true);
        stopBtn.setDisable(true);
        savePrevResultsChb.setSelected(false);
        savePrevResultsChb.setDisable(true);
        removeItem.setDisable(false);
    }

    private void enableLoadingMode() {
        addQueriesBtn.setDisable(true);
        importQueriesBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(true);
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);
        resumeBtn.setDisable(true);
        stopBtn.setDisable(false);
        removeItem.setDisable(true);
    }

    private void enableCompleteMode() {
        addQueriesBtn.setDisable(false);
        importQueriesBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(false);
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
        resumeBtn.setDisable(true);
        stopBtn.setDisable(true);
        removeItem.setDisable(false);
    }

    private void enablePauseMode() {
        addQueriesBtn.setDisable(true);
        importQueriesBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(false);
        startBtn.setDisable(true);
        pauseBtn.setDisable(true);
        resumeBtn.setDisable(false);
        stopBtn.setDisable(false);
        removeItem.setDisable(true);
    }

    @Override
    public void onPositionChecked() {
        table.refresh();
        progBar.setProgress(posChecker.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", posChecker.getProgress() * 100) + "%"));
    }

    @Override
    public void onPause() {
        enablePauseMode();
    }

    @Override
    public void onFinish() {
        enableCompleteMode();
        progBar.setProgress(posChecker.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", posChecker.getProgress() * 100) + "%"));
    }

}
