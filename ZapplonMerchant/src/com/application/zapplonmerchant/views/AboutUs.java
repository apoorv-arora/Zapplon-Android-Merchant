package com.application.zapplonmerchant.views;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.TypefaceSpan;
import com.application.zapplonmerchant.utils.ZWebView;
import com.google.android.gms.plus.PlusOneButton;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AboutUs extends ActionBarActivity {

	private int width;
	PlusOneButton mPlusOneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		width = getWindowManager().getDefaultDisplay().getWidth();
		setUpActionBar();
		fixsizes();
		setListeners();

		mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
		ImageView img = (ImageView) findViewById(R.id.zomato_logo);
		img.getLayoutParams().width = width / 3;
		img.getLayoutParams().height = width / 3;
		// setting image
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher, options);
			options.inSampleSize = CommonLib.calculateInSampleSize(options, width, width);
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Config.RGB_565;
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher, options);

			img.setImageBitmap(bitmap);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		} catch (Exception e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (mPlusOneButton != null)
				mPlusOneButton.initialize("https://market.android.com/details?id=" + getPackageName(), 0);
		} catch (Exception d) {

		}
	}

	private void setUpActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		String str = getResources().getString(R.string.about_us);
		SpannableString s = new SpannableString(str);
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
		if (!isAndroidL)
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

		actionBar.setTitle(s);
	}

	void fixsizes() {

		width = getWindowManager().getDefaultDisplay().getWidth();

		// About us main page layouts
		// findViewById(R.id.home_logo).getLayoutParams().height = 3 * width /
		// 10;
		// findViewById(R.id.home_logo).getLayoutParams().width = 3 * width /
		// 10;
		findViewById(R.id.home_version).setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.home_logo_container).setPadding(width / 20, width / 20, width / 20, width / 20);
		findViewById(R.id.about_us_body).setPadding(width / 20, 0, width / 20, width / 20);
		((LinearLayout.LayoutParams) findViewById(R.id.plus_one_button).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, width / 20);

		RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(width, 9 * width / 80);
		relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		findViewById(R.id.about_us_privacy_policy_container).setLayoutParams(relativeParams2);
		((TextView) ((LinearLayout) findViewById(R.id.about_us_privacy_policy_container)).getChildAt(0))
				.setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.about_us_privacy_policy).setPadding(width / 40, 0, width / 20, 0);

		RelativeLayout.LayoutParams relativeParams3 = new RelativeLayout.LayoutParams(width, 9 * width / 80);
		relativeParams3.addRule(RelativeLayout.ABOVE, R.id.separator3);
		findViewById(R.id.about_us_terms_conditions_container).setLayoutParams(relativeParams3);
		((TextView) ((LinearLayout) findViewById(R.id.about_us_terms_conditions_container)).getChildAt(0))
				.setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.about_us_terms_conditions).setPadding(width / 40, 0, width / 20, 0);
	}

	public void setListeners() {

		LinearLayout btnTermsAndConditons = (LinearLayout) findViewById(R.id.about_us_terms_conditions_container);
		btnTermsAndConditons.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// tracker.trackPageView("/android/About_TC/"+zapp.city_id);
				// trackerAll.trackPageView("/android/About_TC/"+zapp.city_id);

				Intent intent = new Intent(AboutUs.this, ZWebView.class);
				intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
				intent.putExtra("url", "https://www.zapplon.com/terms_mobile.html");
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			}
		});

		LinearLayout btnPrivacyPolicy = (LinearLayout) findViewById(R.id.about_us_privacy_policy_container);
		btnPrivacyPolicy.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// tracker.trackPageView("/android/About_Privacy/"+zapp.city_id);
				// trackerAll.trackPageView("/android/About_Privacy/"+zapp.city_id);

				Intent intent = new Intent(AboutUs.this, ZWebView.class);
				intent.putExtra("title", getResources().getString(R.string.about_us_privacypolicy));
				intent.putExtra("url", "https://www.zapplon.com/privacy_mobile.html");
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			}
		});
	}

	public void goBack(View view) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void actionBarSelected(View v) {

		switch (v.getId()) {

		case R.id.home_icon_container:
			onBackPressed();

		default:
			break;
		}

	}

}
