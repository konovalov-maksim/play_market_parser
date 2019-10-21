package playMarketParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Prefs {

    private Properties prefs = new Properties();

    public Prefs() {
        try (FileInputStream fis = new FileInputStream("/preferences.properties")) {
            prefs.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getInt(String propName, int defValue) {
        try {
            return Integer.parseInt(prefs.getProperty(propName));
        } catch (Exception e) {
            return defValue;
        }
    }

    public String getString(String propName, String defValue) {
        String result = prefs.getProperty(propName);
        return (result != null) ? result : defValue;
    }

    public boolean getBoolean(String propName, boolean defValue) {
        String result = prefs.getProperty(propName);
        if (result != null && result.equals("0")) return false;
        if (result != null && result.equals("1")) return true;
        return defValue;
    }

    public void setProperty(String propName, int value) {
        prefs.setProperty(propName, String.valueOf(value));
    }

    public void setProperty(String propName, String value) {
        prefs.setProperty(propName, value);
    }

    public void setProperty(String propName, boolean value) {
        prefs.setProperty(propName, value ? "1" : "0");
    }
}
