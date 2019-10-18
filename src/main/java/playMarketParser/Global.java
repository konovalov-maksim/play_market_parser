package playMarketParser;

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
}
