package playMarketParser.modules.positionsChecker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PosChecker implements PosLoader.OnPosLoadCompleteListener {

    private String appId;
    private PosCheckListener posCheckListener;
    private int checksCount = 5;
    private int maxThreadsCount = 5;
    private String language;
    private String country;

    private int threadsCount;
    private boolean isPaused;

    private Deque<PosLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<Query> queries;

    public PosChecker(String appId, List<Query> queries, PosCheckListener posCheckListener) {
        this.appId = appId;
        this.queries = Collections.synchronizedList(queries);
        this.posCheckListener = posCheckListener;
    }

    public void start() {
        createThreads();
        startNewLoaders();
    }

    private void createThreads() {
        for (int i = 0; i < checksCount; i++)
            for (Query query : queries)
                unprocessed.addLast(new PosLoader(query, appId, language, country, this));
    }

    private synchronized void startNewLoaders() {
        while (threadsCount < maxThreadsCount && unprocessed.size() > 0) {
            unprocessed.pop().start();
            threadsCount++;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
        startNewLoaders();
    }

    public synchronized void stop(){
        unprocessed.clear();
        if (threadsCount == 0) posCheckListener.onFinish();
    }

    @Override
    public synchronized void onPosLoadingComplete(Query query, boolean isSuccess) {
        threadsCount--;
        posCheckListener.onPositionChecked(query, isSuccess);
        if (isPaused) {
            if (threadsCount == 0)
                if (unprocessed.size() > 0) posCheckListener.onPause();
                else posCheckListener.onFinish();
        } else
            if (unprocessed.size() == 0 && threadsCount == 0) posCheckListener.onFinish();
            else startNewLoaders();
    }

    public interface PosCheckListener {
        void onPositionChecked(Query query, boolean isSuccess);
        void onFinish();
        void onPause();
    }

    public double getProgress() {
        return ((queries.size() * checksCount) - unprocessed.size()) * 1.0 / (queries.size() * checksCount);
    }

    public void setMaxThreadsCount(int maxThreadsCount) {
        this.maxThreadsCount = maxThreadsCount;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setChecksCount(int checksCount) {
        this.checksCount = checksCount;
    }
}
