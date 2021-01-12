package com.appgrade.gp.scraper.modules.appsCollector;

import com.appgrade.gp.scraper.entities.FoundApp;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AppsCollector implements ListingLoader.AppsCollectingListener {

    private final List<String> queries;
    private int maxThreadsCount = 1;
    private String language;
    private String country;
    private AppsCollectingListener appsCollectingListener;
    private Deque<ListingLoader> unprocessed = new ConcurrentLinkedDeque<>();

    private int threadsCount;
    private int processedCount;
    private boolean isPaused;
    private boolean isStopped;


    public AppsCollector(List<String> queries, AppsCollectingListener appsCollectingListener) {
        this.appsCollectingListener = appsCollectingListener;
        this.queries = queries;
    }

    public void start() {
        isPaused = false;
        isStopped = false;
        createThreads();
        startNewLoaders();
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
        isStopped = false;
        startNewLoaders();
    }

    public void stop() {
        isStopped = true;
        unprocessed.clear();
    }

    private void createThreads() {
        for (String query : queries)
            unprocessed.addLast(new ListingLoader(query, language, country, this));
    }

    private synchronized void startNewLoaders() {
        while (threadsCount < maxThreadsCount && unprocessed.size() > 0) {
            unprocessed.pop().start();
            threadsCount++;
        }
    }

    @Override
    public synchronized void onQueryProcessed(List<FoundApp> foundApps, String query, boolean isSuccess) {
        threadsCount--;
        if (isStopped) return;
        if (isPaused) {
            unprocessed.addFirst(new ListingLoader(query, language, country, this));
            return;
        }
        processedCount++;
        appsCollectingListener.onQueryProcessed(foundApps, query, isSuccess);

        if (unprocessed.size() == 0 && threadsCount == 0) appsCollectingListener.onFinish();
        else startNewLoaders();
    }

    public interface AppsCollectingListener {
        void onQueryProcessed(List<FoundApp> foundApps, String query, boolean isSuccess);
        void onFinish();
    }

    public double getProgress() {
        return isStopped ? 1.0 : processedCount * 1.0 / queries.size();
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
}
