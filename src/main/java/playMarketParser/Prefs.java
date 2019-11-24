package playMarketParser;

import java.util.HashMap;
import java.util.prefs.Preferences;

public class Prefs {
    private static Preferences preferences;
    private static HashMap<String, Object> defaults = new HashMap<>();

    private Prefs() {}

    static {
        preferences = Preferences.userNodeForPackage(Prefs.class);
        //general
        defaults.put("lang", "en");
        defaults.put("csv_delimiter", ";");
        defaults.put("title_first", true);
        defaults.put("pos_app_url", "");
        defaults.put("is_window_maximized", false);
        defaults.put("window_x", -1d);
        defaults.put("window_y", -1d);
        defaults.put("window_width", 900d);
        defaults.put("window_height", 600d);
        //connection
        defaults.put("user_agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        defaults.put("accept_language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        defaults.put("proxy", "");
        defaults.put("timeout", 3000);
        defaults.put("parsing_lang", "en");
        defaults.put("parsing_country", "-");
        //positions
        defaults.put("pos_threads_cnt", 5);
        defaults.put("pos_checks_cnt", 5);
        //tips
        defaults.put("tips_threads_cnt", 5);
        defaults.put("tips_parsing_depth", 5);
        defaults.put("alphabet", "auto");
        //apps
        defaults.put("apps_threads_cnt", 5);
    }

    public static int getInt(String propName) {
        return preferences.getInt(propName, (int) defaults.get(propName));
    }

    public static double getDouble(String propName) {
        return preferences.getDouble(propName, (double) defaults.get(propName));
    }

    public static String getString(String propName) {
        return preferences.get(propName, (String) defaults.get(propName));
    }

    public static boolean getBoolean(String propName) {
        return preferences.getBoolean(propName, (boolean) defaults.get(propName));
    }

    public static void put(String propName, String value) {
        preferences.put(propName, value);
    }

    public static void put(String propName, int value) {
        preferences.put(propName, String.valueOf(value));
    }

    public static void put(String propName, double value) {
        preferences.put(propName, String.valueOf(value));
    }

    public static void put(String propName, boolean value) {
        preferences.put(propName, String.valueOf(value));
    }

    public static void remove(String propName) {
        preferences.remove(propName);
    }
}
