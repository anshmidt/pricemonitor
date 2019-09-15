# PriceMonitor

Android app for monitoring prices in online stores.

#### How does it look:

<img src="https://user-images.githubusercontent.com/12444628/62247221-1193d480-b3ee-11e9-8418-d6378190ad1f.jpg" alt="Screen 1" width="280"/>

#### Receiving notification about dropped price:

<img src="https://user-images.githubusercontent.com/12444628/62247025-9df1c780-b3ed-11e9-92ef-0c8640da57fd.gif" alt="Screen 1" width="280"/>

#### Key features:
- Prices of the product are scrapped from websites selected by user.
- PriceMonitor sends price requests periodically.
- Data from previous requests is stored in a database.
- Price trends from different stores are displayed on a graph.
- To add a new product, user enters its name and URL. URL is validated, and if it belongs to known online store, the price is retrieved automatically.
- If background service finds out that price has dropped significantly (for example, more than 5%), it shows a notification about dropped price.

#### Used tools and libraries:
- Volley - for HTTP requests
- Room - provides an abstraction layer over SQLite
- WorkManager API - for scheduling requests
- [GraphView by jjoe64](https://github.com/jjoe64/GraphView) - for displaying price trends on a graph
- Dagger 2 - for dependency injection
- TextDrawable - for displaying store icons
- Jsoup - for HTML parsing
