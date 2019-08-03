package com.anshmidt.pricemonitor.data;

import java.util.Date;

public class CurrentPriceInStore {
    public String storeName;
    public int price;
    public Date date;
    public int storeColor;
    public String productInStoreUrl;

    public CurrentPriceInStore(String storeName, int price, Date date, int storeColor, String productInStoreUrl) {
        this.storeName = storeName;
        this.price = price;
        this.date = date;
        this.storeColor = storeColor;
        this.productInStoreUrl = productInStoreUrl;
    }

}
