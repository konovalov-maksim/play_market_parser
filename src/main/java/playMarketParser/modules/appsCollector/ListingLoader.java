package playMarketParser.modules.appsCollector;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.jsoup.nodes.Document;
import playMarketParser.Connection;
import playMarketParser.FoundApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListingLoader extends Thread {

    private String query;
    private String language;
    private String country;
    private AppsCollectingListener appsCollectingListener;
    private List<FoundApp> foundApps = new ArrayList<>();


    public ListingLoader(String query, String language, String country, AppsCollectingListener appsCollectingListener) {
        this.query = query;
        this.language = language;
        this.country = country;
        this.appsCollectingListener = appsCollectingListener;
    }

    @Override
    public void run() {
        super.run();
        try {
            String url = "https://play.google.com/store/search?c=apps&q=" + query +
                    (language != null ? "&hl=" + language : "") +
                    (country != null ? "&gl=" + country : "");
            Document doc = Connection.getDocument(url);
            parseJson(doc);
            appsCollectingListener.onQueryProcessed(foundApps, query, true);
        } catch (IOException e) {
            appsCollectingListener.onQueryProcessed(foundApps, query, false);
        }
    }

    private void parseJson(Document doc) {
        //Извлекаем JSON
        Pattern pattern = Pattern.compile("\\{key: 'ds:3'.*?return(.*?)}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(doc.data());
        if (!matcher.find()) {
            System.out.printf("%-40s%s%n", query, "Не удалось получить JSON");
            return;
        }
        String jsonData = matcher.group(1);
        JsonArray appsData;
        try {
            JsonArray fullData = (JsonArray) Jsoner.deserialize(jsonData);
            appsData =
                    ((JsonArray) ((JsonArray) ((JsonArray) ((JsonArray) fullData
                            .getCollection(0))
                            .getCollection(1))
                            .getCollection(0))
                            .getCollection(0))
                            .getCollection(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", query, "Не удалось спарсить JSON");
            return;
        }
        //Обходим все блоки с данными о приложении
        for (int i = 0; i < appsData.size(); i++) {
            FoundApp app = new FoundApp();
            app.setQuery(query);
            app.setPos(i + 1);
            JsonArray appData = (JsonArray) appsData.get(i);
            //name
            try {
                app.setName(appData.getString(2));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить имя приложения");
            }
            //icon URL
            try {
                app.setIconUrl(((JsonArray) ((JsonArray) ((JsonArray) ((JsonArray) appData
                        .getCollection(1))
                        .getCollection(1))
                        .getCollection(2))
                        .getCollection(3))
                        .getString(2));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить URL иконки приложения");
            }
            foundApps.add(app);
        }


    }

    interface AppsCollectingListener {
        void onQueryProcessed(List<FoundApp> foundApps, String query, boolean isSuccess);
    }
}
