package playMarketParser.appsParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
            parseAppPage();
            onAppLoadingCompleteListener.onAppParsingComplete(app, true);
        } catch (IOException e) {
            e.printStackTrace();
            onAppLoadingCompleteListener.onAppParsingComplete(app, false);
        }
    }

    private void parseAppPage() throws IOException {
        String url = app.getUrl() +
                (language != null ? "&hl=" + language : "") +
                (country != null ? "&gl=" + country : "");
        Document doc = Connection.getDocument(url);
        if (doc == null) throw new IOException("Не удалось загрузить страницу результатов поиска");
        //TODO devWebSite, installsCount, minAge, size
        //name
        app.setName(doc.getElementsByTag("h1").first().text());
        //devName, devId
        try {
            Element devLink = doc.select("a[href^=/store/apps/developer].hrTbp.R8zArc").first();
            app.setDevName(devLink.text());
            app.setDevId(devLink.attr("href").split("=")[1]);
        } catch (Exception e) {
            System.out.println("Не удалось получить параметры разработчика");
        }
        //deveEmail
        try {
            app.setDevEmail(doc.select("a[href^=mailto:].hrTbp.euBY6b").first().text());
        } catch (Exception e) {System.out.println("Не удалось получить email");}
        //ratesCount
        try {
            app.setRatesCount(Integer.parseInt(
                    doc.select("span.AYi5wd.TBRnV").first().text().replaceAll(" ", "")));
        } catch (Exception e) {System.out.println("Не удалось получить число оценок");}
        //avgRate
        try {
            app.setAvgRate(Double.parseDouble(
                    doc.select("div[aria-label].BHMmbe").first().text().replaceAll(",", ".")));
        } catch (Exception e) {System.out.println("Не удалось получить ср. оценку");}
        //category
        try {
            app.setCategory(doc.select("a[itemprop=genre]").get(0).text());
        } catch (Exception e) {
            System.out.println("Не удалось получить категорию");
        }
    }

    interface OnAppLoadingCompleteListener {
        void onAppParsingComplete(App app, boolean isSuccess);
    }
}
