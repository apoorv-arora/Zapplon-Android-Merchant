package com.application.zapplonmerchant.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicInteger;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.Merchant;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;
import com.application.zapplonmerchant.utils.ZWebView;
import com.application.zapplonmerchant.utils.location.ZLocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SplashScreen extends Activity implements ZLocationCallback, UploadManagerCallback {

	private int width;
	private int height;
	private Activity mContext;
	private boolean destroyed = false;
	ProgressDialog zProgressDialog;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String regId;
	int hardwareRegistered = 0;
	private SharedPreferences prefs;
	private ZApplication zapp;
	private String APPLICATION_ID;
	View mSignupContainer;
	View mViewPager;

	final int DEFAULT_SHOWN = 87;
	final int LOGIN_SHOWN = 88;
	final int SIGNUP_SHOWN = 89;
	int mState = DEFAULT_SHOWN;
	private boolean frgtPswrd = false;
	private boolean mLoginContainerAnimating = false;
	View loginPage;
	private int emailLength = 0;
	private int pswdLength = 0;

	private final int EMAIL_FEEDBACK = 1500;

	private boolean windowHasFocus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		// initialize variables
		mContext = this;
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		APPLICATION_ID = prefs.getString("app_id", "");

		mViewPager = (ViewPager) findViewById(R.id.tour_view_pager);
		mSignupContainer = (RelativeLayout) findViewById(R.id.signup_container);
		loginPage = (RelativeLayout) findViewById(R.id.main_login_container);

		TourPagerAdapter mTourPagerAdpater = new TourPagerAdapter();
		((ViewPager) mViewPager).setAdapter(mTourPagerAdpater);

		((ViewPager) mViewPager).setOnPageChangeListener(new OnPageChangeListener() {
			int position = ((ViewPager) mViewPager).getOffscreenPageLimit();

			@Override
			public void onPageSelected(int arg0) {

				// startPosition = arg0;

				LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);

				int index = 1;
				for (int count = 0; count < index; count++) {
					ImageView dots = (ImageView) dotsContainer.getChildAt(count);

					if (count == arg0)
						dots.setImageResource(R.drawable.tour_image_dots_selected);
					else
						dots.setImageResource(R.drawable.tour_image_dots_unselected);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		// initialize views

		// handle the rest
		final ImageView img = (ImageView) findViewById(R.id.background_image);
		try {
			int imageWidth = width;
			int imageHeight = height;

			Bitmap searchBitmap = CommonLib.getBitmap(this, R.drawable.splash, imageWidth, imageHeight);
			img.getLayoutParams().width = imageWidth;
			img.getLayoutParams().height = imageHeight;
			img.setImageBitmap(searchBitmap);

			searchBitmap = Bitmap.createScaledBitmap(searchBitmap, width / 2, height / 2, true);
			// loginPage.setBackgroundDrawable(new
			// BitmapDrawable(getResources(), CommonLib.fastBlur(searchBitmap,
			// 20)));
			loginPage.setBackgroundDrawable(new BitmapDrawable(getResources(), searchBitmap));

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		}

		UploadManager.addCallback(this);

		findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView email = (TextView) findViewById(R.id.email);

				if (email.getText() == null || email.getText().toString() == null
						|| email.getText().toString().length() < 1) {
					Toast.makeText(SplashScreen.this, "Invalid email", Toast.LENGTH_LONG).show();
					return;
				}

				TextView password = (TextView) findViewById(R.id.password);
				if (password.getText() == null || password.getText().toString() == null
						|| password.getText().toString().length() < 1) {
					Toast.makeText(SplashScreen.this, "Invalid password", Toast.LENGTH_LONG).show();
					return;
				}

				zProgressDialog = ProgressDialog.show(SplashScreen.this, "", "Verifying credentails. Please wait...");
				zProgressDialog.setCancelable(false);

				UploadManager.login(email.getText().toString(), password.getText().toString());

			}
		});
		animate();

		findViewById(R.id.signup_container).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				animaterToScreen2(findViewById(R.id.merchant_login), false);
			}
		});

		updateDotsContainer();

		// start location check
		zapp.zll.forced = true;
		zapp.zll.addCallback(this);
		zapp.startLocationCheck();

	}

	public void goBack(View v) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {

		if (frgtPswrd) {
			// backToSignin(findViewById(R.id.forgot_password_container));

		} else if (mState == LOGIN_SHOWN) {

			mState = DEFAULT_SHOWN;

			Animation animation = new TranslateAnimation(0, 0, 0, height);
			animation.setDuration(CommonLib.ANIMATION_LOGIN);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					loginPage.setVisibility(View.GONE);

					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);
				}
			});
			loginPage.startAnimation(animation);

		} else {

			int position = (int) findViewById(R.id.main_root).getY();

			if (position == 0) {
				// Tour.BACK_PRESSED = 200;
				// setResult(Activity.RESULT_CANCELED);
				super.onBackPressed();
				// //overridePendingTransition(R.anim.fade_in_fast,R.anim.fade_out_fast);

			} else if (!mLoginContainerAnimating) {

				final View root = findViewById(R.id.main_root);
				View focusedView = root.findFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (focusedView != null) {
					imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
					focusedView.clearFocus();

				} else {
					imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);
				}

				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

				root.animate().translationY(0).setDuration(CommonLib.ANIMATION_DURATION_SIGN_IN)
						.setListener(new AnimatorListenerAdapter() {

							@Override
							public void onAnimationStart(Animator animation) {
								mLoginContainerAnimating = true;
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								mLoginContainerAnimating = false;
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								if (root.getY() == 0) {
									// findViewById(R.id.login_container).setVisibility(View.GONE);
									// findViewById(R.id.forgot_pass_text).setVisibility(View.GONE);
									// findViewById(R.id.blank_view).setVisibility(View.GONE);
								}
								mLoginContainerAnimating = false;
							}
						});
			}
		}
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing()) {
			zProgressDialog.dismiss();
		}
		zapp.zll.removeCallback(this);
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private void navigateToHome() {
		if (prefs.getInt("merchant_id", 0) != 0) {
			if (prefs.getInt("store_id", 0) == 0) {
				// Store add activity
				Intent intent = new Intent(this, StoreDetailsScreen.class);
				startActivity(intent);
				finish();
			} else {
				Intent intent = new Intent(this, Home.class);
				startActivity(intent);
				finish();
			}
		}
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.REQUEST_PRE_SIGNUP) {
			if (zProgressDialog != null) {
				zProgressDialog.dismiss();
			}
		} else if (requestType == CommonLib.LOGIN) {
			if (zProgressDialog != null) {
				zProgressDialog.dismiss();
			}

			if (destroyed)
				return;

			if (data == null || !(data instanceof Object[])) {
				if (CommonLib.isNetworkAvailable(SplashScreen.this))
					Toast.makeText(SplashScreen.this, getResources().getString(R.string.invalid_password),
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(SplashScreen.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_LONG).show();
				return;
			}

			Object[] dataArr = ((Object[]) data);
			String accessToken = (String) dataArr[0];
			Merchant merchant = (Merchant) dataArr[1];

			Store store = null;

			if (dataArr.length > 1) {
				store = (Store) dataArr[2];
			}

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("access_token", accessToken);
			editor.putString("merchant_name", merchant.getMerchantName());
			editor.putString("merchant_email", merchant.getEmail());
			editor.putInt("merchant_id", merchant.getMerchantId());

			if (store != null) {
				editor.putInt("store_id", store.getStoreId());
				editor.putInt("store_type", store.getStoreType());
				editor.putFloat("store_lon", (float) store.getLongitude());
				editor.putFloat("store_lat", (float) store.getLatitude());
				editor.putString("store_name", store.getStoreName());
				editor.putString("store_address", store.getAddress());
				editor.putString("store_contact", store.getContactNumber());
				editor.putInt("availability", store.getAvailability());
				editor.putInt("max_occupancy", store.getMaxOccupancy());
				editor.putInt("current_occupancy", store.getCurrentOccupancy());
				editor.putBoolean("accepts_reservation", store.isAcceptsReservation());
			}

			editor.commit();

			checkPlayServices();

		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.REQUEST_PRE_SIGNUP) {
			if (zProgressDialog != null && zProgressDialog.isShowing()) {
				zProgressDialog.dismiss();
			}
			zProgressDialog = ProgressDialog.show(mContext, null,
					mContext.getResources().getString(R.string.signingup_wait));
		}
	}

	private Animation animation1, animation3;

	private void animate() {

		try {
			mSignupContainer.setVisibility(View.INVISIBLE);

			animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);
			animation3.setInterpolator(new DecelerateInterpolator());
			animation3.restrictDuration(700);
			animation3.scaleCurrentDuration(1);
			animation3.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
				}
			});

			animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_center);
			animation1.setDuration(700);
			animation1.restrictDuration(700);
			animation1.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (prefs.getInt("merchant_id", 0) == 0) {
						mSignupContainer.setVisibility(View.VISIBLE);
						mSignupContainer.startAnimation(animation3);
						findViewById(R.id.tour_dots).setVisibility(View.VISIBLE);
					} else {
						checkPlayServices();
					}
				}
			});
			animation1.scaleCurrentDuration(1);
			mViewPager.startAnimation(animation1);

		} catch (Exception e) {
			mSignupContainer.setVisibility(View.VISIBLE);
			findViewById(R.id.tour_dots).setVisibility(View.VISIBLE);
		}
	}

	private void updateDotsContainer() {

		LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);
		dotsContainer.removeAllViews();

		int index = 1;

		for (int count = 0; count < index; count++) {
			ImageView dots = new ImageView(getApplicationContext());

			if (count == 0) {
				dots.setImageResource(R.drawable.tour_image_dots_selected);
				dots.setPadding(width / 40, 0, width / 40, 0);

			} else {
				dots.setImageResource(R.drawable.tour_image_dots_unselected);
				dots.setPadding(0, 0, width / 40, 0);
			}

			final int c = count;
			dots.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						((ViewPager) mViewPager).setCurrentItem(c);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			dotsContainer.addView(dots);
		}
	}

	private class TourPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {

			FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.tour_element, null);

			if (position == 0) {

				ImageView tour_logo = (ImageView) layout.findViewById(R.id.tour_logo);

				// setting image
				try {
					tour_logo.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.logo, width, height));

				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
				}

				tour_logo.setVisibility(View.VISIBLE);

				LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				imageParams.setMargins(15 * width / 50, 3 * width / 16, 15 * width / 50, 0);

			}

			collection.addView(layout, 0);
			return layout;
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			collection.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public void finishUpdate(ViewGroup arg0) {
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(ViewGroup arg0) {
		}

	}

	public void forgotPassword(View v) {
		frgtPswrd = true;
	}

	private void animaterToScreen2(View view, Boolean fromSignUp) {
		int id = view.getId();
		if (id == R.id.merchant_login) {

			mState = LOGIN_SHOWN;

			// animations
			loginPage.setVisibility(View.VISIBLE);
			Animation animation = new TranslateAnimation(0, 0, height, 0);
			animation.setDuration(CommonLib.ANIMATION_LOGIN);
			loginPage.startAnimation(animation);

			// header
			// loginPage.findViewById(R.id.page_header_text).setPadding(width /
			// 20, 0, 0, 0);
			loginPage.findViewById(R.id.page_header_close).setPadding(width / 20, 0, width / 20, 0);
			((TextView) loginPage.findViewById(R.id.page_header_text))
					.setText(getResources().getString(R.string.login));

			loginPage.findViewById(R.id.about_us_terms_conditions_container).setPadding(width / 20, width / 40,
					width / 20, width / 40);

			if (!CommonLib.checkLgManufacturer()) {
				String loginTerms = getResources().getString(R.string.login_terms);
				String termsOfService = getResources().getString(R.string.about_us_terms_of_use);
				String privacyPolicy = getResources().getString(R.string.about_us_privacypolicy);
				String contentPolicy = getResources().getString(R.string.content_policies);

				SpannableStringBuilder finalSpanBuilderStr = new SpannableStringBuilder(loginTerms);

				ClickableSpan cs1 = new ClickableSpan() {
					@Override
					public void onClick(View widget) {
						Intent intent = new Intent(SplashScreen.this, ZWebView.class);
						intent.putExtra("title", getResources().getString(R.string.about_us_privacypolicy));
						intent.putExtra("url", "http://zapplon.com/privacy.html");
						startActivity(intent);
					}

					@Override
					public void updateDrawState(TextPaint ds) {
						super.updateDrawState(ds);
						ds.setUnderlineText(true);
						ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
						ds.setColor(getResources().getColor(R.color.white_trans_fifty));
					}
				};

				ClickableSpan cs2 = new ClickableSpan() {

					@Override
					public void onClick(View widget) {
						Intent intent = new Intent(SplashScreen.this, ZWebView.class);
						intent.putExtra("title", getResources().getString(R.string.content_policies));
						intent.putExtra("url", "https://www.zapplon.com/policy_mobile.html");
						startActivity(intent);
					}

					@Override
					public void updateDrawState(TextPaint ds) {
						super.updateDrawState(ds);
						ds.setUnderlineText(true);
						ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
						ds.setColor(getResources().getColor(R.color.white_trans_fifty));
					}

				};

				ClickableSpan cs3 = new ClickableSpan() {
					@Override
					public void onClick(View widget) {
						Intent intent = new Intent(SplashScreen.this, ZWebView.class);
						intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
						intent.putExtra("url", "http://zapplon.com/terms.html");
						startActivity(intent);
					}

					@Override
					public void updateDrawState(TextPaint ds) {
						super.updateDrawState(ds);
						ds.setUnderlineText(true);
						ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
						ds.setColor(getResources().getColor(R.color.white_trans_fifty));
					}
				};

				try {
					if (loginTerms.indexOf(privacyPolicy) == -1 || loginTerms.indexOf(contentPolicy) == -1) {
						loginPage.findViewById(R.id.about_us_terms_conditions)
								.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
										Intent intent = new Intent(SplashScreen.this, ZWebView.class);
										intent.putExtra("title",
												getResources().getString(R.string.about_us_terms_of_use));
										intent.putExtra("url", "http://zapplon.com/terms.html");
										startActivity(intent);
									}
								});
					}
					finalSpanBuilderStr.setSpan(cs1, loginTerms.indexOf(privacyPolicy),
							loginTerms.indexOf(privacyPolicy) + privacyPolicy.length(),
							SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

					finalSpanBuilderStr.setSpan(cs2, loginTerms.indexOf(contentPolicy),
							loginTerms.indexOf(contentPolicy) + contentPolicy.length(),
							SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

					finalSpanBuilderStr.setSpan(cs3, loginTerms.indexOf(termsOfService),
							loginTerms.indexOf(termsOfService) + termsOfService.length(),
							SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
				} catch (Exception e) {

				}
				((TextView) loginPage.findViewById(R.id.about_us_terms_conditions)).setText(finalSpanBuilderStr,
						TextView.BufferType.SPANNABLE);

				((TextView) loginPage.findViewById(R.id.about_us_terms_conditions))
						.setMovementMethod(LinkMovementMethod.getInstance());

			} else {
				((TextView) loginPage.findViewById(R.id.about_us_terms_conditions))
						.setText(getResources().getString(R.string.login_terms));

				loginPage.findViewById(R.id.about_us_terms_conditions_container)
						.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Intent intent = new Intent(SplashScreen.this, ZWebView.class);
								intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
								intent.putExtra("url", "http://zapplon.com/terms.html");
								startActivity(intent);
							}
						});
			}

			// form
			final EditText email = ((EditText) loginPage.findViewById(R.id.email));
			final EditText pswrd = ((EditText) loginPage.findViewById(R.id.password));

			email.getLayoutParams().height = width / 7;
			pswrd.getLayoutParams().height = width / 7;

			email.setPadding(width / 20, 0, width / 20, 0);
			pswrd.setPadding(width / 20, 0, width / 20, 0);

			loginPage.findViewById(R.id.submit).getLayoutParams().height = width / 7;
			loginPage.findViewById(R.id.forgot_password).getLayoutParams().height = width / 10;
			loginPage.findViewById(R.id.forgot_password).setPadding(0, width / 40, 0, 0);

			loginPage.findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("application/octet-stream");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "shiwang@zapplon.com" });
					intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.forgot_password_request));
					try {
						prefs = getSharedPreferences("application_settings", 0);
						final String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n"
								+ "Connection   : " + CommonLib.getNetworkState(SplashScreen.this) + "_"
								+ CommonLib.getNetworkType(SplashScreen.this) + "\n" + "Identifier   : "
								+ prefs.getString("app_id", "") + "\n" + "User Id     	: " + prefs.getInt("uid", 0)
								+ "\n" + "&device=" + android.os.Build.DEVICE);

						FileOutputStream fOut = openFileOutput("log.txt", MODE_WORLD_READABLE);
						File file = getFileStreamPath("log.txt");
						Uri uri = Uri.fromFile(file);
						OutputStreamWriter osw = new OutputStreamWriter(fOut);
						osw.write(LogString);
						osw.flush();
						osw.close();
						intent.putExtra(Intent.EXTRA_STREAM, uri);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						startActivityForResult(
								Intent.createChooser(intent, getResources().getString(R.string.send_mail)),
								EMAIL_FEEDBACK);
					} catch (android.content.ActivityNotFoundException ex) {
						Toast.makeText(SplashScreen.this, getResources().getString(R.string.no_email_clients),
								Toast.LENGTH_LONG).show();
					}

				}
			});

			// log in button
			((RelativeLayout.LayoutParams) loginPage.findViewById(R.id.submit).getLayoutParams()).setMargins(width / 20,
					0, width / 20, 0);
			((TextView) loginPage.findViewById(R.id.submit)).setText(getResources().getString(R.string.login));

			// separator view
			// ((LinearLayout.LayoutParams)
			// loginPage.findViewById(R.id.login_page_separator).getLayoutParams()).setMargins(width
			// / 20, width / 20, width / 20, width / 20);

			int buttonHeight = (11 * 9 * width) / (80 * 10);

			email.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					emailLength = s.toString().trim().length();

					int filled = emailLength * pswdLength;

					if (filled > 0) {
						loginPage.findViewById(R.id.submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						loginPage.findViewById(R.id.submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			pswrd.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					pswdLength = s.toString().trim().length();
					int filled = emailLength * pswdLength;

					if (filled > 0) {
						loginPage.findViewById(R.id.submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						loginPage.findViewById(R.id.submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			loginPage.findViewById(R.id.submit).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					submitLogin();
				}
			});

			pswrd.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {

					if (keyCode == EditorInfo.IME_ACTION_DONE) {

						View focusedView = v;
						InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
						if (focusedView != null) {
							imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
							focusedView.clearFocus();

						} else
							imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);

						submitLogin();

						return true;
					}
					return false;
				}
			});
		}
	}

	private void submitLogin() {
		TextView email = (TextView) findViewById(R.id.email);

		if (email.getText() == null || email.getText().toString() == null || email.getText().toString().length() < 1) {
			Toast.makeText(SplashScreen.this, "Invalid email", Toast.LENGTH_LONG).show();
			return;
		}

		TextView password = (TextView) findViewById(R.id.password);
		if (password.getText() == null || password.getText().toString() == null
				|| password.getText().toString().length() < 1) {
			Toast.makeText(SplashScreen.this, "Invalid password", Toast.LENGTH_LONG).show();
			return;
		}

		zProgressDialog = ProgressDialog.show(SplashScreen.this, "", "Verifying credentails. Please wait...");
		zProgressDialog.setCancelable(false);

		String regId = prefs.getString("registration_id", "");// TODO:
																// Send
																// the
																// registration
																// id
																// too
		UploadManager.login(email.getText().toString(), password.getText().toString());

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == EMAIL_FEEDBACK) {
			deleteFile("log.txt");
		} else if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
			checkPlayServices();

			if (zProgressDialog != null)
				zProgressDialog.dismiss();

		}
	}

	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				CommonLib.ZLog("google-play-resultcode", resultCode);
				if (resultCode == 2 && !isFinishing()) {

					if (windowHasFocus)
						showDialog(PLAY_SERVICES_RESOLUTION_REQUEST);
				} else {
					navigateToHome();
				}

			} else {
				navigateToHome();
			}

		} else {

			gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			regId = getRegistrationId(mContext);

			if (hardwareRegistered == 0) {
				// Call
				if (prefs.getInt("merchant_id", 0) != 0 && !regId.equals("")) {
					sendRegistrationIdToBackend();
					Editor editor = prefs.edit();
					editor.putInt("HARDWARE_REGISTERED", 1);
					editor.commit();
				}
			}

			if (regId.isEmpty()) {
				CommonLib.ZLog("GCM", "RegID is empty");
				registerInBackground();
			} else {
				CommonLib.ZLog("GCM", "already registered : " + regId);
			}
			navigateToHome();
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p/>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {

		final SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		String registrationId = prefs.getString(CommonLib.PROPERTY_REG_ID, "");

		if (registrationId.isEmpty()) {
			CommonLib.ZLog("GCM", "Registration not found.");
			return "";
		}
		return registrationId;
	}

	private void sendRegistrationIdToBackend() {
		UploadManager.updateRegistrationId(regId);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p/>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {

				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(mContext);
					}

					regId = gcm.register(CommonLib.GCM_SENDER_ID);
					msg = "Device registered, registration ID=" + regId;
					storeRegistrationId(mContext, regId);

					if (prefs.getInt("merchant_id", 0) != 0 && !regId.equals(""))
						sendRegistrationIdToBackend();

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				CommonLib.ZLog("GCM msg", msg);
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void storeRegistrationId(Context context, String regId) {

		prefs = getSharedPreferences("application_settings", 0);
		int appVersion = getAppVersion(context);
		Editor editor = prefs.edit();
		editor.putString(CommonLib.PROPERTY_REG_ID, regId);
		editor.putInt(CommonLib.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);
			return packageInfo.versionCode;

		} catch (Exception e) {
			CommonLib.ZLog("GCM", "EXCEPTION OCCURED" + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void onCoordinatesIdentified(Location loc) {
		if (loc != null && prefs != null) {

			float lat = (float) loc.getLatitude();
			float lon = (float) loc.getLongitude();
			Editor editor = prefs.edit();
			editor.putFloat("lat1", lat);
			editor.putFloat("lon1", lon);
			editor.commit();

			UploadManager.updateLocation(lat, lon);
		}
	}

	@Override
	public void onLocationIdentified() {

	}

	@Override
	public void onLocationNotIdentified() {

	}

	@Override
	public void onDifferentCityIdentified() {

	}

	@Override
	public void locationNotEnabled() {

	}

	@Override
	public void onLocationTimedOut() {

	}

	@Override
	public void onNetworkError() {

	}

}
