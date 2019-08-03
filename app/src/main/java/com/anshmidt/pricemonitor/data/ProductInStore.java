package com.anshmidt.pricemonitor.data;

import java.util.Date;
import java.util.TreeMap;

public class ProductInStore {
    public int id;
    public String url;
    public Store store;
    public TreeMap<Date, Integer> prices;

    public ProductInStore(int id, String url, Store store, TreeMap<Date, Integer> prices) {
        this.id = id;
        this.url = url;
        this.store = store;
        this.prices = prices;
    }
}
