package com.example.handsfree;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;


class MyApp extends Application {
	 public static String number = "919971496664";
	 public static int flag=0;
	 public static int smsflag=0;
	    private static MyApp singleton;
	    public static MyApp getInstance() {
	        return singleton;
	    }
	    @Override
	    public void onCreate() {
	        super.onCreate();
	        singleton = this;
	    }
	  
	    public String getState(){
	    	return number;
	    }
	    public void setState(String s){
	    	number = s;
	    }
	}

public class HandsFree extends Activity {
    private static WakeLock fullWakeLock;
    private static WakeLock partialWakeLock; 
	protected void createWakeLocks(){
	    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
	    partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Loneworker - PARTIAL WAKE LOCK");
	}

	// Called implicitly when device is about to sleep or application is backgrounded
	protected void onPause(){
	    super.onPause();
	    partialWakeLock.acquire();
	}

	
	// Called whenever we need to wake up the device
	public void wakeDevice() {
	    fullWakeLock.acquire();
	    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
	    KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
	    keyguardLock.disableKeyguard();
	}
	
	private BroadcastReceiver smsReceiver;
	private BroadcastReceiver nReceiver;
	private BroadcastReceiver smsReplier;
	private static final int REQUEST_CODE =1234;
	//private static String number;
	
	IntentFilter intentFilter= new IntentFilter("android.intent.action.MAIN");
	IntentFilter intentFilter2 = new IntentFilter("android.intent.action.MAIN2");
    IntentFilter filter = new IntentFilter("com.example.HandsFree.newnoti");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		createWakeLocks();
		stopService(new Intent(this, NotiListener.class));
		stopService(new Intent(this, SmsListener.class));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hands_free);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String smspref = prefs.getString("listPref", "1");
		prefs.edit().putString("text", "Hey I will get back to you in a while");
		Toast.makeText(getApplicationContext(),"Call:"+String.valueOf(prefs.getBoolean("call", true))+"SMS:"+String.valueOf(prefs.getBoolean("sms", true))+"NOTI:"+String.valueOf(prefs.getBoolean("noti", true))+"CHECK:"+smspref, Toast.LENGTH_SHORT).show();

		nReceiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
	        	String temp = intent.getStringExtra("notification_event");
	        	wakeDevice();
	        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	        	Toast.makeText(getApplicationContext(), "i got sometinhg", Toast.LENGTH_SHORT).show();
	        	if(prefs.getBoolean("noti", true)){ 
	            Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
	            if(MyApp.smsflag!=0){
	            	MyApp.smsflag=0;
	            	}
		        else{
		            VoiceNoti(temp);
		            }
	            }
	        	
	        }
		};
		
        registerReceiver(nReceiver,filter);
        
		 smsReceiver = new BroadcastReceiver() {
		        
		   	 @Override
		   	 public void onReceive(Context context, Intent intent) {
		   		 //extract our message from intent
		   		   wakeDevice();
		           String msg_for_me = intent.getStringExtra("sms_event");
		           //Toast.makeText(getApplicationContext(), "Sms Intent Received", Toast.LENGTH_SHORT).show();
		           Toast.makeText(getApplicationContext(), msg_for_me, Toast.LENGTH_SHORT).show();
		           //number=intent.getStringExtra("number");
		           MyApp.number=intent.getStringExtra("com.example.handsfree.number");
		           Toast.makeText(getApplicationContext(), intent.getStringExtra("com.example.handsfree.number") , Toast.LENGTH_SHORT).show();
		           VoiceNotiAndSignal(msg_for_me);
		   	 }
		   };
		   	//registering our receiver
		 this.registerReceiver(this.smsReceiver, intentFilter2);
		 
		 
		 
		 smsReplier = new BroadcastReceiver() {
		        
		   	 @Override
		   	 public void onReceive(Context context, Intent intent) {
		   		 //extract our message from intent
		   		   wakeDevice();
		           String msg_for_me = intent.getStringExtra("voice");
		           //Toast.makeText(getApplicationContext(), "Start Taking Response", Toast.LENGTH_SHORT).show();
		           Toast.makeText(getApplicationContext(), msg_for_me, Toast.LENGTH_SHORT).show();
		           StartVoiceRec();
		   	 }
		   };
		   	//registering our receiver
		 this.registerReceiver(this.smsReplier, intentFilter);
	 
		 if(MyApp.flag==0){
			 Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
		 	startActivity(intent);
		 	MyApp.flag=1;
		 }
		 
		 
	}
	
	protected void sendSMSMessage(String phoneNo, String message) {
	      Log.i("Send SMS", "");
//
//	      String phoneNo = txtphoneNo.getText().toString();
//	      String message = txtMessage.getText().toString();
	      Toast.makeText(getApplicationContext(),"Attempting to send message to: "+ phoneNo + " with details: "+message,
	 	         Toast.LENGTH_SHORT).show();
	      try {
	         SmsManager smsManager = SmsManager.getDefault();
	         smsManager.sendTextMessage(phoneNo, null, message, null, null);
	         Toast.makeText(getApplicationContext(), "SMS sent.",
	         Toast.LENGTH_SHORT).show();
	      } catch (Exception e) {
	         Toast.makeText(getApplicationContext(),
	         "SMS failed, please try again.",
	         Toast.LENGTH_SHORT).show();
	         e.printStackTrace();
	      }
	   }
	
	public void StartVoiceRec(){
		this.startActivity(new Intent(this,VoiceRec.class));
		//startVoiceRecognitionActivity();
		
	}
		
	/*
	 * Call Another Activity
	 * Maybe Change this into a service too...
	 */
	private void startVoiceRecognitionActivity()	
    {
		wakeDevice();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Reply Back?");
        //Toast.makeText(getApplicationContext(), "You are supposed to talk now", Toast.LENGTH_SHORT).show();
        startActivityForResult(intent, REQUEST_CODE);
    }
 
  /*
      Handle the results from the voice recognition activity.
 */    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getApplicationContext(), matches.get(0), Toast.LENGTH_SHORT).show();
            if(matches.get(0).toLowerCase().compareTo("yes")==0){
            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            	Toast.makeText(getApplicationContext(),"Sending SMS to "+MyApp.number, Toast.LENGTH_SHORT).show();
            	String userValue = prefs.getString("listPref", "1");
            	//Toast.makeText(getApplicationContext(),userValue, Toast.LENGTH_SHORT).show();
            	if (userValue.equals("1") ){
            		sendSMSMessage(MyApp.number,prefs.getString("text", "default"));
            	}
            	else if (userValue.equals("2") ){
            		startActivity(new Intent(this,DictateAndSend.class).putExtra("number",MyApp.number));    
            	}
            }else{
            	
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

	
	public void LaunchConfigureScreen(View v){
		startActivity(new Intent(this,ConfigureScreen.class));
	}

	public void StartServices(View v){
		Toast.makeText(getApplicationContext(), "Starting Services", Toast.LENGTH_SHORT).show();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (prefs.getBoolean("call", true)) {
		    // etc
		}
		if (prefs.getBoolean("sms",true)) {
			this.startService(new Intent(this, SmsListener.class));
		}
	}

	public void Exit(View v){
		stopService(new Intent(this, NotiListener.class));
		stopService(new Intent(this, SmsListener.class));
		unregisterReceiver(nReceiver);
		unregisterReceiver(smsReceiver);
		unregisterReceiver(smsReplier);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hands_free, menu);
		return true;
	}
	
	protected void onResume(){
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String smspref = prefs.getString("listPref", "1");
		super.onResume();
	    if(fullWakeLock.isHeld()){
	        fullWakeLock.release();
	    }
	    if(partialWakeLock.isHeld()){
	        partialWakeLock.release();
	    }
		//Toast.makeText(getApplicationContext(),"Call:"+String.valueOf(prefs.getBoolean("call", true))+"SMS:"+String.valueOf(prefs.getBoolean("sms", true))+"NOTI:"+String.valueOf(prefs.getBoolean("noti", true))+"CHECK:"+smspref, Toast.LENGTH_SHORT).show();
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, NotiListener.class));
		stopService(new Intent(this, SmsListener.class));
        unregisterReceiver(nReceiver);
        unregisterReceiver(smsReceiver);
		unregisterReceiver(smsReplier);
    }

	public void MockNoti(View v){
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
        ncomp.setContentTitle("My Notification");
        ncomp.setContentText("Notification Listener Service Example");
        ncomp.setTicker("Notification Listener Service Example");
        ncomp.setSmallIcon(R.drawable.ic_launcher);
        ncomp.setAutoCancel(true);
        nManager.notify((int)System.currentTimeMillis(),ncomp.build());
        Toast.makeText(getApplicationContext(), "Mock NOTI", Toast.LENGTH_SHORT).show();
	}
	
	public void VoiceNoti(String value){
		Toast.makeText(getApplicationContext(), "Voice Noti", Toast.LENGTH_SHORT).show();
		this.startService(new Intent(this,ReadOut.class).putExtra("noti", "You Have a new Notification "+value));
	}
	
	public void VoiceNotiAndSignal(String value){
		Toast.makeText(getApplicationContext(), "Voice Noti", Toast.LENGTH_SHORT).show();
		this.startService(new Intent(this,ReadOutAndSignal.class).putExtra("noti", value));
	}
	
  
}
