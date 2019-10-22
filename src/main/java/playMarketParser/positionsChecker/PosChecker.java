package playMarketParser.positionsChecker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PosChecker implements PosLoader.OnPosLoadCompleteListener {
    private final int CHECKS_COUNT;
    private String appId;

    private PosCheckCompleteListener posCheckCompleteListener;

    private final int MAX_THREADS_COUNT;
    private final boolean isTitleInFirstRow = true;
    private int threadsCount;
    private int processedCount;
    private boolean isAborted;

    private Deque<PosLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<Query> queries;

    public PosChecker(String appId, List<Query> queries, int threadsCount, int checksCount, PosCheckCompleteListener posCheckCompleteListener) {
        this.appId = appId;
        this.queries = Collections.synchronizedList(queries);
        this.posCheckCompleteListener = posCheckCompleteListener;
        this.MAX_THREADS_COUNT = threadsCount;
        this.CHECKS_COUNT = checksCount;
    }

    public void start() {
        isAborted = false;
        createThreads();
        startNewLoaders();
    }

    public synchronized void abort(){
        unprocessed.clear();
        isAborted = true;
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



    @Override
    public synchronized void onPosLoadComplete(PosLoader posLoader) {
        threadsCount--;
        processedCount++;
        if (processedCount < queries.size() * CHECKS_COUNT && !isAborted)
            startNewLoaders();
        else {
            for (Query query : queries) query.calcRealPos();
            posCheckCompleteListener.onPosCheckingComplete(queries);
        }
    }

    public interface PosCheckCompleteListener {
        void onPosCheckingComplete(List<Query> processedQueries);
    }
}
