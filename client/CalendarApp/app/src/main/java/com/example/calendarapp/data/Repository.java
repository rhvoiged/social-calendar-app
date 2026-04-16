package com.example.calendarapp.data;
import com.example.calendarapp.model.Event;
import com.example.calendarapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONArray;
import org.json.JSONObject;

// bridge between api and ui

public class Repository {
    private final ApiService apiService;
    private final FirebaseFirestore db;
    private final String WEATHER_API_KEY = "YOUR_OPENWEATHER_API_KEY";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public Repository() {
        this.apiService = ApiClient.getApiService();
        this.db = FirebaseFirestore.getInstance();
    }

    // login
    public void login(String email, String password, RepositoryCallback<User> callback) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);
        apiService.login(credentials).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {callback.onSuccess(response.body());}
                else if (response.code() == 401) {callback.onError("Email or password is incorrect");}
                else {callback.onError("Server error (" + response.code() + ")");}
            }
            @Override public void onFailure(Call<User> call, Throwable t) {callback.onError("Network error");}
        });
    }

    // register
    public void register(User user, RepositoryCallback<User> callback) {
        apiService.register(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else if (response.code() == 409) {callback.onError("User already exists");}
                else {callback.onError("Registration failed");}
            }
            @Override public void onFailure(Call<User> call, Throwable t) {callback.onError("Network error");}
        });
    }

    // asks for the user's calendar
    public void getMyCalendar(Integer userId, RepositoryCallback<List<Event>> callback) {
        apiService.getMyCalendar(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<List<Event>> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

   // asks for the friend's calendar
    public void getFriendCalendar(Integer friendId, Integer requesterId, RepositoryCallback<List<Event>> callback) {
        apiService.getFriendCalendar(friendId, requesterId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<List<Event>> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // search for users
    public void searchUsers(String query, Integer currentUserId, RepositoryCallback<List<User>> callback) {
        apiService.searchUsers(query, currentUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // asks for the user's friend requests
    public void getFriendRequests(Integer userId, RepositoryCallback<List<User>> callback) {
        apiService.getFriendRequests(userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // accepts a friend request
    public void acceptFriend(Integer userId, Integer requesterId, RepositoryCallback<Void> callback) {
        apiService.acceptFriend(userId, requesterId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {callback.onSuccess(null);}
                else {callback.onError("Error: " + response.code());}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // declines a friend request
    public void declineFriend(Integer userId, Integer requesterId, RepositoryCallback<Void> callback) {
        apiService.declineFriend(userId, requesterId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {callback.onSuccess(null);}
                else {callback.onError("Error: " + response.code());}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // deletes a friend
    public void removeFriend(Integer userId, Integer friendId, RepositoryCallback<Void> callback) {
        apiService.removeFriend(userId, friendId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {callback.onSuccess(null);}
                else {callback.onError("Error: " + response.code());}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // adds a new event
    public void addEvent(Event event, RepositoryCallback<Event> callback) {
        apiService.addEvent(event).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<Event> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // deletes an event
    public void deleteEvent(Integer eventId, RepositoryCallback<Void> callback) {
        apiService.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {callback.onSuccess(null);}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // asks for the user's friend list
    public void getFriends(Integer userId, RepositoryCallback<List<User>> callback) {
        apiService.getFriends(userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // sends a friend request
    public void addFriend(Integer userId, Integer friendId, RepositoryCallback<Void> callback) {
        apiService.addFriend(userId, friendId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {callback.onSuccess(null);}
                else {callback.onError("Error: " + response.code());}
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // overlapping calendars
    public void getOverlapCalendar(Integer userId, Integer friendId, String date, RepositoryCallback<List<Event>> callback) {
        apiService.getOverlapCalendar(userId, friendId, date).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful()) {callback.onSuccess(response.body());}
                else {callback.onError("Error");}
            }
            @Override public void onFailure(Call<List<Event>> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    // saves weather forecast to Firestore
    public void fetchAndStoreForecast(String city) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&units=metric&appid=" + WEATHER_API_KEY;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(okhttp3.Call call, IOException e) {}
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray list = json.getJSONArray("list");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject dataObj = list.getJSONObject(i);
                            String dt_txt = dataObj.getString("dt_txt"); 
                            String date = dt_txt.split(" ")[0];
                            double temp = dataObj.getJSONObject("main").getDouble("temp");
                            String desc = dataObj.getJSONArray("weather").getJSONObject(0).getString("description");
                            String weatherInfo = desc + ", " + Math.round(temp) + "°C";
                            Map<String, Object> data = new HashMap<>();
                            data.put("info", weatherInfo);
                            db.collection("forecast").document(date).set(data);
                        }
                    } catch (Exception e) {e.printStackTrace();}
                }
            }
        });
    }

    // gets weather forecast from Firestore
    public void getWeatherForDate(String date, RepositoryCallback<String> callback) {
        db.collection("forecast").document(date).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {callback.onSuccess(documentSnapshot.getString("info"));}
                else {callback.onSuccess(null);}
            }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // update fcm token
    public void updateFcmToken(Integer userId, String token) {
        updateFcmTokenWithReminders(userId, token, null, new RepositoryCallback<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onError(String error) {}
        });
    }

    // update fcm token and reminder
    public void updateFcmTokenWithReminders(Integer userId, String token, Integer reminderMinutes, RepositoryCallback<Void> callback) {
        apiService.updateFcmToken(userId, token, reminderMinutes).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {callback.onSuccess(null);}
                else {callback.onError("Sync Error: " + response.code());}
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {callback.onError(t.getMessage());}
        });
    }

    public interface RepositoryCallback<T> { void onSuccess(T result); void onError(String error);}
}
