package com.anshmidt.pricemonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.anshmidt.pricemonitor.activities.MainActivity;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;

import javax.inject.Inject;

public class NotificationHelper {

    Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void showPriceDroppedNotificationIfNeeded(int itemId, int priceFromServer, DatabaseHelper databaseHelper) {
        if (priceFromServer == StoreScraper.PRICE_NOT_FOUND) {
            return;
        }

        int previousPriceFromDb = databaseHelper.getLastPrice(itemId);
        if (hasPriceDroppedEnoughToShowNotification(priceFromServer, previousPriceFromDb)) {
            String itemTitle = databaseHelper.getItemTitle(itemId);
            String storeTitle = databaseHelper.getStoreTitle(itemId);
            showPriceDroppedNotification(
                    itemTitle,
                    storeTitle,
                    priceFromServer,
                    previousPriceFromDb
            );
        }
    }

    private void showNotification(String title, String body) {

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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_money)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

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

    private void showPriceDroppedNotification(String itemTitle, String storeTitle, int priceFromServer, int previousPriceFromDb) {
        String notificationTitle = itemTitle + " (" + storeTitle + ")";
        String notificationText = context.getString(R.string.price_dropped_notification_text, priceFromServer, previousPriceFromDb);
        showNotification(notificationTitle, notificationText);
    }



    private boolean hasPriceDroppedEnoughToShowNotification(int currentPrice, int previousPrice) {
        final double PRICE_DROPPING_ENOUGH_TO_SEND_NOTIFICATION_COEFF = 0.01;
        if ( (previousPrice - currentPrice) / currentPrice > PRICE_DROPPING_ENOUGH_TO_SEND_NOTIFICATION_COEFF) {
            return true;
        } else {
            return false;
        }
    }
}
