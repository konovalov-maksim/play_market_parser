package com.github.konovalovmaksim.gp.scraper.gui.controller;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.github.konovalovmaksim.gp.scraper.Global;
import com.github.konovalovmaksim.gp.scraper.Prefs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML private Button bannerBtn;

    private ResourceBundle rb;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();
        new Thread(this::tryToLoadBanner).start();
    }

    @FXML
    private void openPrefs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prefs.fxml"), Global.getBundle());
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(rb.getString("prefs"));
            stage.getIcons().add(new Image("/image/app_icon.png"));
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add("/style.css");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/info.fxml"), Global.getBundle());
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle(rb.getString("infoTitle"));
            stage.getIcons().add(new Image("/image/app_icon.png"));
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add("/style.css");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryToLoadBanner() {
        try {
            String bannerParamsUrl = "https://raw.githubusercontent.com/konovalov-maksim/play_market_parser/develop/src/main/resources/banner.json";
            Document doc = Jsoup
                    .connect(bannerParamsUrl)
                    .ignoreContentType(true)
                    .userAgent(Prefs.getString("user_agent"))
                    .get();
            if (doc == null) return;
            String jsonStr = doc.text();
            JsonObject params = (JsonObject) Jsoner.deserialize(jsonStr);
            String text = Prefs.getString("lang").equals("ru")
                    ? (String) params.get("text_ru")
                    : (String) params.get("text_en");
            String iconUri = (String) params.get("icon_uri");
            String url = (String) params.get("url");
            Platform.runLater(() -> showBanner(text, iconUri, url));
        } catch (Exception e) {
            //do nothing
        }
    }

    private void showBanner(String text, String iconUri, String url) {
        bannerBtn.setText(text);
        bannerBtn.setStyle(String.format("-fx-background-image: url('%s');", iconUri));
        bannerBtn.getStyleClass().add("banner-button");
        bannerBtn.setOnAction(actionEvent -> Global.openUrl(url));
        bannerBtn.setVisible(true);
    }

}
