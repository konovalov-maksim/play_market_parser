package com.appgrade.gp.scraper.modules.appsParser;

import com.appgrade.gp.scraper.entities.App;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AppsParser implements AppLoader.OnAppLoadingCompleteListener{
    private AppParsingListener appParsingListener;
    private int maxThreadsCount = 5;
    private String language;
    private String country;

    private int threadsCount;
    private int processedCount;
    private boolean isPaused;
    private boolean isStopped;

    private Deque<AppLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<App> apps;

    public AppsParser(List<App> apps, AppParsingListener appParsingListener) {
        this.apps = apps;
        this.appParsingListener = appParsingListener;
    }

    private void createThreads() {
        for (App app : apps)
            unprocessed.addLast(new AppLoader(app, this, language, country));
    }

    private synchronized void startNewLoaders() {
        while (threadsCount < maxThreadsCount && unprocessed.size() > 0) {
            unprocessed.pop().start();
            threadsCount++;
        }
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

    public synchronized void stop(){
        isStopped = true;
        unprocessed.clear();
    }

    @Override
    public void onAppParsingComplete(App app, boolean isSuccess) {
        threadsCount--;
        if (isStopped) return;
        if (isPaused) {
            unprocessed.addFirst(new AppLoader(app, this, language, country));
            return;
        }
        processedCount++;
        appParsingListener.onAppParsed(app, isSuccess);

        if (unprocessed.size() == 0 && threadsCount == 0) appParsingListener.onFinish();
        else startNewLoaders();
    }

    public void setAppParsingListener(AppParsingListener appParsingListener) {
        this.appParsingListener = appParsingListener;
    }

    public double getProgress() {
        return isStopped ? 1.0 : processedCount * 1.0 / apps.size();
    }

    public interface AppParsingListener {
        void onAppParsed(App app, boolean isSuccess);
        void onFinish();
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
