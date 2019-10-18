package playMarketParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;



public class DocReader {
    private final static String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36";
    private final static String REFERRER = "https://play.google.com/";
    private final static String ACCEPT_LANGUAGE = "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3";


    public static Document readDocByURL(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .referrer(REFERRER)
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


}