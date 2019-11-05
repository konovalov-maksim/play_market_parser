package playMarketParser.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import playMarketParser.Global;

import java.util.ResourceBundle;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage stage) throws Exception {
        Global.setLocale(Global.Locale.EN);
        ResourceBundle bundle = Global.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/main.fxml"), bundle);
        Parent root = loader.load();
        root.getStylesheets().add("/view/style.css");
        stage.setTitle(bundle.getString("appName"));
        stage.setScene(new Scene(root));
        stage.show();
    }
}
