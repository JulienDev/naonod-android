package fr.naonod.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.naonod.R;

import fr.naonod.application.NaonodApplication;
import fr.naonod.application.NaonodLocationManager;
import fr.naonod.model.MyGeoPoint;
import fr.naonod.model.POI;
import fr.naonod.model.Todo;

public class POIActivity extends MapActivity implements OnCheckedChangeListener, Observer, OnClickListener {

	private LayoutInflater mInflater;
	private NaonodLocationManager mNaonodLocationManager;

	/*
	 * Objects
	 */
	private POI mPOI;
	private ArrayList<Todo> mTodos;

	/*
	 * UI
	 */
	private MapView mMapView;
	private ViewType mCurrentView;
	private View mDetails, mComments;
	private RadioGroup mRgPOIFragment;
	private FrameLayout mFlPOI;
	private TextView mTvPOIDistance;

	private enum ViewType {
		DETAILS, COMMENTS;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_poi);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mTodos = ((NaonodApplication) getApplication()).mTodos;

		Bundle extras = getIntent().getExtras();
		long start = extras.getLong("start");
		mPOI = (POI) extras.getSerializable("poi");
		int time = (int) (System.currentTimeMillis() - start);
		createMapView(time * 2);

		mNaonodLocationManager = ((NaonodApplication) getApplication()).getNaonodLocationManager();
		mNaonodLocationManager.addObserver(this);

		mRgPOIFragment = (RadioGroup) findViewById(R.id.rgPOIFragment);
		mRgPOIFragment.setOnCheckedChangeListener(this);
		mFlPOI = (FrameLayout) findViewById(R.id.flPOI);

		((ImageButton) findViewById(R.id.ibPOIAddTodo)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.ibPOIDirections)).setOnClickListener(this);

		setView(ViewType.DETAILS);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ibPOIAddTodo:
			createPopupDefineTodoHour();
			break;

		case R.id.ibPOIDirections:
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="
					+ mPOI.position.getLatitudeE6() / 1E6 + "," + mPOI.position.getLongitudeE6() / 1E6));
			startActivity(intent);
			break;
		}
	}

	@Override
	protected void onResume() {
		((NaonodApplication) getApplication()).getNaonodLocationManager().startTracking();
		super.onResume();
	}

	@Override
	protected void onPause() {
		((NaonodApplication) getApplication()).getNaonodLocationManager().stopTracking();
		super.onPause();
	}

	public void update(Observable obs, Object x) {

		Log.d("distance", "distance");

		if (obs instanceof NaonodLocationManager) {
			if (mTvPOIDistance != null) {
				float distance = MyGeoPoint.calculateDistance(mPOI.position, mNaonodLocationManager.mLocation);
				if (distance != 0) {
					mTvPOIDistance.setText("à " + distance + " km");
				} else {
					mTvPOIDistance.setText("" + distance);
				}
			}

			if (mMapView != null) {
				if (locationItemizedOverlay != null) {
					if (mMapView.getOverlays().contains(locationItemizedOverlay)) {
						mMapView.getOverlays().remove(locationItemizedOverlay);
					}
				}

				Drawable drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
				locationItemizedOverlay = new LocationItemizedOverlay(drawable);

				Location location = mNaonodLocationManager.mLocation;
				GeoPoint geoPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));

				OverlayItem overlayitem = new OverlayItem(geoPoint, "", "");
				locationItemizedOverlay.addOverlay(overlayitem);

				mMapView.getOverlays().add(locationItemizedOverlay);
			}
		}
	}

	private LocationItemizedOverlay locationItemizedOverlay;

	public class LocationItemizedOverlay extends ItemizedOverlay {
		// Liste des marqueurs
		private ArrayList mOverlays = new ArrayList();
		private Context mContext;

		// private String currentItem = "";

		public LocationItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public LocationItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		// Appeler quand on rajoute un nouvel marqueur a la liste des marqueurs
		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
			mMapView.invalidate();
		}

		@Override
		protected boolean hitTest(OverlayItem arg0, Drawable arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			return super.hitTest(arg0, arg1, arg2, arg3);
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (OverlayItem) mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		public void draw(Canvas arg0, MapView arg1, boolean arg2) {
			super.draw(arg0, arg1, false);
		}
	}

	@Override
	protected void onDestroy() {
		mNaonodLocationManager.deleteObserver(this);
		super.onDestroy();
	}

	private void createPopupDefineTodoHour() {

		final Date date = new Date();
		int hourOfDay = date.getHours();
		int minute = date.getMinutes();

		TimePickerDialog tpDialog = new TimePickerDialog(this, new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				date.setHours(hourOfDay);
				date.setMinutes(minute);

				Todo todo = new Todo(mPOI, date);
				mTodos.add(todo);
				((NaonodApplication) getApplication()).saveTimeline();

				Toast.makeText(getApplicationContext(), "Activité ajoutée à votre Timeline", Toast.LENGTH_SHORT).show();
				finish();
			}
		}, hourOfDay, minute, true);
		tpDialog.setMessage(mPOI.name);
		tpDialog.show();
	}

	public static Intent getIntent(Activity activity, POI poi) {
		Intent intent = new Intent(activity, POIActivity.class);
		intent.putExtra("start", System.currentTimeMillis());
		intent.putExtra("poi", poi);
		return intent;
	}

	private void setView(ViewType viewType) {

		if (mCurrentView == viewType) {
			return;
		}

		mFlPOI.removeAllViews();

		switch (viewType) {
		case DETAILS:
			if (mDetails == null) {
				mDetails = mInflater.inflate(R.layout.fragment_poi_detail, null);

				((TextView) mDetails.findViewById(R.id.tvPOIName)).setText(mPOI.name);
				String address = mPOI.address.concat("\n").concat(String.valueOf(mPOI.zip)).concat(" ").concat(mPOI.city);
				((TextView) mDetails.findViewById(R.id.tvPOIAddress)).setText(address);
				if (mPOI.description.length() > 0) {
					((TextView) mDetails.findViewById(R.id.tvPOIDescription)).setText(mPOI.description);
				} else {
					((TextView) mDetails.findViewById(R.id.tvPOIDescription)).setText("Pas de description");
				}

				mTvPOIDistance = (TextView) mDetails.findViewById(R.id.tvPOIDistance);
			}
			mFlPOI.addView(mDetails);
			break;
		case COMMENTS:
			if (mComments == null) {
				mComments = mInflater.inflate(R.layout.fragment_poi_detail, null);
			}
			mFlPOI.addView(mComments);
			break;
		}
	}

	private void createMapView(int delay) {

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mMapView = new MapView(POIActivity.this, getString(R.string.api_key));
				mMapView.setEnabled(true);
				// mapView.set
				LayoutParams params = new LayoutParams();
				params.width = LayoutParams.FILL_PARENT;
				params.height = LayoutParams.FILL_PARENT;
				FrameLayout mapViewContainer = (FrameLayout) mDetails.findViewById(R.id.mapViewContainer);
				mapViewContainer.addView(mMapView, params);
				MapController mc = mMapView.getController();
				mc.setCenter(mPOI.position.myGeoPointToGeoPoint());
				mc.setZoom(16);

				Drawable drawable = getResources().getDrawable(mPOI.poiType.getPOIDrawable());
				MapItemizedOverlay overlay = new MapItemizedOverlay(drawable);
				overlay.addOverlay(new OverlayItem(mPOI.position.myGeoPointToGeoPoint(), "", ""));
				mMapView.getOverlays().add(overlay);
			}
		}, delay);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (checkedId) {
		case R.id.rPOIDetails:
			setView(ViewType.DETAILS);
			break;
		case R.id.rPOIComments:
			setView(ViewType.COMMENTS);
			break;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class MapItemizedOverlay extends com.google.android.maps.ItemizedOverlay {
		// Liste des marqueurs
		private ArrayList mOverlays = new ArrayList();
		private Context mContext;

		public MapItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public MapItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		// Appeler quand on rajoute un nouvel marqueur a la liste des marqueurs
		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected boolean hitTest(OverlayItem arg0, Drawable arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			return super.hitTest(arg0, arg1, arg2, arg3);
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (OverlayItem) mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
	}
}