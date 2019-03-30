package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

public class StoreScraperFactory {

    Context context;
    public StoreScraperFactory(Context context) {
        this.context = context;
    }

//    public StoreScraper getStoreScraper(String title) {
//        if (title == null) {
//            return null;
//        }
//        if (title.equals(TKitScraper.TITLE)) {
//            return new TKitScraper(context);
//        }
//        if (title.equals(YandexMarketMinScraper.TITLE)) {
//            return new YandexMarketMinScraper(context);
//        }
//        if (title.equals(Digital812Scraper.TITLE)) {
//            return new Digital812Scraper(context);
//        }
//        if (title.equals(EcoDriftScraper.TITLE)) {
//            return new EcoDriftScraper(context);
//        }
//        if (title.equals(DebugRandomScraper.TITLE)) {
//            return new DebugRandomScraper(context);
//        }
//        throw new RuntimeException("Invalid store title: " + title);
//    }

    public StoreScraper getStoreScraper(String storeUrl) {
        if (storeUrl == null) {
            return null;
        }
        if (storeUrl.equals(TKitScraper.URL)) {
            return new TKitScraper(context);
        }
        if (storeUrl.equals(YandexMarketMinScraper.URL)) {
            return new YandexMarketMinScraper(context);
        }
        if (storeUrl.equals(Digital812Scraper.URL)) {
            return new Digital812Scraper(context);
        }
        if (storeUrl.equals(EcoDriftScraper.URL)) {
            return new EcoDriftScraper(context);
        }
        if (storeUrl.equals(DebugRandomScraper.URL)) {
            return new DebugRandomScraper(context);
        }
        if (storeUrl.equals(BoltynScraper.URL)) {
            return new BoltynScraper(context);
        }
        throw new RuntimeException("Invalid store url: " + storeUrl);
    }

}
