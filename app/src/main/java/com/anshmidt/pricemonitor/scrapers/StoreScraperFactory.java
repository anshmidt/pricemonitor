package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

public class StoreScraperFactory {

    Context context;
    public StoreScraperFactory(Context context) {
        this.context = context;
    }

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
