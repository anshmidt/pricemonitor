package com.anshmidt.pricemonitor.data;

import android.content.Context;

public class Store {
    public String url;
    public String title;
    public int id;
    public int color;

    public Store(String url, String title, int id, int color) {
        this.url = url;
        this.title = title;
        this.id = id;
        this.color = color;
    }

    public static String extractStoreUrl(String itemUrl) {
        //example itemUrl: https://market.yandex.ru/product--smartfon-huawei-mate-20x-128gb/385696006
        //expected storeUrl: market.yandex.ru

        String itemUrlWithoutHttp;
        if ( (itemUrl.contains("http://")) || (itemUrl.contains("https://")) ) {
            itemUrlWithoutHttp = itemUrl.split("/")[2];
        } else {
            itemUrlWithoutHttp = itemUrl;
        }

        String storeUrl = itemUrlWithoutHttp.split("/")[0];

        //check for mobile version
        if (storeUrl.startsWith("m.")) {
            storeUrl = storeUrl.substring(2, storeUrl.length());
        }

        return storeUrl;
    }






}
