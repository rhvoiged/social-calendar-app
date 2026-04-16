package com.example.calendarapp.data;
import com.example.calendarapp.model.Event;
import com.example.calendarapp.model.User;
import okhttp3.ResponseBody;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

// retrofit
// @POST             sends data to sv
// @GET               requests data from sv
// @DELETE         deletes data from sv
// @Body             sends object as JSON
// @Query            ads a new parameter to the end of a URL (.../api/weather?city=Bucharest)
// @Path              friends/{userId}     ->     friends/5
// Call<User>       sv responds with the date of a user
// Call<Void>       no data expected


public interface ApiService {
    @POST("register") Call<User> register(@Body User user);
    @POST("login") Call<User> login(@Body Map<String, String> credentials);
    @POST("updateToken") Call<Void> updateFcmToken(@Query("userId") Integer userId, @Query("token") String token, @Query("reminderMinutes") Integer reminderMinutes);
    @GET("friends/{userId}") Call<List<User>> getFriends(@Path("userId") Integer userId);
    @GET("friendRequests/{userId}") Call<List<User>> getFriendRequests(@Path("userId") Integer userId);
    @GET("users/search") Call<List<User>> searchUsers(@Query("query") String query, @Query("currentUserId") Integer currentUserId);
    @POST("addFriend") Call<ResponseBody> addFriend(@Query("userId") Integer userId, @Query("friendId") Integer friendId);
    @POST("acceptFriend") Call<ResponseBody> acceptFriend(@Query("userId") Integer userId, @Query("friendId") Integer requesterId);
    @POST("removeFriend") Call<ResponseBody> declineFriend(@Query("userId") Integer userId, @Query("friendId") Integer requesterId);
    @POST("removeFriend") Call<ResponseBody> removeFriend(@Query("userId") Integer userId, @Query("friendId") Integer friendId);
    @GET("myCalendar") Call<List<Event>> getMyCalendar(@Query("userId") Integer userId);
    @GET("friendCalendar/{friendId}") Call<List<Event>> getFriendCalendar(@Path("friendId") Integer friendId, @Query("requesterId") Integer requesterId);
    @GET("overlapCalendar") Call<List<Event>> getOverlapCalendar(@Query("userId") Integer userId, @Query("friendId") Integer friendId, @Query("date") String date);
    @POST("addEvent") Call<Event> addEvent(@Body Event event);
    @DELETE("deleteEvent") Call<Void> deleteEvent(@Query("eventId") Integer eventId);
    @GET("weather") Call<String> getWeather(@Query("city") String city);
}
