package com.anshmidt.pricemonitor.data;

import java.util.ArrayList;

public class Product {
    public String name;
    public ArrayList<ProductInStore> productInStoreList;

    public Product(String name, ArrayList<ProductInStore> productInStoreList) {
        this.name = name;
        this.productInStoreList = productInStoreList;
    }
}
