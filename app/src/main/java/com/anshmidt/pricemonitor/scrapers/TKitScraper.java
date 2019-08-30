package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TKitScraper extends StoreScraper {

    public static final String URL = "t-kit.ru";
    public static final String NAME = "t-kit";

    public TKitScraper(Context context) {
        super(context, URL, NAME);
    }

    @Override
    public int extractPriceFromFullResponse(String fullResponse) {

        Document doc = Jsoup.parse(fullResponse);

        Elements priceSpans = doc.select("span.prices-current.js-prices-current");

        for (Element priceSpan : priceSpans) {
            String resultString = priceSpan.text().split(" ")[0];
            return Integer.parseInt(resultString);
        }
        return PRICE_NOT_FOUND;


    }
}
