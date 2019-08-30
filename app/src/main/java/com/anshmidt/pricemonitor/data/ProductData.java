package com.anshmidt.pricemonitor.data;

import com.anshmidt.pricemonitor.room.entity.Product;

import java.util.List;

public class ProductData {
    public Product product;
    public List<ItemData> itemDataList;

    public ProductData(Product product, List<ItemData> itemDataList) {
        this.product = product;
        this.itemDataList = itemDataList;
    }
}
