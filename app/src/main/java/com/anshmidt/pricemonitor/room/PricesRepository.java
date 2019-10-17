package com.anshmidt.pricemonitor.room;

import android.database.sqlite.SQLiteException;
import android.os.SystemClock;
import android.util.Log;

import com.anshmidt.pricemonitor.data.ItemData;
import com.anshmidt.pricemonitor.data.ProductData;
import com.anshmidt.pricemonitor.room.dao.ItemDao;
import com.anshmidt.pricemonitor.room.dao.PriceDao;
import com.anshmidt.pricemonitor.room.dao.ProductDao;
import com.anshmidt.pricemonitor.room.dao.StoreDao;
import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.room.entity.Product;
import com.anshmidt.pricemonitor.room.entity.Store;
import com.anshmidt.pricemonitor.scrapers.BoltynScraper;
import com.anshmidt.pricemonitor.scrapers.DebugRandomScraper;
import com.anshmidt.pricemonitor.scrapers.Digital812Scraper;
import com.anshmidt.pricemonitor.scrapers.EcoDriftScraper;
import com.anshmidt.pricemonitor.scrapers.TKitScraper;
import com.anshmidt.pricemonitor.scrapers.YandexMarketMinScraper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.schedulers.Schedulers;


/**
 * Data:
 * Product:
 *      List<Item>
*
 * Item:
 *      Store
 *      List<Price>
 */

@Singleton
public class PricesRepository {
    private final ItemDao itemDao;
    private final PriceDao priceDao;
    private final ProductDao productDao;
    private final StoreDao storeDao;
    private final String LOG_TAG = PricesRepository.class.getSimpleName();

    @Inject
    public PricesRepository(ItemDao itemDao, PriceDao priceDao, ProductDao productDao, StoreDao storeDao) {
        this.itemDao = itemDao;
        this.priceDao = priceDao;
        this.productDao = productDao;
        this.storeDao = storeDao;
    }

    public void addStoresIfNotPresent() {
        addStoreIfNotPresent(new Store(YandexMarketMinScraper.NAME, YandexMarketMinScraper.URL));
        addStoreIfNotPresent(new Store(TKitScraper.NAME, TKitScraper.URL));
        addStoreIfNotPresent(new Store(Digital812Scraper.NAME, Digital812Scraper.URL));
        addStoreIfNotPresent(new Store(EcoDriftScraper.NAME, EcoDriftScraper.URL));
        addStoreIfNotPresent(new Store(DebugRandomScraper.NAME, DebugRandomScraper.URL));
        addStoreIfNotPresent(new Store(BoltynScraper.NAME, BoltynScraper.URL));
    }

    private void addStoreIfNotPresent(Store store) {
        Integer storeId = storeDao.getStoreIdByName(store.name);
        if (storeId == null) {
            storeDao.insert(store);
        }
    }

    public int addItemIfNotExists(Item newItem) {
        Integer itemId = itemDao.getItemId(newItem.url);
        if (itemId == null) {
            return (int) itemDao.insert(newItem);
        } else {
            return itemId;
        }
    }

    public int addProductIfNotExists(Product newProduct) {
        Product product = productDao.getProductByName(newProduct.name);
        if (product == null) {
            return (int) productDao.insert(newProduct);
        } else {
            return product.id;
        }
    }

    public void deleteAllStores() {
        storeDao.deleteAllStores();
    }

    public List<Store> getAllStores() {
        return storeDao.getAllStores();
    }

    public Store getStoreByUrl(String storeUrl) {
        return storeDao.getStoreByUrl(storeUrl);
    }

    public List<Item> getAllItems() {
        return itemDao.getAllItems();
    }

    public Product getProductByName(String productName) {
        return productDao.getProductByName(productName);
    }

    public int addPrice(Price price) {
        return (int) priceDao.insert(price);
    }

    public Item getItemByUrl(String itemUrl) {
        return itemDao.getItemByUrl(itemUrl);
    }

    public Item getItemById(int itemId) {
        return itemDao.getItemById(itemId);
    }

    public Price getLatestPriceForItem(int itemId) {
        return priceDao.getLatestPriceForItem(itemId);
    }

    public Price getPreviousPriceForItem(int itemId) {
        return priceDao.getPreviousPriceForItem(itemId);
    }

    public Product getProductByProductId(int productId) {
        return productDao.getProductById(productId);
    }

    public Store getStoreByStoreId(int storeId) {
        return storeDao.getStoreById(storeId);
    }

    public void deleteLatestPriceForEachItem() {
        List<Item> items = getAllItems();
        for (Item item : items) {
            Price priceToDelete = priceDao.getLatestPriceForItem(item.id);
            priceDao.delete(priceToDelete);
        }
    }

    public void deleteProduct(String productName) {
        productDao.deleteProductByName(productName);
    }

    public void deleteAllItemsForProductName(String productName) {
        int productId = productDao.getProductByName(productName).id;
        itemDao.deleteAllItemsWithProductId(productId);
    }

    public void deleteAllPricesForProductName(String productName) {
        int productId = productDao.getProductByName(productName).id;
        List<Item> items = itemDao.getItemsByProductId(productId);
        for (Item item : items) {
            priceDao.deleteAllPricesForItem(item.id);
        }
    }


    public void clearAllTables() {
        Log.d(LOG_TAG, "Clearing all tables");
        storeDao.deleteAllStores();
        productDao.deleteAllProducts();
        priceDao.deleteAllPrices();
        itemDao.deleteAllItems();
        Log.d(LOG_TAG, "All tables cleared");
    }

    public void fillDbWithTestData() {
        addTestProductSmartphone();
        addTestProductWithRandomPrice();
    }

    private void addTestProductSmartphone() {
        final String PHONE_PRODUCT_NAME = "LG G7";
        int phoneProductId = (int) productDao.insert(new Product(PHONE_PRODUCT_NAME));

        int yandexMarketStoreId = storeDao.getStoreIdByUrl(YandexMarketMinScraper.URL);
        final String PHONE_ON_YANDEX_MARKET_URL = "https://"+YandexMarketMinScraper.URL+"/product--smartfon-lg-g7-thinq-64gb/41449031";
        int phoneOnYandexMarketItemId = (int) itemDao.insert(new Item(
                PHONE_ON_YANDEX_MARKET_URL,
                phoneProductId,
                yandexMarketStoreId
        ));


        Date todayDate = new Date(System.currentTimeMillis());
        Date yesterdayDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        int todayPriceValue = 10000;
        int yesterdayPriceValue = 15000;

        priceDao.insert(new Price(yesterdayDate, phoneOnYandexMarketItemId, yesterdayPriceValue));
        priceDao.insert(new Price(todayDate, phoneOnYandexMarketItemId, todayPriceValue));
    }

    private void addTestProductWithRandomPrice() {
        final String PHONE_PRODUCT_NAME = "Random price product";
        int productId = (int) productDao.insert(new Product(PHONE_PRODUCT_NAME));

        int storeId = storeDao.getStoreIdByUrl(DebugRandomScraper.URL);
        final String ITEM_URL = "https://"+DebugRandomScraper.URL;
        int itemId = (int) itemDao.insert(new Item(
                ITEM_URL,
                productId,
                storeId
        ));


        Date todayDate = new Date(System.currentTimeMillis());
        Date weekAgoDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        int todayPriceValue = 20000;
        int weekAgoPriceValue = 50000;

        priceDao.insert(new Price(weekAgoDate, itemId, weekAgoPriceValue));
        priceDao.insert(new Price(todayDate, itemId, todayPriceValue));
    }

//    public List<Price> getPricesForItem(int itemId) {
//
//    }



    public void printDatabaseToLog() {
        final String LOG_DELIMITER = "-----------------------------------------";
        Log.d(LOG_TAG, LOG_DELIMITER + "\n"
                + PricesDatabase.DATABASE_NAME + "\n"
                + LOG_DELIMITER);

        for (Item item : itemDao.getAllItems()) {
            Log.d(LOG_TAG, item.toString());
        }
        Log.d(LOG_TAG, LOG_DELIMITER);

        for (Price price : priceDao.getAllPrices()) {
            Log.d(LOG_TAG, price.toString());
        }
        Log.d(LOG_TAG, LOG_DELIMITER);

        for (Product product : productDao.getAllProducts()) {
            Log.d(LOG_TAG, product.toString());
        }
        Log.d(LOG_TAG, LOG_DELIMITER);

        for (Store store : storeDao.getAllStores()) {
            Log.d(LOG_TAG, store.toString());
        }
        Log.d(LOG_TAG, LOG_DELIMITER);
    }

    public void printAllItemsAsyncronously() {
        Log.d(LOG_TAG, "^^^^^^^^^^^^^ \n ITEMS async: ");
        itemDao.getAllItemsObs()
                .subscribeOn(Schedulers.io())
                .subscribe(listOfItems -> printListOfItems(listOfItems))
        ;
    }

    // for debugging
    private void printListOfItems(List<Item> itemList) {
        for (Item item : itemList) {
            SystemClock.sleep(5000); //imitation of long running operation

            Log.d(LOG_TAG, "Thread: " + Thread.currentThread());
            Log.d(LOG_TAG, item.toString());
        }
    }

    public List<ProductData> getAllProductData() {
        List<Product> productList = productDao.getAllProducts();
        List<ProductData> productDataList = new ArrayList<>();
        for (Product product : productList) {
            List<Item> itemsForProduct = itemDao.getItemsByProductId(product.id);

            List<ItemData> itemDataList = new ArrayList<>();

            for (Item item : itemsForProduct) {
                int storeId = item.storeId;
                int itemId = item.id;

                Store store = storeDao.getStoreById(storeId);

                List<Price> itemPrices = priceDao.getAllPricesForItem(itemId);

                ItemData itemData = new ItemData(itemPrices, store, item);
                itemDataList.add(itemData);
            }

            ProductData productData = new ProductData(product, itemDataList);
            productDataList.add(productData);
        }
        return productDataList;
    }

}
