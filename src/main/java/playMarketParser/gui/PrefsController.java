package playMarketParser.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import playMarketParser.Prefs;


import java.net.URL;
import java.util.*;


public class PrefsController implements Initializable {

    @FXML private Spinner<Integer> posChecksCntSpin;
    @FXML private Spinner<Integer> posThreadsCntSpin;
    @FXML private Spinner<Integer> tipsThreadsCntSpin;
    @FXML private Spinner<Integer> tipsParsingDepthSpin;

    private Prefs prefs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prefs = new Prefs();

        posChecksCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt("pos_checks_cnt")));
        posThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt("pos_threads_cnt")));

        tipsThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt("tips_threads_cnt")));
        tipsParsingDepthSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt("tips_parsing_depth")));
    }


    @FXML
    private void onOkClick() {
        onApplyClick();
        onCancelClick();
    }

    @FXML
    private void onApplyClick() {
        prefs.put("pos_checks_cnt", posChecksCntSpin.getValue());
        prefs.put("pos_threads_cnt", posThreadsCntSpin.getValue());
        prefs.put("tips_threads_cnt", tipsThreadsCntSpin.getValue());
        prefs.put("tips_parsing_depth", tipsParsingDepthSpin.getValue());
    }

    @FXML
    private void onCancelClick() {
        ((Stage) posChecksCntSpin.getScene().getWindow()).close();
    }




}
