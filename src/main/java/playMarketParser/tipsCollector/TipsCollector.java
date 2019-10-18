package playMarketParser.tipsCollector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TipsCollector implements TipsLoader.OnTipLoadCompleteListener {

    private static final int MAX_THREADS_COUNT = 5;
    private int threadsCount;

    public static void main(String[] args) {
        TipsCollector tipsCollector = new TipsCollector();
        tipsCollector.readQueries();
        tipsCollector.attachQueriesToLoaders();
    }

    private Deque<Query> unprocessed = new ConcurrentLinkedDeque<>();
    private List<Query> processed = Collections.synchronizedList(new ArrayList<>());

    private void readQueries() {
        Path inputPath = Paths.get("inputTips.txt");
        try (Stream<String> lines = Files.lines(inputPath, StandardCharsets.UTF_8)) {
            lines.distinct().forEachOrdered(text -> unprocessed.addLast(new Query(text + " ", null)));
            System.out.println("File reading completed");
        } catch (IOException e) {
            System.out.println("File reading error");
        }
    }

    //Распределяем запросы по потокам
    private void attachQueriesToLoaders() {
        while (threadsCount < MAX_THREADS_COUNT && unprocessed.size() > 0) {
            new TipsLoader(unprocessed.pop(), this).start();
            threadsCount++;
        }
    }

    @Override
    public synchronized void onTipsLoadComplete(TipsLoader tipsLoader) {
        Query query = tipsLoader.getQuery();
        processed.add(query);
        //Добавляем в очередь новые запросы, если найдено не менее 5 подсказок
        if (query.getTips().size() >= 5)
            for (char letter : getAlphabet(query.getText()))
                unprocessed.addLast(new Query(query.getText() + letter, query));
        threadsCount--;
        if (unprocessed.isEmpty() && threadsCount == 0) exportTips();
        else attachQueriesToLoaders();
    }

    //Записываем собранные подсказки в файл
    private void exportTips() {
        Path outputPath = Paths.get("outputTips.csv");
        try (PrintStream ps = new PrintStream(new FileOutputStream(outputPath.toFile()))){
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');
            Files.write(outputPath, "Запрос;Подсказка\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            List<String> csvRows = processed.stream()
                    .flatMap(q -> q.getTips().stream()
                            .map(t -> q.getRootQueryText() +";" + t))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            Files.write(outputPath, csvRows, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            System.out.println("File successfully written");
        } catch (IOException | NullPointerException e) {
            System.out.println("File can't be overwritten!");
        }
    }

    //Получаем алфавит в зависимости от языка запроса (по последним буквам запроса)
    private static List<Character> getAlphabet(String text) {
        List<Character> latin = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        List<Character> cyrillic = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        //Если запрос не оканчивается пробелом, добавляем в алфавиты пробел
        if (text.charAt(text.length() - 1) != ' ') {
            latin.add(' ');
            cyrillic.add(' ');
        }
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) != ' ') {
                if (latin.contains(text.charAt(i))) return latin;
                if (cyrillic.contains(text.charAt(i))) return cyrillic;
            }
        }
        //Если язык определить не получилось, возвращаем оба алфавита
        latin.addAll(cyrillic);
        return latin;
    }

}
