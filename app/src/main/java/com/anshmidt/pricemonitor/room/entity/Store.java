package com.anshmidt.pricemonitor.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = Store.TABLE_NAME)
public class Store {
    public static final String TABLE_NAME = "stores";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";

    @ColumnInfo(name = KEY_ID)
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = KEY_NAME)
    public String name;

    @ColumnInfo(name = KEY_URL)
    public String url;

    // for inserting only
    @Ignore
    public Store(String name, String url) {
        this.name = name;
        this.url = url;
    }


    public Store(int id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
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
