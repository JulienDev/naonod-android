package fr.naonod.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.naonod.R;
import fr.naonod.application.NaonodApplication;
import fr.naonod.model.MyGeoPoint;
import fr.naonod.model.POI;
import fr.naonod.model.POIType;
import fr.naonod.tools.Json;

public class POISearchActivity extends Activity implements OnClickListener{

	/*
	 * UI
	 */
	private ArrayList<POI> mPOIs;
	private ProgressBar mPbSearching;

	public static Intent getIntent(Activity activity) {
		return new Intent(activity, POISearchActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_poi_search);

		mPbSearching = (ProgressBar) findViewById(R.id.pbSearching);
		((ImageButton) findViewById(R.id.ibHelp)).setOnClickListener(this);
		((LinearLayout) findViewById(R.id.llAllPOIs)).setOnClickListener(this);
		((Button) findViewById(R.id.bPOISearch)).setOnClickListener(this);

		mPOIs = ((NaonodApplication) getApplication()).mPOIs;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibHelp:
			startActivity(AboutActivity.getIntent(this));	
			break;
		case R.id.llAllPOIs:
			getAllPOIs();	
			break;
		case R.id.bPOISearch:
			getSelectedPOIs();	
			break;
		}
	}

	private void getSelectedPOIs() {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		if (((CheckBox) findViewById(R.id.cbParks)).isChecked()) {
			nameValuePairs.add(new BasicNameValuePair("parks", "true"));
		}	
		if (((CheckBox) findViewById(R.id.cbPubs)).isChecked()) {
			nameValuePairs.add(new BasicNameValuePair("bars", "true"));
		}
		if (((CheckBox) findViewById(R.id.cbRestaurants)).isChecked()) {
			nameValuePairs.add(new BasicNameValuePair("restaurants", "true"));
		}
		if (((CheckBox) findViewById(R.id.cbCulture)).isChecked()) {
			nameValuePairs.add(new BasicNameValuePair("cultural_venues", "true"));
		}

		if (nameValuePairs.size() > 0) {
			new GetPOI().execute(nameValuePairs);
		} else {
			Toast.makeText(getApplicationContext(), "Aucun type d'activité coché", Toast.LENGTH_SHORT).show();
		}		
	}

	private void getAllPOIs() {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("all", "true"));

		new GetPOI().execute(nameValuePairs);
	}

	private class GetPOI extends AsyncTask<List<NameValuePair>, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			mPbSearching.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {

			return getPOIs(params[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {

			mPbSearching.setVisibility(View.GONE);

			if (result) {
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
			}

			super.onPostExecute(result);
		}	
	}

	private boolean getPOIs(List<NameValuePair> nameValuePairs) {
		try {

			mPOIs.clear();
			JSONArray jsonPOIs = Json.getJSon(getString(R.string.api_url), nameValuePairs);

			if (jsonPOIs == null || jsonPOIs.length() == 0) {
				return false;
			}

			for (int i=0; i<jsonPOIs.length(); i++) {
				JSONObject jsonPoi = (JSONObject) jsonPOIs.get(i);

				int id = jsonPoi.getInt("id");
				String label = jsonPoi.getString("label");
				String description = jsonPoi.getString("description");

				Double latitude = jsonPoi.getDouble("latitude");
				Double longitude = jsonPoi.getDouble("longitude");

				String address = jsonPoi.getString("address");
				int zip = jsonPoi.getInt("zip");
				String city = jsonPoi.getString("city");

				String type = jsonPoi.getString("type");

				Log.d("type", "type:"+ type);

				POIType poiType;
				if (type.equals("parks")) {
					poiType = POIType.PARK;
				} else if (type.equals("culture_places")) {
					poiType = POIType.CULTURE;
				} else if (type.equals("bars")) {
					poiType = POIType.PUB;
				} else if (type.equals("restaurants")) {
					poiType = POIType.RESTAURANT;
				} else {
					poiType = POIType.OTHER;
				}

				MyGeoPoint p = new MyGeoPoint( (int) (latitude * 1E6), (int) (longitude * 1E6));
				POI poi = new POI(id, label, description, address, zip, city, p, poiType, (float) (int) (Math.random()*4+1));
				mPOIs.add(poi);				
			}

			((NaonodApplication) getApplication()).savePOIs();

			return true;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
		}
		return false;
	}
}