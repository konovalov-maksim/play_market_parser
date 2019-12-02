package playMarketParser.modules.tipsCollector;



import playMarketParser.entities.Tip;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class TipsCollector implements TipsLoader.OnTipLoadCompleteListener {

    private Deque<Query> unprocessed = new ConcurrentLinkedDeque<>();
    private int maxThreadsCount = 1;
    private int maxDepth = 5;
    private String alphaType = "auto";
    private String language;
    private String country;
    private TipsLoadingListener tipsLoadingListener;

    private int threadsCount;
    private int collectedCount;
    private boolean isPaused;
    private boolean isStopped;

    private List<Character> latin;
    private List<Character> cyrillic;
    private List<Character> allAlphs;


    public TipsCollector(List<String> queriesStrings, TipsLoadingListener tipsLoadingListener) {
        for (String queryString : queriesStrings) unprocessed.addLast(new Query(queryString + " ", null));
        this.tipsLoadingListener = tipsLoadingListener;

        latin = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        cyrillic = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        allAlphs = new ArrayList<>(latin);
        allAlphs.addAll(cyrillic);
    }

    public void start() {
        isStopped = false;
        isPaused = false;
        attachQueriesToLoaders();
    }

    //Распределяем запросы по потокам
    private synchronized void attachQueriesToLoaders() {
        while (threadsCount < maxThreadsCount && unprocessed.size() > 0) {
            new TipsLoader(unprocessed.pop(), language, country, this).start();
            threadsCount++;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void stop() {
        isStopped = true;
        unprocessed.clear();
    }

    @Override
    public synchronized void onTipsLoadingComplete(TipsLoader tipsLoader, boolean isSuccess) {
        threadsCount--;
        Query query = tipsLoader.getQuery();
        if (isStopped) return;
        if (isPaused) {
            unprocessed.addFirst(query); //Возвращаем запрос в очередь
            return;
        }
        tipsLoadingListener.onQueryProcessed(tipsLoader.getTips(), query.getText(), isSuccess);
        collectedCount += tipsLoader.getTips().size();
        //Добавляем в очередь новые запросы, если найдено не менее 5 неисправленных подсказок
        if (tipsLoader.getTips().size() >= 5 && query.getDepth() < maxDepth) {
            for (char letter : getAlphabet(alphaType, query.getText()))
                unprocessed.addLast(new Query(query.getText() + letter, query));
            if (query.getText().charAt(query.getText().length() - 1) != ' ')
                unprocessed.addLast(new Query(query.getText() + ' ', query));
        }
        if (unprocessed.isEmpty() && threadsCount == 0) tipsLoadingListener.onFinish();
        else attachQueriesToLoaders();
    }

    public interface TipsLoadingListener {
        void onQueryProcessed(List<Tip> tips, String queryText, boolean isSuccess);
        void onFinish();
    }

    //Получаем алфавит в зависимости от языка запроса (по последним буквам запроса)
    private List<Character> getAlphabet(String alphaType, String text) {
        switch (alphaType) {
            case "latin":
                return latin;
            case "cyrillic":
                return cyrillic;
            case "auto":
                for (int i = text.length() - 1; i >= 0; i--)
                    if (text.charAt(i) != ' ') {
                        if (latin.contains(text.charAt(i))) return latin;
                        if (cyrillic.contains(text.charAt(i))) return cyrillic;
                    }
                //Если язык определить не получилось, возвращаем оба алфавита
                return allAlphs;
            default:
                return allAlphs;
        }
    }

    public double getProgress() {
        return unprocessed.size() + collectedCount > 0 ? collectedCount * 1.0 / (unprocessed.size() + collectedCount) : 1;
    }

    public void setMaxThreadsCount(int maxThreadsCount) {
        this.maxThreadsCount = maxThreadsCount;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setAlphaType(String alphaType) {
        this.alphaType = alphaType;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
