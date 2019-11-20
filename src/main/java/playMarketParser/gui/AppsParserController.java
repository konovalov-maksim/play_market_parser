package playMarketParser.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import playMarketParser.App;
import playMarketParser.Global;
import playMarketParser.Prefs;
import playMarketParser.appsParser.AppsParser;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static playMarketParser.Global.showAlert;

public class AppsParserController implements Initializable, AppsParser.AppParsingListener {

    @FXML
    private Button addQueriesBtn;
    @FXML private Button importQueriesBtn;
    @FXML private Button startBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private CheckBox titleFirstChb;
    @FXML private TextField appUrlTf;
    @FXML private Label queriesCntLbl;
    @FXML private Label progLbl;
    @FXML private ProgressBar progBar;
    @FXML private TableView<App> table;
    @FXML private TableColumn<App, String> idCol;

    @FXML private VBox rootPane;

    private Stage stage;

    private AppsParser appsParser;
    private MenuItem removeItem;
    private ResourceBundle rb;

    private ObservableList<App> apps = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void addQueries() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterQueries"), rb.getString("addingQueries"), "");

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            apps.clear();
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> apps.add(new App(s)));
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
        apps.clear();
        try {
            List<String> lines = new LinkedList<>(Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8));
            if (titleFirstChb.isSelected()) {
                lines.remove(0);
            }

            lines.stream().distinct().map(App::new).forEachOrdered(q -> apps.add(q));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("unableToReadFile"), Global.ERROR);
        }
    }

    @FXML
    private void start() {
        if (apps.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noQueries"), Global.ALERT);
            return;
        }
        if (appUrlTf.getText().length() == 0) {
            showAlert(rb.getString("error"), rb.getString("noAppUrl"), Global.ALERT);
            return;
        }
        //for (AndroidApp app : apps) app.reset();
        table.refresh();

        String appId = appUrlTf.getText().replaceAll(".*id=", "");
        Prefs.put("pos_app_url", appUrlTf.getText());

//        posChecker = new PosChecker(appId, queries, this);
//        posChecker.setMaxThreadsCount(Prefs.getInt("pos_threads_cnt"));
//        posChecker.setChecksCount(Prefs.getInt("pos_checks_cnt"));
//        if (!Prefs.getString("pos_lang").equals("-")) posChecker.setLanguage(Prefs.getString("pos_lang"));
//        if (!Prefs.getString("pos_country").equals("-")) posChecker.setCountry(Prefs.getString("pos_country"));

        progBar.setProgress(0);
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", 0f) + "%"));

        enableLoadingMode();
        Global.log(rb.getString("posStarted") + "\n" +
                String.format("%-30s%s%n", rb.getString("threadsCount"), Prefs.getInt("pos_threads_cnt")) +
                String.format("%-30s%s%n", rb.getString("posColLang"), Prefs.getString("pos_lang")) +
                String.format("%-30s%s%n", rb.getString("posColCountry"), Prefs.getString("pos_country")) +
                String.format("%-30s%s%n", rb.getString("acceptLang"), Prefs.getString("accept_language")) +
                String.format("%-30s%s%n", rb.getString("timeout"), Prefs.getInt("timeout")) +
                String.format("%-30s%s%n", rb.getString("proxy"), Prefs.getString("proxy")) +
                String.format("%-30s%s%n", rb.getString("userAgent"), Prefs.getString("user_agent"))
        );
        appsParser.start();
    }

    @FXML
    private void pause() {
        appsParser.pause();
    }

    @FXML
    private void resume() {
        enableLoadingMode();
        Global.log(rb.getString("posResumed"));
        appsParser.resume();
    }

    @FXML
    private void stop() {
        appsParser.stop();
    }

    @FXML
    private void exportResults() {
        if (apps == null || apps.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noResults"), Global.ALERT);
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
            String firstRow = rb.getString("query") + Global.getCsvDelim() + rb.getString("finalPos") + " " + curDate + "\n";
            Files.write(outputFile.toPath(), firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> newContent = new ArrayList<>();
            for (App app : apps) {

            }
            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(rb.getString("saved"), rb.getString("fileSaved") + "\n\n" + rb.getString("posExportTip"), Global.ACCEPT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("alreadyUsing"), Global.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("fileNotSaved"), Global.ERROR);
        }
    }

    @FXML
    private void clearQueries() {
        table.getItems().clear();
        enableReadyMode();
    }

    private void enableReadyMode() {
        appUrlTf.setEditable(true);
        addQueriesBtn.setDisable(false);
        importQueriesBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(true);
        removeItem.setDisable(true);
        Global.setBtnParams(startBtn, true, true);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, false);
    }

    private void enableLoadingMode() {
        appUrlTf.setEditable(false);
        addQueriesBtn.setDisable(true);
        importQueriesBtn.setDisable(true);
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
        addQueriesBtn.setDisable(false);
        importQueriesBtn.setDisable(false);
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
        addQueriesBtn.setDisable(true);
        importQueriesBtn.setDisable(true);
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
    public void onPositionChecked(App app, boolean isSuccess) {
        if (!isSuccess) Global.log(String.format("%-30s%s", app.getId(), rb.getString("connTimeout")));
        table.refresh();
        progBar.setProgress(appsParser.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", appsParser.getProgress() * 100) + "%"));
    }

    @Override
    public void onPause() {
        Global.log(rb.getString("posPaused"));
        enablePauseMode();
    }

    @Override
    public void onFinish() {
        enableCompleteMode();
        Global.log(rb.getString("posComplete"));
        progBar.setProgress(appsParser.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", appsParser.getProgress() * 100) + "%"));
    }
}
