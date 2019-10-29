package playMarketParser.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import playMarketParser.tipsCollector.Tip;
import playMarketParser.tipsCollector.TipsCollector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static playMarketParser.Global.showAlert;


public class TipsCollectorController implements Initializable, TipsCollector.TipsLoadingListener {

    @FXML private Button addQueriesBtn;
    @FXML private Button importQueriesBtn;
    @FXML private Button clearBtn;
    @FXML private Button exportBtn;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private CheckBox titleFirstChb;
    @FXML private Label queriesCntLbl;
    @FXML private Label tipsCntLbl;
    @FXML private Label progLbl;
    @FXML private ProgressBar progBar;
    @FXML private TableView<String> inputTable;
    @FXML private TableColumn<String, String> inputQueryCol;
    @FXML private TableView<Tip> outputTable;
    @FXML private TableColumn<Tip, String> outputQueryCol;
    @FXML private TableColumn<Tip, String> tipCol;
    @FXML private VBox rootPane;

    private ResourceBundle rb;
    private Prefs prefs;
    private TipsCollector tipsCollector;

    private ObservableList<String> queries = FXCollections.observableArrayList();
    private ObservableList<Tip> tips = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();
        prefs = new Prefs();
        enableReadyMode();

        titleFirstChb.setSelected(prefs.getBoolean("title_first"));

        //inputTable
        inputQueryCol.prefWidthProperty().bind(inputTable.widthProperty().multiply(1));
        inputQueryCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue()));
        inputTable.setItems(queries);
        //outputTable
        outputQueryCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.4));
        tipCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.6));
        outputQueryCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        tipCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        outputTable.setItems(tips);

        queriesCntLbl.textProperty().bind(Bindings.size(queries).asString());
    }

    @FXML
    private void addQueries() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterQueries"), rb.getString("addingQueries"), "");

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println(result.get());
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> queries.add(s));
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
                    .forEachOrdered(s -> queries.add(s));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("unableToReadFile"));
        }
    }

    @FXML
    private void exportResults() {
        if (tips == null || tips.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noResults"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(rb.getString("outTips"));
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
            String firstRow = rb.getString("query") + Global.CSV_DELIMITER + rb.getString("tip") + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (Tip tip : tips)
                newContent.add(tip.getQueryText() + Global.CSV_DELIMITER + tip.getText());
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(rb.getString("saved"), rb.getString("fileSaved"));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("fileNotSaved"));
        }
    }

    @FXML
    private void start() {
        if (queries.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noQueries"));
            return;
        }
        outputTable.getItems().clear();

        tipsCollector = new TipsCollector(queries,
                prefs.getInt("tips_threads_cnt"),
                prefs.getInt("tips_parsing_depth"),
                this);
        enableLoadingMode();
        tipsCollector.start();
    }

    @FXML
    private void pause() {
        tipsCollector.pause();
    }

    @FXML
    private void resume() {
        enableLoadingMode();
        tipsCollector.start();
    }

    @FXML
    private void stop() {
        tipsCollector.stop();
    }

    @FXML
    private void clearQueries() {
        inputTable.getItems().clear();
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
    }

    @Override
    public synchronized void onQueryProcessed(List<Tip> collectedTips) {
        tips.addAll(collectedTips);
        Platform.runLater(() -> tipsCntLbl.setText(String.valueOf(tips.size())));

        progBar.setProgress(tipsCollector.getProgress());
        Platform.runLater(() -> progLbl.setText( String.format("%.1f", tipsCollector.getProgress()*100) + "%"));
    }

    @Override
    public void onFinish() {
        enableCompleteMode();
    }

    @Override
    public void onPause() {
        enablePauseMode();
    }
}
