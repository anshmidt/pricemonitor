# PriceMonitor

Android app for monitoring prices in online stores. The price trend is displayed on a graph.  

<img src="https://user-images.githubusercontent.com/12444628/62247221-1193d480-b3ee-11e9-8418-d6378190ad1f.jpg" alt="Screen 1" width="280"/>

The app regularly checks the prices of products in online stores bookmarked by the user. If the price has dropped, a notification is sent to the user, suggesting that now might be a good time to order the product because the price has dropped.  
<img src="https://user-images.githubusercontent.com/12444628/62247025-9df1c780-b3ed-11e9-92ef-0c8640da57fd.gif" alt="Screen 1" width="280"/>

## Key features:
- Prices of the product are scraped from websites selected by the user.
- PriceMonitor periodically sends price requests.
- Data from previous requests is stored in a database.
- Price trends for a single product from different stores are displayed on a graph, providing an easy way to see the trend and compare prices in different stores.
- To add a new product, the user enters its name and URL. The URL is validated, and if it belongs to a known online store, the price is automatically retrieved.
- If the background service detects that the price has dropped significantly (for example, more than 5%), it displays a notification about the dropped price.

## Used technologies:
- Java
- Volley - for HTTP requests
- Room - provides an abstraction layer over SQLite
- WorkManager API - for scheduling requests
- [GraphView by jjoe64](https://github.com/jjoe64/GraphView) - for displaying price trends on a graph
- Dagger 2 - for dependency injection
- TextDrawable - for displaying store icons
- Jsoup - for HTML parsing 
