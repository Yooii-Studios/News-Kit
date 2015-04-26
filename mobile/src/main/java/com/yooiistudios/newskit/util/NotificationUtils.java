package com.yooiistudios.newskit.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.ui.activity.MainActivity;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 4. 6.
 *
 * NotificationUtils
 *
 *  Notification 관리 유틸
 */
public class NotificationUtils {
    public static void issueAsync(final Context context) {
        new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                issue(context);
                return null;
            }
        }.execute();
    }

    public static void issue(Context context) {
        NotificationCompat.Builder builder = createNotificationBuilder(context);
        TaskStackBuilder stackBuilder = createTaskStackBuilder(context);
        Notification notification = createNotification(builder, stackBuilder);
        notify(context, notification);
    }

    private static NotificationCompat.Builder createNotificationBuilder(Context context) {
        Resources resources = context.getResources();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setColor(context.getResources().getColor(R.color.app_color_accent))
                        .setSmallIcon(R.drawable.ic_notification_24dp)
                        .setContentTitle(resources.getString(R.string.notification_title))
                        .setContentText(resources.getString(R.string.notification_description))
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
