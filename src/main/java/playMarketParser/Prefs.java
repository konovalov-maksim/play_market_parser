package playMarketParser;

import java.util.HashMap;
import java.util.prefs.Preferences;

public class Prefs {
    private Preferences preferences;
    private HashMap<String, Object> defaults = new HashMap<>();


    public Prefs() {
        preferences = Preferences.userNodeForPackage(Prefs.class);
        putDefaults();
    }

    public int getInt(String propName) {
        return preferences.getInt(propName, (int) defaults.get(propName));
    }

    public String getString(String propName) {
        return preferences.get(propName, (String) defaults.get(propName));
    }

    public boolean getBoolean(String propName) {
        return preferences.getBoolean(propName, (boolean) defaults.get(propName));
    }

    public void put(String propName, String value) {
        preferences.put(propName, value);
    }

    public void put(String propName, int value) {
        preferences.put(propName, String.valueOf(value));
    }

    public void put(String propName, boolean value) {
        preferences.put(propName, String.valueOf(value));
    }

    private void putDefaults(){
        defaults.put("pos_app_url", "");
        defaults.put("pos_threads_cnt", 5);
        defaults.put("pos_checks_cnt", 5);
        defaults.put("title_first", true);
    }
}
