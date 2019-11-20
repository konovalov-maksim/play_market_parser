package playMarketParser.appsParser;

import playMarketParser.App;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AppsParser implements AppLoader.OnAppLoadingCompleteListener{
    private AppParsingListener appParsingListener;
    private int maxThreadsCount = 5;
    private String language;
    private String country;

    private int threadsCount;
    private boolean isPaused;

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
        createThreads();
        startNewLoaders();
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
        if (threadsCount == 0) appParsingListener.onFinish();
    }

    @Override
    public void onAppParsingComplete(App app, boolean isSuccess) {
        threadsCount--;
        appParsingListener.onAppParsed(app, isSuccess);
        if (isPaused) {
            if (threadsCount == 0)
                if (unprocessed.size() > 0) appParsingListener.onPause();
                else appParsingListener.onFinish();
        } else
        if (unprocessed.size() == 0 && threadsCount == 0) appParsingListener.onFinish();
        else startNewLoaders();
    }

    public void setAppParsingListener(AppParsingListener appParsingListener) {
        this.appParsingListener = appParsingListener;
    }

    public double getProgress() {
        return (apps.size() - unprocessed.size()) * 1.0 / apps.size();
    }

    public interface AppParsingListener {
        void onAppParsed(App app, boolean isSuccess);
        void onFinish();
        void onPause();
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
