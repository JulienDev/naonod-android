package fr.naonod.model;

import java.io.Serializable;

import android.location.Location;
import android.util.FloatMath;

import com.google.android.maps.GeoPoint;

public class MyGeoPoint implements Serializable {

	private int latitudeE6, longitudeE6;

	public int getLatitudeE6() {
		return latitudeE6;
	}

	public int getLongitudeE6() {
		return longitudeE6;
	}

	public MyGeoPoint(int latitudeE6, int longitudeE6) {
		super();
		this.latitudeE6 = latitudeE6;
		this.longitudeE6 = longitudeE6;
	}

	public GeoPoint myGeoPointToGeoPoint() {
		return new GeoPoint(latitudeE6, longitudeE6);
	}

	public static float calculateDistance(MyGeoPoint geoPointFrom, Location locationTo) {
		if (locationTo!=null) {
			Location locationFrom = new Location("");
			locationFrom.setLatitude(geoPointFrom.getLatitudeE6()/1E6);
			locationFrom.setLongitude(geoPointFrom.getLongitudeE6()/1E6);

			float distance = locationTo.distanceTo(locationFrom);
			return (float) Math.floor(distance+0.5) / 1000;
		}
		return 0;
	}
}