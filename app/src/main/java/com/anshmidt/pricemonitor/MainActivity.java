package com.anshmidt.pricemonitor;

import android.app.FragmentManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.anshmidt.pricemonitor.dialogs.AddItemDialogFragment;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;
import com.anshmidt.pricemonitor.scrapers.StoreScraperFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity implements AddItemDialogFragment.AddItemDialogListener {


    Toolbar toolbar;
    DatabaseHelper databaseHelper;
    DataManager dataManager;
//    ItemsListAdapter itemsListAdapter;
    ItemsListMultipleStoresAdapter itemsListMultipleStoresAdapter;
    GraphPlotter graphPlotter;
    FloatingActionButton addItemButton;
    StoreScraperFactory storeScraperFactory;
    final String LOG_TAG = MainActivity.class.getSimpleName();
    final int INTERVAL_BETWEEN_SERVER_REQUESTS = 60;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = DatabaseHelper.getInstance(MainActivity.this);
        dataManager = new DataManager();
        graphPlotter = new GraphPlotter(MainActivity.this);


        storeScraperFactory = new StoreScraperFactory(MainActivity.this);

        //temp
        databaseHelper.addAllStoresAndItemsIfNeeded();
        databaseHelper.printDbToLog();



        ArrayList<Integer> itemsIdList = databaseHelper.getAllItemsIdList();


        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.items_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        itemsListAdapter = new ItemsListAdapter(this, itemsIdList, true);
//        recyclerView.setAdapter(itemsListAdapter);

        itemsListMultipleStoresAdapter = new ItemsListMultipleStoresAdapter(MainActivity.this, databaseHelper.getAllProducts());
        recyclerView.setAdapter(itemsListMultipleStoresAdapter);


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
        itemsListMultipleStoresAdapter.notifyDataSetChanged();
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
                databaseHelper.clearPricesAndItems();
                recreate();
                break;
            }
            case R.id.action_refresh: {
                sendOneTimeServerRequests();
                break;
            }
            case R.id.fill_db_menu_item: {
                databaseHelper.fillDbWithTestData();
                recreate();
                break;
            }
            case R.id.delete_recent_price_menu_item: {
                databaseHelper.deleteLastPriceForEachItem();
                recreate();
                break;
            }
            case R.id.stop_requests_menu_item: {
                WorkManager.getInstance().cancelAllWork();
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemAdded(String itemName, String itemUrl, String storeUrl, int price) {
        int itemId = databaseHelper.addItemIfNotExists(itemName, itemUrl);
        long timestamp = System.currentTimeMillis();
        databaseHelper.addPrice(itemId, price, timestamp);
//        itemsListMultipleStoresAdapter.itemsIdList = databaseHelper.getAllItemsIdList();
        itemsListMultipleStoresAdapter.products = databaseHelper.getAllProducts();
        itemsListMultipleStoresAdapter.notifyDataSetChanged();
    }

    public void onItemDeleted(int itemId) {
        itemsListMultipleStoresAdapter.products = databaseHelper.getAllProducts();
        itemsListMultipleStoresAdapter.notifyDataSetChanged();
    }

    public void sendPeriodicServerRequests() {
        ArrayList<Integer> itemsIdList = databaseHelper.getAllItemsIdList();
        for (final int itemId : itemsIdList) {
            Data inputData = new Data.Builder()
                    .putInt(ServerRequestsWorker.KEY_ITEM_ID, itemId)
                    .build();
            PeriodicWorkRequest serverScraperWorkRequest =
                    new PeriodicWorkRequest.Builder(
                            ServerRequestsWorker.class,
                            INTERVAL_BETWEEN_SERVER_REQUESTS, TimeUnit.MINUTES)
                            .setInputData(inputData)
                            .build();

            String uniqueWorkTag = String.valueOf(itemId);
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
                                Log.d(LOG_TAG, "result of PeriodicWorkRequest received for itemID="+itemId + ". Status: " + workInfo.getState());
                                if (workInfo != null) {

                                    if (workInfo.getState() == WorkInfo.State.ENQUEUED) {
                                        itemsListMultipleStoresAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
        }
    }


    public void sendOneTimeServerRequests() {
        ArrayList<Integer> itemsIdList = databaseHelper.getAllItemsIdList();
        for (int itemId : itemsIdList) {
            Data inputData = new Data.Builder()
                    .putInt(ServerRequestsWorker.KEY_ITEM_ID, itemId)
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
                                Log.d(LOG_TAG, "result of OnetimeWorkRequest received");
                                String itemUrl = workInfo.getOutputData().getString(ServerRequestsWorker.KEY_ITEM_URL);
                                int price = workInfo.getOutputData().getInt(ServerRequestsWorker.KEY_PRICE, StoreScraper.PRICE_NOT_FOUND);
                                onOneTimeResponseFromServer(price, itemUrl);
                            }
                        }
                    });
        }
    }

    public void showNotification(String body) {
        String standardTitle = getString(R.string.app_name);
        showNotification(standardTitle, body);
    }


    public void showNotification(String title, String body) {

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = getString(R.string.app_name) + "Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_money)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    public void onOneTimeResponseFromServer(int priceFromServer, String itemUrl) {
        if (priceFromServer == StoreScraper.PRICE_NOT_FOUND) {
            return;
        }

        int itemId = databaseHelper.getItemId(itemUrl);

        int previousPriceFromDb = databaseHelper.getLastPrice(itemId);
        if (hasPriceDroppedEnoughToShowNotification(priceFromServer, previousPriceFromDb)) {
            String itemTitle = databaseHelper.getItemTitle(itemId);
            String storeTitle = databaseHelper.getStoreTitle(itemId);
            String notificationTitle = itemTitle + " (" + storeTitle + ")";
            String notificationText = getString(R.string.price_dropped_notification_text, priceFromServer, previousPriceFromDb);
            showNotification(notificationTitle, notificationText);
        }

        long currentTimestamp = System.currentTimeMillis();
        databaseHelper.addPrice(itemId, priceFromServer, currentTimestamp);

        itemsListMultipleStoresAdapter.products = databaseHelper.getAllProducts();
        itemsListMultipleStoresAdapter.notifyDataSetChanged();
    }

    public boolean hasPriceDroppedEnoughToShowNotification(int currentPrice, int previousPrice) {
        final double PRICE_DROPPING_ENOUGH_TO_SEND_NOTIFICATION_COEFF = 0.001;
        if ( (previousPrice - currentPrice) / currentPrice > PRICE_DROPPING_ENOUGH_TO_SEND_NOTIFICATION_COEFF) {
            return true;
        } else {
            return false;
        }
    }


    public void onAddItemButtonClicked() {
        FragmentManager manager = getFragmentManager();
        AddItemDialogFragment addItemDialogFragment = new AddItemDialogFragment();
        addItemDialogFragment.show(manager, addItemDialogFragment.FRAGMENT_TAG);
    }


}
