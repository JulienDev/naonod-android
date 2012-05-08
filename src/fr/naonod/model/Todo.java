package fr.naonod.model;

import java.io.Serializable;
import java.util.Date;

public class Todo implements Serializable{

	public POI poi;
	public Date dateStart;
	
	public Todo(POI poi, Date dateStart) {
		super();
		this.poi = poi;
		this.dateStart = dateStart;
	}
}