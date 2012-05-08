package fr.naonod.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.naonod.R;

import fr.naonod.application.NaonodApplication;
import fr.naonod.application.NaonodLocationManager;
import fr.naonod.model.POI;
import fr.naonod.model.POIType;
import fr.naonod.model.Todo;

public class POIMapActivity extends MapActivity implements Observer, OnClickListener {

	private Activity mActivity;
	private LayoutInflater mInflater;
	private Display mDisplay;

	/*
	 * Objects
	 */
	private POI mCurrentPOI;
	private ArrayList<POI> mPOIs;
	private ArrayList<POIType> mPOITypes;
	private HashSet<POIType> mPOITypesSelected;
	private ArrayList<Todo> mTodos = NaonodApplication.getInstance().mTodos;
	private SortTodo mSortTodo;

	private NaonodLocationManager mNaonodLocationManager;
	private LocationItemizedOverlay mLocationItemizedOverlay;

	/*
	 * UI
	 */
	private LinearLayout mLlMapTimeline, mLlMapContent, mLlZoom;
	private RelativeLayout mRlPOIContent;
	private RelativeLayout.LayoutParams mMapContentLayoutParams;
	private TextView mTodosCounter;
	private ListView mLvTodo;
	private View mContentOverlay;
	private TodoAdapter mTodoAdapter;	
	private int mInitialLlPOIContentHeight;
	private int mMinimumPOIRating = 1;
	private TextView mTvPOITitle, mTvPOIRating;
	private ImageButton mIbTimeline, mIbGPSSearching, mIbPOISearch, mIbMapFilter, mIbListView, mIbHelp;
	private MapView mMapView;
	private MapController mMapContoller;
	private boolean mIsCenteringLocation = false;
	private int mCurrentMapZoom = 14;

	//Animations
	private static final int TIMELINE_ANIMATION_SPEED = 200;
	private static final int POI_BAR_ANIMATION_SPEED = 200;

	private OpenPOIBar mOpenPOIBar;
	private ClosePOIBar mClosePOIBar;
	private CloseTimeline mCloseTimeline;
	private OpenTimeline mOpenTimeline;
	private boolean mOpenedTimeline = false;

	public static Intent getIntent(Activity activity) {
		return new Intent(activity, POIMapActivity.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		mActivity=this;
		mNaonodLocationManager = ((NaonodApplication) getApplication()).getNaonodLocationManager();
		mNaonodLocationManager.addObserver(this);

		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDisplay = getWindowManager().getDefaultDisplay();

		mLlMapTimeline = (LinearLayout) findViewById(R.id.llMapTimeline);
		mLlMapContent = (LinearLayout ) findViewById(R.id.llMapContent);
		mRlPOIContent = (RelativeLayout) findViewById(R.id.llPOIContent);
		mMapContentLayoutParams = (LayoutParams) mLlMapContent.getLayoutParams();
		mLlZoom = (LinearLayout) findViewById(R.id.llZoom);
		mContentOverlay = ((View) findViewById(R.id.contentOverlay));
		mContentOverlay.setOnClickListener(this);
		mInitialLlPOIContentHeight = mRlPOIContent.getLayoutParams().height;
		mTvPOITitle = (TextView) findViewById(R.id.tvPOITitle);
		mTvPOIRating = (TextView) findViewById(R.id.tvPOIRating);
		mIbListView = (ImageButton) findViewById(R.id.ibListView);
		mIbListView.setOnClickListener(this);
		mTodosCounter = (TextView) findViewById(R.id.todosCounter);
		mIbTimeline = (ImageButton) findViewById(R.id.ibTimeline);
		mIbTimeline.setOnClickListener(this);
		mIbGPSSearching = (ImageButton) findViewById(R.id.ibGPSSearching);
		mIbGPSSearching.setOnClickListener(this);
		mIbPOISearch = (ImageButton) findViewById(R.id.ibPOISearch);
		mIbPOISearch.setOnClickListener(this);
		mIbMapFilter = (ImageButton) findViewById(R.id.ibMapFilter);
		mIbMapFilter.setOnClickListener(this);
		mIbHelp = (ImageButton) findViewById(R.id.ibHelp);
		mIbHelp.setOnClickListener(this);
		mRlPOIContent.setOnClickListener(this);
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapContoller = mMapView.getController();
		((TextView) findViewById(R.id.zoomIn)).setOnClickListener(this);
		((TextView) findViewById(R.id.zoomOut)).setOnClickListener(this);

		//Timeline
		mLvTodo = (ListView) findViewById(R.id.lvTodo);
		mTodoAdapter = new TodoAdapter();
		mLvTodo.setAdapter(mTodoAdapter);
		mLvTodo.setOnItemClickListener(mTodoAdapter);
//		mLvTodo.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
//
//			@Override
//			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//
//				menu.add(0, 1, 1, "Modifier l'heure");
//				menu.add(0, 2, 2, "Supprimer l'activité");				
//			}
//		});
		mLvTodo.setOnCreateContextMenuListener(this);

		//Animations
		mOpenPOIBar = new OpenPOIBar();
		mClosePOIBar = new ClosePOIBar();
		mCloseTimeline = new CloseTimeline();
		mOpenTimeline = new OpenTimeline();

		mPOITypesSelected = new HashSet<POIType>();
		mPOIs = ((NaonodApplication) getApplication()).mPOIs;
		if (mPOIs.size() == 0) {
			startActivity(POISearchActivity.getIntent(this));
		}
	}

	@Override
	protected void onResume() {

		mPOITypesSelected.clear();
		getPOITypes();
		showSelectedPoiTypes();

		((NaonodApplication) getApplication()).getNaonodLocationManager().startTracking();
		mNaonodLocationManager.addObserver(this);

		setTodosCounter();
		if (mCurrentPOI!=null) {
			mMapContoller.setZoom(mCurrentMapZoom);
			zoomOut();
			zoomIn();
		} else {
			double lat = Double.parseDouble(getString(R.string.city_latitude));
			double lng = Double.parseDouble(getString(R.string.city_longitude));
			GeoPoint geoPointCity = new GeoPoint( (int) (lat * 1E6), (int) (lng * 1E6));

			mMapContoller.animateTo(geoPointCity);
			mMapContoller.setCenter(geoPointCity);
			mMapContoller.setZoom(mCurrentMapZoom); 
		}

		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llPOIContent:
			startActivity(POIActivity.getIntent(this, mCurrentPOI));
			break;
		case R.id.ibTimeline:
			openOrCloseTimeline();
			break;
		case R.id.ibMapFilter:
			createDialogFilter();
			break;
		case R.id.ibGPSSearching:
			setGPSSearching();
			break;
		case R.id.ibPOISearch:
			startActivity(POISearchActivity.getIntent(this));
			break;
		case R.id.ibListView:
			startActivity(POIListActivity.getIntent(this));
			break;
		case R.id.ibHelp:
			startActivity(AboutActivity.getIntent(this));
			break;
		case R.id.contentOverlay:
			setTimelineClose();
			break;
		case R.id.zoomIn:
			zoomIn();
			break;
		case R.id.zoomOut:
			zoomOut();
			break;
		}
	}

	private void zoomIn() {
		mMapContoller.zoomIn();
		mCurrentMapZoom = mMapView.getZoomLevel();
	}

	private void zoomOut() {
		mMapContoller.zoomOut();
		mCurrentMapZoom = mMapView.getZoomLevel();
	}

	private void setGPSSearching() {
		if (!mIsCenteringLocation) {
			mIbGPSSearching.setImageResource(R.drawable.map_overlay_gps_centering);
			mIsCenteringLocation = true;
		} else {
			mIbGPSSearching.setImageResource(R.drawable.map_overlay_gps);
			mIsCenteringLocation = false;
		}
	}

	private void openOrCloseTimeline() {
		if (mOpenedTimeline) {
			setTimelineClose();
		} else {
			setTimelineOpen();
		}
	}

	private void setTodosCounter() {
		if (mTodos.size() > 0) {
			mTodosCounter.setVisibility(View.VISIBLE);
			mTodosCounter.setText(""+ mTodos.size());
		} else {
			mTodosCounter.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		((NaonodApplication) getApplication()).getNaonodLocationManager().stopTracking();
		mNaonodLocationManager.deleteObserver(this);
		super.onPause();
	}

	private void preventExit() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.are_you_sure_to_quit_app)
		.setCancelable(false)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				POIMapActivity.this.finish();
			}
		})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.timeline_menu_items, menu);
	    
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.timeline_define_hour:
			changeTodoHour(info.position);
			break;
		case R.id.timeline_remove_activity:
			removeToDo(info.position);
			break;
		}

		return super.onContextItemSelected(item);
	}

	private void changeTodoHour(int position) {
		createPopupDefineTodoHour(position);
	}

	private void removeToDo(int position) {
		mTodos.remove(position);
		mTodoAdapter.notifyDataSetChanged();
		setTodosCounter();
	}

	private void createPopupDefineTodoHour(final int position) {

		int hourOfDay = mTodos.get(position).dateStart.getHours();
		int minute = mTodos.get(position).dateStart.getMinutes();

		TimePickerDialog tpDialog = new TimePickerDialog(this, new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mTodos.get(position).dateStart.setHours(hourOfDay);
				mTodos.get(position).dateStart.setMinutes(minute);

				((NaonodApplication) getApplication()).saveTimeline();

				Collections.sort(mTodos, mSortTodo);
				mTodoAdapter.notifyDataSetChanged();
			}
		}, hourOfDay, minute, true);
		tpDialog.setMessage(mTodos.get(position).poi.name);
		tpDialog.show();
	}

	private void createDialogFilter() {

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle("Afficher...");

		RelativeLayout filterDialog = (RelativeLayout) mInflater.inflate(R.layout.dialog_filter_pois, null);

		ListView lvPOITypes = (ListView) filterDialog.findViewById(R.id.lvPOITypes);
		lvPOITypes.setAdapter(new POITypeAdapter());

		final RatingBar rbFilterRating = (RatingBar) filterDialog.findViewById(R.id.rbFilterRating);
		rbFilterRating.setProgress(mMinimumPOIRating);

		Button bPOIFilter = (Button) filterDialog.findViewById(R.id.bPOIFilter);
		bPOIFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMinimumPOIRating = rbFilterRating.getProgress();
				showSelectedPoiTypes();
				dialog.dismiss();
			}
		});

		dialog.setContentView(filterDialog);
		dialog.show();
	}

	private class MyOverlayItem extends OverlayItem {

		POI poi;

		public MyOverlayItem(GeoPoint arg0, String arg1, String arg2) {
			super(arg0, arg1, arg2);
		}

		public MyOverlayItem(GeoPoint arg0, String arg1, String arg2, POI poi) {
			super(arg0, arg1, arg2);
			this.poi = poi;
		}
	}

	private class ShowPOI extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mMapView.getOverlays().clear();
			invalidateMapView();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			for (POIType poiType : mPOITypesSelected) {

				Drawable drawable = mActivity.getResources().getDrawable(poiType.getPOIDrawable());
				MapItemizedOverlay overlay = new MapItemizedOverlay(drawable);

				for (POI poi : mPOIs) {
					if (poi.poiType == poiType && poi.rating >= mMinimumPOIRating) {
						overlay.addOverlay(new MyOverlayItem(poi.position.myGeoPointToGeoPoint(), poi.name, ""+ poi.rating, poi));
					}
				}

				mMapView.getOverlays().add(overlay);	
			}
			invalidateMapView();

			return null;
		}

		private void invalidateMapView() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mMapView.invalidate();

				}
			});
		}
	}

	private void showSelectedPoiTypes() {

		new ShowPOI().execute();
	}

	private class POITypeAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mPOITypes.size();
		}

		@Override
		public Object getItem(int position) {
			return mPOITypes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final POIType poiType = ((POIType) getItem(position));
			CheckBox checkBox = new CheckBox(getApplicationContext());
			checkBox.setText(poiType.name());
			Drawable drawable = getResources().getDrawable(poiType.getPOIDrawable());
			checkBox.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

			if (mPOITypesSelected.contains(poiType)) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}

			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						mPOITypesSelected.add(poiType);
					} else {
						mPOITypesSelected.remove(poiType);
					}
				}
			});

			return checkBox;
		}	
	}

	private void getPOITypes() {
		mPOITypes = new ArrayList<POIType>();

		for (POI poi : mPOIs) {
			if (!mPOITypes.contains(poi.poiType)) {
				mPOITypes.add(poi.poiType);
				mPOITypesSelected.add(poi.poiType);
			}
		}
	}

	public void update(Observable obs, Object x) {
		if (obs instanceof NaonodLocationManager) {

			if ( mLocationItemizedOverlay!= null) {	
				if (mMapView.getOverlays().contains(mLocationItemizedOverlay)) {
					mMapView.getOverlays().remove(mLocationItemizedOverlay);
				}
			}

			Drawable drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
			mLocationItemizedOverlay = new LocationItemizedOverlay(drawable);

			Location location = mNaonodLocationManager.mLocation;
			GeoPoint geoPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));

			OverlayItem overlayitem = new OverlayItem(geoPoint, "", "");
			mLocationItemizedOverlay.addOverlay(overlayitem);

			mMapView.getOverlays().add(mLocationItemizedOverlay);

			if (mIsCenteringLocation) {
				mMapContoller.animateTo(geoPoint);
			}
		}
	}

	public class LocationItemizedOverlay extends ItemizedOverlay
	{
		//Liste des marqueurs
		private ArrayList mOverlays = new ArrayList();

		public LocationItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		//Appeler quand on rajoute un nouvel marqueur a la liste des marqueurs
		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
			mMapView.invalidate();
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

	private class TodoAdapter extends BaseAdapter implements OnItemClickListener {

		public TodoAdapter() {
			if (mTodos.size()>0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/y");
				String date =  dateFormat.format(mTodos.get(0).dateStart);
				((TextView) findViewById(R.id.tvTodoDate)).setText(date);
				mSortTodo = new SortTodo();
				Collections.sort(mTodos, mSortTodo);
			}			
		}

		@Override
		public int getCount() {
			return mTodos.size();
		}

		@Override
		public Object getItem(int position) {
			return mTodos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = (RelativeLayout) mInflater.inflate(R.layout.list_item_todo, null);

				holder.tvTodoHour = (TextView) convertView.findViewById(R.id.tvTodoHour);
				holder.tvTodoRating = (TextView) convertView.findViewById(R.id.tvTodoRating);
				holder.tvTodoName = (TextView) convertView.findViewById(R.id.tvTodoName);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Todo todo = mTodos.get(position);

			SimpleDateFormat dateFormat = new SimpleDateFormat("HH'H'mm");
			holder.tvTodoHour.setText(dateFormat.format(todo.dateStart));
			holder.tvTodoRating.setText( ((int) todo.poi.rating) + "" );
			holder.tvTodoName.setText( todo.poi.name );

			return convertView;
		}

		class ViewHolder {
			TextView tvTodoHour;
			TextView tvTodoRating;
			TextView tvTodoName;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {			
			startActivity(POIActivity.getIntent(POIMapActivity.this, mTodos.get(position).poi));
		}			
	}

	private class SortTodo implements Comparator<Todo> {

		@Override
		public int compare(Todo todo1, Todo todo2) {

			if (todo1.dateStart.getTime() > todo2.dateStart.getTime()) {
				return 1;
			} else if (todo1.dateStart.getTime() < todo2.dateStart.getTime()) {
				return -1;
			} else {
				return todo1.poi.name.compareTo(todo2.poi.name);
			}
		}
	}

	public class MapItemizedOverlay extends com.google.android.maps.ItemizedOverlay
	{
		//Liste des marqueurs
		private ArrayList mOverlays = new ArrayList();

		public MapItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		//Appeler quand on rajoute un nouvel marqueur a la liste des marqueurs
		public void addOverlay(MyOverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected MyOverlayItem createItem(int i) {
			return (MyOverlayItem) mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		//Appear quand on clique sur un marqueur
		@Override
		protected boolean onTap(int index) {

			MyOverlayItem item = (MyOverlayItem) mOverlays.get(index);

			if (mCurrentPOI == item.poi) {
				mCurrentPOI = null;
				mRlPOIContent.startAnimation(mClosePOIBar);
			}
			else {
				mCurrentPOI = item.poi;

				mTvPOIRating.setText(item.getSnippet());
				mTvPOITitle.setText(item.getTitle());

				mMapContoller.animateTo(item.getPoint());
				if (mRlPOIContent.getVisibility() != View.VISIBLE) {
					mRlPOIContent.startAnimation(mOpenPOIBar);
				}
			}
			return false;
		}
	} 

	private class OpenPOIBar extends Animation {

		public OpenPOIBar() {
			setDuration(POI_BAR_ANIMATION_SPEED);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {

			if (interpolatedTime == 0.0) {
				mRlPOIContent.getLayoutParams().height = 0;
				mRlPOIContent.setVisibility(View.VISIBLE);
			} 

			mRlPOIContent.getLayoutParams().height = (int) (mInitialLlPOIContentHeight * interpolatedTime);
			mRlPOIContent.requestLayout();
		}
	}

	private class ClosePOIBar extends Animation {

		public ClosePOIBar() {
			setDuration(POI_BAR_ANIMATION_SPEED);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {

			if (interpolatedTime == 1.0) {
				mRlPOIContent.setVisibility(View.GONE);
			}

			mRlPOIContent.getLayoutParams().height = (int) (mInitialLlPOIContentHeight * ( 1 -interpolatedTime));
			mRlPOIContent.requestLayout();
		}
	}


	private void setTimelineOpen() {

		mContentOverlay.setVisibility(View.VISIBLE);
		mIbGPSSearching.setVisibility(View.GONE);
		mLlZoom.setVisibility(View.GONE);

		mRlPOIContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setTimelineClose();
			}
		});

		mLlMapTimeline.getLayoutParams().width = mDisplay.getWidth() - mIbTimeline.getWidth();
		mLlMapContent.startAnimation(mOpenTimeline);
		mOpenedTimeline = true;
	}

	private void setTimelineClose() {

		mContentOverlay.setVisibility(View.GONE);
		mIbGPSSearching.setVisibility(View.VISIBLE);
		mLlZoom.setVisibility(View.VISIBLE);

		mRlPOIContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(POIActivity.getIntent(POIMapActivity.this, mCurrentPOI));
			}
		});

		mLlMapContent.startAnimation(mCloseTimeline);
		mOpenedTimeline = false;
	}

	private class OpenTimeline extends Animation {

		int finalLeftMargin;

		public OpenTimeline() {
			setFillAfter(true);  
			setDuration(TIMELINE_ANIMATION_SPEED);		
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {

			if(interpolatedTime==0.0) {
				finalLeftMargin = mDisplay.getWidth() - mIbTimeline.getWidth();
			}

			int newMarginLeft = (int) (finalLeftMargin * interpolatedTime);
			mMapContentLayoutParams.leftMargin = newMarginLeft;
			mLlMapContent.requestLayout();
		}
	}

	private class CloseTimeline extends Animation {
		int initialLeftMargin;

		public CloseTimeline() {
			setFillAfter(true);  
			setDuration(TIMELINE_ANIMATION_SPEED);		
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {

			if (interpolatedTime == 0.0) {
				initialLeftMargin = mDisplay.getWidth() - mIbTimeline.getWidth();
			}

			int newMarginLeft = (int) (initialLeftMargin * (1-interpolatedTime));
			mMapContentLayoutParams.leftMargin = newMarginLeft;
			mLlMapContent.requestLayout();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onBackPressed() {

		if (mOpenedTimeline) {
			setTimelineClose();
		} else {
			preventExit();
		}
	}
}