package com.anshmidt.pricemonitor;

import com.anshmidt.pricemonitor.data.DataManager;
import com.anshmidt.pricemonitor.data.ItemData;
import com.anshmidt.pricemonitor.data.ProductData;
import com.anshmidt.pricemonitor.exceptions.EmptyDataException;
import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.room.entity.Product;
import com.anshmidt.pricemonitor.room.entity.Store;
import com.anshmidt.pricemonitor.scrapers.BoltynScraper;
import com.anshmidt.pricemonitor.scrapers.EcoDriftScraper;
import com.anshmidt.pricemonitor.scrapers.YandexMarketMinScraper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class DataManagerTest {

    private DataManager dataManager;

    Date todayDate;
    Date yesterdayDate;
    Date weekAgoDate;
    Date monthAgoDate;
    Date tomorrowDate;

//    private class StoreStub extends Store {
//        public StoreStub(String url, String title, int id, int color) {
//            super(url, title, id, color);
//        }
//
//        public StoreStub(int id) {
//            super("url" + id + ".com", "store title " + id, id, 5);
//        }
//    }

//    private class CurrentPriceInStoreStub extends CurrentPriceInStore {
//        public CurrentPriceInStoreStub(String storeName, int price, Date date, int storeColor, String productInStoreUrl) {
//            super(storeName, price, date, storeColor, productInStoreUrl);
//        }
//    }

    @Before
    public void setUp() {
        dataManager = new DataManager();
        todayDate = new Date(System.currentTimeMillis());
        yesterdayDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        weekAgoDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        monthAgoDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        tomorrowDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
    }

    private List<Price> getPriceListForHappyPath() {
        int productId = 12;

        Store store1 = new Store(24, YandexMarketMinScraper.NAME, YandexMarketMinScraper.URL);

        String item1Url = "https://market.yandex.ru/product--gornyi-gibrid-author-kinetic-2019/283568350";
        Item item1 = new Item(22, item1Url, productId, store1.id);
        List<Price> prices1 = new ArrayList<>();
        prices1.add(new Price(400, monthAgoDate, item1.id, 21000));
        prices1.add(new Price(404, yesterdayDate, item1.id, 28000));
        prices1.add(new Price(406, todayDate, item1.id, 25000));
        return prices1;
    }

    private Item getItemForAddItem() {
        int itemId = 56;
        String itemUrl = "https://spb.boltyn.ru/product/34567211";
        int productId = 7;
        int storeId = 44;
        return new Item(itemId, itemUrl, productId, storeId);
    }

    private Store getStoreForAddItem() {
        return new Store(44, BoltynScraper.NAME, BoltynScraper.URL);
    }

    private Price getPriceForAddPrice() {
        return new Price(4012, tomorrowDate, 22, 30000);
    }

    private Item getItemForAddPrice() {
        return getProductDataForHappyPath().itemDataList.get(0).item;
    }

    private Product getProductForAddProduct() {
        return new Product(77, "Scott Fusion");
    }

    private ProductData getProductDataForHappyPath() {
        int productId = 12;
        String productName = "Author Kinetic";

        Store store1 = new Store(24, YandexMarketMinScraper.NAME, YandexMarketMinScraper.URL);
        Store store2 = new Store(35, EcoDriftScraper.NAME, EcoDriftScraper.URL);

        String item1Url = "https://market.yandex.ru/product--gornyi-gibrid-author-kinetic-2019/283568350";
        String item2Url = "https://ecodrift.ru/product/author-kinetic";
        Item item1 = new Item(22, item1Url, productId, store1.id);
        Item item2 = new Item(25, item2Url, productId, store2.id);


        List<Price> prices1 = new ArrayList<>();
        List<Price> prices2 = new ArrayList<>();
        prices1.add(new Price(400, monthAgoDate, item1.id, 21000));
        prices1.add(new Price(404, yesterdayDate, item1.id, 28000));
        prices1.add(new Price(406, todayDate, item1.id, 25000));

        prices2.add(new Price(401, weekAgoDate, item2.id, 24001));
        prices2.add(new Price(403, yesterdayDate, item2.id, 26001));
        prices2.add(new Price(408, todayDate, item2.id, 28001));


        ItemData itemData1 = new ItemData(prices1, store1, item1);
        ItemData itemData2 = new ItemData(prices2, store2, item2);
        List<ItemData> itemDataList = new ArrayList<>();
        itemDataList.add(itemData1);
        itemDataList.add(itemData2);

        Product product = new Product(productId, productName);

        ProductData productData = new ProductData(product, itemDataList);
        return productData;
    }

    private ProductData getProductDataForSecondHappyPath() {
        int productId = 7;
        String productName = "iPhone X";

        Store store1 = new Store(24, YandexMarketMinScraper.NAME, YandexMarketMinScraper.URL);

        String item1Url = "https://market.yandex.ru/product--iphone-x-256/3243250";
        Item item1 = new Item(9, item1Url, productId, store1.id);

        List<Price> prices1 = new ArrayList<>();
        prices1.add(new Price(300, weekAgoDate, item1.id, 96000));
        prices1.add(new Price(301, yesterdayDate, item1.id, 95000));
        prices1.add(new Price(302, todayDate, item1.id, 94000));

        ItemData itemData1 = new ItemData(prices1, store1, item1);
        List<ItemData> itemDataList = new ArrayList<>();
        itemDataList.add(itemData1);

        Product product = new Product(productId, productName);

        return new ProductData(product, itemDataList);
    }

    private ProductData getProductDataIfOneItemEmpty() {
        int productId = 12;
        String productName = "Author Kinetic";

        Store store1 = new Store(24, YandexMarketMinScraper.NAME, YandexMarketMinScraper.URL);
        Store store2 = new Store(35, EcoDriftScraper.NAME, EcoDriftScraper.URL);

        String item1Url = "https://market.yandex.ru/product--gornyi-gibrid-author-kinetic-2019/283568350";
        String item2Url = "https://ecodrift.ru/product/author-kinetic";
        Item item1 = new Item(22, item1Url, productId, store1.id);
        Item item2 = new Item(25, item2Url, productId, store2.id);


        List<Price> prices1 = new ArrayList<>();
        List<Price> prices2 = new ArrayList<>();
        prices2.add(new Price(401, weekAgoDate, item2.id, 24001));
        prices2.add(new Price(403, yesterdayDate, item2.id, 26001));
        prices2.add(new Price(408, todayDate, item2.id, 28001));

        ItemData itemData1 = new ItemData(prices1, store1, item1);
        ItemData itemData2 = new ItemData(prices2, store2, item2);
        List<ItemData> itemDataList = new ArrayList<>();
        itemDataList.add(itemData1);
        itemDataList.add(itemData2);

        Product product = new Product(productId, productName);

        ProductData productData = new ProductData(product, itemDataList);
        return productData;
    }

    private ProductData getProductDataEmptyPrices() {
        int productId = 12;
        String productName = "Author Kinetic";

        Store store1 = new Store(24, YandexMarketMinScraper.NAME, YandexMarketMinScraper.URL);
        Store store2 = new Store(35, EcoDriftScraper.NAME, EcoDriftScraper.URL);

        String item1Url = "https://market.yandex.ru/product--gornyi-gibrid-author-kinetic-2019/283568350";
        String item2Url = "https://ecodrift.ru/product/author-kinetic";
        Item item1 = new Item(22, item1Url, productId, store1.id);
        Item item2 = new Item(25, item2Url, productId, store2.id);


        List<Price> prices1 = new ArrayList<>();
        List<Price> prices2 = new ArrayList<>();

        ItemData itemData1 = new ItemData(prices1, store1, item1);
        ItemData itemData2 = new ItemData(prices2, store2, item2);
        List<ItemData> itemDataList = new ArrayList<>();
        itemDataList.add(itemData1);
        itemDataList.add(itemData2);

        Product product = new Product(productId, productName);

        ProductData productData = new ProductData(product, itemDataList);
        return productData;
    }

    private List<ProductData> getProductDataListForHappyPath() {
        List<ProductData> productDataList = new ArrayList<>();
        productDataList.add(getProductDataForHappyPath());
        productDataList.add(getProductDataForSecondHappyPath());
        return productDataList;
    }

//    private ItemData getItemData(Item item, List<ProductData> productDataList) {
//        for (ProductData productData : productDataList) {
//            if (productData.product.id == item.productId) {
//                List<ItemData> itemDataList = productData.itemDataList;
//
//                for (ItemData itemData : itemDataList) {
//                    if (itemData.item.equals(item)) {
//                        return itemData;
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    private ProductData getProductData(Product product, List<ProductData> productDataList) {
//        for (ProductData productData : productDataList) {
//            if (productData.product.equals(product)) {
//                return productData;
//            }
//        }
//        return null;
//    }
//
//    private ItemData getItemData(Price price, List<ProductData> productDataList) {
//        for (ProductData productData : productDataList) {
//            for (ItemData itemData : productData.itemDataList) {
//                if (itemData.prices.contains(price)) {
//                    return itemData;
//                }
//            }
//        }
//        return null;
//    }

    @Test
    public void getMaxDate_HappyPath() {
        ProductData productData = getProductDataForHappyPath();
        Date actualDate = dataManager.getMaxDate(productData).get();
        Date expectedDate = todayDate;
        Assert.assertEquals(expectedDate, actualDate);
    }

    @Test()
    public void getMaxDate_NoData() {
        Assert.assertTrue(!dataManager.getMaxDate(getProductDataEmptyPrices()).isPresent());
    }

    @Test
    public void getMinDate_HappyPath() {
        ProductData productData = getProductDataForHappyPath();
        Date actualDate = dataManager.getMinDate(productData).get();
        Date expectedDate = monthAgoDate;
        Assert.assertEquals(expectedDate, actualDate);
    }

    @Test()
    public void getMinDate_NoData() {
        Assert.assertTrue(!dataManager.getMinDate(getProductDataEmptyPrices()).isPresent());
    }

    @Test
    public void getMinDate_OneItemEmpty() {
        ProductData productData = getProductDataIfOneItemEmpty();
        Date actualDate = dataManager.getMinDate(productData).get();
        Date expectedDate = weekAgoDate;
        Assert.assertEquals(expectedDate, actualDate);
    }

    @Test
    public void getMinPrice_HappyPath() {
        List<Price> prices = getPriceListForHappyPath();
        Price actualPrice = dataManager.getMinPrice(prices);
        Price expectedPrice = prices.get(0);
        Assert.assertEquals(expectedPrice, actualPrice);
    }

    @Test
    public void getMaxPrice_HappyPath() {
        List<Price> prices = getPriceListForHappyPath();
        Price actualPrice = dataManager.getMaxPrice(prices);
        Price expectedPrice = prices.get(1);
        Assert.assertEquals(expectedPrice, actualPrice);
    }

    @Test(expected = EmptyDataException.class)
    public void getMinPrice_NoData() throws EmptyDataException {
        dataManager.getMinPrice(new ArrayList<>());
    }

    @Test
    public void addItem_HappyPath() {
        List<ProductData> productDataList = getProductDataListForHappyPath();
        Item newItem = getItemForAddItem();
        Store store = getStoreForAddItem();

        List<ProductData> productDataListWithAddedItem = dataManager.addItem(newItem, store, productDataList);

        ProductData productDataWithAddedItem = productDataListWithAddedItem
                .stream()
                .filter(productData -> productData.product.id == 7)
                .findFirst()
                .get();

        Assert.assertEquals(productDataWithAddedItem.itemDataList.size(), 2);
        Assert.assertEquals(productDataWithAddedItem.product.name, "iPhone X");
    }

    @Test
    public void addPrice_HappyPath() {
        List<ProductData> productDataList = getProductDataListForHappyPath();

        List<ProductData> productDataListWithAddedPrice = dataManager.addPrice(
                getPriceForAddPrice(),
                getItemForAddPrice(),
                productDataList);

        ItemData itemData = productDataListWithAddedPrice.get(0).itemDataList.get(0);
        Assert.assertEquals(getPriceForAddPrice().price, dataManager.getMaxPrice(itemData.prices).price);
    }

    @Test
    public void addProduct_HappyPath() {
        List<ProductData> productDataList = getProductDataListForHappyPath();
        List<ProductData> productDataListWithAddedProduct = dataManager.addProduct(
                getProductForAddProduct(),
                productDataList);

        Assert.assertEquals(3, productDataListWithAddedProduct.size());
        Assert.assertEquals(getProductForAddProduct().name, productDataListWithAddedProduct.get(2).product.name);
    }











}
