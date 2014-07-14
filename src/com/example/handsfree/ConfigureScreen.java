package com.example.handsfree;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.Activity;

public class ConfigureScreen extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.configure);
	}
}
