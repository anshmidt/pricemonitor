package com.anshmidt.pricemonitor.activities;

import android.app.FragmentManager;
import android.os.Bundle;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    protected static boolean isVisible = false;

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

        schedulePeriodicRequests();

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
        isVisible = true;
        productsListAdapter.productDataList = pricesRepository.getAllProductData();
        productsListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.action_refresh: {
                scheduleOneTimeRequests();
                break;
            }
            // option for debug purposes
            case R.id.clear_db_menu_item: {
                pricesRepository.clearAllTables();
                pricesRepository.printDatabaseToLog();
                recreate();
                break;
            }
            // option for debug purposes
            case R.id.fill_db_menu_item: {
                pricesRepository.fillDbWithTestData();
                recreate();
                break;
            }
            // option for debug purposes
            case R.id.delete_latest_price_menu_item: {
                pricesRepository.deleteLatestPriceForEachItem();
                recreate();
                break;
            }
            // option for debug purposes
            case R.id.stop_requests_menu_item: {
                WorkManager.getInstance(MainActivity.this).cancelAllWork();
//                List<Item> items = pricesRepository.getAllItems();
//                for (Item item : items) {
//                    String workerTag = String.valueOf(item.id);
//                    WorkManager.getInstance().cancelUniqueWork(workerTag);
//                }
                Toast.makeText(MainActivity.this, "Requests stopped", Toast.LENGTH_SHORT).show();
                break;
            }

        }

        return super.onOptionsItemSelected(menuItem);
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

    public void schedulePeriodicRequests() {
        List<Item> items = pricesRepository.getAllItems();
        WorkManager workManager = WorkManager.getInstance(MainActivity.this);
        final Constraints periodicWorkerConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        for (Item item : items) {
            Data inputData = new Data.Builder()
                    .putInt(ServerRequestsWorker.KEY_ITEM_ID, item.id)
                    .putString(ServerRequestsWorker.KEY_ITEM_URL, item.url)
                    .build();

            String uniqueWorkName = String.valueOf(item.id);

            PeriodicWorkRequest serverScraperWorkRequest =
                    new PeriodicWorkRequest.Builder(
                            ServerRequestsWorker.class,
                            INTERVAL_BETWEEN_SERVER_REQUESTS_MINUTES, TimeUnit.MINUTES)
                            .setInputData(inputData)
                            .setConstraints(periodicWorkerConstraints)
                            .build();

            workManager.enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.REPLACE, serverScraperWorkRequest);


            workManager.getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
                    .observe(this, workInfos -> {
                        for (WorkInfo workInfo : workInfos) {
                            if (workInfo != null && workInfo.getState() == WorkInfo.State.ENQUEUED) {
                                Log.d(LOG_TAG, "Result of PeriodicWorkRequest received");
                                Price priceFromServer = pricesRepository.getLatestPriceForItem(item.id);
                                onResponseFromServer(priceFromServer, item.id);
                            }
                        }
                    });
        }

    }


    public void scheduleOneTimeRequests() {
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
            WorkManager.getInstance(MainActivity.this).enqueue(serverScraperWorkRequest);

            WorkManager.getInstance(MainActivity.this).getWorkInfoByIdLiveData(serverScraperWorkRequest.getId())
                    .observe(this, workInfo -> {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            Log.d(LOG_TAG, "Result of OnetimeWorkRequest received");
                            Price priceFromServer = pricesRepository.getLatestPriceForItem(item.id);
                            onResponseFromServer(priceFromServer, item.id);
                        }
                    });
        }
    }



    public void onResponseFromServer(Price latestPrice, int itemId) {
        if (latestPrice.price == StoreScraper.PRICE_NOT_FOUND) {
            Log.d(LOG_TAG, "Item " + itemId+ ": invalid price received: " + latestPrice);
            return;
        }


        // add price to graph if activity is visible
        if (isVisible) {
            Item item = pricesRepository.getItemById(itemId);
            productsListAdapter.productDataList = dataManager.addPrice(latestPrice, item, productsListAdapter.productDataList);
            productsListAdapter.notifyDataSetChanged();
        }

    }


    public void onAddItemButtonClicked() {
        FragmentManager manager = getFragmentManager();
        AddProductDialogFragment addProductDialogFragment = new AddProductDialogFragment();
        addProductDialogFragment.show(manager, addProductDialogFragment.FRAGMENT_TAG);
    }



}
