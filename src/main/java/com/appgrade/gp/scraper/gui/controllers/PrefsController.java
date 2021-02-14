package com.appgrade.gp.scraper.gui.controllers;

import com.appgrade.gp.scraper.Global;
import com.appgrade.gp.scraper.Prefs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.appgrade.gp.scraper.entities.Connection;
import com.appgrade.gp.scraper.gui.custom.NamedRadioButton;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


public class PrefsController implements Initializable {
    private ResourceBundle rb;

    @FXML private ComboBox<String> csvDelimCb;
    @FXML private ComboBox<String> parsingLangCb;
    @FXML private ComboBox<String> parsingCountryCb;

    private ToggleGroup langTg = new ToggleGroup();
    @FXML private NamedRadioButton ruRb;
    @FXML private NamedRadioButton enRb;
    private ToggleGroup alphabetTg = new ToggleGroup();
    @FXML private NamedRadioButton autoAlphRb;
    @FXML private NamedRadioButton latAlphRb;
    @FXML private NamedRadioButton cyrAlphRb;
    @FXML private NamedRadioButton allAlphRb;

    @FXML private TextField timeoutTxt;
    @FXML private TextField proxyTxt;
    @FXML private TextField userAgentTxt;
    @FXML private TextField acceptLangTxt;

    @FXML private Spinner<Integer> posChecksCntSpin;
    @FXML private Spinner<Integer> posThreadsCntSpin;
    @FXML private Spinner<Integer> tipsThreadsCntSpin;
    @FXML private Spinner<Integer> tipsParsingDepthSpin;
    @FXML private Spinner<Integer> appsThreadsCntSpin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();

        ruRb.setToggleGroup(langTg);
        enRb.setToggleGroup(langTg);
        for (Toggle toggle : langTg.getToggles())
            if (((NamedRadioButton) toggle).getName().equals(Prefs.getString("lang"))) toggle.setSelected(true);

        autoAlphRb.setToggleGroup(alphabetTg);
        latAlphRb.setToggleGroup(alphabetTg);
        cyrAlphRb.setToggleGroup(alphabetTg);
        allAlphRb.setToggleGroup(alphabetTg);
        for (Toggle toggle : alphabetTg.getToggles())
            if (((NamedRadioButton) toggle).getName().equals(Prefs.getString("alphabet"))) toggle.setSelected(true);


        csvDelimCb.setItems(FXCollections.observableArrayList(";", ","));
        parsingLangCb.setItems(FXCollections.observableArrayList("-", "en", "ru", "de", "fr", "es", "it", "be", "pl", "pt", "nl"));
        parsingCountryCb.setItems(FXCollections.observableArrayList("-", "GB", "US", "RU", "DE", "FR", "ES", "IT", "BE", "PL", "PT", "NL"));
        csvDelimCb.setValue(Prefs.getString("csv_delimiter"));
        parsingLangCb.setValue(Prefs.getString("parsing_lang"));
        parsingCountryCb.setValue(Prefs.getString("parsing_country"));

        timeoutTxt.setText(String.valueOf(Prefs.getInt("timeout")));
        proxyTxt.setText(Prefs.getString("proxy"));
        userAgentTxt.setText(Prefs.getString("user_agent"));
        acceptLangTxt.setText(Prefs.getString("accept_language"));

        posChecksCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("pos_checks_cnt")));
        posThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("pos_threads_cnt")));

        tipsThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("tips_threads_cnt")));
        tipsParsingDepthSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, Prefs.getInt("tips_parsing_depth")));

        appsThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("apps_threads_cnt")));
    }


    @FXML
    private void onOkClick() {
        //Валидация данных
        Pattern intPattern = Pattern.compile("\\d+");
        Pattern proxyPattern = Pattern.compile("^$|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$");
        if (!intPattern.matcher(timeoutTxt.getText()).matches()) {
            timeoutTxt.getStyleClass().add("field-wrong");
            return;
        } else timeoutTxt.getStyleClass().remove("field-wrong");
        if (!proxyPattern.matcher(proxyTxt.getText()).matches()) {
            proxyTxt.getStyleClass().add("field-wrong");
            return;
        } else proxyTxt.getStyleClass().remove("field-wrong");

        if (!((NamedRadioButton) langTg.getSelectedToggle()).getName().equals(Prefs.getString("lang")))
            Global.showAlert(rb.getString("restartRequired"), rb.getString("restartPls"), Global.ALERT);

        //Сохранение данных
        Prefs.put("lang", ((NamedRadioButton) langTg.getSelectedToggle()).getName());
        Prefs.put("alphabet", ((NamedRadioButton) alphabetTg.getSelectedToggle()).getName());

        Prefs.put("csv_delimiter", csvDelimCb.getValue());
        Prefs.put("parsing_lang", parsingLangCb.getValue());
        Prefs.put("parsing_country", parsingCountryCb.getValue());

        Prefs.put("timeout", Integer.parseInt(timeoutTxt.getText()));
        Prefs.put("proxy", proxyTxt.getText());
        Prefs.put("user_agent", userAgentTxt.getText());
        Prefs.put("accept_language", acceptLangTxt.getText());

        Prefs.put("pos_checks_cnt", posChecksCntSpin.getValue());
        Prefs.put("pos_threads_cnt", posThreadsCntSpin.getValue());
        Prefs.put("tips_threads_cnt", tipsThreadsCntSpin.getValue());
        Prefs.put("tips_parsing_depth", tipsParsingDepthSpin.getValue());
        Prefs.put("apps_threads_cnt", appsThreadsCntSpin.getValue());

        Connection.reloadPrefs();
        Global.reloadPrefs();
        Global.log(rb.getString("prefsSaved"));
        onCancelClick();
    }

    @FXML
    private void onCancelClick() {
        ((Stage) posChecksCntSpin.getScene().getWindow()).close();
    }

    @FXML
    void resetToDefaults() {
        Prefs.remove("pos_threads_cnt");
        Prefs.remove("pos_checks_cnt");
        Prefs.remove("tips_threads_cnt");
        Prefs.remove("tips_parsing_depth");
        Prefs.remove("user_agent");
        Prefs.remove("accept_language");
        Prefs.remove("timeout");
        Prefs.remove("proxy");
        Prefs.remove("csv_delimiter");
        Prefs.remove("parsing_lang");
        Prefs.remove("parsing_country");
        Prefs.remove("tips_lang");
        Prefs.remove("tips_country");
        Prefs.remove("alphabet");
        Prefs.remove("apps_threads_cnt");
        Prefs.remove("apps_country");
        Prefs.remove("apps_lang");

        onCancelClick();
    }
}
