package playMarketParser.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import playMarketParser.DocReader;
import playMarketParser.Prefs;


import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


public class PrefsController implements Initializable {

    private Scene scene;

    @FXML private TextField timeoutTxt;
    @FXML private TextField proxyTxt;
    @FXML private TextField userAgentTxt;
    @FXML private TextField acceptLangTxt;

    @FXML private Spinner<Integer> posChecksCntSpin;
    @FXML private Spinner<Integer> posThreadsCntSpin;
    @FXML private Spinner<Integer> tipsThreadsCntSpin;
    @FXML private Spinner<Integer> tipsParsingDepthSpin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        timeoutTxt.setText(String.valueOf(Prefs.getInt("timeout")));
        proxyTxt.setText(Prefs.getString("proxy"));
        userAgentTxt.setText(Prefs.getString("user_agent"));
        acceptLangTxt.setText(Prefs.getString("accept_language"));

        posChecksCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("pos_checks_cnt")));
        posThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("pos_threads_cnt")));

        tipsThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("tips_threads_cnt")));
        tipsParsingDepthSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, Prefs.getInt("tips_parsing_depth")));
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scene.getStylesheets().add("/view/style.css");
    }

    @FXML
    private void onOkClick() {
        //Валидация данных
        Pattern intPattern = Pattern.compile("\\d+");
        Pattern proxyPattern = Pattern.compile("^$|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$");
        if (!intPattern.matcher(timeoutTxt.getText()).matches()) {
            timeoutTxt.getStyleClass().add("fieldWrong");
            return;
        } else timeoutTxt.getStyleClass().remove("fieldWrong");
        if (!proxyPattern.matcher(proxyTxt.getText()).matches()) {
            proxyTxt.getStyleClass().add("fieldWrong");
            return;
        } else proxyTxt.getStyleClass().remove("fieldWrong");


        //Сохранение данных
        Prefs.put("timeout", Integer.parseInt(timeoutTxt.getText()));
        Prefs.put("proxy", proxyTxt.getText());
        Prefs.put("user_agent", userAgentTxt.getText());
        Prefs.put("accept_language", acceptLangTxt.getText());

        Prefs.put("pos_checks_cnt", posChecksCntSpin.getValue());
        Prefs.put("pos_threads_cnt", posThreadsCntSpin.getValue());
        Prefs.put("tips_threads_cnt", tipsThreadsCntSpin.getValue());
        Prefs.put("tips_parsing_depth", tipsParsingDepthSpin.getValue());
        DocReader.reloadPrefs();
        onCancelClick();
    }


    @FXML
    private void onCancelClick() {
        ((Stage) posChecksCntSpin.getScene().getWindow()).close();
    }




}
