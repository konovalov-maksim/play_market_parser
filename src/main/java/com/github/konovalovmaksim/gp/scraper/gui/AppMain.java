package com.github.konovalovmaksim.gp.scraper.gui;

import com.github.konovalovmaksim.gp.scraper.Global;
import com.github.konovalovmaksim.gp.scraper.Prefs;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.ResourceBundle;

public class AppMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Stage stage;

    public void start(Stage stage) throws Exception {
        ResourceBundle rb = Global.getBundle();
        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"), rb);
        Parent root = loader.load();
        root.getStylesheets().add("/style.css");
        stage.setTitle(rb.getString("appName"));
        stage.getIcons().add(new Image("/image/app_icon.png"));
        stage.setScene(new Scene(root));
        stage.setMaximized(Prefs.getBoolean("is_window_maximized"));
        stage.setHeight(Prefs.getDouble("window_height"));
        stage.setWidth(Prefs.getDouble("window_width"));
        double x = Prefs.getDouble("window_x");
        double y = Prefs.getDouble("window_y");
        if (x > 0) stage.setX(x);
        if (y > 0) stage.setY(y);
        stage.show();

        Global.setConsoleTa((TextArea) stage.getScene().lookup("#consoleTa"));
        Global.reloadPrefs();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Prefs.put("is_window_maximized", stage.isMaximized());
        if (!stage.isMaximized()) {
            Prefs.put("window_width", stage.getWidth());
            Prefs.put("window_height", stage.getHeight());
            Prefs.put("window_x", stage.getX());
            Prefs.put("window_y", stage.getY());
        }
    }
}
