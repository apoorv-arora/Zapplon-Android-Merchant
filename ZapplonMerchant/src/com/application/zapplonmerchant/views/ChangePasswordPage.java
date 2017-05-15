package com.application.zapplonmerchant.views;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.TypefaceSpan;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePasswordPage extends ActionBarActivity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private int width;
	private ZApplication zapp;
	private Activity mContext;
	private boolean destroyed = false;
	private ProgressDialog zProgressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password_page);
		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		mContext = this;
		setUpActionBar();
		UploadManager.addCallback(this);

		findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String oldPassword = ((TextView) findViewById(R.id.old_password)).getText().toString();
				String newPassword = ((TextView) findViewById(R.id.password)).getText().toString();
				String confirmPassword = ((TextView) findViewById(R.id.confirm_password)).getText().toString();

				if (!newPassword.equals(confirmPassword)) {
					Toast.makeText(ChangePasswordPage.this, getResources().getString(R.string.values_mismatch),
							Toast.LENGTH_LONG).show();
					return;
				}

				UploadManager.updatePassword(oldPassword, newPassword);

			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void setUpActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		String str = getResources().getString(R.string.change_password);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		if (zProgressDialog != null && zProgressDialog.isShowing()) {
			zProgressDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.PASSWORD_UPDATE) {
			if (!destroyed) {
				if (status) {
					Toast.makeText(this, getResources().getString(R.string.success), Toast.LENGTH_LONG).show();
					CommonLib.hideKeyBoard(this, findViewById(R.id.confirm_password));
					finish();
				} else
					Toast.makeText(this, getResources().getString(R.string.err_occurred), Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}
}
