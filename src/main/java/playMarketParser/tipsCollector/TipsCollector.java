package playMarketParser.tipsCollector;


import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class TipsCollector implements TipsLoader.OnTipLoadCompleteListener {

    private final int MAX_THREADS_COUNT;
    private int threadsCount;
    private TipsLoadingListener tipsLoadingListener;
    private boolean isAborted;

    private Deque<Query> unprocessed = new ConcurrentLinkedDeque<>();

    public TipsCollector(List<String> queriesStrings, int maxThreadsCount, TipsLoadingListener tipsLoadingListener) {
        for (String queryString : queriesStrings) unprocessed.addLast(new Query(queryString + " ", null));
        this.MAX_THREADS_COUNT = maxThreadsCount;
        this.tipsLoadingListener = tipsLoadingListener;
    }

    public void start() {
        isAborted = false;
        attachQueriesToLoaders();
    }

    //Распределяем запросы по потокам
    private synchronized void attachQueriesToLoaders() {
        while (threadsCount < MAX_THREADS_COUNT && unprocessed.size() > 0) {
            new TipsLoader(unprocessed.pop(), this).start();
            threadsCount++;
        }
    }

    public void abort() {
        isAborted = true;
    }

    @Override
    public synchronized void onTipsLoadComplete(TipsLoader tipsLoader) {
        Query query = tipsLoader.getQuery();
        tipsLoadingListener.onQueryProcessed(tipsLoader.getTips());
        if (tipsLoader.getTips().size() >= 5)
            //Добавляем в очередь новые запросы, если найдено не менее 5 неисправленных подсказок
            for (char letter : getAlphabet(query.getText()))
                unprocessed.addLast(new Query(query.getText() + letter, query));
        threadsCount--;
        if ((unprocessed.isEmpty() && threadsCount == 0) || isAborted) tipsLoadingListener.onFinish();
        else attachQueriesToLoaders();
    }

    public interface TipsLoadingListener {
        void onQueryProcessed(List<Tip> tips);
        void onFinish();
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

}
