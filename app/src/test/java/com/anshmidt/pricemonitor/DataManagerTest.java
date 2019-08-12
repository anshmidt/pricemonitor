package com.anshmidt.pricemonitor;

import com.anshmidt.pricemonitor.data.CurrentPriceInStore;
import com.anshmidt.pricemonitor.data.Product;
import com.anshmidt.pricemonitor.data.ProductInStore;
import com.anshmidt.pricemonitor.data.Store;
import com.anshmidt.pricemonitor.exceptions.EmptyDataException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;


public class DataManagerTest {

    private DataManager dataManager;

    private class StoreStub extends Store {
        public StoreStub(String url, String title, int id, int color) {
            super(url, title, id, color);
        }

        public StoreStub(int id) {
            super("url" + id + ".com", "store title " + id, id, 5);
        }
    }

    private class CurrentPriceInStoreStub extends CurrentPriceInStore {
        public CurrentPriceInStoreStub(String storeName, int price, Date date, int storeColor, String productInStoreUrl) {
            super(storeName, price, date, storeColor, productInStoreUrl);
        }
    }

    @Before
    public void setUp() {
        dataManager = new DataManager();
    }


    private void temp() {

    }

    @Test
    public void getLastValue_HappyPath() throws EmptyDataException {
        TreeMap<Date, Integer> data = new TreeMap<>();
        data.put(new Date(System.currentTimeMillis()), 100);
        data.put(new Date(System.currentTimeMillis() + 1), 200);
        data.put(new Date(System.currentTimeMillis() + 2), 150);

        Assert.assertEquals(150, dataManager.getLastValue(data));
    }

    @Test(expected = EmptyDataException.class)
    public void getLastValue_NullData_ThrowsException() throws EmptyDataException {
        dataManager.getLastValue(null);
    }

    @Test(expected = EmptyDataException.class)
    public void getLastValue_EmptyData_ThrowsException() throws EmptyDataException {
        dataManager.getLastValue(new TreeMap<Date, Integer>());
    }

    @Test
    public void getLastValue_OneDataElement_Valid() throws EmptyDataException {
        TreeMap<Date, Integer> data = new TreeMap<>();
        data.put(new Date(System.currentTimeMillis()), 100);

        Assert.assertEquals(100, dataManager.getLastValue(data));
    }

    @Test
    public void getMaxValue_HappyPath() throws EmptyDataException {
        TreeMap<Date, Integer> data = new TreeMap<>();
        data.put(new Date(System.currentTimeMillis()), 100);
        data.put(new Date(System.currentTimeMillis() + 1), 200);
        data.put(new Date(System.currentTimeMillis() + 2), 150);

        Assert.assertEquals(200, dataManager.getMaxValue(data));
    }

    @Test(expected = EmptyDataException.class)
    public void getMaxValue_NullData_ThrowsException() throws EmptyDataException {
        dataManager.getMaxValue(null);
    }

    @Test(expected = EmptyDataException.class)
    public void getMaxValue_EmptyData_ThrowsException() throws EmptyDataException {
        dataManager.getMaxValue(new TreeMap<Date, Integer>());
    }

    @Test
    public void getMinValue_HappyPath() throws EmptyDataException {
        TreeMap<Date, Integer> data = new TreeMap<>();
        data.put(new Date(System.currentTimeMillis()), 100);
        data.put(new Date(System.currentTimeMillis() + 1), 80);
        data.put(new Date(System.currentTimeMillis() + 2), 150);

        Assert.assertEquals(80, dataManager.getMinValue(data));
    }

    @Test(expected = EmptyDataException.class)
    public void getMinValue_NullData_ThrowsException() throws EmptyDataException {
        dataManager.getMinValue(null);
    }

    @Test(expected = EmptyDataException.class)
    public void getMinValue_EmptyData_ThrowsException() throws EmptyDataException {
        dataManager.getMinValue(new TreeMap<Date, Integer>());
    }

    @Test
    public void getLastKey_HappyPath() throws EmptyDataException {
        TreeMap<Date, Integer> data = new TreeMap<>();
        Date date0 = new Date(System.currentTimeMillis());
        Date date1 = new Date(System.currentTimeMillis() + 1);
        Date date2 = new Date(System.currentTimeMillis() + 2);
        data.put(date0, 100);
        data.put(date1, 80);
        data.put(date2, 150);

        Assert.assertEquals(date2, dataManager.getLastKey(data));
    }

    @Test(expected = EmptyDataException.class)
    public void getLastKey_NullData_ThrowsException() throws EmptyDataException {
        dataManager.getLastKey(null);
    }

    @Test(expected = EmptyDataException.class)
    public void getLastKey_EmptyData_ThrowsException() throws EmptyDataException {
        dataManager.getLastKey(new TreeMap<Date, Integer>());
    }

    @Test
    public void getSortedKeys_HappyPath() throws EmptyDataException {
        TreeMap<Date, Integer> data = new TreeMap<>();
        Date date0 = new Date(System.currentTimeMillis());
        Date date1 = new Date(System.currentTimeMillis() + 1);
        Date date2 = new Date(System.currentTimeMillis() + 2);
        data.put(date0, 100);
        data.put(date2, 150);
        data.put(date1, 80);

        ArrayList<Date> expectedKeys = new ArrayList<>();
        expectedKeys.add(0, date0);
        expectedKeys.add(1, date1);
        expectedKeys.add(2, date2);

        Assert.assertEquals(expectedKeys, dataManager.getSortedKeys(data));
    }

    @Test
    public void retrieveLastPricesOfProduct_HappyPath() throws EmptyDataException {
        String storeNameA = "first";
        String storeNameB = "Second store";
        String storeUrlA = "one.com";
        String storeUrlB = "two.co.uk";
        int storeColorA = 1;
        int storeColorB = 2;
        int storeIdA = 1;
        int storeIdB = 2;

        StoreStub storeStubA = new StoreStub(storeUrlA, storeNameA, storeIdA, storeColorA);
        StoreStub storeStubB = new StoreStub(storeUrlB, storeNameB, storeIdB, storeColorB);

        Date date0 = new Date(System.currentTimeMillis());
        Date date1 = new Date(System.currentTimeMillis() + 10);
        Date date2 = new Date(System.currentTimeMillis() + 20);

        TreeMap<Date, Integer> pricesA = new TreeMap<>();
        pricesA.put(date0, 100);
        pricesA.put(date1, 200);
        pricesA.put(date2, 140);

        TreeMap<Date, Integer> pricesB = new TreeMap<>();
        pricesB.put(date0, 200);
        pricesB.put(date1, 210);

        ArrayList<ProductInStore> productInStoreList = new ArrayList<>();
        ProductInStore productInStoreA = new ProductInStore(storeIdA, storeUrlA, storeStubA, pricesA);
        ProductInStore productInStoreB = new ProductInStore(storeIdB, storeUrlB, storeStubB, pricesB);
        productInStoreList.add(productInStoreA);
        productInStoreList.add(productInStoreB);

        Product product = new Product("myproduct", productInStoreList);


        ArrayList<CurrentPriceInStore> actualLastPrices = dataManager.retrieveLastPricesOfProduct(product);
        CurrentPriceInStore actualLastPriceA = actualLastPrices.get(0);
        CurrentPriceInStore actualLastPriceB = actualLastPrices.get(1);

        Assert.assertEquals(140, actualLastPriceA.price);
        Assert.assertEquals(210, actualLastPriceB.price);
        Assert.assertEquals(storeNameA, actualLastPriceA.storeName);
        Assert.assertEquals(storeNameB, actualLastPriceB.storeName);
        Assert.assertEquals(date2, actualLastPriceA.date);
        Assert.assertEquals(date1, actualLastPriceB.date);

    }







}
