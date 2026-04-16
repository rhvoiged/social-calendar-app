package com.example.calendarapp.ui;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.example.calendarapp.databinding.ActivityRemindersBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

// reminder settings screen

public class RemindersActivity extends AppCompatActivity {

    private static final String TAG = "RemindersActivity";
    private ActivityRemindersBinding binding;
    private FirebaseFirestore db;
    private Integer userId;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemindersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new Repository();
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {getSupportActionBar().setDisplayHomeAsUpEnabled(true);}
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        userId = new SessionManager(this).getUserId();

        if (userId == null) {Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();finish();return;}

        loadCurrentReminderSettings();
        binding.btnSaveReminder.setOnClickListener(v -> saveReminderSettings());
    }

    // loads previously set reminder settings from cloud
    private void loadCurrentReminderSettings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(String.valueOf(userId)).get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        if (task.getResult().contains("reminderMinutes")) {
                            Long minutes = task.getResult().getLong("reminderMinutes");
                            binding.etReminderMinutes.setText(String.valueOf(minutes));
                        }
                    }
                });
    }

    // syncs with cloud and server
    private void saveReminderSettings() {
        String minutesStr = binding.etReminderMinutes.getText().toString().trim();
        if (minutesStr.isEmpty()) {return;}

        final int minutes = Integer.parseInt(minutesStr);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveReminder.setEnabled(false);

        // sync with Firebase
        Map<String, Object> cloudData = new HashMap<>();
        cloudData.put("reminderMinutes", minutes);
        db.collection("users").document(String.valueOf(userId))
                .set(cloudData, com.google.firebase.firestore.SetOptions.merge());

        // sync with server
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            String token = task.isSuccessful() ? task.getResult() : null;
            
            repository.updateFcmTokenWithReminders(userId, token, minutes, new Repository.RepositoryCallback<Void>() {
                @Override public void onSuccess(Void result) {finishSave("Cloud & Server Synced!");}
                @Override public void onError(String error) {finishSave("Saved to Cloud, but Server sync failed.");}
            });
        });

        // failsafe executed if it takes longer than 4 seconds to sync
        new Handler(Looper.getMainLooper()).postDelayed(() -> {if (binding != null && !binding.btnSaveReminder.isEnabled()) {finishSave("Save request sent.");}}, 4000);
    }

    // finishes save procedure and gives feedback to user
    private void finishSave(String message) {
        runOnUiThread(() -> {
            if (binding != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveReminder.setEnabled(true);
                Toast.makeText(RemindersActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
