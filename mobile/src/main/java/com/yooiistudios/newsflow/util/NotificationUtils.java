package com.yooiistudios.newsflow.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.ui.activity.MainActivity;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 4. 6.
 *
 * NotificationUtils
 *
 *  Notification 관리 유틸
 */
public class NotificationUtils {
    public static void issue(Context context) {
        NotificationCompat.Builder builder = createNotificationBuilder(context);
        TaskStackBuilder stackBuilder = createTaskStackBuilder(context);
        Notification notification = createNotification(builder, stackBuilder);
        notify(context, notification);
    }

    private static NotificationCompat.Builder createNotificationBuilder(Context context) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("News Arrived.")
                        .setContentText("Check out")
                        .setAutoCancel(true);

        Bitmap background = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.img_rss_url_failed
        );
        NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .setBackground(background);

        builder.extend(extender);

        return builder;
    }

    private static TaskStackBuilder createTaskStackBuilder(Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder;
    }

    private static Notification createNotification(NotificationCompat.Builder builder, TaskStackBuilder stackBuilder) {
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    private static void notify(Context context, Notification notification) {
        NotificationManager notificationManager
                = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1234, notification);
    }
}
