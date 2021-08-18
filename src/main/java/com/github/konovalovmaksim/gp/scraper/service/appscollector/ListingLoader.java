package com.github.konovalovmaksim.gp.scraper.service.appscollector;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.konovalovmaksim.gp.scraper.model.FoundApp;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.github.konovalovmaksim.gp.scraper.service.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListingLoader extends Thread {

    private final String query;
    private final String language;
    private final String country;
    private final AppsCollectingListener appsCollectingListener;
    private final List<FoundApp> foundApps = new ArrayList<>();


    ListingLoader(String query, String language, String country, AppsCollectingListener appsCollectingListener) {
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
            parsePage(doc);
            getAdditionalApps(doc);
            appsCollectingListener.onQueryProcessed(foundApps, query, true);
        } catch (IOException e) {
            appsCollectingListener.onQueryProcessed(foundApps, query, false);
        }
    }

    private void parsePage(Document doc) {
        //extract JSON
        Pattern pattern = Pattern.compile("\\{key: 'ds:3'.*?data:(.*?), sideChannel", Pattern.DOTALL);
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
        //iterate all apps
        parseAppsJsonArray(appsData);
    }

    private void getAdditionalApps(Document doc) {
        try {
            String script = doc.select("head script[data-id=_gd]").first().childNode(0).toString();
            String json = script
                    .replaceAll("^window.WIZ_global_data = ", "")
                    .replaceAll(";$", "")
                    .replaceAll("\\n", "");
            JsonObject pageData = (JsonObject) Jsoner.deserialize(json);

            String fSid = (String) pageData.get("FdrFJe");
            String bl = (String) pageData.get("cfb2h");
            String hl = (String) pageData.get("GWsdKe");
            String gl = (String) pageData.get("zQmIje");
            String jsData = doc.getElementById("i8").attr("jsdata");
            //Example: " Hg4CF;_;106 QbiEs;Cgj6noGdAwIIMhAyGhiCARUKCGFwcCBsb2NrEAAiBQgAEPoBWAE6EAoKCghhcHAgbG9jaxAAGAQ;107"
            Pattern tokenPattern = Pattern.compile(".*?;.*?;.*?;(.*?);.*?$");
            Matcher tokenMatcher = tokenPattern.matcher(jsData);
            if (tokenMatcher.find()) {
                String paginationToken = tokenMatcher.group(1);
                loadAdditionalApps(paginationToken, fSid, bl, hl, gl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load additional page");
        }
    }

    private void loadAdditionalApps(String paginationToken, String fSid, String bl, String hl, String gl) {
        try {
            String fReqTemplate = "[[[\"qnKhOb\",\"[[null,[[10,[10,50]],true,null," +
                    "[96,27,4,8,57,30,110,11,16,49,1,3,9,12,104,55,56,51,10,34,31,77]," +
                    "[null,null,null,[[[[7,31],[[1,52,43,112,92,58,69,31,19,96,103]]]]]]]," +
                    "null,\\\"%s\\\"]]\",null,\"generic\"]]]";
            String fReq = String.format(fReqTemplate, paginationToken);
            Document doc = Jsoup.connect("https://play.google.com/_/PlayStoreUi/data/batchexecute")
                    .data("f.req", fReq)
                    .data("rpcids", "qnKhOb")
                    .data("f.sid", fSid)
                    .data("bl", bl)
                    .data("hl", hl)
                    .data("gl", gl)
                    .data("authuser", "")
                    .data("soc-app", "121")
                    .data("soc-platform", "1")
                    .data("soc-device", "1")
                    .data("_reqid", "281242")
                    .data("rt", "c")
                    .header("Accept-Language", Connection.getUserAgent())
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .userAgent(Connection.getUserAgent())
                    .referrer(Connection.getReferrer())
                    .ignoreContentType(true)
                    .proxy(Connection.getProxy())
                    .timeout(Connection.getTimeout())
                    .post();
            String html = doc.outerHtml();

            String fullDataJsonStr = html
                    .split("\n")[3]
                    .replaceFirst("(.*?)\\[\\[", "[[")
                    .replaceFirst("\"generic\"]] .*", "\"generic\"]]");

            parseJson(fullDataJsonStr);
            final Pattern tokenPattern =
                    Pattern.compile("\\\\\"([^\"]*?)\\\\\"][^]]*?][^]]*?][^]]*?][^]]*?][^]]*?\"generic\"]");
            Matcher tokenMatcher = tokenPattern.matcher(html);
            if (tokenMatcher.find()) {
                String nextPaginationToken = tokenMatcher.group(1);
                loadAdditionalApps(nextPaginationToken, fSid, bl, hl, gl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseJson(String fullDataJsonStr) throws JsonException {
        JsonArray fullDataJson = (JsonArray) Jsoner.deserialize(fullDataJsonStr);
        String appsDataJsonStr = ((JsonArray) fullDataJson.getCollection(0)).getString(2);
        JsonArray fullAppsDataJson = (JsonArray) Jsoner.deserialize(appsDataJsonStr);

        JsonArray appsJsonArray;
        try {
            appsJsonArray =
                    ((JsonArray) ((JsonArray) fullAppsDataJson
                            .getCollection(0))
                            .getCollection(0))
                            .getCollection(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", query, "Не удалось спарсить JSON");
            return;
        }
        parseAppsJsonArray(appsJsonArray);
    }

    private void parseAppsJsonArray(JsonArray appsJsonArray) {
        for (Object appArray : appsJsonArray) {
            JsonArray appData = (JsonArray) appArray;
            FoundApp app = new FoundApp();
            app.setQuery(query);
            app.setPosition(foundApps.size() + 1);
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
            //dev name
            try {
                app.setDevName(((JsonArray) ((JsonArray) ((JsonArray) appData
                        .getCollection(4))
                        .getCollection(0))
                        .getCollection(0))
                        .getString(0));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить имя разработчика");
            }
            //dev URL
            try {
                app.setDevUrl("https://play.google.com" + ((JsonArray) ((JsonArray) ((JsonArray) ((JsonArray) ((JsonArray) appData
                        .getCollection(4))
                        .getCollection(0))
                        .getCollection(0))
                        .getCollection(1))
                        .getCollection(4))
                        .getString(2));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить URL разработчика");
            }
            //avg Rate
            try {
                app.setAvgRate(Double.parseDouble((
                        (JsonArray) ((JsonArray) ((JsonArray) ((JsonArray) appData
                                .getCollection(6))
                                .getCollection(0))
                                .getCollection(2))
                                .getCollection(1))
                        .getString(0).replaceAll(",", "."))
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить среднюю оценку");
            }
            //short descr
            try {
                app.setShortDescr(((JsonArray) ((JsonArray) ((JsonArray) ((JsonArray) appData
                        .getCollection(4))
                        .getCollection(1))
                        .getCollection(1))
                        .getCollection(1))
                        .getString(1));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить краткое описание");
            }
            //app ID
            try {
                app.setId(((JsonArray) appData.getCollection(12)).getString(0));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("%-40s%s%n", query, "Не удалось определить ID приложения");
            }
            foundApps.add(app);
        }
    }

    interface AppsCollectingListener {
        void onQueryProcessed(List<FoundApp> foundApps, String query, boolean isSuccess);
    }
}
