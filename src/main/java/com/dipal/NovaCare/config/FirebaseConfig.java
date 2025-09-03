package com.dipal.NovaCare.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String firebaseBase64 = System.getenv("FIREBASE_CONFIG_BASE64");
        FirebaseOptions options;

        if (firebaseBase64 != null && !firebaseBase64.isEmpty()) {
            // ✅ Use environment variable
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseBase64);
            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        } else {
            // ✅ Fallback to local file
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-service-account.json");

            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        }

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
