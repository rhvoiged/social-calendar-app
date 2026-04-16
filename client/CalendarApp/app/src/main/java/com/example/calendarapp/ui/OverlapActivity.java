package com.example.calendarapp.ui;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.example.calendarapp.databinding.ActivityOverlapBinding;
import com.example.calendarapp.model.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// overlapping yours and your friends' calendars

public class OverlapActivity extends AppCompatActivity {
    private ActivityOverlapBinding binding;
    private Repository repository;
    private Integer currentUserId, friendId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final int HOUR_HEIGHT_DP = 60;
    private Calendar currentWeekStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOverlapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        repository = new Repository();
        currentUserId = new SessionManager(this).getUserId();
        friendId = getIntent().getIntExtra("FRIEND_ID", -1);
        String friendName = getIntent().getStringExtra("FRIEND_NAME");
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {getSupportActionBar().setDisplayShowTitleEnabled(false);}
        
        binding.btnBack.setOnClickListener(v -> finish());
        
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);
        
        setupNavigation(friendName);
        setupTimeGrid();
        updateHeader(friendName);
        loadOverlapCalendar();
    }

    // next/prev week buttons
    private void setupNavigation(String friendName) {
        binding.btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            updateHeader(friendName);
            loadOverlapCalendar();
        });
        binding.btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            updateHeader(friendName);
            loadOverlapCalendar();
        });
        binding.tvMonthYear.setOnClickListener(v -> startActivity(new Intent(this, WeatherActivity.class)));
    }

    // updates the header with current dates + you and your friend's names
    private void updateHeader(String friendName) {
        binding.tvMonthYear.setText("Overlap: Me & " + (friendName != null ? friendName : "Friend") + "\n" + monthYearFormat.format(currentWeekStart.getTime()));
        Calendar temp = (Calendar) currentWeekStart.clone();
        TextView[] views = {binding.tvDay0, binding.tvDay1, binding.tvDay2, binding.tvDay3, binding.tvDay4, binding.tvDay5, binding.tvDay6};
        String[] names = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        
        for (int i = 0; i < 7; i++) {
            views[i].setText(names[i] + "\n" + temp.get(Calendar.DAY_OF_MONTH));
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // hours grid
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

    // loads overlapping calendars
    private void loadOverlapCalendar() {
        if (currentUserId == null || friendId == -1) return;
        repository.getOverlapCalendar(currentUserId, friendId, dateFormat.format(currentWeekStart.getTime()), new Repository.RepositoryCallback<List<Event>>() {
            @Override public void onSuccess(List<Event> r) {binding.progressBar.setVisibility(View.GONE);drawEvents(r);}
            @Override public void onError(String e) {binding.progressBar.setVisibility(View.GONE);}
        });
    }

    // maps events on the calendar
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
        
        if (events == null) return;
        
        int dayWidth = (getResources().getDisplayMetrics().widthPixels - (int)(50 * dens)) / 7;
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 7);
        
        List<Event>[] byDay = new List[7];
        for (int i = 0; i < 7; i++) {byDay[i] = new ArrayList<>();}
        // group events by day
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
            // sort by start time and by owner
            Collections.sort(dayEvs, (e1, e2) -> {
                boolean e1IsMine = e1.ownerId.equals(currentUserId);
                boolean e2IsMine = e2.ownerId.equals(currentUserId);
                if (e1IsMine != e2IsMine) {return e1IsMine ? -1 : 1;}
                return e1.startTime.compareTo(e2.startTime);
            });
            
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
                for (int ci = 0; ci < cols.size(); ci++) {for (Event e : cols.get(ci)) {addEv(e, d, ci, cols.size(), dayWidth, dens);}
                }
            }
        }
    }

    // draws event on calendar
    private void addEv(Event e, int d, int ci, int nc, int dw, float dens) {
        int s = toMin(e.startTime);
        int dur = toMin(e.endTime) - s;
        int w = (dw - 4) / nc;
        
        boolean isMine = e.ownerId.equals(currentUserId);
        TextView v = new TextView(this);
        v.setText(e.title);
        v.setBackgroundColor(isMine ? Color.parseColor("#4CAF50") : Color.parseColor("#2196F3"));
        v.setTextColor(Color.WHITE);
        v.setPadding(4, 2, 4, 2);
        v.setTextSize(nc > 2 ? 7 : 8);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        v.setGravity(Gravity.CENTER);
        
        v.setOnClickListener(view -> new AlertDialog.Builder(this).setTitle(e.title).setMessage((isMine ? "My Event" : "Friend's Event") + "\n\n" + e.description + "\n\n" + e.startTime + " - " + e.endTime).setPositiveButton("OK", null).show());
                
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w - 2, (int) (dur * HOUR_HEIGHT_DP * dens / 60));
        lp.topMargin = (int) (s * HOUR_HEIGHT_DP * dens / 60);
        lp.leftMargin = d * dw + 2 + (ci * w);
        binding.eventsContainer.addView(v, lp);
    }

    // checks for overlaps
    private boolean isOverlap(Event e1, Event e2) {return toMin(e1.startTime) < toMin(e2.endTime) && toMin(e2.startTime) < toMin(e1.endTime);}

    // converts time to minutes past midnight
    private int toMin(String t) {
        try {
            String[] p = t.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {return 0;}
    }
}
