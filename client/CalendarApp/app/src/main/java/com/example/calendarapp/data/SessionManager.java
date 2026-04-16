package com.example.calendarapp.data;
import android.content.Context;
import android.content.SharedPreferences;

// saves some of the data from the user in the device

public class SessionManager {
    private final SharedPreferences prefs;
    public SessionManager(Context context) {prefs = context.getSharedPreferences("CalendarAppPrefs", Context.MODE_PRIVATE);}
    public void saveUser(Integer userId, String email, String name) {prefs.edit().putInt("userId", userId == null ? -1 : userId).putString("userEmail", email).putString("userName", name).apply();}
    public Integer getUserId() {int id = prefs.getInt("userId", -1);return id == -1 ? null : id;}
    public String getUserEmail() { 
        return prefs.getString("userEmail", null); 
    }
    public String getUserName() { 
        return prefs.getString("userName", null); 
    }
    public void logout() { 
        prefs.edit().clear().apply(); 
    }
}
