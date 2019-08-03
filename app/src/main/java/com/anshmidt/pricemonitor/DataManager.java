package com.anshmidt.pricemonitor;

import com.anshmidt.pricemonitor.data.CurrentPriceInStore;
import com.anshmidt.pricemonitor.data.Product;
import com.anshmidt.pricemonitor.data.ProductInStore;
import com.anshmidt.pricemonitor.data.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DataManager {

    private <V> ArrayList<Date> getKeys(Map<Date, V> map) {
        Set<Date> keysSet = map.keySet();
        ArrayList<Date> keysList = new ArrayList<>();
        keysList.addAll(keysSet);
        return keysList;
    }

    public int getLastValue(TreeMap<Date, Integer> data) {
        if ((data != null) && (!data.isEmpty())) {
            Date lastKey = getSortedKeys(data).get(data.size() - 1);
            return data.get(lastKey);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int getMaxValue(TreeMap<Date, Integer> data) {
        ArrayList<Integer> values = new ArrayList<>(data.values());
        if ((data != null) && (!data.isEmpty())) {
            int maxValue = values.get(0);
            for (int value : values) {
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        } else {
            throw new RuntimeException("Data is empty or null");
        }
    }

    public int getMinValue(TreeMap<Date, Integer> data) {
        ArrayList<Integer> values = new ArrayList<>(data.values());
        if ((data != null) && (!data.isEmpty())) {
            int minValue = values.get(0);
            for (int value : values) {
                if (value < minValue) {
                    minValue = value;
                }
            }
            return minValue;
        } else {
            throw new RuntimeException("Data is empty or null");
        }
    }



    public Date getLastKey(TreeMap<Date, Integer> data) {
        if ((data != null) && (!data.isEmpty())) {
            return getSortedKeys(data).get(data.size() - 1);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public ArrayList<Date> getSortedKeys(TreeMap<Date, Integer> data) {
        return getKeys(data);
    }

    public TreeMap<Date, Integer> removeAllElementsWithValue(TreeMap<Date, Integer> data, Integer unwantedValue) {
        data.values().removeAll(Collections.singleton(unwantedValue));
        return data;
    }

    public ArrayList<CurrentPriceInStore> retrieveLastPricesOfProduct(Product product) {
        ArrayList<ProductInStore> items = product.productInStoreList;
        ArrayList<CurrentPriceInStore> currentPricesInStore = new ArrayList<>();
        for (ProductInStore productInStore : items) {
            int itemId = productInStore.id;
            String itemUrl = productInStore.url;
            Store store = productInStore.store;
            TreeMap<Date, Integer> itemPrices = productInStore.prices;

            Date lastDate = getLastKey(itemPrices);
            int lastPrice = getLastValue(itemPrices);

            CurrentPriceInStore currentPriceInStore = new CurrentPriceInStore(store.title, lastPrice, lastDate, store.color, itemUrl);
            currentPricesInStore.add(currentPriceInStore);
        }
        Collections.sort(currentPricesInStore, new Comparator<CurrentPriceInStore>() {
            @Override
            public int compare(CurrentPriceInStore o1, CurrentPriceInStore o2) {
                return o1.price - o2.price;
            }
        });
        return currentPricesInStore;
    }

    public boolean doAllProductsListContainProduct(String productName, ArrayList<Product> allProducts) {
        for (Product product : allProducts) {
            if (productName.equals(product.name)) {
                return true;
            }
        }
        return false;
    }

    public Product getProductWithName(String productName, ArrayList<Product> allProducts) {
        for (Product product : allProducts) {
            if (productName.equals(product.name)) {
                return product;
            }
        }
        return null;
    }

    public Date getMaxDate(ArrayList<ProductInStore> items) {
        Date maxDate = items.get(0).prices.firstKey();
        for (ProductInStore item : items) {
            TreeMap<Date, Integer> prices = item.prices;
            Date itemMax = getKeys(prices).get(prices.size() - 1);
            if (itemMax.getTime() > maxDate.getTime()) {
                maxDate = itemMax;
            }

        }
        return maxDate;
    }

    public Date getMinDate(ArrayList<ProductInStore> items) {
        Date minDate = new Date();
        for (ProductInStore item : items) {
            TreeMap<Date, Integer> prices = item.prices;
            Date itemMin = getKeys(prices).get(0);
            if (itemMin.getTime() < minDate.getTime()) {
                minDate = itemMin;
            }
        }
        return minDate;
    }

    public int getStoreColor(int storeId, int[] storesColors) {
        int colorsCount = storesColors.length;
        int colorId = storeId % colorsCount;
        return storesColors[colorId];
    }



}
