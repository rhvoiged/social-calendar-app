package com.example.calendarapp.fcm;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.calendarapp.MainActivity;
import com.example.calendarapp.R;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// manages notifications from firebase and ensures the token is updated

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // notification data
    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        String t = "Memento Eveniment";
        String m = "";
        
        if (msg.getNotification() != null) {
            t = msg.getNotification().getTitle();
            m = msg.getNotification().getBody();
        } else if (msg.getData().size() > 0) {
            t = msg.getData().getOrDefault("title", t);
            m = msg.getData().getOrDefault("message", "");
        }
        send(t, m);
    }

    // updates token
    @Override
    public void onNewToken(@NonNull String token) {
        Integer id = new SessionManager(this).getUserId();
        if (id != null) {new Repository().updateFcmToken(id, token);}
    }

    // sends notifications
    private void send(String t, String m) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String cid = "calendar_reminders_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(cid, "Calendar", NotificationManager.IMPORTANCE_HIGH);
            c.enableLights(true); 
            c.setLightColor(Color.RED); 
            c.enableVibration(true);
            if (nm != null) {nm.createNotificationChannel(c);}
        }
        Intent it = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), it, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, cid).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(t).setContentText(m).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pi);
                
        if (nm != null) {nm.notify((int)System.currentTimeMillis(), b.build());}
    }
}
