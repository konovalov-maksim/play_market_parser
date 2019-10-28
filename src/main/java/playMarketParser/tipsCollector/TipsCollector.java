package playMarketParser.tipsCollector;



import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class TipsCollector implements TipsLoader.OnTipLoadCompleteListener {

    private int maxThreadsCount;
    private int threadsCount;
    private TipsLoadingListener tipsLoadingListener;
    private boolean isPaused;

    private Deque<Query> unprocessed = new ConcurrentLinkedDeque<>();

    public TipsCollector(List<String> queriesStrings, int maxThreadsCount, TipsLoadingListener tipsLoadingListener) {
        for (String queryString : queriesStrings) unprocessed.addLast(new Query(queryString + " ", null));
        this.maxThreadsCount = maxThreadsCount;
        this.tipsLoadingListener = tipsLoadingListener;
    }

    public void start() {
        isPaused = false;
        attachQueriesToLoaders();
    }

    //������������ ������� �� �������
    private synchronized void attachQueriesToLoaders() {
        while (threadsCount < maxThreadsCount && unprocessed.size() > 0) {
            new TipsLoader(unprocessed.pop(), this).start();
            threadsCount++;
        }
    }

    public void pause() {
        isPaused = true;
    }

//    public voi

    @Override
    public synchronized void onTipsLoadComplete(TipsLoader tipsLoader) {
        Query query = tipsLoader.getQuery();
        tipsLoadingListener.onQueryProcessed(tipsLoader.getTips());
        if (tipsLoader.getTips().size() >= 5)
            //��������� � ������� ����� �������, ���� ������� �� ����� 5 �������������� ���������
            for (char letter : getAlphabet(query.getText()))
                unprocessed.addLast(new Query(query.getText() + letter, query));
        threadsCount--;
        if (isPaused) {
            if (threadsCount == 0) tipsLoadingListener.onPause();
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

    //�������� ������� � ����������� �� ����� ������� (�� ��������� ������ �������)
    private static List<Character> getAlphabet(String text) {
        List<Character> latin = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        List<Character> cyrillic = "��������������������������������".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        //���� ������ �� ������������ ��������, ��������� � �������� ������
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
        //���� ���� ���������� �� ����������, ���������� ��� ��������
        latin.addAll(cyrillic);
        return latin;
    }

}
