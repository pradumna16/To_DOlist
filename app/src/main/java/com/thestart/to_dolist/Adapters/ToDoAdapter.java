package com.thestart.to_dolist.Adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.thestart.to_dolist.AddNewTask;
import com.thestart.to_dolist.MainActivity;
import com.thestart.to_dolist.Model.ToDoModel;
import com.thestart.to_dolist.R;
import com.thestart.to_dolist.Utils.DatabaseHandler;

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private DatabaseHandler db;
    private MainActivity activity;

    public ToDoAdapter(DatabaseHandler db, MainActivity activity) {
        this.db = db;
        this.activity = activity;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();

        final ToDoModel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));
        //here im adding reminder functions
        if (item.getReminderTime()!=null){
            holder.reminderTimeTextView.setText("Reminder " + convertTimeToString(item.getReminderTime()));
        }else {
            holder.reminderTimeTextView.setVisibility(View.GONE);
        }

        holder.reminderTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getReminderTime() == null){
                    showDateTimePicker(holder, item);
                }else {
                    showEditDateTimePicker(holder, item);
                }
            }
        });

        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    db.updateStatus(item.getId(), 1);
                } else {
                    db.updateStatus(item.getId(), 0);
                }
            }
        });
    }
    //showDateTimePicker and showEditDAteTimePicker methods
    private void showDateTimePicker(ViewHolder holder, ToDoModel item) {
        // Initialize calendar with current date and time
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Create time picker dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        // Save reminder time to database
                                        long reminderTime = calendar.getTimeInMillis();
                                        db.updateReminderTime(item.getId(), reminderTime);
                                        holder.reminderTimeTextView.setText("Reminder: " + convertTimeToString(reminderTime));
                                        holder.reminderTimeTextView.setVisibility(View.VISIBLE);
                                    }
                                }, hour, minute, false);
                        timePickerDialog.show();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showEditDateTimePicker(ViewHolder holder, ToDoModel item) {
        // Initialize calendar with current date and time
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(item.getReminderTime());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Create time picker dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        // Save updated reminder time to database
                                        long reminderTime = calendar.getTimeInMillis();
                                        db.updateReminderTime(item.getId(), reminderTime);
                                        holder.reminderTimeTextView.setText("Reminder: " + convertTimeToString(reminderTime));
                                    }
                                }, hour, minute, false);
                        timePickerDialog.show();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private String convertTimeToString(Long reminderTime) {
        if(reminderTime==null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a" , Locale.getDefault());
        Date date = new Date(reminderTime);
        return sdf.format(date);
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView reminderTimeTextView;
        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            reminderTimeTextView = view.findViewById(R.id.reminderTimeTextView);
        }
    }
}