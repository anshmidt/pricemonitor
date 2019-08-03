package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;

import java.util.Random;

public class DebugRandomScraper extends StoreScraper {

    public static final String URL = "stackoverflow.com";
    public static final String TITLE = "debug random";

    public DebugRandomScraper(Context context) {
        super(context, URL, TITLE);
    }

    @Override
    public int extractPriceFromFullResponse(String fullResponse) {

        Random random = new Random();
        return random.nextInt(90)*1000 + 2000;
//        return 50000;


    }
}
