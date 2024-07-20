package com.example.Library_Bee.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@EnableScheduling
@SpringBootApplication
public class DemoLibraryBeeApplication {

	private static FirebaseApp firebaseApp;

	static {
		try {
			ClassLoader classLoader = DemoLibraryBeeApplication.class.getClassLoader();
			FileInputStream serviceAccount = new FileInputStream(Objects.requireNonNull(classLoader.getResource("serviceAccountKey.json")).getFile());

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://library-bee-864f6-default-rtdb.firebaseio.com")
					.build();

			firebaseApp = FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoLibraryBeeApplication.class, args);
	}


}