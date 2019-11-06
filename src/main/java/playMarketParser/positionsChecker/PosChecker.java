package playMarketParser.positionsChecker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PosChecker implements PosLoader.OnPosLoadCompleteListener {
    private final int CHECKS_COUNT;
    private String appId;

    private PosCheckListener posCheckListener;

    private final int MAX_THREADS_COUNT;
    private int threadsCount;
    private boolean isPaused;

    private Deque<PosLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<Query> queries;

    public PosChecker(String appId, List<Query> queries, int threadsCount, int checksCount, PosCheckListener posCheckListener) {
        this.appId = appId;
        this.queries = Collections.synchronizedList(queries);
        this.posCheckListener = posCheckListener;
        this.MAX_THREADS_COUNT = threadsCount;
        CHECKS_COUNT = checksCount;
        createThreads();
    }

    public void start() {
        isPaused = false;
        startNewLoaders();
    }

    private void createThreads() {
        for (int i = 0; i < CHECKS_COUNT; i++)
            for (Query query : queries)
                unprocessed.addLast(new PosLoader(query, appId, this));
    }

    private synchronized void startNewLoaders() {
        while (threadsCount < MAX_THREADS_COUNT && unprocessed.size() > 0) {
            unprocessed.pop().start();
            threadsCount++;
        }
    }

    public void pause() {
        isPaused = true;
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
        return ((queries.size() * CHECKS_COUNT) - unprocessed.size()) * 1.0 / (queries.size() * CHECKS_COUNT);
    }
}
