package playMarketParser;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Global {

    private static TextArea consoleTa;
    public final static String CSV_DELIMITER = ";";

    public static void setLocale(Locale locale) {
        Global.locale = locale;
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(locale.getBundlePath());
    }

    private static Locale locale;
    public enum Locale {
        EN("view.bundles.strings_en"),
        RU("view.bundles.strings_ru");

        private String bundlePath;

        Locale(String bundlePath) {
            this.bundlePath = bundlePath;
        }

        private String getBundlePath() {
            return bundlePath;
        }
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
}
