package playMarketParser.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import playMarketParser.Connection;
import playMarketParser.Global;
import playMarketParser.Prefs;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


public class PrefsController implements Initializable {
    private ResourceBundle rb;

    @FXML private ComboBox<String> csvDelimCb;
    @FXML private ComboBox<String> tipsLangCb;
    @FXML private ComboBox<String> tipsCountryCb;

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
        csvDelimCb.setValue(Prefs.getString("csv_delimiter"));
        tipsLangCb.setItems(FXCollections.observableArrayList("en", "ru", "de", "fr", "es", "it", "be", "pl", "pt", "nl"));
        tipsLangCb.setValue(Prefs.getString("tips_lang"));
        tipsCountryCb.setItems(FXCollections.observableArrayList("-", "GB", "US", "RU", "DE", "FR", "ES", "IT", "BE", "PL", "PT", "NL"));
        tipsCountryCb.setValue(Prefs.getString("tips_country"));

        timeoutTxt.setText(String.valueOf(Prefs.getInt("timeout")));
        proxyTxt.setText(Prefs.getString("proxy"));
        userAgentTxt.setText(Prefs.getString("user_agent"));
        acceptLangTxt.setText(Prefs.getString("accept_language"));

        posChecksCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("pos_checks_cnt")));
        posThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("pos_threads_cnt")));

        tipsThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, Prefs.getInt("tips_threads_cnt")));
        tipsParsingDepthSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, Prefs.getInt("tips_parsing_depth")));
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

        //Сохранение данных
        Prefs.put("lang", ((NamedRadioButton) langTg.getSelectedToggle()).getName());
        Prefs.put("alphabet", ((NamedRadioButton) alphabetTg.getSelectedToggle()).getName());

        Prefs.put("csv_delimiter", csvDelimCb.getValue());
        Prefs.put("tips_lang", tipsLangCb.getValue());
        Prefs.put("tips_country", tipsCountryCb.getValue());

        Prefs.put("timeout", Integer.parseInt(timeoutTxt.getText()));
        Prefs.put("proxy", proxyTxt.getText());
        Prefs.put("user_agent", userAgentTxt.getText());
        Prefs.put("accept_language", acceptLangTxt.getText());

        Prefs.put("pos_checks_cnt", posChecksCntSpin.getValue());
        Prefs.put("pos_threads_cnt", posThreadsCntSpin.getValue());
        Prefs.put("tips_threads_cnt", tipsThreadsCntSpin.getValue());
        Prefs.put("tips_parsing_depth", tipsParsingDepthSpin.getValue());

        Connection.reloadPrefs();
        Global.reloadPrefs();
        Global.log(rb.getString("prefsSaved"));
        onCancelClick();
    }

    @FXML
    private void onCancelClick() {
        ((Stage) posChecksCntSpin.getScene().getWindow()).close();
    }
}
