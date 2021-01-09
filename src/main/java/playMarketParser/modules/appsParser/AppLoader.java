package playMarketParser.modules.appsParser;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import playMarketParser.entities.App;
import playMarketParser.entities.Connection;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AppLoader extends Thread {
    private App app;
    private OnAppLoadingCompleteListener onAppLoadingCompleteListener;
    private String language;
    private String country;

    AppLoader(App app, OnAppLoadingCompleteListener onAppLoadingCompleteListener, String language, String country) {
        this.app = app;
        this.onAppLoadingCompleteListener = onAppLoadingCompleteListener;
        this.language = language;
        this.country = country;
    }

    interface OnAppLoadingCompleteListener {
        void onAppParsingComplete(App app, boolean isSuccess);
    }

    @Override
    public void run() {
        super.run();
        try {
            String url = app.getUrl() +
                    (language != null ? "&hl=" + language : "") +
                    (country != null ? "&gl=" + country : "");
            Document doc = Connection.getDocument(url);
            if (doc == null) throw new IOException("Не удалось загрузить страницу результатов поиска");
            parseJson(doc);
            parseHtml(doc);
            onAppLoadingCompleteListener.onAppParsingComplete(app, true);
        } catch (IOException e) {
            e.printStackTrace();
            onAppLoadingCompleteListener.onAppParsingComplete(app, false);
        }
    }

    private void parseJson(Document doc) {
        //Извлекаем JSON
        Pattern pattern = Pattern.compile("\\{key: 'ds:5'.*?data:(.*?), sideChannel", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(doc.data());
        if (!matcher.find()) {
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить JSON");
            return;
        }
        String jsonData = matcher.group(1);
        JsonArray data;
        try {
            JsonArray fullData = (JsonArray) Jsoner.deserialize(jsonData);
            data = ((JsonArray) fullData.getCollection(0)).getCollection(12);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось спарсить JSON");
            return;
        }
        //Мин. возраст
        try {
            app.setMinAge(((JsonArray) data.getCollection(4)).getString(0));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить минимальный возраст");
        }
        //Разработчик
        try {
            JsonArray devData = data.getCollection(5);
            app.setDevName(devData.getString(1));
            app.setDevEmail(((JsonArray) devData.getCollection(2)).getString(0));
            app.setDevUrl("https://play.google.com" +
                    ((JsonArray) ((JsonArray) devData.getCollection(5)).getCollection(4)).getString(2));
            if (devData.getCollection(3) != null)
                app.setDevWebSite(((JsonArray) ((JsonArray) devData.getCollection(3)).getCollection(5)).getString(2));
            if (devData.getCollection(4) != null)
                app.setDevAddress(((JsonArray) ((JsonArray) data.getCollection(5)).getCollection(4)).getString(0).replaceAll("\n", " "));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить информацию о разработчике");
        }
        //Число установок
        try {
            app.setInstallsCount(Long.parseLong(((JsonArray) data.getCollection(9)).getString(2)));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить информацию о числе установок");
        }
        //Стоимость
        try {
            if (data.getCollection(12) != null) {
                app.setOffersPurchases(true);
                app.setContentCost(((JsonArray) data.getCollection(12)).getString(0));
            } else app.setOffersPurchases(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить информацию о стоимости");
        }
        //Наличие рекламы
        try {
            app.setContainsAds(data.getCollection(14) != null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить информацию о наличии рекламы");
        }
        //Release date
        try {
            app.setReleaseDate(data.getString(36));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить информацию о дате");
        }
        //Icon Url
        try {
            app.setIconUrl(((JsonArray)((JsonArray)data.get(1)).get(3)).getString(2));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить URL иконки");
        }
    }

    private void parseHtml(Document doc) {
        //name
        app.setName(doc.getElementsByTag("h1").first().text());
        //ratesCount
        try {
            Element element = doc.select("span.AYi5wd.TBRnV").first();
            app.setRatesCount(element != null ? Integer.parseInt(element.text().replaceAll("[\\s,]", "")) : 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить число оценок");
        }
        //avgRate
        try {
            Element element = doc.select("div[aria-label].BHMmbe").first();
            app.setAvgRate(element != null ? Double.parseDouble(element.text().replaceAll(",", ".")) : 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить ср. оценку");
        }
        //category
        try {
            app.setCategory(doc.select("a[itemprop=genre]").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить категорию");
        }
        //whats new
        try {
            Element div = doc.select("c-wiz[jsrenderer=eG38Ge] div[itemprop=description].DWPxHb").first();
            if (div != null) app.setWhatsNew(div.text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить Что нового");
        }
        //description
        try {
            app.setDescription(doc.select("c-wiz[jsrenderer=UsuzQd] div[itemprop=description].DWPxHb").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить описание");
        }
        //similar apps
        try {
            for (Element element : doc.select("c-wiz[jsrenderer=PRm2u] a.poRVub"))
                app.addSimilarApp("https://play.google.com" + element.attr("href"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить похожие приложения");
        }
        //last update, size, sdk version, app version
        try {
            Elements elements = doc.select("c-wiz[jsrenderer=HEOg8] div.IQ1z0d > span.htlgb");
            app.setLastUpdate(elements.get(0).text());
            app.setSizeMb(elements.get(1).text());
            app.setVersion(elements.get(3).text());
            app.setMinSdkVer(elements.get(4).text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить похожие приложения");
        }

        //!Дублирование JSON!
        // devName, devUrl
        if (app.getDevName() == null || app.getDevUrl() == null)
            try {
                Element devLink = doc.select("a[href^=/store/apps/dev].hrTbp.R8zArc").first();
                app.setDevName(devLink.text());
                app.setDevUrl("https://play.google.com" + devLink.attr("href"));
            } catch (Exception e) {
                System.out.println("Не удалось получить параметры разработчика");
            }
        //devEmail
        if (app.getDevEmail() == null)
            try {
                app.setDevEmail(doc.select("a[href^=mailto:].hrTbp.euBY6b").first().text());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", app.getId(), "Не удалось получить email");
            }
    }

}
