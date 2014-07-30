package com.example.handsfree;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.R.string;
import android.app.Notification;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotiListener extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Notification Services Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    public static String getpackagename(StatusBarNotification s){
    	return s.getPackageName().split(".")[s.getPackageName().split(".").length -1];
    }
    
    public static List<String> getText(Notification notification)
    {
        // We have to extract the information from the view
        RemoteViews        views = notification.bigContentView;
        if (views == null) views = notification.contentView;
        if (views == null) return null;

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<String>();
        try
        {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions)
            {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) continue;

                // View ID
                parcel.readInt();
                String methodName = parcel.readString();
                if (methodName == null) continue;

                // Save strings
                else if (methodName.equals("setText"))
                {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();

                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                }

                // Save times. Comment this section out if the notification time isn't important
                else if (methodName.equals("setTime"))
                {
                    
                }

                parcel.recycle();
            }
        }

        // It's not usually good style to do this, but then again, neither is the use of reflection...
        catch (Exception e)
        {
            Log.e("NotificationClassifier", e.toString());
        }

        return text;
    }
    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" +  "\t" + sbn.getPackageName());
        Intent i = new Intent("com.example.handsfree.newnoti");
        i.putExtra("notification_event",getText(sbn.getNotification()) + "\n");
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