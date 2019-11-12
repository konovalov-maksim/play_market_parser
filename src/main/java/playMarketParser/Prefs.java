package playMarketParser;

import java.util.HashMap;
import java.util.prefs.Preferences;

public class Prefs {
    private static Preferences preferences;
    private static HashMap<String, Object> defaults = new HashMap<>();

    private Prefs() {}

    static {
        preferences = Preferences.userNodeForPackage(Prefs.class);

        defaults.put("pos_app_url", "");
        defaults.put("pos_threads_cnt", 5);
        defaults.put("pos_checks_cnt", 5);
        defaults.put("title_first", true);
        defaults.put("tips_threads_cnt", 5);
        defaults.put("tips_parsing_depth", 5);
        defaults.put("user_agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        defaults.put("accept_language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        defaults.put("timeout", 3000);
        defaults.put("proxy", "");
        defaults.put("csv_delimiter", ";");
        defaults.put("lang", "en");
        defaults.put("pos_lang", "en");
        defaults.put("pos_country", "-");
        defaults.put("tips_lang", "en");
        defaults.put("tips_country", "-");
        defaults.put("alphabet", "auto");
    }

    public static int getInt(String propName) {
        return preferences.getInt(propName, (int) defaults.get(propName));
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

    public static void put(String propName, boolean value) {
        preferences.put(propName, String.valueOf(value));
    }

}
