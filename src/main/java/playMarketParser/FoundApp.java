package playMarketParser;

public class FoundApp extends App{

    private Integer pos;

    private String query;

    private Integer getPos() {
        return pos;
    }

    private String shortDescr;

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getShortDescr() {
        return shortDescr;
    }

    public void setShortDescr(String shortDescr) {
        this.shortDescr = shortDescr;
    }
}
