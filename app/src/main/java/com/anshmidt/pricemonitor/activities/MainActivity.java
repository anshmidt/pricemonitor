package com.anshmidt.pricemonitor.activities;

import android.app.FragmentManager;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.anshmidt.pricemonitor.data.DataManager;
import com.anshmidt.pricemonitor.GraphPlotter;
import com.anshmidt.pricemonitor.ProductsListAdapter;
import com.anshmidt.pricemonitor.NotificationHelper;
import com.anshmidt.pricemonitor.PriceMonitorApplication;
import com.anshmidt.pricemonitor.dialogs.ProductSettingsBottomSheetFragment;
import com.anshmidt.pricemonitor.room.PricesRepository;
import com.anshmidt.pricemonitor.R;
import com.anshmidt.pricemonitor.ServerRequestsWorker;
import com.anshmidt.pricemonitor.StoreColorAssigner;
import com.anshmidt.pricemonitor.dialogs.AddProductDialogFragment;
import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.room.entity.Product;
import com.anshmidt.pricemonitor.room.entity.Store;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity implements
        AddProductDialogFragment.AddProductDialogListener,
        ProductSettingsBottomSheetFragment.ProductSettingsBottomSheetListener {

    FloatingActionButton addItemButton;
    final String LOG_TAG = MainActivity.class.getSimpleName();
    final int INTERVAL_BETWEEN_SERVER_REQUESTS_MINUTES = 15; //it cannot be < 15 according to WorkManager spec

    @Inject ProductsListAdapter productsListAdapter;
    @Inject PricesRepository pricesRepository;
    @Inject DataManager dataManager;
    @Inject GraphPlotter graphPlotter;
    @Inject int[] storesColors;
    @Inject NotificationHelper notificationHelper;
    @Inject StoreColorAssigner storeColorAssigner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PriceMonitorApplication.getComponent().inject(this);

        pricesRepository.addStoresIfNotPresent();
        pricesRepository.printDatabaseToLog();


        RecyclerView recyclerView = findViewById(R.id.items_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productsListAdapter.productDataList = pricesRepository.getAllProductData();
        recyclerView.setAdapter(productsListAdapter);

        sendPeriodicServerRequests();

        addItemButton = (FloatingActionButton) findViewById(R.id.add_item_button);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddItemButtonClicked();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy < 0 && !addItemButton.isShown()) {
                    addItemButton.show();
                }
                else if (dy > 0 && addItemButton.isShown()) {
                    addItemButton.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        productsListAdapter.productDataList = pricesRepository.getAllProductData();
        productsListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.clear_db_menu_item: {
                pricesRepository.clearAllTables();
                pricesRepository.printDatabaseToLog();
                recreate();
                break;
            }
            case R.id.action_refresh: {
                sendOneTimeServerRequests();
                break;
            }
            case R.id.fill_db_menu_item: {
                pricesRepository.fillDbWithTestData();
                recreate();
                break;
            }
            case R.id.delete_recent_price_menu_item: {
                pricesRepository.deleteRecentPriceForEachItem();
                recreate();
                break;
            }
            case R.id.stop_requests_menu_item: {
                WorkManager.getInstance().cancelAllWork();
                Toast.makeText(MainActivity.this, "Requests stopped", Toast.LENGTH_SHORT).show();
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductAdded(String productName, String itemUrl, String storeUrl, int price) {
        Store store = pricesRepository.getStoreByUrl(storeUrl);
        int newProductId = pricesRepository.addProductIfNotExists(new Product(productName));
        Product newProduct = new Product(newProductId, productName);

        int itemId = pricesRepository.addItemIfNotExists(new Item(itemUrl, newProduct.id, store.id));
        Item newItem = new Item(itemId, itemUrl, newProduct.id, store.id);

        Date currentDate = new Date(System.currentTimeMillis());
        int priceId = pricesRepository.addPrice(new Price(currentDate, itemId, price));
        Price newPrice = new Price(priceId, currentDate, itemId, price);

        productsListAdapter.productDataList = dataManager.addProduct(newProduct, productsListAdapter.productDataList);
        productsListAdapter.productDataList = dataManager.addItem(newItem, store, productsListAdapter.productDataList);
        productsListAdapter.productDataList = dataManager.addPrice(newPrice, newItem, productsListAdapter.productDataList);


        productsListAdapter.notifyDataSetChanged();
    }

    public void onProductDeleted(String productName) {
        pricesRepository.deleteAllPricesForProductName(productName);
        pricesRepository.deleteAllItemsForProductName(productName);
        pricesRepository.deleteProduct(productName);

        productsListAdapter.productDataList = pricesRepository.getAllProductData();
        productsListAdapter.notifyDataSetChanged();
    }

    public void sendPeriodicServerRequests() {
        List<Item> items = pricesRepository.getAllItems();
        for (Item item : items) {
            Log.d(LOG_TAG, "Scheduling periodic requests for item: " + item);
            Data inputData = new Data.Builder()
                    .putInt(ServerRequestsWorker.KEY_ITEM_ID, item.id)
                    .putString(ServerRequestsWorker.KEY_ITEM_URL, item.url)
                    .build();
            Constraints periodicWorkerConstraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            PeriodicWorkRequest serverScraperWorkRequest =
                    new PeriodicWorkRequest.Builder(
                            ServerRequestsWorker.class,
                            INTERVAL_BETWEEN_SERVER_REQUESTS_MINUTES, TimeUnit.MINUTES)
                            .setInputData(inputData)
                            .setConstraints(periodicWorkerConstraints)
                            .build();

            String uniqueWorkTag = String.valueOf(item.id);
            WorkManager.getInstance().enqueueUniquePeriodicWork(uniqueWorkTag, ExistingPeriodicWorkPolicy.REPLACE, serverScraperWorkRequest);

            WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(uniqueWorkTag)
                    .observe(this, new Observer<List<WorkInfo>>() {
                        @Override
                        public void onChanged(@Nullable List<WorkInfo> workInfos) {
                            if (workInfos == null) {
                                Log.d(LOG_TAG, "workinfo is null");
                                return;
                            }

                            for (WorkInfo workInfo : workInfos) {
                                //receiving back the data
                                Log.d(LOG_TAG, "Result of PeriodicWorkRequest received. Status: " + workInfo.getState());
                                int itemId = workInfo.getOutputData().getInt(ServerRequestsWorker.KEY_ITEM_ID, Item.ID_NOT_FOUND);
                                int price = workInfo.getOutputData().getInt(ServerRequestsWorker.KEY_PRICE, StoreScraper.PRICE_NOT_FOUND);
                                Log.d(LOG_TAG, "Result of PeriodicWorkRequest: itemId: "+itemId + ", price: " + price);
                                onPeriodicResponseFromServer(price, itemId);
                            }
                        }
                    });
        }
    }


    public void sendOneTimeServerRequests() {
        List<Item> items = pricesRepository.getAllItems();
        for (Item item : items) {
            Data inputData = new Data.Builder()
                    .putInt(ServerRequestsWorker.KEY_ITEM_ID, item.id)
                    .putString(ServerRequestsWorker.KEY_ITEM_URL, item.url)
                    .build();
            OneTimeWorkRequest serverScraperWorkRequest =
                    new OneTimeWorkRequest.Builder(ServerRequestsWorker.class)
                            .setInputData(inputData)
                            .build();
            WorkManager.getInstance().enqueue(serverScraperWorkRequest);

            WorkManager.getInstance().getWorkInfoByIdLiveData(serverScraperWorkRequest.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(@Nullable WorkInfo workInfo) {
                            //receiving back the data
                            if (workInfo != null && workInfo.getState().isFinished()) {
                                Log.d(LOG_TAG, "Result of OnetimeWorkRequest received");
                                int itemId = workInfo.getOutputData().getInt(ServerRequestsWorker.KEY_ITEM_ID, Item.ID_NOT_FOUND);
                                int price = workInfo.getOutputData().getInt(ServerRequestsWorker.KEY_PRICE, StoreScraper.PRICE_NOT_FOUND);
                                onOneTimeResponseFromServer(price, itemId);
                            }
                        }
                    });
        }
    }

    public void onOneTimeResponseFromServer(int priceFromServer, int itemId) {
        if (priceFromServer == StoreScraper.PRICE_NOT_FOUND) {
            Log.d(LOG_TAG, "Item " + itemId+ ": invalid price received: " + priceFromServer);
            return;
        }

        Item item = pricesRepository.getItemById(itemId);

        int recentPriceFromDb = pricesRepository.getRecentPriceForItem(itemId).price;
        String productName = pricesRepository.getProductByProductId(item.productId).name;
        String storeName = pricesRepository.getStoreByStoreId(item.storeId).name;

        notificationHelper.showPriceDroppedNotificationIfNeeded(
                priceFromServer,
                recentPriceFromDb,
                productName,
                storeName
        );

        Date currentDate = new Date(System.currentTimeMillis());
        int priceId = pricesRepository.addPrice(new Price(currentDate, item.id, priceFromServer));

        Price newPrice = new Price(priceId, currentDate, item.id, priceFromServer);
        productsListAdapter.productDataList = dataManager.addPrice(newPrice, item, productsListAdapter.productDataList);
        productsListAdapter.notifyDataSetChanged();
    }

    public void onPeriodicResponseFromServer(int priceFromServer, int itemId) {
        if (priceFromServer == StoreScraper.PRICE_NOT_FOUND) {
            Log.d(LOG_TAG, "Item " + itemId+ ": invalid price received: " + priceFromServer);
            return;
        }

        Item item = pricesRepository.getItemById(itemId);

        int recentPriceFromDb = pricesRepository.getRecentPriceForItem(itemId).price;
        String productName = pricesRepository.getProductByProductId(item.productId).name;
        String storeName = pricesRepository.getStoreByStoreId(item.storeId).name;

        notificationHelper.showPriceDroppedNotificationIfNeeded(
                priceFromServer,
                recentPriceFromDb,
                productName,
                storeName
        );

        Date currentDate = new Date(System.currentTimeMillis());
        int priceId = pricesRepository.addPrice(new Price(currentDate, item.id, priceFromServer));

        //products list doesn't get updated on each result. It is updated only onResume
    }


    public void onAddItemButtonClicked() {
        FragmentManager manager = getFragmentManager();
        AddProductDialogFragment addProductDialogFragment = new AddProductDialogFragment();
        addProductDialogFragment.show(manager, addProductDialogFragment.FRAGMENT_TAG);
    }



}
