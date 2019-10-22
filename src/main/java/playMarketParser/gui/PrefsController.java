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

    Prefs prefs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prefs = new Prefs();

        posChecksCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt("pos_checks_cnt")));
        posThreadsCntSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt("pos_threads_cnt")));
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
    }

    @FXML
    private void onCancelClick() {
        ((Stage) posChecksCntSpin.getScene().getWindow()).close();
    }




}
