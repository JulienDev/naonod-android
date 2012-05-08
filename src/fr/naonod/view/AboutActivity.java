package fr.naonod.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.naonod.R;

import fr.naonod.tools.Tools;

public class AboutActivity extends Activity implements OnClickListener{

	public static Intent getIntent(Activity activity) {
		return new Intent(activity, AboutActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);

		TextView tv = (TextView) findViewById(R.id.aboutContest);
		Drawable img = getResources().getDrawable(R.drawable.logo_nod);
		img.setFilterBitmap(true);
		int density = (int) Tools.getDensity(getApplicationContext());
		img.setBounds( 0, 0, 55*density, 40*density );
		tv.setCompoundDrawables(null,null,img,null);

		((TextView) findViewById(R.id.tvAboutJulien)).setOnClickListener(this);
		((TextView) findViewById(R.id.tvAboutThomas)).setOnClickListener(this);
		((TextView) findViewById(R.id.tvAboutArzhel)).setOnClickListener(this);
		((TextView) findViewById(R.id.tvAboutJeremy)).setOnClickListener(this);
		((TextView) findViewById(R.id.aboutContest)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvAboutJulien:
			sendEmail(getString(R.string.about_email_julien));
			break;
		case R.id.tvAboutThomas:
			sendEmail(getString(R.string.about_email_thomas));
			break;
		case R.id.tvAboutArzhel:
			sendEmail(getString(R.string.about_email_arzhel));
			break;
		case R.id.tvAboutJeremy:
			sendEmail(getString(R.string.about_email_jeremy));
			break;
		case R.id.aboutContest:
			visitWebsite(getString(R.string.about_nod_url));
			break;
		}
	}
	
	private void sendEmail(String address) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("plain/text");
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ address});
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
		startActivity(Intent.createChooser(intent, getString(R.string.about_write_us)));
	}

	private void visitWebsite(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(url);
		intent.setData(uri);
		startActivity(intent); 
	}
}