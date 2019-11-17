package playMarketParser;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

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

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(null);
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
}
