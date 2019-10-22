package playMarketParser.positionsChecker;

import playMarketParser.Global;

import java.util.*;

public class Query {
    private String text;
    private List<Integer> pseudoPos = Collections.synchronizedList(new ArrayList<>());
    private String fullRowText;
    private Integer realPos;

    public Query(String fullRowText) {
        this.fullRowText = fullRowText;
        text = fullRowText.split(Global.CSV_DELIMITER)[0];
    }

    synchronized void addPseudoPos(int pos) {
        pseudoPos.add(pos);
    }

    void calcRealPos() {
        Map<Integer, Integer> positionsFreqs = new HashMap<>();
        for (Integer pos : pseudoPos) {
            Integer freq = positionsFreqs.get(pos) == null ? 1 : positionsFreqs.get(pos) + 1;
            positionsFreqs.put(pos, freq);
        }
        //Находим элемент в HashMap с наибольшей частотой
        Integer maxValueInMap = (Collections.max(positionsFreqs.values()));
        //Определяем его индекс (key)
        for (HashMap.Entry<Integer, Integer> entry : positionsFreqs.entrySet())
            if (entry.getValue().equals(maxValueInMap)) {
                realPos = entry.getKey();
                return;
            }
        realPos = 0;
    }

    public String getRealPosString() {
        return realPos == null? "" : realPos.toString();
    }

    public Integer getRealPos() {
        return realPos;
    }

    public String getText() {
        return text;
    }

    public String getFullRowText() {
        return fullRowText;
    }

    public void clearPositions() {
        pseudoPos.clear();
        realPos = null;
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
