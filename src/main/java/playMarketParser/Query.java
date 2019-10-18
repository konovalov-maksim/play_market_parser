package playMarketParser;

import java.util.*;

public class Query {
    private String text;
    private Set<String> tipsSet = new LinkedHashSet<>();

    public Query(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public List<String> getTips() {
        return new ArrayList<>(tipsSet);
    }

    //Добавление подсказок подзапроса
    public void addTips(Query query) {
        tipsSet.addAll(query.getTips());
    }

    //Добавление неисправленных подсказок
    public void addTips(List<String> inputTips) {
        for (String tip : inputTips)
            if (tip.length() >= text.length() && tip.substring(0, text.length()).equals(text))
                tipsSet.add(tip);
    }

}
