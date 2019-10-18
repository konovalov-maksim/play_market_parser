package playMarketParser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

public class PosChecker {
    private final static int CHECKED_POS_COUNT = 50;
    private final static int DEF_CHECKS_COUNT = 7;

    public static void main(String[] args) {
        PosChecker posChecker = new PosChecker();
        int checksCount; //Количество съемов позиций для каждого запроса
        if (args.length>0) {
            checksCount = Integer.parseInt(args[0]);
        }
        else {
            checksCount = DEF_CHECKS_COUNT;
        }
        posChecker.checkPositions(checksCount);
        quit();
    }

    private void checkPositions(int checksCount) {
        Path filePath = Paths.get("positions.csv");

        //Читаем файл в список строк
        List<String> fileContent = null;
        try {
            fileContent = new ArrayList<>(Files.readAllLines(filePath, StandardCharsets.UTF_8));
            System.out.println("File reading completed");
        } catch (Exception e) {
            System.out.println("File can't be read!");
        }

        //Читаем app ID
        String appID = fileContent.get(0).split(";")[1];
        System.out.println("Collecting positions of app " + appID + " :");
        String appURL = "/store/apps/details?id=" + appID;

        List<String> queries = new ArrayList<>();

        //Получаем список запросов
        for (int i = 2; i < fileContent.size(); i++) {
            //Получаем запрос, т. е. значение ячейки i-ой строки 1-го столбца
            String query = fileContent.get(i).split(";")[0];
            queries.add(query);
        }


        int[][] positions = new int[queries.size()][checksCount];

        //Определяем первичные позиции N раз для каждого запроса
        for (int i = 0; i < checksCount; i++) {
            for (int k = 0; k < queries.size(); k++) {
                positions[k][i] = getPos(appURL, queries.get(k));
            }
        }

        //Создаем список с реальными позициями
        List<Integer> realPositions = new ArrayList<>();
        //Расчитываем реальные позиции для каждого запроса
        for (int k=0; k<queries.size(); k++) {
            Integer realPos = getRealPos(positions[k]);
            realPositions.add(realPos);
        }

        //Выводим данные в консоль
        for (int k = 0; k < queries.size(); k++) {
            System.out.printf("%-40s", queries.get(k) + ": ");
            for (int i = 0; i < checksCount; i++) {
                System.out.print(positions[k][i] + " ");
            }
            System.out.println(": " + realPositions.get(k));
        }

        /*  Записываем данные в файл  */

        //Записываем текущую дату во 2-ую строку
        String date = LocalDateTime.now().toLocalDate().toString();
        fileContent.set(1, fileContent.get(1) + ";" + date);

        //Обходим все строки, начиная с 3-ей
        for (int i = 2; i < fileContent.size(); i++) {
            //Дописываем позицию в новый столбец каждой строки
            String newLine = fileContent.get(i) + ";" + realPositions.get(i-2);

            //Записываем обновленную строку в список строк
            fileContent.set(i, newLine);

        }

        System.out.println("--------------");

        //Перезаписываем содержимое файла
        try {
            Files.write(filePath, fileContent, StandardCharsets.UTF_8);
            System.out.println("File succesfully overwriting");
        } catch (IOException | NullPointerException e) {
            System.out.println("File couldn't be overwrited!");
        }
    }


    private static int getPos(String appURL, String query) {
        //CSS класс div-а со ссылкой на страницу приложения
        String appLinkClass = "b8cIId ReQCgd Q9MA7b";
        //Формируем url страницы поиска
        String url = "https://play.google.com/store/search?q=" + query + "&c=apps";
        //Скачиваем страницу
        Document doc = DocReader.readDocByURL(url);
        Elements appsLinksDivs;
        if (doc != null) {
            //Получаем список div-ов со ссылками на приложения
            appsLinksDivs = doc.getElementsByClass(appLinkClass);
        } else {
            return 0;
        }

        //Получаем список ссылок на приложения
        List<String> appsURLs = new ArrayList<>();
        for (int i = 0; i < Math.min(appsLinksDivs.size(), CHECKED_POS_COUNT); i++) {
            appsURLs.add(appsLinksDivs.get(i).child(0).attr("href"));
        }

        //Определяем, на какой позиции находится искомое приложение
        if (appsURLs.contains(appURL))
            return appsURLs.indexOf(appURL) + 1;
        else {
            //System.out.println("Приложение отсутствует в ТОП-" + CHECKED_POS_COUNT);
            return 0;
        }
    }

    private int getRealPos(int[] positions) {
        //List<Integer> positions = new ArrayList<>();
        Map<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < positions.length; i++) {
            Integer frequency = hashMap.get(positions[i]);
            hashMap.put(positions[i], frequency == null ? 1 : frequency + 1);
        }
        //Находим элемент в HashMap с наибольшей частотой
        Integer maxValueInMap=(Collections.max(hashMap.values()));
        //Определяем его индекс (key)
        for (HashMap.Entry<Integer, Integer> entry : hashMap.entrySet()) {
            if (entry.getValue().equals(maxValueInMap)) {
                return entry.getKey();
            }
        }
        return 0;
    }


    private static void quit() {
        System.out.println("Quit or restart? (Q/R)");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextLine()) {
            case "Q":
                break;
            case "R":
                main(null);
                break;
            default:
                quit();
                break;
        }
    }


}
