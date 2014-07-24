package com.example.handsfree;

import java.util.ArrayList;

import android.app.*;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class CallAnswer extends Activity{
	// Called from onCreate
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

	// Called implicitly when device is about to wake up or foregrounded
	protected void onResume(){
	    super.onResume();
	    if(fullWakeLock.isHeld()){
	        fullWakeLock.release();
	    }
	    if(partialWakeLock.isHeld()){
	        partialWakeLock.release();
	    }
	}

	// Called whenever we need to wake up the device
	public void wakeDevice() {
	    fullWakeLock.acquire();
	    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
	    KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
	    keyguardLock.disableKeyguard();
	}
	
	private static final int REQUEST_CODE = 1234;
	private BroadcastReceiver callcheck;
	IntentFilter filtercheck= new IntentFilter("android.intent.action.callcheck");
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dictate);
		createWakeLocks();
		wakeDevice();
		
		 callcheck = new BroadcastReceiver() {
		   	 @Override
		   	 public void onReceive(Context context, Intent intent) {
		           Toast.makeText(getApplicationContext(),"check", Toast.LENGTH_SHORT).show();
		           startVoiceRecognitionActivity();
		   	 }
		   };
		   	//registering our receiver
		 this.registerReceiver(this.callcheck,filtercheck);
		 this.startService(new Intent(this,ReadOut.class).putExtra("noti", "Do you want to answer?").putExtra("type", "call"));	
//		Toast.makeText(getApplicationContext(), "Starting Voice", Toast.LENGTH_SHORT).show();
//		this.startService(new Intent(this,ReadOut.class).putExtra("noti", "Please Dictate your Message"));	
	}
	
    protected void onDestroy() {
    	super.onDestroy();
    }
	
	public static void acceptCall(Context context) 
	{
	    Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
	    buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, 
	      new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
	    context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	} 
	/*
	 * A function to reject an incoming call
	 */
	private void rejectCall(Context context) 
	{
	Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON); buttonDown.putExtra(Intent.EXTRA_KEY_EVENT,
	new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
	context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");}
	   
    
	private void startVoiceRecognitionActivity()	
    {
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
            	acceptCall(getApplicationContext());
            }else{
            	rejectCall(getApplicationContext());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
