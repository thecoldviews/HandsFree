package com.example.handsfree;

import java.util.ArrayList;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class DictateAndSend extends Activity{
	private static final int REQUEST_CODE = 1234;
	private BroadcastReceiver check;
	IntentFilter filtercheck= new IntentFilter("android.intent.action.check");
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Dialog);
		this.setContentView(R.layout.dictate);
		 check = new BroadcastReceiver() {
		   	 @Override
		   	 public void onReceive(Context context, Intent intent) {
		           Toast.makeText(getApplicationContext(),"check", Toast.LENGTH_SHORT).show();
		           startVoiceRecognitionActivity();
		   	 }
		   };
		   	//registering our receiver
		 this.registerReceiver(this.check,filtercheck);
		
		Toast.makeText(getApplicationContext(), "Starting Voice", Toast.LENGTH_SHORT).show();
		this.startService(new Intent(this,ReadOut.class).putExtra("noti", "Please Dictate your Message"));	
	}
	
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(check);
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
	private void startVoiceRecognitionActivity()	
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Dictate Your Message!");
        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
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
           // Toast.makeText(getApplicationContext(), matches.get(0), Toast.LENGTH_SHORT).show();
            	//Toast.makeText(getApplicationContext(),"Sending SMS to "+MyApp.number, Toast.LENGTH_SHORT).show();
            	sendSMSMessage(MyApp.number,matches.get(0).toLowerCase());
            	
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
