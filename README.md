# Play Market Parser

Read in another languages: [English](readme.md), [Русский](readme.ru.md)

Play Market Parser - is an instrument that allows you to mass collect data from Google Play Market. Collected information can be used for analytics, formation app's promotion strategy or analysis of competitors. At the moment Play Market has 4 modes:
- [Searching tips collecting](#searching-tips-collecting)
- [App's positions checking by list of queries](#app's-positions-checking)
- [Collecting apps by queries list](#collecting-apps)
- [Collecting detailed information about applications](#collecting-detailed-info)

##### Features:
- Parsing data for different languages ​​and countries
- Possibility of multi-thread parsing
- Collecting information hidden from users: **the exact number of installs** and **release date**
- User-friendly interface
- Possibility of unloading the collected data in the CSV
- English and Russian localizations

## Modes

### Searching tips collecting
The mode allows to collect tips from the Play Market's search line. At the input you should submit a list of queries. Collected tips can be used to analyze user demand or to perform app's page text optimization.
For each query it can be collected not only the first 5 most popular tips, but also all frequently used continuations of query. 
If for some original query is found 5 tips, for finding the remaining tips will be formed sub-queries, that iterate all letters of the alphabet. Alphabet for iterating detects automatically (by input query), or it can be set manually. Max parsing depth (i. e. the max number of characters that can be added to the original query) set in Preferences. Set it to 1, if you want to collect tips only directly by original query (no more than 5 for each). The algorithm does not collect the tips generated for the corrected query. 
For example, for the query "facebook ma" tip "facebook message app" won't be collected, as it corresponds to corrected query, not original.


### App's positions checking
The mode allows to check app's positions in Play Market search engine results page. Collected data can be used to analyze the dynamics of visibility of the application.
Sometimes, instead of displaying the app on the actual position, Google Play "raises" or "lowers" it. Therefore, to get the real position, it is recommended to set the number of checks for each request 3-5.
The exported CSV file with the results can be imported for re-checking the positions (eg the next day). In this case, if you select "Include previous results" checkbox, the new results will be recorded in a new column to the right of the old, making it easier to analyze the data.

### Collecting apps
Режим позволяет собрать ссылки и основную информацию по приложениям, найденным по указанным запросам.
Собирается следующая информация:
- название приложения
- URL страницы приложения
- позиция приложения в выдаче
- краткое описание
- средняя оценка
- URL иконки
- имя разработчика
- URL страницы разработчика
По найденным ссылкам может быть проведен сбор детальной информации о приложениях.

### Collecting detailed info
Режим позволяет спарсить детальную информацию о приложениях по списку URL.
Собираются следующие данные:
- Имя
- Точное число установок
- Число оценок
- Средняя оценка
- Дата релиза
- Дата последнего обновления
- Категория
- Рзмер
- Признак наличия рекламы
- Признак наличи покупок внутри приложения
- Стоимость контента
- Возрастной рейтинг
- URL страницы разработчика
- Имя разработчика
- URL сайта разработчика
- Email разработчика
- URL иконки
- Детальное описание
- Версия
- Что нового
- Мин. версия Android
- URL страниц похожих приложений

## Настройки
Для каждого из режимов возможно задать язык и страну, число потоков, таймаут соединения, прокси сервер, а также http заголовки user-agent и accept-language.
Если не задана страна, Play Market определит ее по ip (или ip прокси сервера).
Если не задан язык, Play Market определит его по заголовку accept-language.