package fr.naonod.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.naonod.R;

import fr.naonod.application.NaonodApplication;
import fr.naonod.application.NaonodLocationManager;
import fr.naonod.model.MyGeoPoint;
import fr.naonod.model.POI;
import fr.naonod.model.POIType;
import fr.naonod.model.Todo;
import fr.naonod.view.component.HorizontalListView;

public class POIListActivity extends Activity implements Observer, OnClickListener{

	private LayoutInflater mInflater;
	private NaonodLocationManager mNaonodLocationManager;

	/*
	 * Objects
	 */
	private ArrayList<Todo> mTodos;
	private ArrayList<POIType> mPOITypes;
	private HashSet<POIType> mPOITypesSelected;
	private ArrayList<POI> mPOIs = new ArrayList<POI>();
	private TodoAdapter mTodoAdapter;
	private SortToDos mSortTodo;
	private SortPOIs mSortPOIs;
	private int mSelectedTodoPosition;
	private int mMinimumPOIRating = 1;

	/*
	 * UI
	 */
	private ImageButton mIbPOIsMap, mIbPOISearch, mIbListFilter, mIbHelp;
	private HorizontalListView mHmHlvToDo;
	private ListView mLvPOIs;
	private TextView mTvPOIsNb, mTodosCounter;
	private POIAdapter mPOIAdapter;

	public static Intent getIntent(Activity activity) {
		Intent intent = new Intent(activity, POIListActivity.class);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pois_list);

		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mPOIs = ((NaonodApplication) getApplication()).mPOIs;
		mSortPOIs = new SortPOIs();
		mTodos = ((NaonodApplication) getApplication()).mTodos;
		mNaonodLocationManager = ((NaonodApplication) getApplication()).getNaonodLocationManager();

		mTodosCounter = (TextView) findViewById(R.id.todosCounter);
		mIbPOIsMap = (ImageButton) findViewById(R.id.ibPOIsMap);
		mIbPOIsMap.setOnClickListener(this);
		mIbListFilter = (ImageButton) findViewById(R.id.ibListFilter);
		mIbListFilter.setOnClickListener(this);
		mIbPOISearch = (ImageButton) findViewById(R.id.ibPOISearch);
		mIbPOISearch.setOnClickListener(this);
		mIbHelp = (ImageButton) findViewById(R.id.ibHelp);
		mIbHelp.setOnClickListener(this);
		mTvPOIsNb = (TextView) findViewById(R.id.tvPOIsNb);

		mPOITypes = new ArrayList<POIType>();
		mPOITypesSelected = new HashSet<POIType>();

		mLvPOIs = (ListView) findViewById(R.id.lvPOIs);
		mPOIAdapter = new POIAdapter();
		mLvPOIs.setAdapter(mPOIAdapter);
		mLvPOIs.setOnItemClickListener(mPOIAdapter);

		mHmHlvToDo = (HorizontalListView) findViewById(R.id.hlvTodo);
		mTodoAdapter = new TodoAdapter();
		mHmHlvToDo.setAdapter(mTodoAdapter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.timeline_menu_items, menu);
	    
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.timeline_define_hour:
			changeTodoHour(mSelectedTodoPosition);
			break;
		case R.id.timeline_remove_activity:
			removeToDo(mSelectedTodoPosition);
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onResume() {
		((NaonodApplication) getApplication()).getNaonodLocationManager().startTracking();
		mNaonodLocationManager.addObserver(this);
		Collections.sort(mPOIs, mSortPOIs);
		getPOITypes();
		getListElements();
		mPOIAdapter.notifyDataSetChanged();
		setTodosCounter();
		mTodoAdapter.notifyDataSetChanged();
		super.onResume();
	}
	
	private void setActivitiesNb(int nb) {
		mTvPOIsNb.setText(nb + " activités trouvées");	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibPOIsMap:
			finish();
			break;
		case R.id.ibHelp:
			startActivity(AboutActivity.getIntent(this));
			break;
		case R.id.ibListFilter:
			createDialogFilter();
			break;
		case R.id.ibPOISearch:
			startActivity(POISearchActivity.getIntent(this));
			break;
		}
	}

	private void changeTodoHour(int position) {
		createPopupDefineTodoHour(position);
	}

	private void createPopupDefineTodoHour(final int position) {

		int hourOfDay = mTodos.get(position).dateStart.getHours();
		int minute = mTodos.get(position).dateStart.getMinutes();

		TimePickerDialog tpDialog = new TimePickerDialog(this, new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mTodos.get(position).dateStart.setHours(hourOfDay);
				mTodos.get(position).dateStart.setMinutes(minute);

				Collections.sort(mTodos, mSortTodo);
				mTodoAdapter.notifyDataSetChanged();
			}
		}, hourOfDay, minute, true);
		tpDialog.setMessage(mTodos.get(position).poi.name);
		tpDialog.show();
	}

	private void removeToDo(int position) {
		setTodosCounter();
		mTodos.remove(position);
		mTodoAdapter.notifyDataSetChanged();
	}

	private class TodoAdapter extends BaseAdapter {

		public TodoAdapter() {
			if (mTodos.size()>0) {
				mSortTodo = new SortToDos();
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
		public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = (RelativeLayout) mInflater.inflate(R.layout.list_item_todo_horizontal, null);
				holder.tvTodoHour = (TextView) convertView.findViewById(R.id.tvTodoHour);
				holder.tvTodoName = (TextView) convertView.findViewById(R.id.tvTodoName);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final Todo todo = mTodos.get(position);

			((View) convertView).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(POIActivity.getIntent(POIListActivity.this, todo.poi));
				}
			});
			((View) convertView).setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					mSelectedTodoPosition = position;
					registerForContextMenu(v);
					openContextMenu(v);
					return false;
				}
			});

			SimpleDateFormat dateFormat = new SimpleDateFormat("HH'H'mm");
			holder.tvTodoHour.setText(dateFormat.format(todo.dateStart));
			holder.tvTodoName.setText( todo.poi.name );

			return convertView;
		}

		class ViewHolder {
			TextView tvTodoHour;
			TextView tvTodoName;
		}
	}

	private class SortToDos implements Comparator<Todo> {

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

	private void getPOITypes() {
		mPOITypes.clear();
		mPOITypesSelected.clear();

		for (POI poi : mPOIs) {
			if (!mPOITypes.contains(poi.poiType)) {
				mPOITypes.add(poi.poiType);
				mPOITypesSelected.add(poi.poiType);
			}
		}
	}

	public void update(Observable obs, Object x) {
		if (obs instanceof NaonodLocationManager) {
			if (mPOIAdapter!= null) {
				mPOIAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void onPause() {
		((NaonodApplication) getApplication()).getNaonodLocationManager().stopTracking();
		mNaonodLocationManager.deleteObserver(this);
		super.onPause();
	}

	private void getListElements() {
		if (mPOIs.size() == 0) {
			return;
		}
		POIType currentPOIType = null;

		mPOIAdapter.mData.clear();
		mPOIAdapter.mSeparatorsSet.clear();

		for (POI poi : mPOIs) {
			if (mPOITypesSelected.contains(poi.poiType)) {
				if (currentPOIType == null) {
					currentPOIType = poi.poiType;
					mPOIAdapter.addSeparatorItem(poi.poiType);
				}
				else if (currentPOIType != poi.poiType) {
					mPOIAdapter.addSeparatorItem(poi.poiType);
				}

				currentPOIType = poi.poiType;
				if (poi.rating >= mMinimumPOIRating ) {
					mPOIAdapter.addItem(poi);
				}
			}
		}
		
		setActivitiesNb(mPOIAdapter.mData.size() - mPOIAdapter.mSeparatorsSet.size());
	}

	private class SortPOIs implements Comparator<POI> {

		@Override
		public int compare(POI poi1, POI poi2) {

			if (poi1.poiType == poi2.poiType) {
				return poi1.name.compareTo(poi2.name);
			} else {
				return poi1.poiType.getPOIName().compareTo(poi2.poiType.getPOIName());
			}			
		}
	}

	private class POIAdapter extends BaseAdapter implements OnItemClickListener{

		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;
		private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

		ArrayList mData = new ArrayList();
		LayoutInflater mInflater;
		TreeSet mSeparatorsSet = new TreeSet();

		public POIAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(POI poi) {
			mData.add(poi);
		}

		public void addSeparatorItem(POIType poiType) {
			mData.add(poiType);
			mSeparatorsSet.add(mData.size() - 1);
		}

		@Override
		public int getItemViewType(int position) {
			return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			int type = getItemViewType(position);
			switch (type) {
			case TYPE_ITEM:
				POI poi = (POI) mData.get(position);

				ViewHolderPOI holderPOI;
				if (convertView == null || convertView.getTag() instanceof ViewHolderSeparator) {
					holderPOI = new ViewHolderPOI();
					convertView = (RelativeLayout) mInflater.inflate(R.layout.list_item_poi, null);
					holderPOI.tvPOIName = (TextView) convertView.findViewById(R.id.tvPOIName);
					holderPOI.tvPOIRating = (TextView) convertView.findViewById(R.id.tvPOIRating);
					holderPOI.tvPOIDistance = (TextView) convertView.findViewById(R.id.tvPOIDistance);
					holderPOI.flPOIType = (FrameLayout) convertView.findViewById(R.id.flPOIType);
					convertView.setTag(holderPOI);
				} else {
					holderPOI = (ViewHolderPOI) convertView.getTag();
				}

				holderPOI.tvPOIName.setText(poi.name);
				holderPOI.tvPOIRating.setText( ((int) poi.rating) + "" );
				holderPOI.tvPOIDistance.setText("À "+ MyGeoPoint.calculateDistance(poi.position, mNaonodLocationManager.mLocation) + " km");
				holderPOI.flPOIType.setBackgroundColor(Color.parseColor(poi.poiType.getPOIColor()));
				return convertView;
			case TYPE_SEPARATOR:
				POIType poiType = (POIType) mData.get(position);

				ViewHolderSeparator holderPOIType;
				if (convertView == null  || convertView.getTag() instanceof ViewHolderPOI) {
					holderPOIType = new ViewHolderSeparator();			
					convertView = (LinearLayout) mInflater.inflate(R.layout.list_item_poi_separator, null);
					holderPOIType.tvPOIType = (TextView) convertView.findViewById(R.id.tvPOIType);
					convertView.setTag(holderPOIType);
				} else {
					holderPOIType = (ViewHolderSeparator) convertView.getTag();
				}

				holderPOIType.tvPOIType.setText(poiType.getPOIName());
				return convertView;
			}
			return null;
		}

		private class ViewHolderPOI {
			private TextView tvPOIName;
			private TextView tvPOIDistance;
			private TextView tvPOIRating;
			private FrameLayout flPOIType;
		}

		private class ViewHolderSeparator {
			private TextView tvPOIType;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			startActivity(POIActivity.getIntent(POIListActivity.this, (POI) mData.get(position)));
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
				getListElements();
				mPOIAdapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});

		dialog.setContentView(filterDialog);
		dialog.show();
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
}