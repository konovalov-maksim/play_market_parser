package com.github.konovalovmaksim.gp.scraper.service.tipscollector;


import org.jsoup.nodes.Document;
import com.github.konovalovmaksim.gp.scraper.service.Connection;
import com.github.konovalovmaksim.gp.scraper.model.Tip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TipsLoader extends Thread {

    private Query query;
    private OnTipLoadCompleteListener onTipLoadCompleteListener;
    private List<Tip> tips = new ArrayList<>();
    private String language;
    private String country;

    TipsLoader(Query query, String language, String country, OnTipLoadCompleteListener onTipLoadCompleteListener) {
        this.query = query;
        this.language = language;
        this.country = country;
        this.onTipLoadCompleteListener = onTipLoadCompleteListener;
    }

    @Override
    public void run() {
        super.run();
        try {
            collectTips();
            onTipLoadCompleteListener.onTipsLoadingComplete(this, true);
        } catch (IOException e) {
            onTipLoadCompleteListener.onTipsLoadingComplete(this, false);
        }
    }

    private void collectTips() throws IOException {
        String queryText = query.getText();
        String url = "https://market.android.com/suggest/SuggRequest?json=1&c=3&query=" + queryText +
                (language != null ? "&hl=" + language : "") +
                (country != null ? "&gl=" + country : "");
        Document doc = Connection.getDocument(url);

        String content = doc.text();
        if (content.equals("[]")) {
//            System.out.println(queryText);
            return;
        }
        //Убираем шлак
        content = content.replace("\"", "");
        content = content.replace("{", "");
        content = content.replace("[s:", "");
        content = content.replace(",t:q}]", "");
        //Получаем список подсказок
        String[] tipsArray = content.split(",t:q},s:");
        for (String tip : tipsArray) {
            if (isUncorrected(queryText, tip)) tips.add(new Tip(query.getRootQueryText(), tip, query.getDepth()));
//            System.out.printf("%-35s%-50s%n", queryText, tip);
        }
    }

    interface OnTipLoadCompleteListener {
        void onTipsLoadingComplete(TipsLoader tipsLoader, boolean isSuccess);
    }

    Query getQuery() {
        return query;
    }

    List<Tip> getTips() {
        return tips;
    }

    private static boolean isUncorrected(String query, String tip) {
        return (tip.length() > query.length() && tip.substring(0, query.length()).equals(query));
    }
}
