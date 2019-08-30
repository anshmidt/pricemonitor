package com.anshmidt.pricemonitor.data;

import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.room.entity.Store;

import java.util.List;

public class ItemData {
    public List<Price> prices;
    public Store store;
    public Item item;

    public ItemData(List<Price> prices, Store store, Item item) {
        this.prices = prices;
        this.store = store;
        this.item = item;
    }
}
