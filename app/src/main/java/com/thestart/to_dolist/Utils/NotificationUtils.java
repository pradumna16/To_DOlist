package com.thestart.to_dolist.Utils;

import static com.thestart.to_dolist.MainActivity.CHANNEL_ID;
import static com.thestart.to_dolist.MainActivity.NOTIFICATION_ID;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.thestart.to_dolist.MainActivity;
import com.thestart.to_dolist.R;

public class NotificationUtils {
    public static void showNotification(Context context, String task, Long reminderTime) {
        if (reminderTime != null && reminderTime > System.currentTimeMillis()) {
            // Inflate the notification layout
            RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

            // Set the task text
            notificationLayout.setTextViewText(R.id.notificationContent, task);
            // Create an intent to open the app when the notification is clicked
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            // Create the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("ToDo Reminder")
                    .setContentText(task)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);


            builder.setContentIntent(pendingIntent);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // Calculate the time difference between the current time and the reminder time
                long currentTime = System.currentTimeMillis();
                long delayInMillis = reminderTime - currentTime;

                // Create a pending intent for the notification
                PendingIntent notificationIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Schedule the notification to be shown at the specified reminder time
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMillis, notificationIntent);

            } else {
                Log.e("DatabaseHandler", "Failed to schedule notification: alarmManager is null");
            }

            // Show the notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            } else {
                // Handle the case where notification manager is null
                Log.e("DatabaseHandler", "Failed to show notification: notificationManager is null");
            }
        }else {
            Log.e("NotificationUtils", "Reminder time is null");
        }
    }
}
