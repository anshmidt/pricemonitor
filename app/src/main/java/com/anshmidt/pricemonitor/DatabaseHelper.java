package com.anshmidt.pricemonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.anshmidt.pricemonitor.data.Product;
import com.anshmidt.pricemonitor.data.ProductInStore;
import com.anshmidt.pricemonitor.data.Store;
import com.anshmidt.pricemonitor.scrapers.BoltynScraper;
import com.anshmidt.pricemonitor.scrapers.DebugRandomScraper;
import com.anshmidt.pricemonitor.scrapers.Digital812Scraper;
import com.anshmidt.pricemonitor.scrapers.EcoDriftScraper;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;
import com.anshmidt.pricemonitor.scrapers.TKitScraper;
import com.anshmidt.pricemonitor.scrapers.YandexMarketMinScraper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "prices";

    private static final String PRICES_TABLE_NAME = "prices";
    private static final String KEY_PRICE = "price";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_RECORD_ID = "record_id";

    private static final String ITEMS_TABLE_NAME = "items";
    private static final String KEY_ITEM_ID = "item_id";
    private static final String KEY_ITEM_TITLE = "item_title";
    private static final String KEY_ITEM_URL = "item_url";

    private static final String STORES_TABLE_NAME = "stores";
    private static final String KEY_STORE_ID = "store_id";
    private static final String KEY_STORE_TITLE = "store_title";
    private static final String KEY_STORE_URL = "store_url";
    private static final int STORE_NOT_FOUND_ID = -1;
    private static final int ITEM_NOT_FOUND_ID  = -1;


    private SQLiteDatabase db;

    private final String LOG_TAG = DatabaseHelper.class.getSimpleName();
    private Context context;
    private DataManager dataManager;

    @Inject
    public DatabaseHelper(Context context, DataManager dataManager) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.db = this.getWritableDatabase();  //so db is opened only once
        this.dataManager = dataManager;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRICES_TABLE = "CREATE TABLE IF NOT EXISTS " + PRICES_TABLE_NAME + " ("
                + KEY_RECORD_ID + " INTEGER PRIMARY KEY, "
                + KEY_ITEM_ID + " INTEGER, "
                + KEY_PRICE + " INTEGER, "
                + KEY_TIMESTAMP + " INTEGER)";

        String CREATE_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE_NAME + " ("
                + KEY_ITEM_ID + " INTEGER PRIMARY KEY, "  // SQLite points this column to ROWID column
                + KEY_STORE_ID + " INTEGER, "
                + KEY_ITEM_TITLE + " TEXT, "
                + KEY_ITEM_URL + " TEXT, "
                + "FOREIGN KEY(" + KEY_ITEM_ID + ") REFERENCES "
                + PRICES_TABLE_NAME + "(" + KEY_ITEM_ID + "))";

        String CREATE_STORES_TABLE = "CREATE TABLE IF NOT EXISTS " + STORES_TABLE_NAME + " ("
                + KEY_STORE_ID + " INTEGER PRIMARY KEY, "  // SQLite points this column to ROWID column
                + KEY_STORE_TITLE + " TEXT, "
                + KEY_STORE_URL + " TEXT, "
                + "FOREIGN KEY(" + KEY_STORE_ID + ") REFERENCES "
                + ITEMS_TABLE_NAME + "(" + KEY_STORE_ID + "))";

        db.execSQL(CREATE_PRICES_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
        db.execSQL(CREATE_STORES_TABLE);

        addAllStores();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void addAllStores() {
        addStoreIfNotExists(YandexMarketMinScraper.TITLE, YandexMarketMinScraper.URL);
        addStoreIfNotExists(TKitScraper.TITLE, TKitScraper.URL);
        addStoreIfNotExists(Digital812Scraper.TITLE, Digital812Scraper.URL);
        addStoreIfNotExists(EcoDriftScraper.TITLE, EcoDriftScraper.URL);
        addStoreIfNotExists(DebugRandomScraper.TITLE, DebugRandomScraper.URL);
        addStoreIfNotExists(BoltynScraper.TITLE, BoltynScraper.URL);
    }

    // for debug purposes
    private void addAllItems() {
        addItemIfNotExists("Huawei Mate 20X","https://market.yandex.ru/product--smartfon-huawei-mate-20x-128gb/385696006");
        addItemIfNotExists("Huawei Mate 20X", "https://t-kit.ru/collection/huawei-mate-20-x/product/huawei-mate-20-x-6128gb-midnight-blue-2");
        addItemIfNotExists("Huawei Mate 20X","http://digital812.su/huawei-mate-20-x-128gb-blue-polunochnyj-sinij-eu");
        addItemIfNotExists("KingSong KS16S","https://ecodrift.ru/product/monokoleso-kingsong-ks16s-840wh-rubber-black/");
        addItemIfNotExists("Test item 1", "https://stackoverflow.com/questions/50761374/how-does-workmanager-schedule-get-requests-to-rest-api");
    }



    public void clearPricesAndItems() {
//        db.execSQL("DROP TABLE IF EXISTS " + PRICES_TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + STORES_TABLE_NAME);
//        onCreate(db); //in case there is no tables

        db.execSQL("DELETE FROM " + PRICES_TABLE_NAME);
        db.execSQL("DELETE FROM " + ITEMS_TABLE_NAME);
        db.execSQL("DELETE FROM " + STORES_TABLE_NAME);
    }

    public void recreateDb() {
        db.execSQL("DROP TABLE IF EXISTS " + PRICES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STORES_TABLE_NAME);
        onCreate(db);
    }

    public void addPrice(int itemId, int price, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_ID, itemId);
        values.put(KEY_PRICE, price);
        values.put(KEY_TIMESTAMP, timestamp);
        Log.d(LOG_TAG, "Adding price to database: itemId = "+itemId+", price = "+price);
        db.insert(PRICES_TABLE_NAME, null, values);
    }

    public void addPriceWithCurrentTimestamp(int itemId, int price) {
        long currentTimestamp = System.currentTimeMillis();
        addPrice(itemId, price, currentTimestamp);
    }

    public int addItemIfNotExists(String itemTitle, String itemUrl) {  //returns itemId
        int itemIdIfExists = getItemId(itemUrl);
        if (itemIdIfExists == ITEM_NOT_FOUND_ID) {
            return addItem(itemTitle, itemUrl);
        } else {
            return itemIdIfExists;
        }
    }

    private int addItem(String itemTitle, String itemUrl) {
        String storeUrl = Store.extractStoreUrl(itemUrl);
        int storeId = getStoreIdByUrl(storeUrl);

        if (storeId == STORE_NOT_FOUND_ID) {
            Log.d(LOG_TAG, "Cannot find id for store with url " + storeUrl);
            return ITEM_NOT_FOUND_ID;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_STORE_ID, storeId);
        values.put(KEY_ITEM_TITLE, itemTitle);
        values.put(KEY_ITEM_URL, itemUrl);
        return (int)db.insert(ITEMS_TABLE_NAME, null, values);

    }

    private void deleteItem(int itemId) {
        String query = "DELETE FROM " + ITEMS_TABLE_NAME + " WHERE " + KEY_ITEM_ID + " = " + itemId;
        db.execSQL(query);
        Log.d(LOG_TAG, "Product with id " + itemId + " deleted from table " + ITEMS_TABLE_NAME);
    }

    public void deleteItemWithAllItsPrices(int itemId) {
        deleteAllPricesForItem(itemId);
        deleteItem(itemId);
    }

    public void deleteAllItemsWithName(String productName) {
        if (productName.isEmpty()) {
            Log.d(LOG_TAG, "product deletion failed: product name is empty");
            return;
        }
        ArrayList<Integer> itemsIdForProductList = getItemsForProductIdList(productName);
        for (int itemId : itemsIdForProductList) {
            deleteItemWithAllItsPrices(itemId);
        }
    }



    private void addStoreIfNotExists(String storeTitle, String storeUrl) {
        if (getStoreIdByUrl(storeUrl) == STORE_NOT_FOUND_ID) {
            addStore(storeTitle, storeUrl);
        }
    }

    private int addStore(String storeTitle, String storeUrl) {
        ContentValues values = new ContentValues();
        values.put(KEY_STORE_TITLE, storeTitle);
        values.put(KEY_STORE_URL, storeUrl);
        return (int)db.insert(STORES_TABLE_NAME, null, values);
    }

    private int getStoreIdByTitle(String storeTitle) {
        String query = "SELECT " + KEY_STORE_ID
                + " FROM " + STORES_TABLE_NAME
                + " WHERE " + KEY_STORE_TITLE + " = '" + storeTitle + "'";
        Cursor cursor = db.rawQuery(query, null);

        int storeId = STORE_NOT_FOUND_ID;
        if (cursor.moveToFirst()) {
            storeId = cursor.getInt(cursor.getColumnIndex(KEY_STORE_ID));
        }
        cursor.close();
        return storeId;
    }

    public String getItemTitle(int itemId) {
        String query = "SELECT " + KEY_ITEM_TITLE
                + " FROM " + ITEMS_TABLE_NAME
                + " WHERE " + KEY_ITEM_ID + " = '" + itemId + "'";
        Cursor cursor = db.rawQuery(query, null);

        String itemTitle = "";
        if (cursor.moveToFirst()) {
            itemTitle = cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE));
        }
        cursor.close();
        return itemTitle;
    }

    public String getItemUrl(int itemId) {
        String query = "SELECT " + KEY_ITEM_URL
                + " FROM " + ITEMS_TABLE_NAME
                + " WHERE " + KEY_ITEM_ID + " = '" + itemId + "'";
        Cursor cursor = db.rawQuery(query, null);

        String itemUrl = "";
        if (cursor.moveToFirst()) {
            itemUrl = cursor.getString(cursor.getColumnIndex(KEY_ITEM_URL));
        }
        cursor.close();
        return itemUrl;
    }

    public int getItemId(String itemUrl) {
        String query = "SELECT " + KEY_ITEM_ID
                + " FROM " + ITEMS_TABLE_NAME
                + " WHERE " + KEY_ITEM_URL + " = '" + itemUrl + "'";
        Cursor cursor = db.rawQuery(query, null);

        int itemId = ITEM_NOT_FOUND_ID;
        if (cursor.moveToFirst()) {
            itemId = cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID));
        }
        cursor.close();
        return itemId;
    }

    public ArrayList<Integer> getAllItemsIdList() {
        String query = "SELECT " + KEY_ITEM_ID
                + " FROM " + ITEMS_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Integer> itemsIdList = new ArrayList<>();
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                int itemId = cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID));
                itemsIdList.add(itemId);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemsIdList;
    }

    public ArrayList<Integer> getItemsForProductIdList(String productName) {
        String query = "SELECT " + KEY_ITEM_ID
                + " FROM " + ITEMS_TABLE_NAME
                + " WHERE " + KEY_ITEM_TITLE + " = '" + productName + "'";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Integer> itemsIdList = new ArrayList<>();
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                int itemId = cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID));
                itemsIdList.add(itemId);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemsIdList;
    }

    public String getStoreTitle(int itemId) {
        String query = "SELECT " + KEY_STORE_TITLE
                + " FROM " + ITEMS_TABLE_NAME
                + " LEFT OUTER JOIN " + STORES_TABLE_NAME
                +" ON " + ITEMS_TABLE_NAME + "." + KEY_STORE_ID + " = " + STORES_TABLE_NAME + "." + KEY_STORE_ID
                + " WHERE " + ITEMS_TABLE_NAME + "." + KEY_ITEM_ID + " = '" + itemId + "'";
        Cursor cursor = db.rawQuery(query, null);

        String storeTitle = "";
        if (cursor.moveToFirst()) {
            storeTitle = cursor.getString(cursor.getColumnIndex(KEY_STORE_TITLE));
        }
        cursor.close();
        return storeTitle;
    }

    public String getStoreUrl(int itemId) {
        String query = "SELECT " + KEY_STORE_URL
                + " FROM " + ITEMS_TABLE_NAME
                + " LEFT OUTER JOIN " + STORES_TABLE_NAME
                +" ON " + ITEMS_TABLE_NAME + "." + KEY_STORE_ID + " = " + STORES_TABLE_NAME + "." + KEY_STORE_ID
                + " WHERE " + ITEMS_TABLE_NAME + "." + KEY_ITEM_ID + " = '" + itemId + "'";
        Cursor cursor = db.rawQuery(query, null);

        String storeUrl = "";
        if (cursor.moveToFirst()) {
            storeUrl = cursor.getString(cursor.getColumnIndex(KEY_STORE_URL));
        }
        cursor.close();
        return storeUrl;
    }

    public int getStoreIdByUrl(String storeUrl) {

        String query = "SELECT " + KEY_STORE_ID
                + " FROM " + STORES_TABLE_NAME
                + " WHERE " + KEY_STORE_URL + " = '" + storeUrl + "'";
        Cursor cursor = db.rawQuery(query, null);

        int storeId = STORE_NOT_FOUND_ID;
        if (cursor.moveToFirst()) {
            storeId = cursor.getInt(cursor.getColumnIndex(KEY_STORE_ID));
        }
        cursor.close();
        return storeId;
    }

    public ArrayList<String> getAllStoreUrls() {
        String query = "SELECT " + KEY_STORE_URL
                + " FROM " + STORES_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<String> storeUrls = new ArrayList<>();
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                String storeUrl = cursor.getString(cursor.getColumnIndex(KEY_STORE_URL));
                storeUrls.add(storeUrl);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return storeUrls;
    }


    public void fillDbWithTestData() {
        clearPricesAndItems();

        String huaweiUrl = "https://market.yandex.ru/product--smartfon-huawei-mate-20x-128gb/385696006";
        String huaweiItemTitle = "Huawei Mate 20X";
        int huaweiItemId = addItem(huaweiItemTitle, huaweiUrl);

        Long currentTimestamp = System.currentTimeMillis();
        addPrice(huaweiItemId, 41394, currentTimestamp - 45*24*3600*1000L);
        addPrice(huaweiItemId, 47000, currentTimestamp - 2*24*3600*1000);
    }

    public void printDbToLog() {
        printTableToLog(PRICES_TABLE_NAME);
        printTableToLog(ITEMS_TABLE_NAME);
        printTableToLog(STORES_TABLE_NAME);
    }

    private void printTableToLog(String tableName) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        if(cursor.getCount() != 0){
            cursor.moveToFirst();

            do{
                String row_values = "";

                for(int i = 0 ; i < cursor.getColumnCount(); i++){
                    row_values = row_values + " || " + cursor.getString(i);
                }

                Log.d("TABLE " + tableName.toUpperCase(), row_values);

            }while (cursor.moveToNext());
        }
        cursor.close();
    }


    private TreeMap<Long, Integer> getAllPricesWithTimestamps(int itemId) {
        TreeMap<Long, Integer> allData = new TreeMap<>();

        String query = "SELECT " + KEY_PRICE + ", " + KEY_TIMESTAMP
                + " FROM " + PRICES_TABLE_NAME
                + " WHERE " + KEY_ITEM_ID + " = '" + itemId + "'";
        Cursor cursor;
        try {
            cursor = db.rawQuery(query, null);
        } catch (SQLiteException e) {
            return null;
        }

        long timestamp;
        int price;
        try {
            if (cursor.moveToFirst()) {
                do {
                    price = cursor.getInt(cursor.getColumnIndex(KEY_PRICE));
                    timestamp = cursor.getLong(cursor.getColumnIndex(KEY_TIMESTAMP));
                    allData.put(timestamp, price);
                } while (cursor.moveToNext());

            }
        } catch (SQLiteException e) {
            return null;
        }
        cursor.close();

        return allData;
    }

    public TreeMap<Date, Integer> getAllPricesWithDate(int itemId) {
        TreeMap<Long, Integer> allPricesWithTimestamps = getAllPricesWithTimestamps(itemId);
        if (allPricesWithTimestamps == null) {
            return null;
        }
        TreeMap<Date, Integer> allPrices = new TreeMap<>();
        for (TreeMap.Entry<Long, Integer> entry : allPricesWithTimestamps.entrySet()) {
            Long timestampKey = entry.getKey();
            Timestamp timestamp = new Timestamp(timestampKey);
            Date date = new Date(timestamp.getTime());
            allPrices.put(date, entry.getValue());
        }
        return allPrices;
    }

    public int getLastPrice(int itemId) {
        TreeMap<Date, Integer> allPrices = getAllPricesWithDate(itemId);
        Map.Entry<Date, Integer> lastEntry = allPrices.lastEntry();
        if (lastEntry == null) {
            return StoreScraper.PRICE_NOT_FOUND;
        } else {
            return lastEntry.getValue();
        }
    }

    public void deleteLastPriceForEachItem() {
        ArrayList<Integer> itemsIdList = getAllItemsIdList();
        for (int itemId : itemsIdList) {
            deleteLastPrice(itemId);
        }
    }

    public void deleteLastPrice(int itemId) {
        String query = "DELETE FROM " + PRICES_TABLE_NAME
                + " WHERE " + KEY_ITEM_ID + " = " + itemId +
                " ORDER BY " + KEY_TIMESTAMP + " DESC LIMIT 1;";
        db.execSQL(query);
        Log.d(LOG_TAG, "Last price for item " + itemId + " deleted");
    }

    public void deleteAllPricesForItem(int itemId) {
        String query = "DELETE FROM " + PRICES_TABLE_NAME
                + " WHERE " + KEY_ITEM_ID + " = " + itemId;
        db.execSQL(query);
        Log.d(LOG_TAG, "All prices for item " + itemId + " deleted");
    }

    public ArrayList<Product> getAllProducts(int[] storeColors) {

        ArrayList<Product> allProducts = new ArrayList<>();
        ArrayList<Integer> allItemIds = getAllItemsIdList();
        for (int itemId : allItemIds) {
            String itemName = getItemTitle(itemId);
            String itemUrl = getItemUrl(itemId);
            String storeName = getStoreTitle(itemId);
            String storeUrl = getStoreUrl(itemId);
            int storeId = getStoreIdByTitle(storeName);
            int storeColor = dataManager.getStoreColor(storeId, storeColors);
            TreeMap<Date, Integer> pricesForItem = getAllPricesWithDate(itemId);

            Store store = new Store(storeUrl, storeName, storeId, storeColor);
            ProductInStore productInStore = new ProductInStore(itemId, itemUrl, store, pricesForItem);

            boolean doAllProductsListContainProduct = dataManager.doAllProductsListContainProduct(itemName, allProducts);

            if (doAllProductsListContainProduct) {
                Product product = dataManager.getProductWithName(itemName, allProducts);
                product.productInStoreList.add(productInStore);
            } else {
                ArrayList<ProductInStore> productInStoreList = new ArrayList<>();
                productInStoreList.add(productInStore);
                Product product = new Product(itemName, productInStoreList);
                allProducts.add(product);
            }
        }
        return allProducts;
    }



}
