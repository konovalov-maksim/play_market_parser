package com.appgrade.gp.scraper.modules.positionsChecker;

import com.appgrade.gp.scraper.entities.Query;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.appgrade.gp.scraper.entities.Connection;

import java.io.IOException;


public class PosLoader extends Thread {

    private final String appURL;
    private Query query;
    private OnPosLoadCompleteListener onPosLoadCompleteListener;
    private String language;
    private String country;

    PosLoader(Query query, String appId, String language, String country, OnPosLoadCompleteListener onPosLoadCompleteListener) {
        this.onPosLoadCompleteListener = onPosLoadCompleteListener;
        this.query = query;
        this.country = country;
        this.language = language;
        appURL = "/store/apps/details?id=" + appId;
    }

    @Override
    public void run() {
        super.run();
        try {
            int pseudoPos = getPos();
            onPosLoadCompleteListener.onPosLoadingComplete(query, pseudoPos,true);
        } catch (IOException e) {
            onPosLoadCompleteListener.onPosLoadingComplete(query, -1,false);
        }
    }

    private int getPos() throws IOException {
        //CSS класс div-а со ссылкой на страницу приложения
        String appLinkClass = "b8cIId ReQCgd Q9MA7b";
        //Формируем url страницы поиска
        String url = "https://play.google.com/store/search?q=" + query.getText() + "&c=apps" +
                (language != null ? "&hl=" + language : "") +
                (country != null ? "&gl=" + country : "");
        Document doc = Connection.getDocument(url);
        if (doc == null) throw new IOException("Не удалось загрузить страницу результатов поиска");
        //Получаем список div-ов со ссылками на приложения
        Elements appsLinksDivs = doc.getElementsByClass(appLinkClass);
        String format = "%-30s%-2s%n";
        //Получаем список ссылок на приложения
        for (int i = 0; i < appsLinksDivs.size(); i++) {
            String curURL = appsLinksDivs.get(i).child(0).attr("href");
            if (appURL.equals(curURL)) {
                System.out.printf(format, query.getText(), i + 1);
                return i + 1;
            }
        }
//        System.out.printf(format, query.getText(), "Приложение отсутствует в ТОП-" + appsLinksDivs.size());
        return 0;
    }

    interface OnPosLoadCompleteListener {
        void onPosLoadingComplete(Query query, int pseudoPos, boolean isSuccess);
    }

}
