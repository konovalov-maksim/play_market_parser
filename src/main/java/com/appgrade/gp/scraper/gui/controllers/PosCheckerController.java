package com.appgrade.gp.scraper.gui.controllers;

import com.appgrade.gp.scraper.Global;
import com.appgrade.gp.scraper.Prefs;
import com.appgrade.gp.scraper.entities.Query;
import com.appgrade.gp.scraper.gui.customElements.TableContextMenu;
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
import com.appgrade.gp.scraper.gui.customElements.RowNumCellFactory;
import com.appgrade.gp.scraper.gui.customElements.TextAreaDialog;
import com.appgrade.gp.scraper.modules.positionsChecker.PosChecker;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;


public class PosCheckerController implements Initializable, PosChecker.PosCheckListener {

    @FXML private Button addBtn;
    @FXML private Button importBtn;
    @FXML private Button startBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private TextField appUrlTf;
    @FXML private Label queriesCntLbl;
    @FXML private Label progLbl;
    @FXML private ProgressBar progBar;
    @FXML private TableView<Query> table;
    @FXML private TableColumn<Query, Integer> rowNumCol;
    @FXML private TableColumn<Query, String> queryCol;
    @FXML private TableColumn<Query, String> pseudoPosCol;
    @FXML private TableColumn<Query, String> realPosCol;
    @FXML private VBox rootPane;

    private CheckBox titleFirstChb;
    private CheckBox savePrevResultsChb;

    private MenuItem removeItem;
    private PosChecker posChecker;
    private ResourceBundle rb;

    private String titleRow = "";

    private ObservableList<Query> queries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();

        appUrlTf.setText(Prefs.getString("pos_app_url"));

        //Tables
        rowNumCol.setPrefWidth(RowNumCellFactory.WIDTH);
        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5).subtract(RowNumCellFactory.WIDTH));
        pseudoPosCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        realPosCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        rowNumCol.setCellFactory(new RowNumCellFactory<>());
        queryCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        pseudoPosCol.setCellValueFactory(new PropertyValueFactory<>("pseudoPosString"));
        realPosCol.setCellValueFactory(new PropertyValueFactory<>("realPosString"));
        table.setItems(queries);

        //Context menus
        TableContextMenu tableContextMenu = new TableContextMenu(table);
        removeItem = tableContextMenu.getRemoveItem();

        //Привязки
        queriesCntLbl.textProperty().bind(Bindings.size(queries).asString());

        //PopOvers с чекбоксами
        titleFirstChb = new CheckBox(rb.getString("titleFirst"));
        titleFirstChb.setSelected(Prefs.getBoolean("title_first"));
        savePrevResultsChb = new CheckBox(rb.getString("savePrevResults"));
        Global.addPopOver(importBtn, titleFirstChb);
        Global.addPopOver(exportBtn, savePrevResultsChb);

        //Подсказки кнопок и чекбоксов
        addBtn.setTooltip(new Tooltip(rb.getString("addQueries")));
        importBtn.setTooltip(new Tooltip(rb.getString("importQueries")));
        clearBtn.setTooltip(new Tooltip(rb.getString("clearData")));
        exportBtn.setTooltip(new Tooltip(rb.getString("exportResults")));
        titleFirstChb.setTooltip(new Tooltip(rb.getString("skipFirstTip")));
        savePrevResultsChb.setTooltip(new Tooltip(rb.getString("savePrevResultsTip")));

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
            boolean multiplyColumns = lines.size() > 0 && lines.get(0).contains(Global.getCsvDelim());
            savePrevResultsChb.setSelected(multiplyColumns);
            savePrevResultsChb.setDisable(!multiplyColumns);
            lines.stream().distinct().map(Query::new).forEachOrdered(q -> queries.add(q));
        } catch (Exception e) {
            e.printStackTrace();
            Global.showAlert(rb.getString("error"), rb.getString("unableToReadFile"), Global.ERROR);
        }
    }

    @FXML
    private void start() {
        if (queries.size() == 0) {
            Global.showAlert(rb.getString("error"), rb.getString("noQueries"), Global.ALERT);
            return;
        }
        if (appUrlTf.getText().length() == 0) {
            Global.showAlert(rb.getString("error"), rb.getString("noAppUrl"), Global.ALERT);
            return;
        }
        for (Query query : queries) query.reset();
        table.refresh();

        String appId = appUrlTf.getText().replaceAll(".*id=", "");
        Prefs.put("pos_app_url", appUrlTf.getText());

        posChecker = new PosChecker(appId, queries, this);
        posChecker.setMaxThreadsCount(Prefs.getInt("pos_threads_cnt"));
        posChecker.setChecksCount(Prefs.getInt("pos_checks_cnt"));
        if (!Prefs.getString("parsing_lang").equals("-")) posChecker.setLanguage(Prefs.getString("parsing_lang"));
        if (!Prefs.getString("parsing_country").equals("-")) posChecker.setCountry(Prefs.getString("parsing_country"));

        progBar.setProgress(0);
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", 0f) + "%"));

        enableLoadingMode();
        Global.log(rb.getString("posStarted") + "\n" +
                String.format("%-30s%s%n", rb.getString("appUrl"), appUrlTf.getText()) +
                String.format("%-30s%s%n", rb.getString("threadsCount"), Prefs.getInt("pos_threads_cnt")) +
                String.format("%-30s%s%n", rb.getString("checksCount"), Prefs.getInt("pos_checks_cnt")) +
                String.format("%-30s%s%n", rb.getString("parsingLang"), Prefs.getString("parsing_lang")) +
                String.format("%-30s%s%n", rb.getString("parsingCountry"), Prefs.getString("parsing_country")) +
                String.format("%-30s%s%n", rb.getString("acceptLang"), Prefs.getString("accept_language")) +
                String.format("%-30s%s%n", rb.getString("timeout"), Prefs.getInt("timeout")) +
                String.format("%-30s%s%n", rb.getString("proxy"), Prefs.getString("proxy")) +
                String.format("%-30s%s%n", rb.getString("userAgent"), Prefs.getString("user_agent"))
        );
        posChecker.start();
    }

    @FXML
    private void pause() {
        posChecker.pause();
        Global.log(rb.getString("posPaused"));
        enablePauseMode();
    }

    @FXML
    private void resume() {
        posChecker.resume();
        Global.log(rb.getString("posResumed"));
        enableLoadingMode();
    }

    @FXML
    private void stop() {
        posChecker.stop();
        onFinish();
    }

    @FXML
    private void exportResults() {
        if (queries == null || queries.size() == 0) {
            Global.showAlert(rb.getString("error"), rb.getString("noResults"), Global.ALERT);
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
            Global.showAlert(rb.getString("error"), rb.getString("cantWrite"), Global.ERROR);
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
                    + Global.getCsvDelim() + rb.getString("finalPos") + " " + curDate + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (Query query : queries)
                newContent.add((savePrevResultsChb.isSelected() ? query.getFullRowText() : query.getText())
                        + Global.getCsvDelim() + query.getRealPos());
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            Global.showAlert(rb.getString("saved"), rb.getString("fileSaved") + "\n\n" + rb.getString("posExportTip"), Global.ACCEPT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Global.showAlert(rb.getString("error"), rb.getString("alreadyUsing"), Global.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            Global.showAlert(rb.getString("error"), rb.getString("fileNotSaved"), Global.ERROR);
        }
    }

    @FXML
    private void clearQueries() {
        table.getItems().clear();
        enableReadyMode();
    }

    private void enableReadyMode() {
        appUrlTf.setEditable(true);
        addBtn.setDisable(false);
        importBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(true);
        removeItem.setDisable(false);
        savePrevResultsChb.setSelected(false);
        savePrevResultsChb.setDisable(true);
        Global.setBtnParams(startBtn, true, true);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, false);
    }

    private void enableLoadingMode() {
        appUrlTf.setEditable(false);
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
        appUrlTf.setEditable(true);
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
        appUrlTf.setEditable(false);
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
    public void onPositionChecked(Query query, boolean isSuccess) {
        if (!isSuccess) Global.log(String.format("%-30s%s", query.getText(), rb.getString("connTimeout")));
        table.refresh();
        progBar.setProgress(posChecker.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", posChecker.getProgress() * 100) + "%"));
    }

    @Override
    public void onFinish() {
        enableCompleteMode();
        Global.log(rb.getString("posComplete"));
        progBar.setProgress(posChecker.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", posChecker.getProgress() * 100) + "%"));
    }

}
