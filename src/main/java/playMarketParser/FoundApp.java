package playMarketParser;

public class FoundApp extends App{

    public FoundApp(String AppUrl) {
        super(AppUrl);
    }

    private Integer pos;

    private String query;

    public Integer getPos() {
        return pos;
    }

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

}
