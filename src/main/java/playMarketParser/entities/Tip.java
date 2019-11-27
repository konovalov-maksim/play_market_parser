package playMarketParser.entities;

import java.util.Objects;

public class Tip {
    private String text;
    private String queryText;
    private int depth;

    public Tip(String query, String text, int depth) {
        this.text = text;
        this.queryText = query;
        this.depth = depth;
    }

    public String getText() {
        return text;
    }

    public String getQueryText() {
        return queryText;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return text.equals(tip.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
