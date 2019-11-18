package playMarketParser.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Scene;
import playMarketParser.Global;

import java.util.ResourceBundle;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        ResourceBundle rb = Global.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/main.fxml"), rb);
        Parent root = loader.load();
        root.getStylesheets().add("/view/style.css");
        stage.setTitle(rb.getString("appName"));
        stage.getIcons().add(new Image("/images/app_icon.png"));
        stage.setScene(new Scene(root));
        stage.show();

        Global.setConsoleTa((TextArea) stage.getScene().lookup("#consoleTa"));
        Global.reloadPrefs();
    }
}
