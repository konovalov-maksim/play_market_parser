package com.appgrade.gp.scraper.gui.controllers;

import com.appgrade.gp.scraper.Global;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    private ResourceBundle rb;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();
    }

    @FXML
    private void openPrefs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/prefs.fxml"), Global.getBundle());
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(rb.getString("prefs"));
            stage.getIcons().add(new Image("/image/app_icon.png"));
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add("/view/style.css");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/info.fxml"), Global.getBundle());
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(rb.getString("infoTitle"));
            stage.getIcons().add(new Image("/image/app_icon.png"));
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add("/view/style.css");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
