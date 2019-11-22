package playMarketParser;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Global {

    public static void reloadPrefs() {
        csvDelim = Prefs.getString("csv_delimiter");
    }

    private static String csvDelim;

    public static String getCsvDelim() {
        return csvDelim;
    }

    private static TextArea consoleTa;

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("view.bundles.strings_" + Prefs.getString("lang"));
    }

    private static File getUserDir() {
        File initDir = new File(System.getProperty("user.home"));
        if (!initDir.isDirectory() || !initDir.canRead() || !initDir.canWrite()) initDir = null;
        return initDir;
    }

    public static File getInitDir(String pathType) {
        String pathString = Prefs.getString(pathType);
        if (pathString == null) return getUserDir();

        File initPath = new File(Prefs.getString(pathType));
        if (initPath.isDirectory() && initPath.canRead() && initPath.canWrite()) return initPath;
        return getUserDir();
    }

    public final static String ERROR = "/images/icons/error.png";
    public final static String ACCEPT = "/images/icons/accept.png";
    public final static String ALERT = "/images/icons/alert.png";

    public static void showAlert(String title, String message, String imgUri) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(new ImageView(imgUri));
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void setConsoleTa(TextArea consoleTa) {
        Global.consoleTa = consoleTa;
    }

    public static void log(String logString) {
        String curTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        Platform.runLater(() -> {
            consoleTa.setText(consoleTa.getText() + curTime + ": " + logString +"\n");
            consoleTa.positionCaret(consoleTa.getLength());
        });

    }

    public static void setBtnParams(Button button, boolean isVisible, boolean isEnabled){
        button.setVisible(isVisible);
        button.setManaged(isVisible);
        button.setDisable(!isEnabled);
    }

    public static void addPopOver(Node hovered, Node content) {
        PopOver popOver = new PopOver(hovered);
        popOver.setArrowSize(2);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        AnchorPane wrapper = new AnchorPane();
        wrapper.getChildren().add(content);
        wrapper.setStyle("-fx-padding: 12");
        AnchorPane.setBottomAnchor(content, 0d);
        AnchorPane.setLeftAnchor(content, 0d);
        AnchorPane.setTopAnchor(content, 0d);
        AnchorPane.setRightAnchor(content, 0d);
        popOver.setContentNode(wrapper);

        final Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000)));
        timeline.setOnFinished(finishEvent -> {
            if (hovered.isHover() || wrapper.isHover()) timeline.play();
            else popOver.hide();
        });
        hovered.setOnMouseEntered(mouseEvent -> {if (!popOver.isShowing()) popOver.show(hovered);});
        hovered.setOnMouseExited(mouseEvent -> timeline.play());
    }

}
