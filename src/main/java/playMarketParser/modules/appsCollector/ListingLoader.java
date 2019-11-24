package playMarketParser.modules.appsCollector;

import org.jsoup.nodes.Document;
import playMarketParser.Connection;
import playMarketParser.FoundApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            collectApps(doc);
            appsCollectingListener.onQueryProcessed(foundApps, query,true);
        } catch (IOException e) {
            appsCollectingListener.onQueryProcessed(foundApps, query,false);
        }
    }

    private void collectApps(Document doc) {
        //TODO парсинг листинга
        foundApps.add(new FoundApp(doc.title()));
    }

    interface AppsCollectingListener {
        void onQueryProcessed(List<FoundApp> foundApps, String query, boolean isSuccess);
    }
}
