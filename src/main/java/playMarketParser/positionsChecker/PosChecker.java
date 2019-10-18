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
    private final static String APP_ID = "com.appgrade.cinemaguru";

    private PosCheckCompleteListener posCheckCompleteListener;

    private final int MAX_THREADS_COUNT;
    private final boolean isTitleInFirstRow = true;
    private int threadsCount;
    private int processedCount;
    private Path outputPath;

    private Deque<PosLoader> unprocessed = new ConcurrentLinkedDeque<>();
    private List<Query> queries;

/*    public static void main(String[] args) {
        PosChecker posChecker = new PosChecker();
        posChecker.readQueries();
        posChecker.createThreads();
        posChecker.startNewLoaders();
    }*/

    public PosChecker(List<Query> queries, int threadsCount, int checksCount, PosCheckCompleteListener posCheckCompleteListener) {
        this.queries = Collections.synchronizedList(queries);
        this.posCheckCompleteListener = posCheckCompleteListener;
        this.MAX_THREADS_COUNT = threadsCount;
        this.CHECKS_COUNT = checksCount;
    }

    public void start() {
        createThreads();
        startNewLoaders();
    }

    private void readQueries() {
        outputPath = Paths.get("pos.csv");

        //Читаем файл в список строк
        try (Stream<String> lines = Files.lines(outputPath, StandardCharsets.UTF_8)) {
            //if (isTitleInFirstRow) firstRow = lines.findFirst().orElse("");
            lines.skip(isTitleInFirstRow ? 1 : 0)
                    .distinct()
                    .forEachOrdered(r -> queries.add(new Query(r)));
            System.out.println("File reading completed");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("File can't be read!");
        }
    }

    private void createThreads() {
        for (int i = 0; i < CHECKS_COUNT; i++)
            for (Query query : queries)
                unprocessed.addLast(new PosLoader(query, APP_ID, this));
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
//            exportPos();
            for (Query query : queries) query.calcRealPos();
            posCheckCompleteListener.onPosCheckingComplete(queries);
        }
    }

    private void exportPos() {
        String firstRow = Global.CSV_DELIMITER;
        //Формируем заголовок при необходимости
        if (isTitleInFirstRow)
            try (Stream<String> lines = Files.lines(outputPath, StandardCharsets.UTF_8)) {
                firstRow = lines.findFirst().orElse(Global.CSV_DELIMITER);
            } catch (IOException e) {
                System.out.println("File couldn't be overwrited!");
            }

        try (PrintStream ps = new PrintStream(new FileOutputStream(outputPath.toFile()))) {
            //Указываем кодировку файла UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');
            //Добавляем заголовок при необходимости
            if (isTitleInFirstRow) {
                firstRow = firstRow + Global.CSV_DELIMITER +
                        new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis())) + "\n";
                Files.write(outputPath, firstRow.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            }
            List<String> newContent = new ArrayList<>();
            for (Query query : queries)
                newContent.add(query.getFullRowText() + Global.CSV_DELIMITER + query.getRealPos());
            Files.write(outputPath, newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            System.out.println("File succesfully overwriting");
        } catch (IOException | NullPointerException e) {
            System.out.println("File couldn't be overwrited!");
        }
    }

    public interface PosCheckCompleteListener {
        void onPosCheckingComplete(List<Query> processedQueries);
    }
}
