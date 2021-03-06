package com.anshmidt.pricemonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import com.anshmidt.pricemonitor.activities.MainActivity;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;

import javax.inject.Inject;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    Context context;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void showPriceDroppedNotificationIfNeeded(int currentPrice, int previousPrice, String productName, String storeName, int itemId) {
        if (currentPrice == StoreScraper.PRICE_NOT_FOUND) {
            return;
        }

        if (hasPriceDroppedEnoughToShowNotification(currentPrice, previousPrice)) {
            showPriceDroppedNotification(
                    currentPrice,
                    previousPrice,
                    productName,
                    storeName,
                    itemId
            );
        }
    }

    private void showNotification(String title, String body, int itemId) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "channel-01";
        String channelName = context.getString(R.string.app_name) + "Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

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

        int notificationId = itemId;
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showPriceDroppedNotification(int currentPrice, int previousPrice, String productName, String storeName, int itemId) {
        String notificationTitle = productName + " (" + storeName + ")";
        String notificationText = context.getString(R.string.price_dropped_notification_text, currentPrice, previousPrice);
        showNotification(notificationTitle, notificationText, itemId);
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
