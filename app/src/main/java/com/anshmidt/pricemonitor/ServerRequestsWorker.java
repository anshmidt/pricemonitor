package com.anshmidt.pricemonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anshmidt.pricemonitor.activities.MainActivity;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;
import com.anshmidt.pricemonitor.scrapers.StoreScraperFactory;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ServerRequestsWorker extends Worker {

    public interface ResponseListener {
        void onResponse(int price, String url);
    }

    Context context;
    @Inject DatabaseHelper databaseHelper;
    @Inject StoreScraperFactory storeScraperFactory;
    @Inject NotificationHelper notificationHelper;
    public static final String KEY_PRICE = "key_price";
    public static final String KEY_ITEM_URL = "key_item_url";
    public static final String KEY_ITEM_ID = "key_item_id";

    final String LOG_TAG = ServerRequestsWorker.class.getSimpleName();


    public ServerRequestsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        PriceMonitorApplication.getComponent().inject(this);
        this.context = context;
    }


    @NonNull
    @Override
    public Result doWork() {

        final int DEFAULT_ITEM_ID = -1;
        int itemId = getInputData().getInt(KEY_ITEM_ID, DEFAULT_ITEM_ID);
        String storeUrl = databaseHelper.getStoreUrl(itemId);


        Log.d(LOG_TAG, "Sending request for itemId: " + itemId + " ("+databaseHelper.getItemTitle(itemId)+" from store "+storeUrl+")");

        StoreScraper storeScraper = storeScraperFactory.getStoreScraper(storeUrl);

        String itemUrl = databaseHelper.getItemUrl(itemId);
        int itemPrice = storeScraper.sendSynchronousRequest(itemUrl);

        Log.d(LOG_TAG, "Received response for itemId " + itemId + ": price == " + itemPrice);

        Data outputData = new Data.Builder()
                .putInt(KEY_PRICE, itemPrice)
                .putString(KEY_ITEM_URL, itemUrl)
                .build();


        if (itemPrice != StoreScraper.PRICE_NOT_FOUND) {
            notificationHelper.showPriceDroppedNotificationIfNeeded(itemId, itemPrice, databaseHelper);
            databaseHelper.addPriceWithCurrentTimestamp(itemId, itemPrice);
        }

        return Result.success(outputData);
    }












}
