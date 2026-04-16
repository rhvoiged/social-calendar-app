package com.example.calendarapp;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.example.calendarapp.databinding.ActivityMainBinding;
import com.example.calendarapp.databinding.DialogAddEventBinding;
import com.example.calendarapp.model.Event;
import com.example.calendarapp.ui.FriendsActivity;
import com.example.calendarapp.ui.LoginActivity;
import com.example.calendarapp.ui.RemindersActivity;
import com.example.calendarapp.ui.WeatherActivity;
import com.example.calendarapp.viewmodel.MainViewModel;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.messaging.FirebaseMessaging;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// main screen

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private SessionManager sessionManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final int HOUR_HEIGHT_DP = 60;
    private Calendar currentWeekStart;
    private Repository repository;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private FrameLayout timeLineContainer;

    //perms for notifcation
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {if (isGranted) {syncToken();}});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new Repository();
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {getSupportActionBar().setDisplayShowTitleEnabled(false);}
        
        sessionManager = new SessionManager(this);
        Integer userId = sessionManager.getUserId();
        
        if (userId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);}
        syncToken();
        
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);
        while (currentWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {currentWeekStart.add(Calendar.DAY_OF_YEAR, -1);}
        setupTimeGrid();
        
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.events.observe(this, this::drawEvents);
        viewModel.isLoading.observe(this, loading -> binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        
        viewModel.eventAdded.observe(this, s -> {if (s != null && s) {viewModel.refreshCalendar(userId);viewModel.resetStatus();}});
        viewModel.eventDeleted.observe(this, s -> {if (s != null && s) {viewModel.refreshCalendar(userId);viewModel.resetStatus();}});
        viewModel.fetchMyCalendar(userId);
        
        binding.btnPrevWeek.setOnClickListener(v -> changeWeek(-1));
        binding.btnNextWeek.setOnClickListener(v -> changeWeek(1));
        
        binding.tvMonthYear.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                currentWeekStart.set(y, m, d);
                while (currentWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {currentWeekStart.add(Calendar.DAY_OF_YEAR, -1);}
                updateHeader();
                viewModel.refreshCalendar(userId);
                updateTimeLine();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        
        binding.fabAddEvent.setOnClickListener(v -> showAddEventDialog(userId));
        
        repository.fetchAndStoreForecast("Targu Mures");
        updateHeader();
        
        timeHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTimeLine();
                timeHandler.postDelayed(this, 60000);
            }
        });
        
        binding.getRoot().postDelayed(this::scrollToCurrentTime, 800);
    }


    private void syncToken() {FirebaseMessaging.getInstance().getToken().addOnCompleteListener(t -> {if (t.isSuccessful()) {repository.updateFcmToken(sessionManager.getUserId(), t.getResult());}});}

    // scroll to current time and draw a red line
    private void scrollToCurrentTime() {
        if (binding == null) return;
        Calendar now = Calendar.getInstance();
        int yPos = (int) (((now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE)) * HOUR_HEIGHT_DP * getResources().getDisplayMetrics().density / 60);
        binding.scrollView.smoothScrollTo(0, Math.max(0, yPos - (binding.scrollView.getHeight() / 2)));
    }

    private void updateTimeLine() {
        if (binding == null || binding.eventsContainer == null) return;
        if (timeLineContainer != null) {binding.eventsContainer.removeView(timeLineContainer);}
        
        Calendar now = Calendar.getInstance();
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 7);
        
        if (!now.before(currentWeekStart) && now.before(weekEnd)) {
            float dens = getResources().getDisplayMetrics().density;
            int dayWidth = (getResources().getDisplayMetrics().widthPixels - (int)(50 * dens)) / 7;
            
            timeLineContainer = new FrameLayout(this);
            View line = new View(this);
            line.setBackgroundColor(Color.RED);
            timeLineContainer.addView(line, new FrameLayout.LayoutParams(dayWidth, (int)(2 * dens), Gravity.CENTER_VERTICAL));
            
            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.time_dot);
            timeLineContainer.addView(dot, new FrameLayout.LayoutParams((int)(8 * dens), (int)(8 * dens), Gravity.START | Gravity.CENTER_VERTICAL));
            
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dayWidth, (int)(10 * dens));
            lp.topMargin = (int) (((now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE)) * HOUR_HEIGHT_DP * dens / 60) - (int)(5 * dens);
            lp.leftMargin = (now.get(Calendar.DAY_OF_WEEK) - 1) * dayWidth;
            
            binding.eventsContainer.addView(timeLineContainer, lp);
        }
    }

    // next/prev week
    private void changeWeek(int delta) {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, delta);
        updateHeader();
        viewModel.refreshCalendar(sessionManager.getUserId());
        updateTimeLine();
    }

    // matches day of week with date
    private void updateHeader() {
        binding.tvMonthYear.setText(monthYearFormat.format(currentWeekStart.getTime()));
        Calendar temp = (Calendar) currentWeekStart.clone();
        TextView[] views = {binding.tvDay0, binding.tvDay1, binding.tvDay2, binding.tvDay3, binding.tvDay4, binding.tvDay5, binding.tvDay6};
        String[] names = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        
        for (int i = 0; i < 7; i++) {
            views[i].setText(names[i] + "\n" + temp.get(Calendar.DAY_OF_MONTH));
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // hour grid
    private void setupTimeGrid() {
        float dens = getResources().getDisplayMetrics().density;
        for (int i = 0; i < 24; i++) {
            TextView l = new TextView(this);
            l.setLayoutParams(new LinearLayout.LayoutParams((int) (50 * dens), (int) (HOUR_HEIGHT_DP * dens)));
            l.setText(String.format(Locale.getDefault(), "%02d:00", i));
            l.setTextColor(Color.GRAY);
            l.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            l.setPadding(0, 8, 0, 0);
            l.setTextSize(10);
            binding.timeLabels.addView(l);
        }
    }

    // maps events on calendar
    private void drawEvents(List<Event> events) {
        binding.eventsContainer.removeAllViews();
        float dens = getResources().getDisplayMetrics().density;

        // hour separators
        for (int i = 0; i < 24; i++) {
            View l = new View(this);
            l.setBackgroundColor(Color.parseColor("#333333"));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            lp.topMargin = (int) (i * HOUR_HEIGHT_DP * dens);
            binding.eventsContainer.addView(l, lp);
        }
        
        if (events == null) {updateTimeLine();return;}
        
        int dayWidth = (getResources().getDisplayMetrics().widthPixels - (int)(50 * dens)) / 7;
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 7);

        // group events by day
        List<Event>[] byDay = new List[7];
        for (int i = 0; i < 7; i++) {byDay[i] = new ArrayList<>();}
        for (Event e : events) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dt = sdf.parse(e.date);
                if (dt != null && !dt.before(currentWeekStart.getTime()) && dt.before(weekEnd.getTime())) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(dt);
                    byDay[c.get(Calendar.DAY_OF_WEEK) - 1].add(e);
                }
            } catch (Exception ignored) {}
        }
        
        for (int d = 0; d < 7; d++) {
            List<Event> dayEvs = byDay[d];
            if (dayEvs.isEmpty()) continue;
            // sort by start time
            Collections.sort(dayEvs, (e1, e2) -> e1.startTime.compareTo(e2.startTime));
            List<List<Event>> clusters = new ArrayList<>();
            List<Event> curClust = new ArrayList<>();
            int clEnd = -1;
            // cluster events that overlap
            for (Event e : dayEvs) {
                int s = toMin(e.startTime);
                int end = toMin(e.endTime);
                if (s >= clEnd && !curClust.isEmpty()) {
                    clusters.add(new ArrayList<>(curClust));
                    curClust.clear();
                    clEnd = -1;
                }
                curClust.add(e);
                clEnd = Math.max(clEnd, end);
            }
            if (!curClust.isEmpty()) clusters.add(curClust);
            // cluster separation
            for (List<Event> clust : clusters) {
                List<List<Event>> cols = new ArrayList<>();
                for (Event e : clust) {
                    boolean placed = false;
                    for (List<Event> col : cols) {
                        if (!isOverlap(e, col.get(col.size() - 1))) {
                            col.add(e);
                            placed = true;
                            break;
                        }
                    }
                    if (!placed) {
                        List<Event> nc = new ArrayList<>();
                        nc.add(e);
                        cols.add(nc);
                    }
                }
                // distributes events into columns to manage their positioning
                for (int ci = 0; ci < cols.size(); ci++) {for (Event e : cols.get(ci)) {addEv(e, d, ci, cols.size(), dayWidth, dens);}}
            }
        }
        updateTimeLine(); // update red line
    }

    // draws event on calendar
    private void addEv(Event e, int d, int ci, int nc, int dw, float dens) {
        int s = toMin(e.startTime);
        int dur = toMin(e.endTime) - s;
        int w = (dw - 4) / nc;
        
        TextView v = new TextView(this);
        v.setText(e.title);
        v.setBackgroundResource(R.drawable.event_bg);
        v.setTextColor(Color.WHITE);
        v.setPadding(4, 2, 4, 2);
        v.setTextSize(nc > 2 ? 7 : 8);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        v.setGravity(Gravity.CENTER);
        
        v.setOnClickListener(view -> new AlertDialog.Builder(this).setTitle(e.title).setMessage(e.description + "\n\n" + e.startTime + " - " + e.endTime).setPositiveButton("OK", null).setNegativeButton("Delete", (di, wi) -> viewModel.deleteEvent(e.id)).show());
                
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w - 2, (int) (dur * HOUR_HEIGHT_DP * dens / 60));
        lp.topMargin = (int) (s * HOUR_HEIGHT_DP * dens / 60);
        lp.leftMargin = d * dw + 2 + (ci * w);
        binding.eventsContainer.addView(v, lp);
    }

    // checks if 2 events overlap
    private boolean isOverlap(Event e1, Event e2) {return toMin(e1.startTime) < toMin(e2.endTime) && toMin(e2.startTime) < toMin(e1.endTime);}

    // convers time of day to minutes since midnight
    private int toMin(String t) {
        try {
            String[] p = t.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {return 0;}
    }

    // add new event menu
    private void showAddEventDialog(Integer userId) {
        DialogAddEventBinding b = DialogAddEventBinding.inflate(LayoutInflater.from(this));
        AlertDialog d = new AlertDialog.Builder(this).setTitle("New Event").setView(b.getRoot()).setPositiveButton("Add", null).setNegativeButton("Cancel", null).create();
        
        d.setOnShowListener(di -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = b.etTitle.getText().toString().trim();
            String dt = b.etDate.getText().toString().trim();
            String s = b.etStartTime.getText().toString().trim();
            String e = b.etEndTime.getText().toString().trim();
            
            if (t.isEmpty() || dt.isEmpty() || s.isEmpty() || e.isEmpty()) {Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();return;}
            if (toMin(e) <= toMin(s)) {Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();return;}
            
            viewModel.addEvent(new Event(null, t, b.etDescription.getText().toString().trim(), dt, s, e, userId));
            d.dismiss();
        }));
        
        b.etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, day) -> b.etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, day)), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        
        b.etStartTime.setOnClickListener(v -> showPicker("Start", time -> b.etStartTime.setText(time)));
        b.etEndTime.setOnClickListener(v -> showPicker("End", time -> b.etEndTime.setText(time)));
        d.show();
    }

    // pick time
    private void showPicker(String t, TimePickerCallback cb) {
        MaterialTimePicker p = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(12).setMinute(0).setTitleText(t).build();
        p.addOnPositiveButtonClickListener(v -> cb.onTimeSelected(String.format(Locale.getDefault(), "%02d:%02d", p.getHour(), p.getMinute())));
        p.show(getSupportFragmentManager(), "TP");
    }

    // reusable callback time picker interface
    private interface TimePickerCallback { void onTimeSelected(String time);}

    // butoane meniu
    @Override public boolean onCreateOptionsMenu(Menu menu) {getMenuInflater().inflate(R.menu.main_menu, menu);return true;}
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {sessionManager.logout();goToLogin();return true;}
        if (item.getItemId() == R.id.action_friends) {startActivity(new Intent(this, FriendsActivity.class));return true;}
        if (item.getItemId() == R.id.action_weather) {startActivity(new Intent(this, WeatherActivity.class));return true;}
        if (item.getItemId() == R.id.action_reminders) {startActivity(new Intent(this, RemindersActivity.class));return true;}
        return super.onOptionsItemSelected(item);
    }

    // at start, if not logged in, go to log in screen
    private void goToLogin() {startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));finish();}
}
