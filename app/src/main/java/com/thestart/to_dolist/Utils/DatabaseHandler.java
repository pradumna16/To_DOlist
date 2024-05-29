package com.thestart.to_dolist.Utils;

import static com.thestart.to_dolist.MainActivity.CHANNEL_ID;
import static com.thestart.to_dolist.MainActivity.NOTIFICATION_ID;
import static com.thestart.to_dolist.Utils.NotificationUtils.showNotification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import com.thestart.to_dolist.MainActivity;
import com.thestart.to_dolist.Model.ToDoModel;
import com.thestart.to_dolist.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String NAME = "toDoListDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String REMINDER_TIME = "reminder_time";
    private Context context;
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, "
            + STATUS + " INTEGER, " + REMINDER_TIME + " INTEGER)" ;

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    public void updateReminderTime(int id, long reminderTime) {
        ContentValues cv = new ContentValues();
        cv.put(REMINDER_TIME, reminderTime);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        // Create tables again
        onCreate(db);
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }
    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public void insertTask(ToDoModel task){
        if (db == null || !db.isOpen()) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, 0);
        long insertedId = db.insert(TODO_TABLE, null, cv);
        if (insertedId != -1) {
            // Task inserted successfully, now show notification
            showNotification(context, task.getTask(), task.getReminderTime());
        } else {
            // Handle insertion failure
            Log.e("DatabaseHandler", "Failed to insert task into database");
        }
    }


    public List<ToDoModel> getAllTasks(){
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try{
            cur = db.query(TODO_TABLE, null, null, null, null, null, null, null);
            if(cur != null){
                if(cur.moveToFirst()){
                    do{
                        ToDoModel task = new ToDoModel();
                        task.setId(cur.getInt(cur.getColumnIndex(ID)));
                        task.setTask(cur.getString(cur.getColumnIndex(TASK)));
                        task.setStatus(cur.getInt(cur.getColumnIndex(STATUS)));
                        task.setReminderTime(cur.getLong(cur.getColumnIndex(REMINDER_TIME)));
                        taskList.add(task);
                    }
                    while(cur.moveToNext());
                }
            }
        }
        finally {
            db.endTransaction();
            assert cur != null;
            cur.close();
        }
        return taskList;
    }

    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void updateTask(int id, String task) {
        if (db == null || !db.isOpen()) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteTask(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
    }
}

