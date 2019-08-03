package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YandexMarketMinScraper extends StoreScraper {

    public static final String URL = "market.yandex.ru";
    public static final String TITLE = "ya.market min";

    public YandexMarketMinScraper(Context context) {
        super(context, URL, TITLE);
    }

    @Override
    public int extractPriceFromFullResponse(String fullResponse) {

        Pattern pattern = Pattern.compile("\"prices\":\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(fullResponse);
        String priceLine = "";
        if (matcher.find()) {
            priceLine = matcher.group(1);  // "min":"53990","max":"55487","currency":"RUR","avg":"55485"
        }

        pattern = Pattern.compile("\"min\":\"(\\d+)\",");
        matcher = pattern.matcher(priceLine);
        String price = "";
        if (matcher.find()) {
            price = matcher.group(1);
            return Integer.parseInt(price);
        } else {
            return PRICE_NOT_FOUND;
        }
    }
}
