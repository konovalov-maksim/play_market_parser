# ![app_icon](https://user-images.githubusercontent.com/49783652/69971722-6c227600-1531-11ea-87f2-d51bd7b00379.png) Play Market Parser

*Read this in another languages: ![en](https://user-images.githubusercontent.com/49783652/69971412-e56d9900-1530-11ea-8516-f9f1f6219147.png) [English](https://github.com/konovalov-maksim/play_market_parser/blob/master/README.md), ![ru](https://user-images.githubusercontent.com/49783652/69971413-e56d9900-1530-11ea-8937-a7989b8d727d.png) [Русский](https://github.com/konovalov-maksim/play_market_parser/blob/master/README.ru.md).*

Play Market Parser - is an instrument that allows you to mass collect data from Google Play Market. Collected information can be used for analytics, formation app's promotion strategy or analysis of competitors.

At the moment Play Market has 4 modes:
- [Searching tips collecting](#searching-tips-collecting)
- [App's positions checking by list of queries](#app's-positions-checking)
- [Collecting apps by queries list](#collecting-apps)
- [Collecting detailed information about applications](#collecting-detailed-info)

#### Features:
- Parsing data for different languages ​​and countries
- Possibility of multi-thread parsing
- Collecting information hidden from users: **the exact number of installs** and **release date**
- User-friendly interface
- Possibility of unloading the collected data in the CSV
- English and Russian localizations

![apps-parsing-en](https://user-images.githubusercontent.com/49783652/69831631-506e5580-123b-11ea-9138-de99b59d4c3d.png)

## Modes

### Searching tips collecting
The mode allows to collect tips from the Play Market's search line. At the input you should submit a list of queries. Collected tips can be used to analyze user demand or to perform app's page text optimization.

![searching_tips](https://user-images.githubusercontent.com/49783652/69968618-d46e5900-152b-11ea-83ce-c4adf0cf80b9.png)

For each query it can be collected not only the first 5 most popular tips, but also all frequently used continuations of query. 
If for some original query is found 5 tips, for finding the remaining tips will be formed sub-queries that iterate all letters of the alphabet.Alphabet for iterating detects automatically (by input query), or it can be set manually. Max parsing depth (i. e. the max number of characters that can be added to the original query) set in Preferences. Set it to 1 if you want to collect tips only directly by original query (no more than 5 for each).

The algorithm does not collect the tips generated for the corrected query. 
For example, for the query "facebook ma" tip "facebook message app" won't be collected, as it corresponds to corrected query, not original.

![tips-en](https://user-images.githubusercontent.com/49783652/69831637-5106ec00-123b-11ea-8842-ce593f956803.png)

### App's positions checking
The mode allows to check app's positions in Play Market search engine results page. Collected data can be used to analyze the dynamics of visibility of the application.

Sometimes, instead of displaying the app on the actual position, Google Play "raises" or "lowers" it. Therefore, to get the real position, it is recommended to set the number of checks for each request 3-5.

The exported CSV file with the results can be imported for re-checking the positions (eg the next day). In this case, if you select "Include previous results" checkbox, the new results will be recorded in a new column to the right of the old, making it easier to analyze the data.

![pos-en](https://user-images.githubusercontent.com/49783652/69831633-5106ec00-123b-11ea-8da8-0d7e217a4bb6.png)


### Collecting apps
The mode allows to collect links and basic apps info found by specified queries. The following app info is collected:
- name
- page URL 
- SERP position
- short decription
- average rate
- icon URL
- developer name
- developer page URL
Detailed information about applications can be collected from the found links.

![apps-col-en](https://user-images.githubusercontent.com/49783652/69831629-506e5580-123b-11ea-9385-21778201892d.png)

### Collecting detailed info
Mode allows parse detail apps info by list of URLs.
The following app info is collecting:
- name
- **exact number of installs**
- rates count
- average rate
- release date
- last update date
- category
- size
- presence of ads
- presence of in-app purchases
- content cost
- Content Rating
- developer page URL
- developer name
- developer website URL
- developer email 
- icon URL
- detailed description
- version
- what's new
- min. Android version
- similar apps pages URLs

## Preferences
For each of the modes is possible to set the language and country, the number of threads, connection timeout, proxy server, and http headers user-agent, accept-language.
If no country is specified, Play Market will detect it by IP (or proxy server IP).
If language not specified, Play Market will detect it by http header accept-language.

![prefs-en](https://user-images.githubusercontent.com/49783652/69831635-5106ec00-123b-11ea-84fe-1ef2e501248f.png)