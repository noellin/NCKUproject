package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener,
													OnAddGeofencesResultListener, LocationListener {
	LocationManager mLocationManager;
	LocationClient mLocationClient;
	// Persistent storage for geofences
	SimpleGeofenceStore mGeofenceStorage;
	// Internal List of Geofence objects
	ArrayList<Geofence> mGeofenceList;
	// PendingIntent
	PendingIntent mGeofencePendingIntent;
	// Google map
	GoogleMap mMap;
	// Intent receiver
	BroadcastReceiver mReceiver;
	public static final String LOCATION_UPDATE = "com.android.ckstudent";
	// Dialog builder
	Builder mBuilder;
	ArrayList<Location> mMockLocations;
	ArrayList<LatLng> markerPoints;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_main);
		markerPoints = new ArrayList<LatLng>();
		mMockLocations = new ArrayList<Location>();
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();				
		mBuilder = new AlertDialog.Builder(this);
		createGeofences();
		// Setup broadcast receiver so that we can get notified when user's location is changed
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String[] enteringLocations;
				enteringLocations = intent.getStringArrayExtra("EnteringLocations");
				displayLocationInfo(enteringLocations);
			}			
		};
		LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LOCATION_UPDATE);
		bManager.registerReceiver(mReceiver, intentFilter);					

		// Move camera to Taipei 101
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);	
		mMap.setMyLocationEnabled(true); // Display user's current location on the map
		LatLng taipei101 = new LatLng(22.998881,120.216082);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(taipei101, 16);
		mMap.animateCamera(update);	

		addMarkersToMap();
		
		if(mMap != null){
            // Setting onclick event listener for the map
            mMap.setOnMapClickListener(new OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {
                    // Already two locations
                    if(markerPoints.size() > 1) {
                        markerPoints.clear();
                        mMap.clear();
                        addMarkersToMap();
                    }
                    // Adding new item to the ArrayList
                    markerPoints.add(point);
                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();
                    // Setting the position of the marker
                    options.position(point);
                    /**
                    * For the start location, the color of marker is RED and
                    * for the end location, the color of marker is RED.
                    */
                    if(markerPoints.size() == 1){
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }else if(markerPoints.size() == 2){
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                    // Add new marker to the Google Map Android API V2
                    mMap.addMarker(options);
                    // Checks, whether start and end locations are captured
                    if(markerPoints.size() >= 2){
                        LatLng origin = markerPoints.get(0);
                        LatLng dest = markerPoints.get(1);
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }
            });
        }
	}


	private void addMarkersToMap() {
		mMap.addMarker(new MarkerOptions().position(new LatLng(23.001533,120.217031))
				.title("國立成功大學")
				.snippet("力行校區(綠色魔法學校、附設醫院第二大樓)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.addMarker(new MarkerOptions().position(new LatLng(23.001533,120.220572))
				.title("國立成功大學")
				.snippet("建國校區(成杏廳、附設醫院)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.addMarker(new MarkerOptions().position(new LatLng(23.001533,120.223248))
				.title("國立成功大學")
				.snippet("敬業校區(教職員宿舍、敬業宿舍、醫生宿舍、網球場)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.addMarker(new MarkerOptions().position(new LatLng(22.998239,120.217031))
				.title("國立成功大學")
				.snippet("光復校區(學生活動中心、修齊大樓、雲平大樓、唯農大樓、軍訓室、中正堂、光復宿舍、操場、球場)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.addMarker(new MarkerOptions().position(new LatLng(22.998239,120.220572))
				.title("國立成功大學")
				.snippet("成功校區(博物館、資訊大樓、綜合大樓、圖書館、格致堂)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.addMarker(new MarkerOptions().position(new LatLng(22.998239,120.223248))
				.title("國立成功大學")
				.snippet("自強校區(奇美大樓、科技大樓、自強操場)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.addMarker(new MarkerOptions().position(new LatLng(22.995904,120.219466))
				.title("國立成功大學")
				.snippet("勝利校區(K館、校友會館、太子學舍、勝利宿舍、游泳池、體適能中心)")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

		mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
	}

	class CustomInfoWindowAdapter implements InfoWindowAdapter {
		private final View infoWindow;
		CustomInfoWindowAdapter() {
			infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null);
		}
		public View getInfoWindow(Marker marker) {
            String title = marker.getTitle();
            TextView textViewTitle = (TextView) infoWindow.findViewById(R.id.title);
            textViewTitle.setText(title);
            String snippet = marker.getSnippet();
            TextView textViewSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
            textViewSnippet.setText(snippet);
            return infoWindow;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }
	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}	

	/**
	 * Get the geofence parameters for each geofence from the UI
	 * and add them to a List.
	 */
	public void createGeofences() {
		// Get locations info
		String[] locations = getResources().getStringArray(R.array.locations);
		String[] latitudes = getResources().getStringArray(R.array.latitudes);
		String[] longitudes = getResources().getStringArray(R.array.longitudes);

		// Instantiate a new geofence storage area
		mGeofenceStorage = new SimpleGeofenceStore(this);
		// Instantiate the current List of geofences
		mGeofenceList = new ArrayList<Geofence>();

		// Create SimpleGeofence objects
		for (int i = 0; i < locations.length; i++) {
			SimpleGeofence geofence = new SimpleGeofence(
					locations[i],						// Geofence ID
					Float.valueOf(latitudes[i]),		// Latitude
					Float.valueOf(longitudes[i]),		// Longitude
					100,								// Radius
					Geofence.NEVER_EXPIRE,	    		// Expiration time in milliseconds
					Geofence.GEOFENCE_TRANSITION_ENTER); // Records only entry transitions
			// Store this flat version
			mGeofenceStorage.setGeofence(locations[i], geofence);        
			mGeofenceList.add(geofence.toGeofence());

			// Create mock location objects
			Location mockLocation = new Location("MY_PROVIDER");
			mockLocation.setLatitude(Float.valueOf(latitudes[i]));
			mockLocation.setLongitude(Float.valueOf(longitudes[i]));
			mockLocation.setAccuracy(3.0f);
			mMockLocations.add(mockLocation);
		}
	}

	private void displayLocationInfo(String[] entringLocations) {
		String[] locations = getResources().getStringArray(R.array.locations);
		String[] descriptions = getResources().getStringArray(R.array.descriptions);
		int index = -1;
		// Use the first triggered geofence as the current location
		for (int i = 0; i < locations.length; i++) {
			if (locations[i].equals(entringLocations[0])) {
				index = i;
				break;
			}
		}
		// Remove the following code and show location info/picture in a dialog
		mBuilder.setTitle(locations[index]);
		mBuilder.setMessage(descriptions[index]);
		if(index == 0){
			mBuilder.setIcon(R.drawable.ncku);
		}else if(index == 1){
			mBuilder.setIcon(R.drawable.banyan);
		}else if(index == 2){
			mBuilder.setIcon(R.drawable.lake);
		}else if(index == 3){
			mBuilder.setIcon(R.drawable.museum);
		}else if(index == 4){
			mBuilder.setIcon(R.drawable.books);
		}else if(index == 5){
			mBuilder.setIcon(R.drawable.memorial);
		}else if(index == 6){
			mBuilder.setIcon(R.drawable.green);
		}else if(index == 7){
			mBuilder.setIcon(R.drawable.library);
		}else if(index == 8){
			mBuilder.setIcon(R.drawable.gate);
		}else if(index == 9){
			mBuilder.setIcon(R.drawable.hospital);
		}else if(index == 10){
			mBuilder.setIcon(R.drawable.da_tung);
		}else if(index == 11){
			mBuilder.setIcon(R.drawable.flower);
		}
		mBuilder.show();
		mBuilder.setPositiveButton("關閉", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
	}
	
	private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
 
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
    
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i=0; i<result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0; j<path.size(); j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }
    
    private String getDirectionsUrl(LatLng origin,LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		if (LocationStatusCodes.SUCCESS == statusCode) {
			/*
			 * Handle successful addition of geofences here.
			 * You can send out a broadcast intent or update the UI.
			 * geofences into the Intent's extended data.
			 */
			Toast.makeText(this, "載入地點成功!", Toast.LENGTH_LONG).show();   
		} else {
			/*
			 * Report errors here. 
			 * You can log the error using Log.e() or update
			 * the UI.
			 */
			Toast.makeText(this, "載入地點失敗!", Toast.LENGTH_LONG).show();
		}
		// Turn off the in progress flag and disconnect the client
		//mLocationClient.disconnect();		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
		// Create an explicit Intent
		Intent intent = new Intent(this, GeofenceIntentService.class);
		intent.setAction("GeofenceIntentService");

		// Enable mock location mode
		mLocationClient.setMockMode(true);
		mLocationClient.setMockLocation(mMockLocations.get(0));

		// Create a PendingIntent
		mGeofencePendingIntent = PendingIntent.getService(
				this,
				0,		// Request code, not used
				intent,	// An intent describing the service to be started
				PendingIntent.FLAG_UPDATE_CURRENT); // Update existing PendingIntent if already exists 

		// Send a request to add the current geofences
		mLocationClient.addGeofences(
				mGeofenceList, mGeofencePendingIntent, this);

		// Request location update
		LocationRequest request = LocationRequest.create();
		request.setInterval(60000); // update every 1 minute
		request.setSmallestDisplacement(10.0f); // do not update unless the user moved more than 10 meters
		mLocationClient.requestLocationUpdates(request, (LocationListener)this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Location currentLocation;
		int locationIndex = 0;
		
		switch(item.getItemId()) {
		case R.id.loc_ncku:
			locationIndex = 0;
	    	break;
		case R.id.loc_banyan_tree_park:
			locationIndex = 1;
			break;
		case R.id.loc_ncku_lake:
			locationIndex = 2;
			break;
		case R.id.loc_ncku_museum:
			locationIndex = 3;
			break;
		case R.id.loc_ncku_books_department:
			locationIndex = 4;
			break;
		case R.id.loc_qiong_li_zhi_zhi_memorial_arch:
			locationIndex = 5;
			break;
		case R.id.loc_green_magic_school:
			locationIndex = 6;
			break;
		case R.id.loc_ncku_library:
			locationIndex = 7;
			break;
		case R.id.loc_small_western_gate:
			locationIndex = 8;
			break;
		case R.id.loc_ncku_hospital:
			locationIndex = 9;
			break;
		case R.id.loc_da_tung_market:
			locationIndex = 10;
			break;
		case R.id.loc_flower_night_market:
			locationIndex = 11;
			break;
		}
		currentLocation = mMockLocations.get(locationIndex);
		currentLocation.setTime(System.currentTimeMillis());
		mLocationClient.setMockLocation(currentLocation);
		return true;		
	}
	
	@Override
	public void onDisconnected() {
	}

	@Override
	public void onLocationChanged(Location location) {
		// Move camera
		LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());		
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 16);
		mMap.animateCamera(update);	
	}

	public void onInfoWindowClick(Marker marker) {
		Toast.makeText(getBaseContext(), 
				"Info Window clicked@" + marker.getId(), 
				Toast.LENGTH_SHORT).show();
	}
}