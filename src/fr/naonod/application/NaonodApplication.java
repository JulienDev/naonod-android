package fr.naonod.application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Application;
import fr.naonod.model.POI;
import fr.naonod.model.Todo;

public class NaonodApplication extends Application {

	private static NaonodApplication sInstance;
	public NaonodLocationManager naonodLocationManager;

	public static NaonodApplication getInstance() {
		return sInstance;
	}

	public ArrayList<POI> mPOIs;
	public ArrayList<Todo> mTodos;

	public NaonodLocationManager getNaonodLocationManager() {
		if (naonodLocationManager==null) {
			naonodLocationManager = new NaonodLocationManager(getApplicationContext());
		}
		return naonodLocationManager;
	}

	@Override
	public void onCreate() {
		super.onCreate();  
		sInstance = this;
		sInstance.initializeInstance();
	}

	protected void initializeInstance() {
		if (!restorePOIs()) {
			mPOIs = new ArrayList<POI>();	
		}
		
		if (!restoreTimeline()) {
			mTodos = new ArrayList<Todo>();	
		}	
	}

	@Override
	public void onTerminate() {
		saveTimeline();
		savePOIs();
		super.onTerminate();
	}

	String dir = "/data/data/com.naonod/";
	String filenameTimeline = dir + "timeline.ser";
	String filenamePOIs = dir + "POIs.ser";
	
	public void savePOIs() {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(filenamePOIs);
			out = new ObjectOutputStream(fos);
			out.writeObject(mPOIs);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public void saveTimeline() {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(filenameTimeline);
			out = new ObjectOutputStream(fos);
			out.writeObject(mTodos);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private boolean restoreTimeline() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filenameTimeline);
			in = new ObjectInputStream(fis);
			mTodos = (ArrayList<Todo>)in.readObject();
			in.close();
			return true;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean restorePOIs() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filenamePOIs);
			in = new ObjectInputStream(fis);
			mPOIs = (ArrayList<POI>)in.readObject();
			in.close();
			return true;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
}