package com.thestart.to_dolist;
import static com.thestart.to_dolist.Utils.NotificationUtils.showNotification;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thestart.to_dolist.Adapters.ToDoAdapter;
import com.thestart.to_dolist.Model.ToDoModel;
import com.thestart.to_dolist.Utils.DatabaseHandler;
import com.thestart.to_dolist.Utils.NotificationUtils;


import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener{

    //solely for notification and alarm only

    // Notification channel ID and name
     public static final String CHANNEL_ID = "ToDoReminderChannel";
     static final String CHANNEL_NAME = "ToDo Reminder";

    // Notification ID
    public static final int NOTIFICATION_ID = 123;

    // Alarm request code
    private static final int ALARM_REQUEST_CODE = 456;

    // Notification manager
    private NotificationManager notificationManager;

    private DatabaseHandler db;

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;

    private List<ToDoModel> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        createNotificationChannel();

        db = new DatabaseHandler(this);
        db.openDatabase();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db,MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        fab = findViewById(R.id.fab);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);

        tasksAdapter.setTasks(taskList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View addNewTaskLayout = getLayoutInflater().inflate(R.layout.new_task, null);

                // Find views in the inflated layout
                EditText newTaskText = addNewTaskLayout.findViewById(R.id.newTaskText);

                // Show the dialog with the inflated layout
                new AlertDialog.Builder(MainActivity.this)
                        .setView(addNewTaskLayout)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Get the task text after the user clicks Save
                                String task = newTaskText.getText().toString();
                                if (!task.isEmpty()) {
                                    long reminderTime = System.currentTimeMillis();
                                    ToDoModel newTask = new ToDoModel();
                                    newTask.setTask(task);
                                    newTask.setReminderTime(reminderTime);

                                    // Insert the task into the database
                                    db.insertTask(newTask);

                                    // Refresh the task list from the database
                                    taskList.clear();
                                    taskList.addAll(db.getAllTasks());
                                    tasksAdapter.notifyDataSetChanged();

                                    // Show the notification
                                    showNotification(MainActivity.this, task, reminderTime);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

    }
    private void createNotificationChannel() {
        // Check if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channel
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setDescription("Notification for ToDo reminders");

            // Get the notification manager
            notificationManager = getSystemService(NotificationManager.class);
            // Create the notification channel
            notificationManager.createNotificationChannel(channel);
            } else {
                // Log an error or handle the case where permission is not granted
                Log.e("NotificationUtils", "POST_NOTIFICATION permission not granted");
            }
        }
    }

    // Method to set alarm for reminder
    public void setAlarm(Context context, long reminderTime, String task) {
        // Create an intent to start the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("task", task);
        intent.putExtra("reminderTime", reminderTime);

        // Create a pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android M and above, use setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                // For older versions, use setExact
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        }
    }

    // Method to show notification



    @Override
    public void handleDialogClose(DialogInterface dialog){
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}