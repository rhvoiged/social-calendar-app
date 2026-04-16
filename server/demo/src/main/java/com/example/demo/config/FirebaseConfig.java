package com.example.demo.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

// automatically starts the Firebase service when the server boots up  and establishes a secure link

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // reading the service account key from resources
            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            if (!resource.exists()) {System.err.println("Firebase config file not found in resources.");return;}

            try (InputStream is = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(is)).build();

                if (FirebaseApp.getApps().isEmpty()) {FirebaseApp.initializeApp(options);}
                System.out.println("Firebase has been initialized successfully.");
            }
        } catch (IOException e) {System.err.println("Error initializing Firebase: " + e.getMessage());}
    }
}
