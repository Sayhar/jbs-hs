package org.jbs.happysad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
//import android.util.Log;
import android.widget.Toast;


/**
 * Creates the More activity
 * @author HS
 */

public class More extends Activity implements OnClickListener {
	//for debugging purposes, delete after debugging.
	//private static final String TAG = "there's more screen";

	//fields
	private LocationManager gpsLocationManager;
	private LocationManager networkLocationManager;
	private LocationListener networkLocationListener;
	private LocationListener gpsLocationListener;
	private float GPS_latitude;
	private float GPS_longitude;
	private float Network_latitude;
	private float Network_longitude;
	private HappyData dataHelper;
	short emotion = -1;
	String extradata;
	long myID =1;
	String shareString = "";


	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	// We can be in one of these 3 states for the zooming
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	/**
	 * Initializes activity
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//This updates the userId to the proper id. it must be here because Shared Preferences has to be called after
		//on create, or it crashes.
		SharedPreferences sp = getSharedPreferences(Prompt.USER_DATA,0);
		myID =  sp.getInt( "usernameint", 0);

		//Intent to figure out whether they clicked happy or sad from Prompt.java
		Intent sender = getIntent();
		extradata = sender.getExtras().getString("Clicked");
		emotion = (short) sender.getExtras().getInt("Emotion");

		if(emotion == 1){
			setContentView(R.layout.more);
		}
		else if(emotion == 0){
			setContentView(R.layout.moresad);
		}

		//prevent text edit from being focused onCreate
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		

		//this creates the on touch listenter for the photo button
		View buttonImageCapture = (View) findViewById(R.id.camera_button);
		buttonImageCapture.setOnClickListener(this);
		
		//Finds the share_button view
		View shareButton = findViewById(R.id.share);
		shareButton.setOnClickListener(this);

		//Finds the submit_button view
		View submitButton = findViewById(R.id.more_to_dash);
		submitButton.setOnClickListener(this);
	}  	


	/**
     * Invoked when a view is clicked
     */
	public void onClick(View v) {		
		switch (v.getId()) {
		case R.id.more_to_dash:
			String shareString = ((TextView) findViewById(R.id.more_textbox)).getText().toString();
			if (!shareString.equals("")) {
			Intent i = new Intent(this, GlobalMap.class);
			saveUpdate(shareString); 
			finish();
			startActivity(i);
			} else {
				Toast toast = Toast.makeText(getApplicationContext(), "Please Enter a Reason", 500);
				toast.show();
			}
			break;
		case R.id.camera_button:
			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, 0);
			break;	
		case R.id.share:
			if (emotion == 1){
				shareString = ((TextView) findViewById(R.id.more_textbox)).getText().toString();
				share("Happy", shareString);
			}
			else{
				shareString = ((TextView) findViewById(R.id.more_textbox)).getText().toString();
				share("Sad", shareString);
			}
			break;	
		}
	} 

	/** Share
	 * 
	 */
	public void share(String subject,String text) {
		final Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		startActivity(Intent.createChooser(intent, getString(R.string.share)));
	}



	/**
	 * Helper method to deal with location.
	 */
	private void locationStuff(){

		// Acquire a reference to the system Location Manager
		gpsLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		networkLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a GPS listener that responds to location updates
		gpsLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				//updateNetworkLocation(location);
				makeUseOfNewLocation(location);}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		// Define a Network listener that responds to location updates
		networkLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				updateNetworkLocation(location);
				//makeUseOfNewLocation(location);
				}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		//registers the location managers
		gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,	0, gpsLocationListener);
		networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,	0, networkLocationListener);

		try {
			Location locationGPS = new Location(LocationManager.GPS_PROVIDER);
			Location locationNetwork = new Location(LocationManager.NETWORK_PROVIDER);			
			makeUseOfNewLocation(locationGPS);
			updateNetworkLocation(locationNetwork);

		}
		catch (Exception e){
		}

	}

	/**
	 * Updates GPS location
	 * @param location
	 */
	private void makeUseOfNewLocation(Location location) {		

		if(GPS_longitude == 0 && GPS_latitude == 0){
			GPS_longitude = (float) location.getLongitude();
			GPS_latitude = (float) location.getLatitude();
		}

	}

	/**
	 * Updates Network location
	 * @param location
	 */
	private void updateNetworkLocation(Location location) {
		if(Network_longitude == 0 && Network_latitude == 0){
			Network_longitude = (float) location.getLongitude();
			Network_latitude = (float) location.getLatitude();
		}
	}

	/**
	 * Saves the update as a bottle and adds the bottle to the DB
	 * @param msg
	 */
	private void saveUpdate(String msg){
		if (GPS_longitude == 0 && GPS_latitude == 0){
			GPS_longitude = Network_longitude;
			GPS_latitude = Network_latitude;
		}

		HappyBottle b = new HappyBottle(myID, GPS_latitude, GPS_longitude, emotion, msg, System.currentTimeMillis());
		dataHelper = new HappyData(this);
		dataHelper.addBottle(b);
	}


	/**
	 * Disables GPS Managers and Listeners
	 */
	public void onPause() {
		super.onPause();
		gpsLocationManager.removeUpdates(gpsLocationListener);
		networkLocationManager.removeUpdates(networkLocationListener);
		gpsLocationManager = null;
		networkLocationManager = null;
	}

	/**
	 * Enables GPS Managers and Listeners
	 */
	public void onResume() {
		super.onResume();
		locationStuff();
	}
}

