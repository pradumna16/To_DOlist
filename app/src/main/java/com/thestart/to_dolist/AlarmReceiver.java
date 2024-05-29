package com.thestart.to_dolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thestart.to_dolist.Utils.NotificationUtils;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the task from the intent
        String task = intent.getStringExtra("task");
        Long reminderTime = intent.getLongExtra("reminderTime",0);
        // Show the notification
        NotificationUtils.showNotification(context, task, reminderTime);
    }
}
