package com.anshmidt.pricemonitor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.anshmidt.pricemonitor.room.entity.Item;
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

//        Log.d(LOG_TAG, "worker works");
//        Data outputData = new Data.Builder()
//                .putString("asdf", "asdf")
//                .build();

        int itemId = getInputData().getInt(KEY_ITEM_ID, Item.ID_NOT_FOUND);
        String itemUrl = getInputData().getString(KEY_ITEM_URL);
        StoreScraper storeScraper = storeScraperFactory.getStoreScraperByItemUrl(itemUrl);

        Log.d(LOG_TAG, "Sending request for itemId: " + itemId + ", itemUrl: " + itemUrl);
        int itemPrice = storeScraper.sendSynchronousRequest(itemUrl);
        Log.d(LOG_TAG, "Received response for itemId " + itemId + ": price == " + itemPrice);

        Data outputData = new Data.Builder()
                .putInt(KEY_PRICE, itemPrice)
                .putInt(KEY_ITEM_ID, itemId)
                .build();


        return Result.success(outputData);
    }












}
