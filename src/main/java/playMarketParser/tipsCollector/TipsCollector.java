package playMarketParser.tipsCollector;



import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class TipsCollector implements TipsLoader.OnTipLoadCompleteListener {

    private int maxThreadsCount;
    private int threadsCount;
    private int maxDepth;
    private int collectedCount;
    private TipsLoadingListener tipsLoadingListener;
    private boolean isPaused;
    private boolean isStopped;

    private Deque<Query> unprocessed = new ConcurrentLinkedDeque<>();

    public TipsCollector(List<String> queriesStrings, int maxThreadsCount, int maxDepth, TipsLoadingListener tipsLoadingListener) {
        for (String queryString : queriesStrings) unprocessed.addLast(new Query(queryString + " ", null));
        this.maxThreadsCount = maxThreadsCount;
        this.maxDepth = maxDepth;
        this.tipsLoadingListener = tipsLoadingListener;
        isStopped = false;
    }

    public void start() {
        isPaused = false;
        attachQueriesToLoaders();
    }

    //Распределяем запросы по потокам
    private synchronized void attachQueriesToLoaders() {
        while (threadsCount < maxThreadsCount && unprocessed.size() > 0) {
            new TipsLoader(unprocessed.pop(), this).start();
            threadsCount++;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void stop() {
        isStopped = true;
        unprocessed.clear();
        if (threadsCount == 0) tipsLoadingListener.onFinish();
    }

    @Override
    public synchronized void onTipsLoadingComplete(TipsLoader tipsLoader) {
        Query query = tipsLoader.getQuery();
        tipsLoadingListener.onQueryProcessed(tipsLoader.getTips());
        collectedCount += tipsLoader.getTips().size();
        if (tipsLoader.getTips().size() >= 5)
            //Добавляем в очередь новые запросы, если найдено не менее 5 неисправленных подсказок
            for (char letter : getAlphabet(query.getText()))
                if (!isStopped && query.getDepth() < maxDepth) unprocessed.addLast(new Query(query.getText() + letter, query));
        threadsCount--;
        if (isPaused) {
            if (threadsCount == 0)
                if (unprocessed.size() > 0) tipsLoadingListener.onPause();
                else tipsLoadingListener.onFinish();
        }
        else
            if (unprocessed.isEmpty() && threadsCount == 0) tipsLoadingListener.onFinish();
            else attachQueriesToLoaders();
    }

    public interface TipsLoadingListener {
        void onQueryProcessed(List<Tip> tips);
        void onFinish();
        void onPause();
    }

    //Получаем алфавит в зависимости от языка запроса (по последним буквам запроса)
    private static List<Character> getAlphabet(String text) {
        List<Character> latin = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        List<Character> cyrillic = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        //Если запрос не оканчивается пробелом, добавляем в алфавиты пробел
        if (text.charAt(text.length() - 1) != ' ') {
            latin.add(' ');
            cyrillic.add(' ');
        }
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) != ' ') {
                if (latin.contains(text.charAt(i))) return latin;
                if (cyrillic.contains(text.charAt(i))) return cyrillic;
            }
        }
        //Если язык определить не получилось, возвращаем оба алфавита
        latin.addAll(cyrillic);
        return latin;
    }

    public double getProgress() {
        return unprocessed.size() + collectedCount > 0 ? collectedCount * 1.0 / (unprocessed.size() + collectedCount) : 1;
    }
}
