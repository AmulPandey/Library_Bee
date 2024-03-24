package com.example.LibraryBee;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            // Extract the message from the data payload.
            String message = remoteMessage.getData().get("message");

            // Broadcast the received message to the fragment
            Intent intent = new Intent("MESSAGE_RECEIVED");
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}

