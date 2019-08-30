package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EcoDriftScraper extends StoreScraper {


    public static final String URL = "ecodrift.ru";
    public static final String NAME = "ecodrift";

    public EcoDriftScraper(Context context) {
        super(context, URL, NAME);
    }

    @Override
    public int extractPriceFromFullResponse(String fullResponse) {

        Document doc = Jsoup.parse(fullResponse);

        Elements priceSpans = doc.select("span.woocommerce-Price-amount.amount");
        //<span class="woocommerce-Price-amount amount">66,900<span class="woocommerce-Price-currencySymbol">₽</span></span>

        for (Element priceSpan : priceSpans) {
            String resultString = priceSpan.text();
            String resultStringWithoutOddChars = resultString.replaceAll("[^0-9]", "");
            return Integer.parseInt(resultStringWithoutOddChars);
        }
        return PRICE_NOT_FOUND;


    }
}
