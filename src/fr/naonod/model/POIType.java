package fr.naonod.model;

import com.naonod.R;

public enum POIType {
	
	PARK("Parc", "#3dc2aa", R.drawable.map_pin_green),
	CULTURE("Culture", "#0284c5", R.drawable.map_pin_blue),
	PUB("Bar", "#e00608", R.drawable.map_pin_red),
	RESTAURANT("Restaurant", "#f5a521", R.drawable.map_pin_orange),
	OTHER("Autre", "#343434", R.drawable.map_pin_black);
	
	private POIType(String name, String color, int drawable) {
		this.mName = name;
		this.mColor = color;
		this.mDrawable = drawable;
	}
	
	private final String mName;
	private final String mColor;
	private final int mDrawable;
	
	 public String getPOIName() {
		 return mName;
	 }
	 
	 public String getPOIColor() {
		 return mColor;
	 }
	 
	 public int getPOIDrawable() {
		 return mDrawable;
	 }
}
