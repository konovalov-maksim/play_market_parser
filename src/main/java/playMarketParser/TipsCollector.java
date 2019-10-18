package playMarketParser;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TipsCollector {


    public static void main(String[] args) {
//        Query test = new Query("угадай фильм");
//        test.collectAllTips();
        collectTips(false);
    }

    private static void collectTips(boolean isAll) {

        Path inputPath = Paths.get("inputTips.txt");
        List<Query> queries = new ArrayList<>();
        try {
            System.out.println("File reading completed");
            queries = Files.readAllLines(inputPath, StandardCharsets.UTF_8).stream()
                    .map(Query::new)
                    .peek((q) -> System.out.println(q.getText()))
                    .peek((q) -> collectTips(q, isAll, true))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("File reading error");
            return;
        }
        //Записываем собранные подсказки в файл
        Path outputPath = Paths.get("outputTips.csv");
        try {
            Files.write(outputPath, getCsvRows(queries), StandardCharsets.UTF_8);
            System.out.println("File succesfully writen");
        } catch (IOException | NullPointerException e) {
            System.out.println("File can't be overwrited!");
        }
    }

    private static List<String> getTips(String query) {
        System.out.println(":::" + query + ":::");
        //Формируем из запроса url
        String url = "https://market.android.com/suggest/SuggRequest?json=1&c=3&query=" + query + "&hl=ru&gl=RU";
        //Загружаем js документ
        Document doc = DocReader.readDocByURL(url);
        if (doc == null) return new ArrayList<>();
        //Получаем контент документа в виде строки
        String content = doc.text();
        if (content.equals("[]")) return new ArrayList<>();
        //Убираем шлак
        content = content.replace("\"", "");
        content = content.replace("{", "");
        //Убираем ненужные символы в начале и в конце строки
        content = content.replace("[s:", "");
        content = content.replace(",t:q}]", "");
        //Извлекаем из строки нужные данные в массив, а затем в список
        String[] tips = content.split(",t:q},s:");
        for (String tip : tips) System.out.println(tip);
        return new ArrayList<>(Arrays.asList(tips));
    }

    private static void collectTips(Query query, boolean isAllTips, boolean addSpace) {
        if (query == null || query.getText() == null || query.getText().length() == 0) return;
        List<String> tips = getTips(query.getText());
        query.addTips(tips);
        if (query.getTips().size() < 5 || !isAllTips) return;

        String queryText = addSpace ? query.getText() + " " : query.getText();
        for (char letter : getAlphabet(queryText)) {
            Query subQuery = new Query(queryText + letter);
            collectTips(subQuery, true, false);
            query.addTips(subQuery);
        }
    }

    //Получаем алфавит в зависимости от языка запроса (по последним буквам запроса)
    private static List<Character> getAlphabet(String query) {
        List<Character> latin = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        List<Character> cyrillic = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя".chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        //Если запрос не оканчивается пробелом, добавляем в алфавиты пробел
        if (query.charAt(query.length() - 1) != ' ') {
            latin.add(' ');
            cyrillic.add(' ');
        }
        for (int i = query.length() - 1; i >= 0; i--) {
            if (query.charAt(i) != ' ') {
                if (latin.contains(query.charAt(i))) return latin;
                if (cyrillic.contains(query.charAt(i))) return cyrillic;
            }
        }
        //Если язык определить не получилось, возвращаем оба алфавита
        latin.addAll(cyrillic);
        return latin;
    }

    private static List<String> getCsvRows(List<Query> queries) {
        List<String> csvRows = new ArrayList<>();
        for (Query query : queries)
            csvRows.addAll(query.getTips().stream().map((s) -> query.getText() + ";" + s).collect(Collectors.toList()));
        return csvRows;
    }

}
