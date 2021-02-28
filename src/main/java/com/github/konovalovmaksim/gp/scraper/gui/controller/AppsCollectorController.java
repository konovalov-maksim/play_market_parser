package com.github.konovalovmaksim.gp.scraper.gui.controller;

import com.github.konovalovmaksim.gp.scraper.Global;
import com.github.konovalovmaksim.gp.scraper.Prefs;
import com.github.konovalovmaksim.gp.scraper.model.FoundApp;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import com.github.konovalovmaksim.gp.scraper.gui.custom.RowNumCellFactory;
import com.github.konovalovmaksim.gp.scraper.gui.custom.TableContextMenu;
import com.github.konovalovmaksim.gp.scraper.gui.custom.TextAreaDialog;
import com.github.konovalovmaksim.gp.scraper.service.appscollector.AppsCollector;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class AppsCollectorController implements Initializable, AppsCollector.AppsCollectingListener {

    @FXML private Button addBtn;
    @FXML private Button importBtn;
    @FXML private Button startBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private Label queriesCntLbl;
    @FXML private Label appsCntLbl;
    @FXML private Label progLbl;

    @FXML private ProgressBar progBar;

    @FXML private VBox rootPane;

    @FXML private TableView<String> inputTable;
    @FXML private TableColumn<String, Integer> inRowNumCol;
    @FXML private TableColumn<String, String> inputQueryCol;
    @FXML private TableView<FoundApp> outputTable;
    @FXML private TableColumn<FoundApp, Integer> outRowNumCol;
    @FXML private TableColumn<FoundApp, String> appQueryCol;
    @FXML private TableColumn<FoundApp, Integer> positionCol;
    @FXML private TableColumn<FoundApp, String> urlCol;
    @FXML private TableColumn<FoundApp, String> nameCol;
    @FXML private TableColumn<FoundApp, String> shortDescrCol;
    @FXML private TableColumn<FoundApp, String> iconUrlCol;
    @FXML private TableColumn<FoundApp, Double> avgRateCol;
    @FXML private TableColumn<FoundApp, String> devUrlCol;
    @FXML private TableColumn<FoundApp, String> devNameCol;

    private CheckBox titleFirstChb;
    private MenuItem removeItem;

    private AppsCollector appsCollector;

    private ResourceBundle rb;
    private ObservableList<FoundApp> foundApps = FXCollections.observableArrayList();
    private ObservableList<String> queries = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();

        outRowNumCol.setPrefWidth(RowNumCellFactory.WIDTH);
        appQueryCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1).subtract(RowNumCellFactory.WIDTH));
        positionCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        urlCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        nameCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.2));
        shortDescrCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        avgRateCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        iconUrlCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        devUrlCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        devNameCol.prefWidthProperty().bind(outputTable.widthProperty().multiply(0.1));
        outRowNumCol.setCellFactory(new RowNumCellFactory<>());
        appQueryCol.setCellValueFactory(new PropertyValueFactory<>("query"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        shortDescrCol.setCellValueFactory(new PropertyValueFactory<>("shortDescr"));
        avgRateCol.setCellValueFactory(new PropertyValueFactory<>("avgRate"));
        iconUrlCol.setCellValueFactory(new PropertyValueFactory<>("iconUrl"));
        devUrlCol.setCellValueFactory(new PropertyValueFactory<>("devUrl"));
        devNameCol.setCellValueFactory(new PropertyValueFactory<>("devName"));
        outputTable.setItems(foundApps);

        inRowNumCol.setPrefWidth(RowNumCellFactory.WIDTH);
        inputQueryCol.prefWidthProperty().bind(inputTable.widthProperty().multiply(1).subtract(RowNumCellFactory.WIDTH));
        inputQueryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        inRowNumCol.setCellFactory(new RowNumCellFactory<>());
        inputTable.setItems(queries);

        //Context menus
        TableContextMenu outputContextMenu = new TableContextMenu(outputTable);
        outputContextMenu.getRemoveItem().setVisible(false);
        TableContextMenu inputContextMenu = new TableContextMenu(inputTable);
        removeItem = inputContextMenu.getRemoveItem();

        //Привязки
        queriesCntLbl.textProperty().bind(Bindings.size(queries).asString());
        appsCntLbl.textProperty().bind(Bindings.size(foundApps).asString());

        //PopOvers с чекбоксами
        titleFirstChb = new CheckBox(rb.getString("titleFirst"));
        titleFirstChb.setSelected(Prefs.getBoolean("title_first"));
        Global.addPopOver(importBtn, titleFirstChb);

        //Подсказки кнопок и чекбоксов
        addBtn.setTooltip(new Tooltip(rb.getString("addQueries")));
        importBtn.setTooltip(new Tooltip(rb.getString("importQueries")));
        clearBtn.setTooltip(new Tooltip(rb.getString("clearData")));
        exportBtn.setTooltip(new Tooltip(rb.getString("exportResults")));
        titleFirstChb.setTooltip(new Tooltip(rb.getString("skipFirstTip")));

        enableReadyMode();
    }

    @FXML
    private void addQueries() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterQueries"), rb.getString("addingQueries"), "");

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            queries.clear();
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> queries.add(s));
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
        try (Stream<String> lines = Files.lines(inputFile.toPath(), StandardCharsets.UTF_8)) {
            lines.skip(titleFirstChb.isSelected() ? 1 : 0)
                    .distinct()
                    .forEachOrdered(q -> queries.add(q));
        } catch (Exception e) {
            e.printStackTrace();
            Global.showAlert(rb.getString("error"), rb.getString("unableToReadFile"), Global.ERROR);
        }
    }

    @FXML
    private void start() {
        if (queries.size() == 0) {
            Global.showAlert(rb.getString("error"), rb.getString("noAppsSpec"), Global.ALERT);
            return;
        }
        outputTable.getItems().clear();

        appsCollector = new AppsCollector(queries, this);
        appsCollector.setMaxThreadsCount(Prefs.getInt("apps_threads_cnt"));
        if (!Prefs.getString("parsing_lang").equals("-")) appsCollector.setLanguage(Prefs.getString("parsing_lang"));
        if (!Prefs.getString("parsing_country").equals("-")) appsCollector.setCountry(Prefs.getString("parsing_country"));

        progBar.setProgress(0);
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", 0f) + "%"));

        enableLoadingMode();
        Global.log(rb.getString("appsColStarted") + "\n" +
                String.format("%-30s%s%n", rb.getString("threadsCount"), Prefs.getInt("pos_threads_cnt")) +
                String.format("%-30s%s%n", rb.getString("checksCount"), Prefs.getInt("pos_checks_cnt")) +
                String.format("%-30s%s%n", rb.getString("parsingLang"), Prefs.getString("parsing_lang")) +
                String.format("%-30s%s%n", rb.getString("parsingCountry"), Prefs.getString("parsing_country")) +
                String.format("%-30s%s%n", rb.getString("acceptLang"), Prefs.getString("accept_language")) +
                String.format("%-30s%s%n", rb.getString("timeout"), Prefs.getInt("timeout")) +
                String.format("%-30s%s%n", rb.getString("proxy"), Prefs.getString("proxy")) +
                String.format("%-30s%s%n", rb.getString("userAgent"), Prefs.getString("user_agent"))
        );
        appsCollector.start();
    }

    @FXML
    private void pause() {
        appsCollector.pause();
        Global.log(rb.getString("appsColPaused"));
        enablePauseMode();
    }

    @FXML
    private void resume() {
        appsCollector.resume();
        Global.log(rb.getString("appsColResumed"));
        enableLoadingMode();
    }

    @FXML
    private void stop() {
        appsCollector.stop();
        onFinish();
    }

    @FXML
    private void exportResults() {
        if (foundApps == null || foundApps.size() == 0) {
            Global.showAlert(rb.getString("error"), rb.getString("noResults"), Global.ALERT);
            return;
        }

        String curDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(rb.getString("outCollectedApps") + " " + curDate);
        fileChooser.setInitialDirectory(Global.getInitDir("output_path"));
        File outputFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;
        if (!outputFile.getParentFile().canWrite()) {
            Global.showAlert(rb.getString("error"), rb.getString("cantWrite"), Global.ERROR);
            return;
        }
        Prefs.put("output_path", outputFile.getParentFile().toString());

        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile))) {
            //set file encoding UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');

            String csvDelim = Global.getCsvDelim();
            List<String> newContent = new ArrayList<>();
            //add header
            StringBuilder firstRow = new StringBuilder();
            for (TableColumn col : outputTable.getColumns())
                if (col.isVisible() && !col.getText().equals("#"))
                    firstRow.append(col.getText()).append(csvDelim);
            newContent.add(firstRow.toString());

            //iterate over all rows in table
            for (int r = 0; r < outputTable.getItems().size(); r++) {
                StringBuilder newRow = new StringBuilder();
                for (TableColumn col : outputTable.getColumns())
                    if (col.isVisible() && !col.getText().equals("#")) {
                        Object cellData = col.getCellData(r);
                        String cellString = cellData != null ? cellData.toString() : "";
                        //Кодируем спец символы перед записью в CSV
                        if (cellString.contains(csvDelim) || cellString.contains("\"")) {
                            cellString = cellString.replaceAll("\"", "\"\"");
                            cellString = "\"" + cellString + "\"";
                        }
                        newRow.append(cellString).append(csvDelim);
                    }
                newContent.add(newRow.toString());
            }

            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            Global.showAlert(rb.getString("saved"), rb.getString("fileSaved"), Global.ACCEPT);
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
        inputTable.getItems().clear();
        outputTable.getItems().clear();
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
    public synchronized void onQueryProcessed(List<FoundApp> foundApps, String query, boolean isSuccess) {
        if (!isSuccess) Global.log(String.format("%-30s%s", query, rb.getString("connTimeout")));
        else if (foundApps.isEmpty()) Global.log(String.format("%-30s%s", query, rb.getString("noApps")));
        else {
            Platform.runLater(() -> outputTable.getItems().addAll(foundApps));
            outputTable.refresh();
            progBar.setProgress(appsCollector.getProgress());
            Platform.runLater(() -> progLbl.setText(String.format("%.1f", appsCollector.getProgress() * 100) + "%"));
        }
    }

    @Override
    public void onFinish() {
        enableCompleteMode();
        Global.log(rb.getString("appsColComplete"));
        progBar.setProgress(appsCollector.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", appsCollector.getProgress() * 100) + "%"));
    }

}
