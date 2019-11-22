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
import javafx.stage.Stage;
import playMarketParser.Global;
import playMarketParser.Prefs;
import playMarketParser.tipsCollector.Tip;
import playMarketParser.tipsCollector.TipsCollector;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static playMarketParser.Global.showAlert;


public class TipsCollectorController implements Initializable, TipsCollector.TipsLoadingListener {

    @FXML private Button addBtn;
    @FXML private Button importBtn;
    @FXML private Button clearBtn;
    @FXML private Button exportBtn;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private Label queriesCntLbl;
    @FXML private Label tipsCntLbl;
    @FXML private Label progLbl;
    @FXML private ProgressBar progBar;
    @FXML private TableView<String> inputTable;
    @FXML private TableColumn<String, String> inputQueryCol;
    @FXML private TableView<Tip> outputTable;
    @FXML private TableColumn<Tip, String> outputQueryCol;
    @FXML private TableColumn<Tip, String> tipCol;
    @FXML private TableColumn<Tip, Integer> depthCol;
    @FXML private VBox rootPane;

    private Stage stage;
    private CheckBox titleFirstChb;

    private MenuItem removeItem;
    private ResourceBundle rb;
    private TipsCollector tipsCollector;

    private ObservableList<String> queries = FXCollections.observableArrayList();
    private ObservableList<Tip> tips = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();

        //inputTable
        inputQueryCol.prefWidthProperty().bind(inputTable.widthProperty().multiply(1));
        inputQueryCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue()));
        inputTable.setItems(queries);
        //outputTable
        outputQueryCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.4));
        tipCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.5));
        depthCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        outputQueryCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        tipCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        depthCol.setCellValueFactory(new PropertyValueFactory<>("depth"));
        outputTable.setItems(tips);

        //Context menu
        TableContextMenu inputTableContextMenu = new TableContextMenu(inputTable);
        removeItem = inputTableContextMenu.getRemoveItem();
        TableContextMenu outputTableContextMenu = new TableContextMenu(outputTable);
        outputTableContextMenu.getRemoveItem().setVisible(false);

        //PopOver с чекбоксом
        titleFirstChb = new CheckBox(rb.getString("titleFirst"));
        titleFirstChb.setSelected(Prefs.getBoolean("title_first"));
        Global.addPopOver(importBtn, titleFirstChb);

        //Привязки
        queriesCntLbl.textProperty().bind(Bindings.size(queries).asString());

        //Подсказки кнопок и чекбоксов
        addBtn.setTooltip(new Tooltip(rb.getString("addQueries")));
        importBtn.setTooltip(new Tooltip(rb.getString("importQueries")));
        clearBtn.setTooltip(new Tooltip(rb.getString("clearQueries")));
        exportBtn.setTooltip(new Tooltip(rb.getString("exportResults")));
        titleFirstChb.setTooltip(new Tooltip(rb.getString("skipFirstTip")));


        enableReadyMode();
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void addQueries() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterQueries"), rb.getString("addingQueries"), "");

        Optional result = dialog.showAndWait();
        if (result.isPresent())
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> queries.add(s));
    }

    @FXML
    private void importQueries() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("txtDescr"), "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("csvDescr"), "*.csv"));
        fileChooser.setInitialDirectory(Global.getInitDir("input_path"));
        File inputFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (inputFile == null) return;
        Prefs.put("input_path", inputFile.getParentFile().toString());
        Prefs.put("title_first", titleFirstChb.isSelected());

        try (Stream<String> lines = Files.lines(inputFile.toPath(), StandardCharsets.UTF_8)) {
            lines.skip(titleFirstChb.isSelected() ? 1 : 0)
                    .distinct()
                    .forEachOrdered(s -> queries.add(s));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("unableToReadFile"), Global.ERROR);
        }
    }

    @FXML
    private void exportResults() {
        if (tips == null || tips.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noResults"), Global.ALERT);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(rb.getString("outTips"));
        fileChooser.setInitialDirectory(Global.getInitDir("output_path"));
        File outputFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;
        if (!outputFile.getParentFile().canWrite()) {
            showAlert(rb.getString("error"), rb.getString("cantWrite"), Global.ERROR);
            return;
        }

        Prefs.put("output_path", outputFile.getParentFile().toString());

        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile))) {
            //Указываем кодировку файла UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');

            //Добавляем заголовок
            String firstRow = rb.getString("query") + Global.getCsvDelim() + rb.getString("tip")
                    + Global.getCsvDelim() + rb.getString("depth") + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (Tip tip : tips)
                newContent.add(tip.getQueryText() + Global.getCsvDelim() + tip.getText() + Global.getCsvDelim() + tip.getDepth());
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(rb.getString("saved"), rb.getString("fileSaved"), Global.ACCEPT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("alreadyUsing"), Global.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("fileNotSaved"), Global.ERROR);
        }
    }

    @FXML
    private void start() {
        if (queries.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noQueries"), Global.ALERT);
            return;
        }
        outputTable.getItems().clear();

        tipsCollector = new TipsCollector(queries,this);
        tipsCollector.setMaxThreadsCount(Prefs.getInt("tips_threads_cnt"));
        tipsCollector.setMaxDepth(Prefs.getInt("tips_parsing_depth"));
        tipsCollector.setAlphaType(Prefs.getString("alphabet"));
        if (!Prefs.getString("tips_lang").equals("-")) tipsCollector.setLanguage(Prefs.getString("tips_lang"));
        if (!Prefs.getString("tips_country").equals("-")) tipsCollector.setCountry(Prefs.getString("tips_country"));
        enableLoadingMode();
        Global.log(rb.getString("tipsStarted") + "\n" +
                String.format("%-30s%s%n", rb.getString("threadsCount"), Prefs.getInt("tips_threads_cnt")) +
                String.format("%-30s%s%n", rb.getString("tipsParsingDepth"), Prefs.getInt("tips_parsing_depth")) +
                String.format("%-30s%s%n", rb.getString("parsingLang"), Prefs.getString("tips_lang")) +
                String.format("%-30s%s%n", rb.getString("parsingCountry"), Prefs.getString("tips_country")) +
                String.format("%-30s%s%n", rb.getString("alphaType"), Prefs.getString("alphabet")) +
                String.format("%-30s%s%n", rb.getString("timeout"), Prefs.getInt("timeout")) +
                String.format("%-30s%s%n", rb.getString("proxy"), Prefs.getString("proxy")) +
                String.format("%-30s%s%n", rb.getString("acceptLang"), Prefs.getString("accept_language")) +
                String.format("%-30s%s%n", rb.getString("userAgent"), Prefs.getString("user_agent"))
        );
        tipsCollector.start();
    }

    @FXML
    private void pause() {
        tipsCollector.pause();
    }

    @FXML
    private void resume() {
        Global.log(rb.getString("tipsResumed"));
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
        addBtn.setDisable(false);
        importBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(true);
        removeItem.setDisable(false);
        Global.setBtnParams(startBtn, true, true);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, false);
    }

    private void enableLoadingMode() {
        addBtn.setDisable(true);
        importBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(true);
        removeItem.setDisable(true);
        Global.setBtnParams(startBtn, false, false);
        Global.setBtnParams(pauseBtn, true, true);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, true);
    }

    private void enableCompleteMode() {
        addBtn.setDisable(false);
        importBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(false);
        removeItem.setDisable(false);
        Global.setBtnParams(startBtn, true, true);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, false);
    }

    private void enablePauseMode() {
        addBtn.setDisable(true);
        importBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(false);
        removeItem.setDisable(true);
        Global.setBtnParams(startBtn, false, false);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, true, true);
        Global.setBtnParams(stopBtn, true, true);
    }

    @Override
    public synchronized void onQueryProcessed(List<Tip> collectedTips, String queryText, boolean isSuccess) {
        if (!isSuccess) Global.log(String.format("%-30s%s", queryText, rb.getString("connTimeout")));
        tips.addAll(collectedTips);
        Platform.runLater(() -> tipsCntLbl.setText(String.valueOf(tips.size())));

        progBar.setProgress(tipsCollector.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", tipsCollector.getProgress() * 100) + "%"));
    }

    @Override
    public void onPause() {
        Global.log(rb.getString("tipsPaused"));
        enablePauseMode();
    }

    @Override
    public void onFinish() {
        Global.log(rb.getString("tipsComplete"));
        enableCompleteMode();
        progBar.setProgress(tipsCollector.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", tipsCollector.getProgress() * 100) + "%"));
    }
}
