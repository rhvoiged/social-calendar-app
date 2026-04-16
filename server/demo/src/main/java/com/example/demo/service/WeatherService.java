package com.example.demo.service;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.List;

// service that fetches real-time weather data from OpenWeatherMap API

@Service
public class WeatherService {

    @Value("${weather.api.key:}")
    private String apiKey;

    // HTTP client used to send requests to external APIs
    private final RestTemplate restTemplate = new RestTemplate();

    public String getWeather(String city) {
        if (apiKey == null || apiKey.isEmpty()) return "Weather unavailable";
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric&lang=en";
            // execute the GET request and map the JSON response to a Java Map
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            if (resp != null && resp.containsKey("main")) {
                // navigate through the JSON structure (Main -> temp)
                double temp = (double) ((Map) resp.get("main")).get("temp");
                // navigate through the JSON structure (Weather list -> first item -> description)
                String desc = (String) ((Map) ((List) resp.get("weather")).get(0)).get("description");
                // return formatted text: "22.5°C, clear sky"
                return String.format("%.1f°C, %s", temp, desc);
            }
        } catch (Exception e) {return "Weather error";}
        return "City not found";
    }
}
