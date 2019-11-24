package playMarketParser;

public class FoundApp extends App{

    private Integer pos;

    private String query;

    public Integer getPos() {
        return pos;
    }

    public String shortDescr;

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void reset() {
        super.reset();
    }

    public String getShortDescr() {
        return shortDescr;
    }

    public void setShortDescr(String shortDescr) {
        this.shortDescr = shortDescr;
    }
}
