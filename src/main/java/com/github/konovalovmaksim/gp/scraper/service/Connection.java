package com.github.konovalovmaksim.gp.scraper.service;

import com.github.konovalovmaksim.gp.scraper.Prefs;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;


public class Connection {
    private final static String referrer = "https://play.google.com/";

    private static String userAgent;
    private static String acceptLanguage;
    private static Proxy proxy;
    private static int timeout;

    static {
        reloadPrefs();
    }

    public static Document getDocument(String url) throws IOException {
            return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Accept-Language", acceptLanguage)
                    .referrer(referrer)
                    .ignoreContentType(true)
                    .proxy(proxy)
                    .timeout(timeout)
                    .get();
    }

    public static void reloadPrefs() {
        userAgent = Prefs.getString("user_agent");
        acceptLanguage = Prefs.getString("accept_language");
        timeout = Prefs.getInt("timeout");
        String proxyString = Prefs.getString("proxy");
        if (proxyString.equals("")) proxy = Proxy.NO_PROXY;
        else
            try {
                proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(proxyString.split(":")[0], Integer.parseInt(proxyString.split(":")[1])));
            } catch (Exception e) {
                e.printStackTrace();
                proxy = Proxy.NO_PROXY;
                Prefs.put("proxy", "");
            }
    }
}