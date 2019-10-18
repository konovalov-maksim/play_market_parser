package playMarketParser.tipsCollector;

import java.util.*;

class Query {
    private String text;
    private List<String> tipsSet = new ArrayList<>();
    private Query parentQuery;

    Query(String text, Query parentQuery) {
        this.text = text;
        this.parentQuery = parentQuery;
    }

    String getText() {
        return text;
    }

    List<String> getTips() {
        return new ArrayList<>(tipsSet);
    }

    //Добавление неисправленных подсказок
    void addTips(List<String> inputTips) {
        for (String tip : inputTips)
            if (tip.length() >= text.length() && tip.substring(0, text.length()).equals(text))
                tipsSet.add(tip);
    }

    String getRootQueryText() {
        if (parentQuery == null) return text.trim();
        else return parentQuery.getRootQueryText();
    }

    boolean isRoot() {
        return parentQuery == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return Objects.equals(text, query.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
