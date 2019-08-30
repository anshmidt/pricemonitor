package com.anshmidt.pricemonitor.data;

import com.anshmidt.pricemonitor.exceptions.EmptyDataException;
import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.room.entity.Product;
import com.anshmidt.pricemonitor.room.entity.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataManager {

    private final Comparator<Price> PRICE_BY_DATE_COMPARATOR;
    private final Comparator<Price> PRICE_COMPARATOR;

    public DataManager() {
        PRICE_BY_DATE_COMPARATOR = (a, b) -> a.date.compareTo(b.date);
        PRICE_COMPARATOR = (a, b) -> ((Integer)a.price).compareTo((Integer) b.price);
    }

    public int getRecentPriceValue(ItemData itemData) throws EmptyDataException {
        checkItemDataNotEmpty(itemData);
        List<Price> allItemPrices = itemData.prices;
        Collections.sort(allItemPrices, PRICE_BY_DATE_COMPARATOR);
        Price recentPrice = allItemPrices.get(allItemPrices.size() - 1);
        return recentPrice.price;
    }

    public Date getRecentDate(ItemData itemData) throws EmptyDataException {
        checkItemDataNotEmpty(itemData);
        List<Price> allItemPrices = itemData.prices;
        Collections.sort(allItemPrices, PRICE_BY_DATE_COMPARATOR);
        Price recentPrice = allItemPrices.get(allItemPrices.size() - 1);
        return recentPrice.date;
    }

    private void checkItemDataNotEmpty(ItemData itemData) throws EmptyDataException {
        if ((itemData == null) || (itemData.prices.isEmpty())) {
            throw new EmptyDataException();
        }
    }



    private void checkPriceListNotEmpty(List<Price> priceList) {
        if ((priceList == null) || (priceList.isEmpty())) {
            throw new EmptyDataException();
        }
    }

    public Optional<Date> getMinDate(ProductData productData) {
        List<ItemData> itemDataList = productData.itemDataList;
        return itemDataList
                .stream()
                .map(this::getMinDateForItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Date::compareTo);
    }


    private Optional<Date> getMinDateForItem(ItemData itemData) {
        if (isEmpty(itemData)) {
            return Optional.empty();
        } else {
            return Optional.of(Collections.min(itemData.prices, PRICE_BY_DATE_COMPARATOR).date);
        }
    }


    public Optional<Date> getMaxDate(ProductData productData) throws EmptyDataException {
        List<ItemData> itemDataList = productData.itemDataList;
        return itemDataList
                .stream()
                .map(this::getMaxDateForItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Date::compareTo);
    }

    private Optional<Date> getMaxDateForItem(ItemData itemData) {
        if (isEmpty(itemData)) {
            return Optional.empty();
        } else {
            return Optional.of(Collections.max(itemData.prices, PRICE_BY_DATE_COMPARATOR).date);
        }
    }

    public boolean isEmpty(ProductData productData) {
        if (productData == null) {
            return true;
        }
        if (productData.itemDataList == null) {
            return true;
        }
        if (productData.itemDataList.size() < 1) {
            return true;
        }
        if (!getMinDate(productData).isPresent()) {
            return true;
        }
        return false;
    }

    private boolean isEmpty(ItemData itemData) {
        if (itemData == null) {
            return true;
        }
        if (itemData.prices == null) {
            return true;
        }
        if (itemData.prices.size() < 1) {
            return true;
        }
        return false;
    }

    public List<Date> getSortedDates(List<Price> prices) {
        return prices
                .stream()
                .map(price -> price.date)
                .sorted()
                .collect(Collectors.toList());
    }

    public Price getMinPrice(List<Price> prices) {
        checkPriceListNotEmpty(prices);
        return Collections.min(prices, PRICE_COMPARATOR);
    }

    public Price getMaxPrice(List<Price> prices) {
        checkPriceListNotEmpty(prices);
        return Collections.max(prices, PRICE_COMPARATOR);
    }

    public List<ProductData> addItem(Item newItem, Store itemStore, List<ProductData> productDataList) {
        for (ProductData productData : productDataList) {
            if (productData.product.id == newItem.productId) {
                ItemData newItemData = new ItemData(
                        new ArrayList<Price>(),
                        itemStore,
                        newItem
                );
                productData.itemDataList.add(newItemData);
            }
        }
        return productDataList;
    }

    public List<ProductData> addPrice(Price price, Item item, List<ProductData> productDataList) {
        for (ProductData productData : productDataList) {
            if (productData.product.id == item.productId) {
                List<ItemData> itemDataList = productData.itemDataList;

                for (ItemData itemData : itemDataList) {
                    if (itemData.item.id == price.itemId) {
                        itemData.prices.add(price);
                    }
                }

                productData.itemDataList = itemDataList;
            }
        }
        return productDataList;
    }

    public List<ProductData> addProduct(Product product, List<ProductData> productDataList) {
        ProductData newProductData = new ProductData(product, new ArrayList<ItemData>());
        productDataList.add(newProductData);
        return productDataList;
    }





//    private <V> ArrayList<Date> getKeys(Map<Date, V> map) {
//        Set<Date> keysSet = map.keySet();
//        ArrayList<Date> keysList = new ArrayList<>();
//        keysList.addAll(keysSet);
//        return keysList;
//    }
//
//    private void checkDataNotEmpty(TreeMap<Date, Integer> data) throws EmptyDataException {
//        if ((data == null) || (data.isEmpty())) {
//            throw new EmptyDataException();
//        }
//    }
//
//    public int getLastValue(TreeMap<Date, Integer> data) throws EmptyDataException {
//        checkDataNotEmpty(data);
//        Date lastKey = getSortedKeys(data).get(data.size() - 1);
//        return data.get(lastKey);
//    }
//
//    public int getMaxValue(TreeMap<Date, Integer> data) throws EmptyDataException {
//        checkDataNotEmpty(data);
//        ArrayList<Integer> values = new ArrayList<>(data.values());
//        return Collections.max(values);
//    }
//
//    public int getMinValue(TreeMap<Date, Integer> data) throws EmptyDataException {
//        checkDataNotEmpty(data);
//        ArrayList<Integer> values = new ArrayList<>(data.values());
//        return Collections.min(values);
//
//    }
//
//    public Date getLastKey(TreeMap<Date, Integer> data) throws EmptyDataException {
//        checkDataNotEmpty(data);
//        return getSortedKeys(data).get(data.size() - 1);
//    }
//
//    public ArrayList<Date> getSortedKeys(TreeMap<Date, Integer> data) throws EmptyDataException {
//        checkDataNotEmpty(data);
//        return getKeys(data);
//    }
//
//    public TreeMap<Date, Integer> removeAllElementsWithValue(TreeMap<Date, Integer> data, Integer unwantedValue) {
//        data.values().removeAll(Collections.singleton(unwantedValue));
//        return data;
//    }
//
//    public ArrayList<CurrentPriceInStore> retrieveLastPricesOfProduct(Product product) throws EmptyDataException {
//        ArrayList<ProductInStore> items = product.productInStoreList;
//        ArrayList<CurrentPriceInStore> currentPricesInStore = new ArrayList<>();
//        for (ProductInStore productInStore : items) {
//            int itemId = productInStore.id;
//            String itemUrl = productInStore.url;
//            Store store = productInStore.store;
//            TreeMap<Date, Integer> itemPrices = productInStore.prices;
//
//            checkDataNotEmpty(itemPrices);
//
//            Date lastDate = getLastKey(itemPrices);
//            int lastPrice = getLastValue(itemPrices);
//
//            CurrentPriceInStore currentPriceInStore = new CurrentPriceInStore(store.title, lastPrice, lastDate, store.color, itemUrl);
//            currentPricesInStore.add(currentPriceInStore);
//        }
//        Collections.sort(currentPricesInStore, new Comparator<CurrentPriceInStore>() {
//            @Override
//            public int compare(CurrentPriceInStore o1, CurrentPriceInStore o2) {
//                return o1.price - o2.price;
//            }
//        });
//        return currentPricesInStore;
//    }
//
//    public boolean doAllProductsListContainProduct(String productName, ArrayList<Product> allProducts) {
//        for (Product product : allProducts) {
//            if (productName.equals(product.name)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public Product getProductWithName(String productName, ArrayList<Product> allProducts) {
//        for (Product product : allProducts) {
//            if (productName.equals(product.name)) {
//                return product;
//            }
//        }
//        return null;
//    }
//
//    public Date getMaxDate(ArrayList<ProductInStore> items) {
//        Date maxDate = items.get(0).prices.firstKey();
//        for (ProductInStore item : items) {
//            TreeMap<Date, Integer> prices = item.prices;
//            Date itemMax = getKeys(prices).get(prices.size() - 1);
//            if (itemMax.getTime() > maxDate.getTime()) {
//                maxDate = itemMax;
//            }
//
//        }
//        return maxDate;
//    }
//
//    public Date getMinDate(ArrayList<ProductInStore> items) {
//        Date minDate = new Date();
//        for (ProductInStore item : items) {
//            TreeMap<Date, Integer> prices = item.prices;
//            Date itemMin = getKeys(prices).get(0);
//            if (itemMin.getTime() < minDate.getTime()) {
//                minDate = itemMin;
//            }
//        }
//        return minDate;
//    }

//    public int getStoreColor(int storeId, int[] storesColors) {
//        int colorsCount = storesColors.length;
//        int colorId = storeId % colorsCount;
//        return storesColors[colorId];
//    }



}
