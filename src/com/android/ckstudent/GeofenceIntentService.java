package com.android.ckstudent;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GeofenceIntentService extends IntentService {
    /**
     * Sets an identifier for the service
     */
    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }
    /**
     * Handles incoming intents
     *@param intent The Intent sent by Location Services. This
     * Intent is provided
     * to Location Services (inside a PendingIntent) when you call
     * addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("GeofenceIntentService",
                    "Location Services error: " +
                    Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ||
            	transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                List <Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
                String[] triggerIds = new String[triggerList.size()];

                if (triggerList != null) {
	                for (int i = 0; i < triggerIds.length; i++) {
	                    // Store the Id of each geofence
	                    triggerIds[i] = triggerList.get(i).getRequestId();
	                }
                }
                
                // Send location updates
                Intent locationUpdate = new Intent(MapActivity.LOCATION_UPDATE);
                locationUpdate.putExtra("EnteringLocations", triggerIds);
                LocalBroadcastManager.getInstance(this).sendBroadcast(locationUpdate);                              
            } else {
            	// An invalid transition was reported
	            Log.e("GeofenceIntentService",
	                    "Geofence transition error: " +
	                    Integer.toString(transitionType));
            }
        }
    }
}
