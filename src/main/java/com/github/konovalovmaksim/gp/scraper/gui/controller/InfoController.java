package com.github.konovalovmaksim.gp.scraper.gui.controller;

import com.github.konovalovmaksim.gp.scraper.Global;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class InfoController implements Initializable {

    @FXML private Pane rootPane;
    @FXML private Button emailBtn;

    private ResourceBundle rb;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rb = Global.getBundle();
    }

    @FXML
    private void openEmailClient() {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                String mailto = "mailto:" + emailBtn.getText() + "?subject=" + URLEncoder.encode(rb.getString("appName"), StandardCharsets.UTF_8);
                URI mailtoUri = new URI(mailto);
                desktop.mail(mailtoUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Global.showAlert(rb.getString("error"), rb.getString("emailFailed"), Global.ERROR);
        }
    }

    @FXML
    private void showHelp() {
        Global.openUrl("https://github.com/konovalov-maksim/play_market_parser/blob/master/README.md");
    }

    @FXML
    private void showLicense() {
        Global.openUrl("https://github.com/konovalov-maksim/play_market_parser/blob/master/LICENSE");
    }

    @FXML
    private void closeInfo() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }



}
