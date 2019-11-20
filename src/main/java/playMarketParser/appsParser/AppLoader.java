package playMarketParser.appsParser;

import org.jsoup.nodes.Document;
import playMarketParser.App;
import playMarketParser.Connection;

import java.io.IOException;

class AppLoader extends Thread {
    private App app;
    OnAppLoadingCompleteListener onAppLoadingCompleteListener;
    private String language;
    private String country;

    public AppLoader(App app, OnAppLoadingCompleteListener onAppLoadingCompleteListener, String language, String country) {
        this.app = app;
        this.onAppLoadingCompleteListener = onAppLoadingCompleteListener;
        this.language = language;
        this.country = country;
    }

    @Override
    public void run() {
        super.run();
        try {
            parseApp();
            onAppLoadingCompleteListener.onAppParsingComplete(app, true);
        } catch (IOException e) {
            e.printStackTrace();
            onAppLoadingCompleteListener.onAppParsingComplete(app, false);
        }
    }

    private void parseApp() throws IOException {
        String url = app.getUrl() +
                (language != null ? "&hl=" + language : "") +
                (country != null ? "&gl=" + country : "");
        Document doc = Connection.getDocument(url);
        if (doc == null) throw new IOException("Не удалось загрузить страницу результатов поиска");
        //TODO парсинг документа и заполнение полей сущности app
        app.setCategory(doc.title()); //test
    }

    interface OnAppLoadingCompleteListener {
        void onAppParsingComplete(App app, boolean isSuccess);
    }
}
