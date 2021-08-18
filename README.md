# ![app_icon](https://user-images.githubusercontent.com/49783652/69971722-6c227600-1531-11ea-87f2-d51bd7b00379.png) Google Play Scraper

*Read this in another languages: ![en](https://user-images.githubusercontent.com/49783652/69971412-e56d9900-1530-11ea-8516-f9f1f6219147.png) 
[English](https://github.com/konovalov-maksim/play_market_parser/blob/master/README.md), 
![ru](https://user-images.githubusercontent.com/49783652/69971413-e56d9900-1530-11ea-8937-a7989b8d727d.png) 
[Русский](https://github.com/konovalov-maksim/play_market_parser/blob/master/README.ru.md).*

Google Play Scraper is a tool that allows you to mine data from Google Play. 
Collected information can be used for analytics, app's promotion strategy formation or analysis of competitors.

![download](https://user-images.githubusercontent.com/49783652/70123296-6b99f480-1683-11ea-8f71-ac9d1e14fd54.png) Download release (v1.3.1): 
[GooglePlayScraper-1.3.1.exe + JRE](https://github.com/konovalov-maksim/play_market_parser/releases/download/1.3.1/GooglePlayScraper-1.3.1.zip) (36.05 Mb) | 
[GooglePlayScraper-1.3.1.jar](https://github.com/konovalov-maksim/play_market_parser/releases/download/1.3.1/GooglePlayScraper-1.3.1.jar) (11.61 Mb)

Google Play Scraper has 4 modes:
- [Search suggestions collection](#search-suggestions-collection)
- [App's positions checking by list of queries](#apps-positions-check)
- [Collecting apps by queries list](#apps-collection)
- [Collecting detailed information about applications](#detailed-info-collection)

#### Features:
- Data scraping for different languages and countries
- Multi-thread data loading
- Extraction of information hidden from users: **the exact number of installs** and **release date**
- HTTP proxy support
- User-friendly interface
- Export of the collected data to CSV
- English and Russian localizations

![apps-parsing-en](https://user-images.githubusercontent.com/49783652/69831631-506e5580-123b-11ea-9138-de99b59d4c3d.png)

## Modes

### Search suggestions collection
The mode allows you to collect tips from the Google Play search line. At the input you should submit a list of queries. 
Collected tips can be used to analyze user demand or to perform app's page text optimization.

![searching_tips](https://user-images.githubusercontent.com/49783652/69968618-d46e5900-152b-11ea-83ce-c4adf0cf80b9.png)

It can be collected not only the first 5 most popular tips for each query, but also all frequently used continuations of query. 
If 5 tips are found for some original query, subqueries will be formed for the remaining tips searching. 
These subqueries iterate all letters of the alphabet. Alphabet for iterating detects automatically (by input query), or it can be set manually. 
Max parsing depth (i. e. the max number of characters that can be added to the original query) set in Preferences. 
Set it to 1 if you want to collect tips only directly by an original query (no more than 5 for each).

The algorithm does not collect the tips generated for the corrected query. 
For example, for the query "facebook ma" tip "facebook message app" won't be collected as it corresponds to corrected query, not original.

![tips-en](https://user-images.githubusercontent.com/49783652/69831637-5106ec00-123b-11ea-8842-ce593f956803.png)

### Apps positions check
This mode allows you to check apps' positions on Google Play search engine results page. 
Collected data can be used to analyze the dynamics of visibility of the application.

Sometimes, instead of displaying the app on the actual position, Google Play "raises" or "lowers" it. 
Therefore, to get the real position, it is recommended to set the number of checks for each request in the range 3-5.

The exported CSV file with the results can be imported for re-checking the positions (eg the next day). 
In this case, if you select "Include previous results" checkbox, 
the new results will be recorded in a new column to the right of the old, making it easier to analyze the data.

![pos-en](https://user-images.githubusercontent.com/49783652/69831633-5106ec00-123b-11ea-8da8-0d7e217a4bb6.png)


### Apps searching
This mode allows you to collect links and basic apps' info found by specified queries. The following apps' info is collected:
- name
- page URL 
- position on SERP
- short description
- average rate
- icon URL
- developer name
- developer page URL
Detailed information about applications can be collected from the found links.

![apps-col-en](https://user-images.githubusercontent.com/49783652/69831629-506e5580-123b-11ea-9385-21778201892d.png)

### Detailed info collection
This mode allows you to extract detail apps' info using list of URLs.
The following apps' info is collected:
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
- content rating
- developer page URL
- developer name
- developer website URL
- developer email 
- developer address
- icon URL
- detailed description
- version
- what's new
- min. android version
- similar apps pages URLs

## Preferences
For each mode it's possible to set the language and country, the number of threads, connection timeout, proxy server, and http headers user-agent, 
accept-language.  
If no country is specified, Google Play will detect it by IP (or proxy server IP).  
If language not specified, Google Play will detect it by http header accept-language.  

![prefs-en](https://user-images.githubusercontent.com/49783652/69831635-5106ec00-123b-11ea-84fe-1ef2e501248f.png)
