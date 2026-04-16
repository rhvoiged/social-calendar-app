package com.example.calendarapp.data;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// 1. a request is made
// 2. retrofit parses the request
// 3. GSON  converts Java object to JSON
// 4. OkHttpClient sends JSON
// 5. sv recieves data
// 6. OkHttpClient delivers response to retrofit
// 7. GSON  converts JSON back to Java object
// 8. retrofit delivers response to caller

public class ApiClient {
    private static final String BASE_URL = "http://YOUR_SERVER_IP:8080/api/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        }
        return retrofit.create(ApiService.class);
    }
}
