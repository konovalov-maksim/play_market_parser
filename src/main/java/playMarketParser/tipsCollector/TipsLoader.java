package playMarketParser.tipsCollector;

import org.jsoup.nodes.Document;
import playMarketParser.DocReader;

import java.util.ArrayList;
import java.util.List;

class TipsLoader extends Thread {

    private Query query;
    private OnTipLoadCompleteListener onTipLoadCompleteListener;
    private List<Tip> tips = new ArrayList<>();

    TipsLoader(Query query, OnTipLoadCompleteListener onTipLoadCompleteListener) {
        this.query = query;
        this.onTipLoadCompleteListener = onTipLoadCompleteListener;
    }

    @Override
    public void run() {
        super.run();
        collectTips();
        onTipLoadCompleteListener.onTipsLoadComplete(this);
    }

    private void collectTips() {
        String queryText = query.getText();
        //Формируем из запроса url
        String url = "https://market.android.com/suggest/SuggRequest?json=1&c=3&query=" + queryText + "&hl=ru&gl=RU";
        //Загружаем js документ
        Document doc = DocReader.readDocByURL(url);
        if (doc == null) return;
        //Получаем контент документа в виде строки
        String content = doc.text();
        if (content.equals("[]")) {
            System.out.println(queryText);
            return;
        }
        //Парсим строку
        content = content.replace("\"", "");
        content = content.replace("{", "");
        content = content.replace("[s:", "");
        content = content.replace(",t:q}]", "");
        //Извлекаем из строки нужные данные в массив, а затем в список
        String[] tipsArray = content.split(",t:q},s:");
        for (String tip : tipsArray) {
            if (isUncorrected(queryText, tip)) tips.add(new Tip(query.getRootQueryText(), tip));
            System.out.printf("%-35s%-50s%n", queryText, tip);
        }
    }

    interface OnTipLoadCompleteListener {
        void onTipsLoadComplete(TipsLoader tipsLoader);
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
