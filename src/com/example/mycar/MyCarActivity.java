package com.example.mycar;
	
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
	
public class MyCarActivity extends Activity {
	
	public static final String TAG = "MyCar";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mycar);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_mycar, menu);
		return true;
	}
	
	/** Called when the user clicks the car button */
	public void startCarActivity(View view) {
	    Intent intent = new Intent(this, MyCarListener.class);
	    intent.putExtra("a", 1);
	    startActivity(intent);
	}
	
	/** Called when the user clicks the remote button */
	public void startRemoteActivity(View view) {
	    Intent intent = new Intent(this, MyCarListener.class);
	    intent.putExtra("a", 2);
	    startActivity(intent);
	}
	
}
