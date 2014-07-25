package com.example.handsfree;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

public class NotiListener extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Notification Services Started poori service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
    	Toast.makeText(getApplicationContext(), "Dead", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

        
    public void onNotificationPosted(StatusBarNotification sbn) {
    	Toast.makeText(getApplicationContext(), "Huhuh", Toast.LENGTH_SHORT).show();
    	Log.i(TAG,"********** onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent i = new Intent("com.example.handsfree.newnoti");
        //(sbn.getPackageName().split("."))[(sbn.getPackageName().split(".")).length -1]+" "+
        i.putExtra("notification_event",sbn.getNotification().tickerText + "\n");
        
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new Intent("com.example.handsfree.oldnoti");
        i.putExtra("notification_even","onNotificationRemoved :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);
    }

}