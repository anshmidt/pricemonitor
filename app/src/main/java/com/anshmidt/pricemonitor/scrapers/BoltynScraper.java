package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoltynScraper extends StoreScraper {

    public static final String URL = "spb.boltyn.ru";
    public static final String TITLE = "boltyn.ru";

    public BoltynScraper(Context context) {
        super(context, URL, TITLE);
    }

    @Override
    public int extractPriceFromFullResponse(String fullResponse) {
        //"priceCurrency": "RUB","price": "52599", availab
        Pattern pattern = Pattern.compile("\"price\": \"(.+)\",");
        Matcher matcher = pattern.matcher(fullResponse);
        String price = "";
        if (matcher.find()) {
            price = matcher.group(1);
        }
        if (!price.isEmpty()) {
            return Integer.parseInt(price);
        } else {
            return PRICE_NOT_FOUND;
        }


    }
}
