package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Digital812Scraper extends StoreScraper {

    public static final String URL = "digital812.su";
    public static final String NAME = "digital 812";

    public Digital812Scraper(Context context) {
        super(context, URL, NAME);
    }

    @Override
    public int extractPriceFromFullResponse(String fullResponse) {

        Document doc = Jsoup.parse(fullResponse);

        Elements priceSpans = doc.select("span.price");

        for (Element priceSpan : priceSpans) {
            String resultString = priceSpan.text();
            String resultStringWithoutOddChars = resultString.replaceAll("[^0-9]", "");
            return Integer.parseInt(resultStringWithoutOddChars);

        }
        return PRICE_NOT_FOUND;


    }

}
