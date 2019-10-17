package com.anshmidt.pricemonitor;

import android.content.Context;
import android.util.Log;

import com.anshmidt.pricemonitor.activities.MainActivity;
import com.anshmidt.pricemonitor.dagger.AppModule;
import com.anshmidt.pricemonitor.dagger.DaggerAppComponent;
import com.anshmidt.pricemonitor.room.PricesRepository;
import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;
import com.anshmidt.pricemonitor.scrapers.StoreScraperFactory;

import java.util.Date;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ServerRequestsWorker extends Worker {

    public interface ResponseListener {
        void onResponse(int price, String url);
    }

    Context context;
    @Inject StoreScraperFactory storeScraperFactory;
    @Inject PricesRepository pricesRepository;
    @Inject NotificationHelper notificationHelper;
    public static final String KEY_ITEM_URL = "key_item_url";
    public static final String KEY_ITEM_ID = "key_item_id";

    private final String LOG_TAG = ServerRequestsWorker.class.getSimpleName();


    public ServerRequestsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
//        PriceMonitorApplication.getComponent().inject(this);
        DaggerAppComponent.builder()
                .appModule(new AppModule(context))
                .build()
                .inject(this);
        this.context = context;
    }


    @NonNull
    @Override
    public Result doWork() {

        int itemId = getInputData().getInt(KEY_ITEM_ID, Item.ID_NOT_FOUND);
        String itemUrl = getInputData().getString(KEY_ITEM_URL);

        if (itemUrl == null) {
            Log.d(LOG_TAG, "itemUrl is null for itemId: " + itemId);
        }

        StoreScraper storeScraper = storeScraperFactory.getStoreScraperByItemUrl(itemUrl);

        Log.d(LOG_TAG, "Sending request for itemId: " + itemId + ", itemUrl: " + itemUrl);
        int latestPrice = storeScraper.sendSynchronousRequest(itemUrl);
        Log.d(LOG_TAG, "Received response for itemId " + itemId + ": price == " + latestPrice);

        // Output data from PeriodicWorkRequest is not received by WorkManager design

        if (latestPrice == StoreScraper.PRICE_NOT_FOUND) {
            return Result.failure();
        } else {
            Date currentDate = new Date(System.currentTimeMillis());
            pricesRepository.addPrice(new Price(currentDate, itemId, latestPrice));

            int previousPrice = pricesRepository.getPreviousPriceForItem(itemId).price;
            Item item = pricesRepository.getItemById(itemId);
            String productName = pricesRepository.getProductByProductId(item.productId).name;
            String storeName = pricesRepository.getStoreByStoreId(item.storeId).name;

            notificationHelper.showPriceDroppedNotificationIfNeeded(
                    latestPrice,
                    previousPrice,
                    productName,
                    storeName,
                    itemId
            );

            return Result.success();
        }



    }












}
