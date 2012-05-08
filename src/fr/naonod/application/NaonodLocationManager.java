package fr.naonod.application;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class NaonodLocationManager extends Observable implements LocationListener{

	public Location mLocation;
	public LocationManager mLocationManager;

	public NaonodLocationManager(Context context) {
		mLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
		startTracking();
	}

	public void startTracking() {
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	public void stopTracking() {
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		this.mLocation = location;
		Log.d("mLocation", "mLocation:" + mLocation.getLatitude() + " - " + mLocation.getLongitude());

		setChanged();
		notifyObservers();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
