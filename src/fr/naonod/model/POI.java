package fr.naonod.model;

import java.io.Serializable;


public class POI implements Serializable{

	public int id;
	public String name;
	public String description;
	public MyGeoPoint position;
	public POIType poiType;
	public float rating;
	public String address;
	public int zip;
	public String city;
	
//	public POI(int id, String name, String description, GeoPoint position, POIType poiType, float rating) {
//		super();
//		this.id = id;
//		this.name = name;
//		this.description = description;
//		this.position = position;
//		this.poiType = poiType;
//		this.rating = rating;
//	}
	
	public POI(int id, String name, String description, String address, int zip, String city,  MyGeoPoint position, POIType poiType, float rating) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.address = address;
		this.zip = zip;
		this.city = city;
		this.position = position;
		this.poiType = poiType;
		this.rating = rating;
	}
}
