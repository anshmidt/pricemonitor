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

import com.anshmidt.pricemonitor.scrapers.StoreScraper;
import com.anshmidt.pricemonitor.scrapers.StoreScraperFactory;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ServerRequestsWorker extends Worker {

    public interface ResponseListener {
        void onResponse(int price, String url);
    }

    Context context;
    DatabaseHelper databaseHelper;
    StoreScraperFactory storeScraperFactory;
    public static final String KEY_PRICE = "key_price";
    public static final String KEY_ITEM_URL = "key_item_url";
    public static final String KEY_ITEM_ID = "key_item_id";

    final String LOG_TAG = ServerRequestsWorker.class.getSimpleName();


    public ServerRequestsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        databaseHelper = DatabaseHelper.getInstance(context);
        storeScraperFactory = new StoreScraperFactory(context);

    }


    @NonNull
    @Override
    public Result doWork() {
//        displayNotification("worker", "worker works");

        final int DEFAULT_ITEM_ID = -1;
        int itemId = getInputData().getInt(KEY_ITEM_ID, DEFAULT_ITEM_ID);
//        String title = databaseHelper.getStoreTitle(itemId);
        String storeUrl = databaseHelper.getStoreUrl(itemId);

        Log.d(LOG_TAG, "Sending request for itemId: " + itemId + " ("+databaseHelper.getItemTitle(itemId)+" from store "+storeUrl+")");


        StoreScraper storeScraper = storeScraperFactory.getStoreScraper(storeUrl);

        String itemUrl = databaseHelper.getItemUrl(itemId);
        int itemPrice = storeScraper.sendSynchronousRequest(itemUrl);

        Log.d(LOG_TAG, "Received response for itedId " + itemId + ": price == " + itemPrice);

        Data outputData = new Data.Builder()
                .putInt(KEY_PRICE, itemPrice)
                .putString(KEY_ITEM_URL, itemUrl)
                .build();

        writeResponseToDb(itemId, itemPrice);

        return Result.success(outputData);
    }



    private void displayNotification(String title, String task) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("simplifiedcoding", "simplifiedcoding", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "simplifiedcoding")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(1, notification.build());
    }

    private void writeResponseToDb(int itemId, int priceFromServer) {
        if (priceFromServer == StoreScraper.PRICE_NOT_FOUND) {
            return;
        }

        int previousPriceFromDb = databaseHelper.getLastPrice(itemId);
        if (hasPriceDroppedEnoughToShowNotification(priceFromServer, previousPriceFromDb)) {
            String itemTitle = databaseHelper.getItemTitle(itemId);
            String storeTitle = databaseHelper.getStoreTitle(itemId);
            String notificationTitle = itemTitle + " (" + storeTitle + ")";
            String notificationText = context.getString(R.string.price_dropped_notification_text, priceFromServer, previousPriceFromDb);
            showNotification(notificationTitle, notificationText);
        }

        long currentTimestamp = System.currentTimeMillis();
        databaseHelper.addPrice(itemId, priceFromServer, currentTimestamp);
    }

    public boolean hasPriceDroppedEnoughToShowNotification(int currentPrice, int previousPrice) {
        if (previousPrice == StoreScraper.PRICE_NOT_FOUND) {
            return false;
        }
        if (currentPrice < previousPrice) {
            return true;
        } else {
            return false;
        }
    }

    public void showNotification(String body) {
        String standardTitle = context.getString(R.string.app_name);
        showNotification(standardTitle, body);
    }


    public void showNotification(String title, String body) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = context.getString(R.string.app_name) + "Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_money)
                .setContentTitle(title)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }




}
