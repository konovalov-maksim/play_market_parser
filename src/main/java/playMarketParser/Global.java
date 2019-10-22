package playMarketParser;

import java.io.File;
import java.util.ResourceBundle;

public class Global {
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

    public static File getInitDir(Prefs prefs, String pathType) {
        String pathString = prefs.getString(pathType);
        if (pathString == null) return getUserDir();

        File initPath = new File(prefs.getString(pathType));
        if (initPath.isDirectory() && initPath.canRead() && initPath.canWrite()) return initPath;
        return getUserDir();
    }


}
