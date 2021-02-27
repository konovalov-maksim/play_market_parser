package com.github.konovalovmaksim.gp.scraper.service.positionschecker;

import com.github.konovalovmaksim.gp.scraper.model.Query;

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
    private int processedCount;
    private boolean isPaused;
    private boolean isStopped;

    private Deque<PosLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<Query> queries;

    public PosChecker(String appId, List<Query> queries, PosCheckListener posCheckListener) {
        this.appId = appId;
        this.queries = Collections.synchronizedList(queries);
        this.posCheckListener = posCheckListener;
    }

    public void start() {
        isPaused = false;
        isStopped = false;
        createThreads();
        startNewLoaders();
    }

    public void resume() {
        isPaused = false;
        isStopped = false;
        startNewLoaders();
    }

    public void pause() {
        isPaused = true;
    }

    public synchronized void stop(){
        isStopped = true;
        unprocessed.clear();
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

    @Override
    public synchronized void onPosLoadingComplete(Query query, int pseudoPos, boolean isSuccess) {
        threadsCount--;
        if (isStopped) return;
        if (isPaused) {
            unprocessed.addFirst(new PosLoader(query, appId, language, country, this));
            return;
        }
        processedCount++;
        if (isSuccess) query.addPseudoPos(pseudoPos);
        posCheckListener.onPositionChecked(query, isSuccess);
        if (unprocessed.size() == 0 && threadsCount == 0) posCheckListener.onFinish();
        else startNewLoaders();
    }

    public interface PosCheckListener {
        void onPositionChecked(Query query, boolean isSuccess);
        void onFinish();
    }

    public double getProgress() {
        return isStopped ? 1.0 : processedCount * 1.0 / (queries.size() * checksCount);
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
