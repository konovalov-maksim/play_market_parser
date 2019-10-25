package playMarketParser.tipsCollector;

import java.util.Objects;

public class Tip {
    private String text;
    private String queryText;

    public Tip(String query, String text) {
        this.text = text;
        this.queryText = query;
    }

    public String getText() {
        return text;
    }

    public String getQueryText() {
        return queryText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return text.equals(tip.text) &&
                queryText.equals(tip.queryText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, queryText);
    }
}
