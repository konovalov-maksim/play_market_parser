package playMarketParser.positionsChecker;

import playMarketParser.Global;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

public class PosChecker implements PosLoader.OnPosLoadCompleteListener {
    private final int CHECKS_COUNT;
    private String appId;

    private PosCheckCompleteListener posCheckCompleteListener;

    private final int MAX_THREADS_COUNT;
    private final boolean isTitleInFirstRow = true;
    private int threadsCount;
    private int processedCount;

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
        createThreads();
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

    @Override
    public synchronized void onPosLoadComplete(PosLoader posLoader) {
        threadsCount--;
        processedCount++;
        if (processedCount < queries.size() * CHECKS_COUNT)
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
