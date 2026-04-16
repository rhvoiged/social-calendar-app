package com.example.calendarapp.ui;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.calendarapp.adapter.WeatherAdapter;
import com.example.calendarapp.databinding.ActivityWeatherBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 5-day weather forecast

public class WeatherActivity extends AppCompatActivity {

    private ActivityWeatherBinding binding;
    private WeatherAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ascundem header-ul cu SUN, MON... deoarece acum arătăm doar o listă de 5 zile
        if (binding.getRoot().findViewById(com.example.calendarapp.R.id.toolbar).getParent() instanceof android.view.ViewGroup) {
            View headerDays = findViewById(com.example.calendarapp.R.id.headerDays);
            if (headerDays != null) {headerDays.setVisibility(View.GONE);}
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("5-Day Forecast");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        loadFiveDayForecast();
    }

    // prepares containers for the 5 days
    private void setupRecyclerView() {
        adapter = new WeatherAdapter();
        binding.rvWeather.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWeather.setAdapter(adapter);
    }

    // loads weather forecast from cloud
    private void loadFiveDayForecast() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // takes the 5 dates from Firestore sorted by date
        db.collection("forecast").orderBy("__name__", Query.Direction.ASCENDING).limit(5).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    List<Map<String, String>> weatherList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, String> item = new HashMap<>();
                        item.put("date", doc.getId());
                        item.put("info", doc.getString("info"));
                        weatherList.add(item);
                    }
                    if (weatherList.isEmpty()) {Toast.makeText(this, "No forecast data available in Cloud.", Toast.LENGTH_SHORT).show();}

                    adapter.setWeatherList(weatherList);
                }).addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
