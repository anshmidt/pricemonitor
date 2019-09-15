package com.anshmidt.pricemonitor.data;

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

    public Optional<Integer> getLatestPriceValue(ItemData itemData) {
        if (isEmpty(itemData)) {
            return Optional.empty();
        }
        List<Price> allItemPrices = itemData.prices;
        Collections.sort(allItemPrices, PRICE_BY_DATE_COMPARATOR);
        Price latestPrice = allItemPrices.get(allItemPrices.size() - 1);
        return Optional.of(latestPrice.price);
    }

    public Optional<Date> getLatestDate(ItemData itemData) {
        if (isEmpty(itemData)) {
            return Optional.empty();
        }
        List<Price> allItemPrices = itemData.prices;
        Collections.sort(allItemPrices, PRICE_BY_DATE_COMPARATOR);
        Price latestPrice = allItemPrices.get(allItemPrices.size() - 1);
        return Optional.of(latestPrice.date);
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


    public Optional<Date> getMaxDate(ProductData productData) {
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








}
