package playMarketParser.tipsCollector;

import java.util.*;

public class Query {
    private String text;
    private Query parentQuery;

    Query(String text, Query parentQuery) {
        this.text = text;
        this.parentQuery = parentQuery;
    }

    String getText() {
        return text;
    }

//    String getRootQueryText() {
//        if (parentQuery == null) return text.trim();
//        else return parentQuery.getRootQueryText();
//    }

    boolean isRoot() {
        return parentQuery == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return text.equals(((Query) o).getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
