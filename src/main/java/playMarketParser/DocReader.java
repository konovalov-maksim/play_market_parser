package playMarketParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;


public class DocReader {
    private final static String referrer = "https://play.google.com/";

    private static String userAgent;
    private static String acceptLanguage;
    private static Proxy proxy;
    private static int timeout;

    static {
        reloadPrefs();
    }

    public static Document readDocByURL(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Accept-Language", acceptLanguage)
                    .referrer(referrer)
                    .ignoreContentType(true)
                    .proxy(proxy)
                    .timeout(timeout)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void reloadPrefs() {
        userAgent = Prefs.getString("user_agent");
        acceptLanguage = Prefs.getString("accept_language");
        timeout = Prefs.getInt("timeout");
        if (Prefs.getString("proxy").equals(""))
            proxy = Proxy.NO_PROXY;
        else
            proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(Prefs.getString("proxy_address"), Prefs.getInt("proxy_port")));
    }
}