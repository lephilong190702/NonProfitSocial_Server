package com.csn.charity.firebase;

import java.io.FileInputStream;
import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Service
public class FirebaseConfig {

   @PostConstruct
   public void init() {
       FileInputStream serviceAccount;
       try {
           serviceAccount = new FileInputStream("./serviceAccountKey.json");

           FirebaseOptions options = new FirebaseOptions.Builder()
                   .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                   .build();

           FirebaseApp.initializeApp(options);
       } catch (Exception e) {
           e.printStackTrace();
       }

   }
}
