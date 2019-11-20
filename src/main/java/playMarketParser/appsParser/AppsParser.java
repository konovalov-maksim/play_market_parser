package playMarketParser.appsParser;

import playMarketParser.App;
import playMarketParser.positionsChecker.PosLoader;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AppsParser {
    private AppParsingListener appParsingListener;
    private int checksCount = 5;
    private int maxThreadsCount = 5;
    private String language;
    private String country;

    private int threadsCount;
    private boolean isPaused;

    private Deque<PosLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<App> apps;

    public AppsParser() {
    }

    public void start() {

    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;

    }

    public synchronized void stop(){
        unprocessed.clear();
        if (threadsCount == 0) appParsingListener.onFinish();
    }

    public void setAppParsingListener(AppParsingListener appParsingListener) {
        this.appParsingListener = appParsingListener;
    }

    public double getProgress() {
        return 0;
    }

    public interface AppParsingListener {
        void onPositionChecked(App app, boolean isSuccess);
        void onFinish();
        void onPause();
    }
}
