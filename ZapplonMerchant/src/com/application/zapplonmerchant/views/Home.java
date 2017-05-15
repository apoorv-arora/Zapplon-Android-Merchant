package com.application.zapplonmerchant.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.AppConfig;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.data.UserWish;
import com.application.zapplonmerchant.services.ZNewBookingService;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.NoSwipeViewPager;
import com.application.zapplonmerchant.utils.PagerSlidingTabStrip;
import com.application.zapplonmerchant.utils.RequestWrapper;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.ZTabClickCallback;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends ActionBarActivity implements ZTabClickCallback, OrdersAdapter.OnTabActionPerformedListener {

	private ZApplication zapp;
	private SharedPreferences prefs;
	private int width;

	private NoSwipeViewPager homePager;
	private SparseArray<SoftReference<Fragment>> fragments = new SparseArray<SoftReference<Fragment>>();

	public static DrawerLayout mDrawerLayout;
	LayoutInflater inflater;

	private boolean destroyed = false;

	int currentPageSelected = 0;
	public boolean fromExternalSource = false;

	// Viewpager Fragment Index
	private static final int VIEWPAGER_INDEX_HOME_FRAGMENT = 0;
	private static final int VIEWPAGER_INDEX_BOOKING_FRAGMENT = 2;
	private static final int VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT = 1;
	public static final int VIEWPAGER_INDEX_ME_FRAGMENT = 2;

	public ActionBarDrawerToggle mActionBarDrawerToggle;

	private int mScrollState;
	ArrayList<GetImage> getImageArray = new ArrayList<GetImage>();
	private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

	// FAB Stuff
	private View mFABOverlay;
	private boolean mFABExpanded = false;
	private boolean mFABVisible = false;

	private final int EMAIL_FEEDBACK = 1500;
	public static final int REQUEST_CODE_MAP = 101;
	public static final int RESULT_CODE_OK = 102;

	private final int CALL_DIALOG = 1;
	private final int MAKE_CALL_INTENT = 20;

	private Store currentlySelectedRestaurant;

	private NewBookingFragment newOrderFragment;

	protected ArrayList<Store> storesList = new ArrayList<Store>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = LayoutInflater.from(this);
		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		setContentView(R.layout.activity_home);
		getWindow().setBackgroundDrawable(null);

		// UI Related stuff
		try {
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
		} catch (Exception e) {
		}

		// Home tabs
		homePager = (NoSwipeViewPager) findViewById(R.id.home_pager);
		homePager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
		homePager.setOffscreenPageLimit(4);
		homePager.setCurrentItem(VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT);
		homePager.setSwipeable(true);

		setupActionBar();
		setUpDrawer();

		setUpFAB();

		// hide the thin line below tabs on androidL
		if (CommonLib.isAndroidL())
			findViewById(R.id.tab_thin_line).setVisibility(View.GONE);

		setUpTabs();

		if (prefs.getString("support_contact", "").length() > 3) {
			mDrawerLayout.findViewById(R.id.support).setVisibility(View.VISIBLE);
		} else {
			new AppConfigAsync().execute(null, null, null);
		}

		mDrawerLayout.findViewById(R.id.support).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String contact = prefs.getString("support_contact", "");
				if (contact.length() > 3) {
					final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact));
					AlertDialog.Builder builder_loc = new AlertDialog.Builder(Home.this, AlertDialog.THEME_HOLO_DARK);

					Dialog dialog = null;
					builder_loc.setMessage(contact).setCancelable(true)
							.setPositiveButton(Home.this.getResources().getString(R.string.dialog_call),
									new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

							dialog.cancel();
							try {
								startActivityForResult(intent, MAKE_CALL_INTENT);
							} catch (ActivityNotFoundException e) {
							}
						}
					}).setNegativeButton(getResources().getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					dialog = builder_loc.create();
					dialog.show();

				}
			}
		});

		findViewById(R.id.drawer_user_stat_cont).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (storesList == null || storesList.isEmpty()) {
					zProgressDialog = ProgressDialog.show(Home.this, "Please wait...", "Fetching list");
					new GetStoresList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
				} else {
					Bundle resBundle = new Bundle();
					resBundle.putSerializable("list_items", storesList);
					showRestaurantSelectorDialog(resBundle);
				}
			}
		});

		((TextView) findViewById(R.id.drawer_user_name)).setText(prefs.getString("store_name", ""));

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mNotificationReceived,
				new IntentFilter(CommonLib.LOCAL_BROADCAST_NOTIFICATIONS));

		if (getIntent() != null && getIntent().hasExtra("booking_id")) {
			if (!CommonLib.isServiceRunning(ZNewBookingService.class, this)) {
				Intent service = new Intent(this, ZNewBookingService.class);
				startService(service);
			}
			findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
			findViewById(R.id.fab_post_request).setVisibility(View.GONE);
		} else {
			if (!CommonLib.isServiceRunning(ZNewBookingService.class, this)) {
				Intent service = new Intent(this, ZNewBookingService.class);
				startService(service);
			}
		}
	}

	private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			try {

				// just refresh the list
				if (!CommonLib.isServiceRunning(ZNewBookingService.class, context)) {
					Intent service = new Intent(context, ZNewBookingService.class);
					startService(service);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected synchronized void onNewIntent(Intent intent) {
		CommonLib.ZLog("xxHome", "onNewIntent");
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
			// startActivity(new Intent(this, Splash.class));
			// finish();
		}

		if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("fragment")) {

			Bundle bundle = intent.getExtras();
			ArrayList<UserWish> newOrderList = bundle.containsKey("orderList")
					? (ArrayList<UserWish>) bundle.get("orderList") : null;

			if (newOrderFragment != null && newOrderFragment.isAdded()) {

				CommonLib.ZLog("xxHome", "onNewIntent updateTabs");

				if (newOrderList != null)
					newOrderFragment.updateList(newOrderList);

			} else if (newOrderList.size() >= 1) {

				FragmentManager fm = getSupportFragmentManager();
				String fragmentKey = intent.getStringExtra("fragment");

				if (fragmentKey.equalsIgnoreCase("neworderfragment")) {
					CommonLib.disableKeyguard(Home.this);
					CommonLib.ZLog("xxHome", "onNewIntent create fragment");
					newOrderFragment = NewBookingFragment.newInstance(newOrderList);
					final FragmentTransaction ft = fm.beginTransaction();
					ft.add(R.id.fragment_container, newOrderFragment, "NewOrderFragment");
					ft.addToBackStack(null);
					ft.commitAllowingStateLoss();
					fm.executePendingTransactions();
					findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
					findViewById(R.id.fab_post_request).setVisibility(View.GONE);
				}
			}
		}
	}

	public void removeFragment(Fragment fragment, int inAnimation, int outAnimation) {
		try {
			FragmentManager fm = getSupportFragmentManager();
			/**
			 * Starts a new transaction
			 */
			final FragmentTransaction transaction = fm.beginTransaction();
			transaction.setCustomAnimations(inAnimation, outAnimation);
			transaction.remove(fragment);
			transaction.commit();
			findViewById(R.id.fab_post_request).setVisibility(View.VISIBLE);
			findViewById(R.id.fragment_container).setVisibility(View.GONE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		destroyed = false;
		if (zProgressDialog != null && zProgressDialog.isShowing())
			zProgressDialog.dismiss();
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mNotificationReceived);
		super.onDestroy();
	}

	private void setupActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		View v = inflater.inflate(R.layout.home_action_bar, null);

		v.findViewById(R.id.action_buttons).setVisibility(View.GONE);
		v.findViewById(R.id.home_icon_zomato).setVisibility(View.GONE);

		v.findViewById(R.id.home_icon_container).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openOrCloseDrawer();
			}
		});

		((TextView) v.findViewById(R.id.title)).setText("Hello " + prefs.getString("merchant_name", ""));

		try {
			v.findViewById(R.id.home_icon_zomato).setPadding(width / 80, width / 80, width / 80, width / 80);
		} catch (Exception e) {

		}

		// user handle
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setPadding(width / 80, 0, width / 40, 0);
		actionBar.setCustomView(v);

	}

	private void setUpTabs() {
		// tabs
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setmZomatoHome(true);
		tabs.setAllCaps(false);
		tabs.setForegroundGravity(Gravity.LEFT);
		tabs.setShouldExpand(true);
		tabs.setViewPager(homePager);
		tabs.setDividerColor(getResources().getColor(R.color.transparent1));
		tabs.setBackgroundColor(getResources().getColor(R.color.zomato_red));
		tabs.setUnderlineColor(getResources().getColor(R.color.zhl_dark));
		tabs.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold), 0);
		tabs.setIndicatorColor(getResources().getColor(R.color.white));
		tabs.setIndicatorHeight((int) getResources().getDimension(R.dimen.height3));
		tabs.setTextSize((int) getResources().getDimension(R.dimen.size15));
		tabs.setUnderlineHeight(0);
		tabs.setTabPaddingLeftRight(12);
		tabs.setInterfaceForClick(this);

		final int tabsUnselectedColor = R.color.zhl_darker;
		final int tabsSelectedColor = R.color.white;

		final TextView homeSearchHeader = (TextView) ((LinearLayout) tabs.getChildAt(0))
				.getChildAt(VIEWPAGER_INDEX_HOME_FRAGMENT);
		final TextView homeDynamicPricingFragment = (TextView) ((LinearLayout) tabs.getChildAt(0))
				.getChildAt(VIEWPAGER_INDEX_BOOKING_FRAGMENT);
		final TextView semiDynamicPricingFragment = (TextView) ((LinearLayout) tabs.getChildAt(0))
				.getChildAt(VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT);

		homeSearchHeader.setTextColor(getResources().getColor(tabsUnselectedColor));
		homeDynamicPricingFragment.setTextColor(getResources().getColor(tabsUnselectedColor));
		semiDynamicPricingFragment.setTextColor(getResources().getColor(tabsSelectedColor));

		setPageChangeListenerOnTabs(tabs, tabsUnselectedColor, tabsSelectedColor, homeSearchHeader,
				homeDynamicPricingFragment, semiDynamicPricingFragment);
	}

	@Override
	public void onTabClick(int position) {
		if (currentPageSelected == position) {

			try {
				switch (position) {

				case VIEWPAGER_INDEX_HOME_FRAGMENT:

					// Home Scroll Top
					if (fragments.get(VIEWPAGER_INDEX_HOME_FRAGMENT) != null) {
						HomeFragment hf = (HomeFragment) fragments.get(VIEWPAGER_INDEX_HOME_FRAGMENT).get();
						if (hf != null) {
							hf.scrollHomeToTop();
						}
					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								HomeFragment fragMent = (HomeFragment) hAdapter.instantiateItem(homePager,
										VIEWPAGER_INDEX_HOME_FRAGMENT);
								if (fragMent != null)
									fragMent.scrollHomeToTop();
							} catch (Exception e) {
							}
						}
					}
					break;

				case VIEWPAGER_INDEX_BOOKING_FRAGMENT:

					// Search Scroll Top
					if (fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT) != null) {
						BookingFragment srf = (BookingFragment) fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT).get();
						if (srf != null) {
							srf.scrollHomeToTop();
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								BookingFragment fragMent = (BookingFragment) hAdapter.instantiateItem(homePager,
										VIEWPAGER_INDEX_BOOKING_FRAGMENT);
								if (fragMent != null)
									fragMent.scrollHomeToTop();
							} catch (Exception e) {
							}
						}
					}

					break;

				}
			} catch (Exception e) {

			}

		}

	}

	private void setPageChangeListenerOnTabs(PagerSlidingTabStrip tabs, final int tabsUnselectedColor,
			final int tabsSelectedColor, final TextView homeSearchHeader, final TextView homeDynamicPricingFragment,
			final TextView semiDynamicPricingFragment) {
		tabs.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {

				currentPageSelected = arg0;

				if (arg0 == VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT) {

					if (fragments.get(VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT) != null) {

						if (fragments.get(VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT)
								.get() instanceof SemiDynamicPricingFragment) {
							SemiDynamicPricingFragment srf = (SemiDynamicPricingFragment) fragments
									.get(VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT).get();
							if (srf != null) {

								// if (!srf.searchCallsInitiatedFromHome)
								// srf.initiateSearchCallFromHome();

							}
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								SemiDynamicPricingFragment fragMent = (SemiDynamicPricingFragment) hAdapter
										.instantiateItem(homePager, VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT);
								if (fragMent != null) {

									// if
									// (!fragMent.searchCallsInitiatedFromHome)
									// fragMent.initiateSearchCallFromHome();

								}
							} catch (Exception e) {
								// Crashlytics.logException(e);
							}
						}
					}

					homeSearchHeader.setTextColor(getResources().getColor(tabsUnselectedColor));
					homeDynamicPricingFragment.setTextColor(getResources().getColor(tabsUnselectedColor));
					semiDynamicPricingFragment.setTextColor(getResources().getColor(tabsSelectedColor));

				} else if (arg0 == VIEWPAGER_INDEX_HOME_FRAGMENT) {

					homeSearchHeader.setTextColor(getResources().getColor(tabsSelectedColor));
					homeDynamicPricingFragment.setTextColor(getResources().getColor(tabsUnselectedColor));
					semiDynamicPricingFragment.setTextColor(getResources().getColor(tabsUnselectedColor));
					try {
						((DrawerLayout) findViewById(R.id.drawer_layout))
								.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
					} catch (Exception e) {
					}

				} else if (arg0 == VIEWPAGER_INDEX_BOOKING_FRAGMENT) {

					if (fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT) != null) {

						if (fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT).get() instanceof BookingFragment) {
							BookingFragment srf = (BookingFragment) fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT)
									.get();
							if (srf != null) {

								// if (!srf.searchCallsInitiatedFromHome)
								// srf.initiateSearchCallFromHome();

							}
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								BookingFragment fragMent = (BookingFragment) hAdapter.instantiateItem(homePager,
										VIEWPAGER_INDEX_BOOKING_FRAGMENT);
								if (fragMent != null) {

								}
							} catch (Exception e) {
							}
						}
					}

					homeSearchHeader.setTextColor(getResources().getColor(tabsUnselectedColor));
					homeDynamicPricingFragment.setTextColor(getResources().getColor(tabsSelectedColor));
					semiDynamicPricingFragment.setTextColor(getResources().getColor(tabsUnselectedColor));
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

				if (position == 0) {

					int alphaValueUnderline = (int) ((((positionOffset - 0) * (255 - 0)) / (1 - 0)) + 0);
					((PagerSlidingTabStrip) findViewById(R.id.tabs))
							.setUnderlineColor(Color.argb(alphaValueUnderline, 228, 228, 228));

				} else if (position > 0) {
					((PagerSlidingTabStrip) findViewById(R.id.tabs)).setUnderlineColor(Color.argb(255, 228, 228, 228));
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mFABExpanded) {
			toggleFAB();
			return;
		}

		if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
			mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
			return;
		}

		super.onBackPressed();
	}

	private class HomePagerAdapter extends FragmentStatePagerAdapter {

		public HomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			fragments.put(position, null);
			super.destroyItem(container, position, object);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {

			case VIEWPAGER_INDEX_HOME_FRAGMENT:
				CommonLib.ZLog("HomePage", "Creating new home page fragment");
				HomeFragment home = new HomeFragment();
				fragments.put(VIEWPAGER_INDEX_HOME_FRAGMENT, new SoftReference<Fragment>(home));
				return home;

			case VIEWPAGER_INDEX_BOOKING_FRAGMENT:
				BookingFragment dpFragment = new BookingFragment();
				fragments.put(VIEWPAGER_INDEX_BOOKING_FRAGMENT, new SoftReference<Fragment>(dpFragment));
				return dpFragment;

			case VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT:
				SemiDynamicPricingFragment sdpFragment = new SemiDynamicPricingFragment();
				fragments.put(VIEWPAGER_INDEX_SEMI_DYNAMIC_PRICING_FRAGMENT, new SoftReference<Fragment>(sdpFragment));
				return sdpFragment;

			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		private String[] ids = { getResources().getString(R.string.active_deals),
				getResources().getString(R.string.dynamic), getResources().getString(R.string.booking) };

		public String getPageTitle(int pos) {
			return ids[pos];
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (prefs.getInt("merchant_id", 0) != 0) {
			if (prefs.getInt("store_id", 0) == 0) {
				// Store add activity
				Intent intent = new Intent(this, StoreDetailsScreen.class);
				startActivity(intent);
				finish();
			}
		} else {
			Intent intent = new Intent(this, SplashScreen.class);
			startActivity(intent);
			finish();
		}
	}

	public void openOrCloseDrawer() {

		// if (!mFABExpanded) {
		if (mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
			InputMethodManager mgr = (InputMethodManager) getApplicationContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));

		} else {
			mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
		}
		// }
	}

	private void setUpDrawer() {

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		final View drawerIcon = findViewById(R.id.ic_drawer);
		drawerIcon.measure(0, 0);
		drawerIcon.setPivotX(0);
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				drawerIcon.setScaleX((1 - arg1 / 2));
				scaleFAB(arg1);
			}

			@Override
			public void onDrawerOpened(View arg0) {
			}

			@Override
			public void onDrawerClosed(View arg0) {
			}
		});

		setUpUserSettingsInDrawer();
	}

	private void setUpUserSettingsInDrawer() {

		// user snippet in drawer
		// findViewById(R.id.`).getLayoutParams().height = width / 3;
		findViewById(R.id.drawer_user_stat_cont).setPadding(3 * width / 80, 0, 0, 0);
		findViewById(R.id.drawer_user_gradient_bottom).getLayoutParams().height = (12 * width / 90);
		// findViewById(R.id.drawer_user_info_background_image).getLayoutParams().height
		// = width / 3;
		// findViewById(R.id.seperator).getLayoutParams().height = width / 30;

		// user click
		findViewById(R.id.drawer_user_container).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Intent intent = new Intent(Home.this,
				// UserPageActivity.class);
				// startActivity(intent);
			}
		});

		setImageInDrawer();
	}

	// called from
	// 1. Home when drawer is being set.
	// 2. Me fragment after EDIT PROFILE.
	public void setImageInDrawer() {
		// Blurred user image
		ImageView imageBackground = (ImageView) findViewById(R.id.drawer_user_info_background_image);
		setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageBackground, "", width, width, false, false);

		ImageView imageBlur = (ImageView) findViewById(R.id.drawer_user_info_blur_background_image);
		setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageBlur, "", width, width, false, true);
	}

	public void aboutus(View view) {
		Intent intent = new Intent(this, AboutUs.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

	}

	public void settings(View view) {
		Intent intent = new Intent(this, SettingsPage.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void logoutConfirm(View V) {
		final AlertDialog logoutDialog;
		logoutDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.logout))
				.setMessage(getResources().getString(R.string.logout_confirm))
				.setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						String accessToken = prefs.getString("access_token", "");
						UploadManager.logout(accessToken);

						Editor editor = prefs.edit();
						editor.putInt("uid", 0);
						editor.putInt("merchant_id", 0);
						editor.putInt("availability", 0);
						editor.putInt("store_id", 0);
						editor.putInt("selected_store_id", 0);
						editor.putInt("store_type", 0);
						editor.putFloat("store_lon", 0);
						editor.putFloat("store_lat", 0);
						editor.putString("store_name", "");
						editor.putString("store_address", "");
						editor.putString("store_contact", "");
						editor.putInt("availability", 0);
						editor.putInt("max_occupancy", 0);
						editor.putInt("current_occupancy", 0);
						editor.putString("thumbUrl", "");
						editor.putString("access_token", "");
						editor.remove("username");
						editor.remove("support_contact");
						editor.remove("profile_pic");
						editor.remove("HSLogin");
						editor.remove("INSTITUTION_NAME");
						editor.remove("STUDENT_ID");
						editor.putBoolean("facebook_post_permission", false);
						editor.putBoolean("post_to_facebook_flag", false);
						editor.putBoolean("facebook_connect_flag", false);
						editor.putBoolean("twitter_status", false);

						editor.commit();

						if (prefs.getInt("uid", 0) == 0) {
							Intent intent = new Intent(zapp, SplashScreen.class);
							startActivity(intent);
							finish();
						}
					}
				}).setNegativeButton(getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
				.create();
		logoutDialog.show();
	}

	public void feedback(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("application/octet-stream");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "apoorv@zapplon.com" });
		intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_email_subject));
		mDrawerLayout.closeDrawers();
		try {
			prefs = getSharedPreferences("application_settings", 0);
			final String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
					+ CommonLib.getNetworkState(Home.this) + "_" + CommonLib.getNetworkType(Home.this) + "\n"
					+ "Identifier   : " + prefs.getString("app_id", "") + "\n" + "User Id     	: "
					+ prefs.getInt("uid", 0) + "\n" + "&device=" + android.os.Build.DEVICE);

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
			startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.send_mail)),
					EMAIL_FEEDBACK);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(Home.this, getResources().getString(R.string.no_email_clients), Toast.LENGTH_LONG).show();
		}
	}

	public void passwordChange(View view) {
		Intent intent = new Intent(this, ChangePasswordPage.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == EMAIL_FEEDBACK) {
			deleteFile("log.txt");
			// keyboard down if up
			CommonLib.hideKeyBoard(this, findViewById(R.id.homenew_root));
		}
	}

	public void rate(View v) {

		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
			startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {

		} catch (Exception e) {

		}
	}

	private ProgressDialog zProgressDialog;
	View promptsView;

	@SuppressLint("NewApi")
	public void verify(View v) {
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(this);
		promptsView = li.inflate(R.layout.deal_verification_input_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText key = (EditText) promptsView.findViewById(R.id.key);

		key.requestFocus();

		// set dialog message
		alertDialogBuilder.setNegativeButton(getResources().getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setPositiveButton("Verify", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String keyText = key.getText().toString();

						if (keyText == null || keyText.length() < 1) {
							Toast.makeText(Home.this, "Invalid key", Toast.LENGTH_SHORT).show();
							return;
						}

						zProgressDialog = ProgressDialog.show(Home.this, null, "Verifying! Please wait...");
						// get the data and show a dialog
						new GetVerificationPin(keyText).execute(null, null, null);
					}
				});

		if (Build.VERSION.SDK_INT > 17) {
			alertDialogBuilder.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (!destroyed)
						CommonLib.hideKeyBoard(Home.this, promptsView.findViewById(R.id.key));

				}
			});
		}

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.setCancelable(false);
		// show it
		alertDialog.show();
	}

	private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
			int height, boolean useDiskCache, boolean fastBlur) {

		if (cancelPotentialWork(url, imageView)) {

			GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type, fastBlur);

			final AsyncDrawable asyncDrawable = new AsyncDrawable(Home.this.getResources(), zapp.cache.get(url), task);
			imageView.setImageDrawable(asyncDrawable);
			if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
				((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
			}
			if (zapp.cache.get(url) == null) {
				try {
					task.executeOnExecutor(CommonLib.THREAD_POOL_EXECUTOR_IMAGE);
				} catch (RejectedExecutionException e) {
					CommonLib.sPoolWorkQueueImage.clear();
				}
				getImageArray.add(task);
			} else {
				imageView.setBackgroundResource(0);
				Bitmap blurBitmap = null;
				if (imageView != null) {
					blurBitmap = CommonLib.fastBlur(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 4);
				}
				if (imageView != null && blurBitmap != null) {
					imageView.setImageBitmap(blurBitmap);
				}
				if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
					((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
				}
			}
		} else if (imageView != null && imageView.getDrawable() != null
				&& ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
			imageView.setBackgroundResource(0);
			if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
				((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
			}
		}
	}

	private class GetImage extends AsyncTask<Object, Void, Bitmap> {

		String url = "";
		private WeakReference<ImageView> imageViewReference;
		private int width;
		private int height;
		boolean useDiskCache;
		String type;
		String url2 = "";
		boolean fastBlur = false;

		public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type,
				boolean fastBlur) {
			this.url = url;
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.width = width;
			this.height = height;
			this.useDiskCache = true;// useDiskCache;
			this.type = type;
			this.fastBlur = fastBlur;
		}

		@Override
		protected void onPreExecute() {
			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar)
					((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
			}
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
			try {
				if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
					if (!destroyed) {
						if (useDiskCache) {
							bitmap = CommonLib.getBitmapFromDisk(url, getApplicationContext());
						}

						if (bitmap == null) {
							try {
								BitmapFactory.Options opts = new BitmapFactory.Options();
								opts.inJustDecodeBounds = true;
								opts.inPreferredConfig = Config.RGB_565;
								BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

								opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
								opts.inJustDecodeBounds = false;
								opts.inPreferredConfig = Config.RGB_565;

								bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null,
										opts);

								if (fastBlur)
									bitmap = CommonLib.fastBlur(bitmap, 4);
								if (useDiskCache) {
									// if
									// (CommonLib.shouldScaleDownBitmap(Home.this,
									// bitmap)) {
									// bitmap =
									// Bitmap.createScaledBitmap(bitmap, width,
									// height, false);
									// }
									CommonLib.writeBitmapToDisk(url, bitmap, Home.this.getApplicationContext(),
											Bitmap.CompressFormat.JPEG);
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							} catch (Error e) {
								zapp.cache.clear();
							}
						}

					} else {
						this.cancel(true);
					}
				}
			} catch (Exception e) {
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (isCancelled()) {
				bitmap = null;
			}

			if (bitmap != null) {
				if (this.type.equalsIgnoreCase("user"))
					bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);

				zapp.cache.put(url, bitmap);

			} else if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				GetImage task = getBitmapWorkerTask(imageView);
				if (task != null) {
					if (task.url2.equals("")) {
						task.url2 = new String(task.url);
					}
					task.url = "";
				}
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();

				if (imageView != null && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					GetImage currentTask = getBitmapWorkerTask(imageView);

					if ((!url.equals("")) && currentTask != null
							&& (currentTask.url.equals(url) || currentTask.url2.equals(url))) {
						GetImage task = new GetImage(url, imageView, width, height, true, type, fastBlur);
						final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), bitmap, task);
						imageView.setImageDrawable(asyncDrawable);
						imageView.setBackgroundResource(0);
						if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
								&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
								&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
							((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
						}
					} else {
						CommonLib.ZLog("getimagearray-imageview", "wrong bitmap");
					}
					getImageArray.remove(this);

				} else if (imageView != null) {
					GetImage task = getBitmapWorkerTask(imageView);
					if (task != null) {
						// if(task.url2.equals("")) {
						task.url2 = new String(task.url);
						// }
						task.url = "";
					}
				} else if (imageView == null) {
					CommonLib.ZLog("getimagearray-imageview", "null");
				}
			}

			/*
			 * if (imageViewReference != null && bitmap != null) { final
			 * ImageView imageView = imageViewReference.get(); if (imageView !=
			 * null) { imageView.setImageBitmap(bitmap);
			 * imageView.setBackgroundResource(0); getImageArray.remove(this); }
			 * }
			 */
		}
	}

	private GetImage getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	private class AsyncDrawable extends BitmapDrawable {
		// private final SoftReference<GetImage> bitmapWorkerTaskReference;
		private final GetImage bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
			super(res, bitmap);
			// bitmapWorkerTaskReference = new
			// SoftReference<GetImage>(bitmapWorkerTask);
			bitmapWorkerTaskReference = bitmapWorkerTask;
		}

		public GetImage getBitmapWorkerTask() {
			return bitmapWorkerTaskReference;
			// return bitmapWorkerTaskReference.get();
		}
	}

	public boolean cancelPotentialWork(String data, ImageView imageView) {
		final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {

			final String bitmapData = bitmapWorkerTask.url;
			if (!bitmapData.equals(data)) {
				if (bitmapWorkerTask.url2.equals("")) {
					bitmapWorkerTask.url2 = new String(bitmapWorkerTask.url);
				}
				// Cancel previous task
				bitmapWorkerTask.url = "";
				bitmapWorkerTask.cancel(true);
				// getImageArray.clear();
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	// fab
	private void setUpFAB() {

		// ((FABControl)
		// findViewById(R.id.multiple_actions)).setOnFloatingActionsMenuUpdateListener(this);
		// overlay behind FAB
		mFABOverlay = findViewById(R.id.fab_overlay);
		mFABOverlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// toggleFAB();
			}
		});
		mFABOverlay.setClickable(false);

		showFAB(true);
	}

	private void toggleFAB() {
		// ((FABControl) findViewById(R.id.multiple_actions)).toggle();
	}

	// makes FAB visible.
	public void showFAB(boolean delayed) {

		if (!mFABVisible) {
			mFABVisible = true;

			findViewById(R.id.fab_post_request).setVisibility(View.VISIBLE);

			if (delayed) {
				ViewPropertyAnimator animator = findViewById(R.id.fab_post_request).animate().scaleX(1).scaleY(1)
						.setDuration(250).setInterpolator(new AccelerateInterpolator()).setStartDelay(700);
				// required | dont remove
				animator.setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						try {
							// showCashlessInFab();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				});

			} else {

				ViewPropertyAnimator animator = findViewById(R.id.fab_post_request).animate().scaleX(1).scaleY(1)
						.setDuration(200).setInterpolator(new AccelerateInterpolator());
				// required | dont remove
				animator.setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {

					}

					@Override
					public void onAnimationRepeat(Animator animation) {

					}

					@Override
					public void onAnimationEnd(Animator animation) {
						try {
							// showCashlessInFab();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {

					}

				});
			}
		}
	}

	// Controls visibility/scale of FAB on drawer open/close
	private void scaleFAB(float input) {
		if (input < .7f) {

			if (findViewById(R.id.fab_post_request).getVisibility() != View.VISIBLE)
				findViewById(R.id.fab_post_request).setVisibility(View.VISIBLE);

			findViewById(R.id.fab_post_request).setScaleX(1 - input);
			findViewById(R.id.fab_post_request).setScaleY(1 - input);

		} else {

			if (findViewById(R.id.fab_post_request).getScaleX() != 0)
				findViewById(R.id.fab_post_request).setScaleX(0);

			if (findViewById(R.id.fab_post_request).getScaleY() != 0)
				findViewById(R.id.fab_post_request).setScaleY(0);

			if (findViewById(R.id.fab_post_request).getVisibility() != View.GONE)
				findViewById(R.id.fab_post_request).setVisibility(View.GONE);
		}

	}

	public void actionBarSelected(View view) {
		switch (view.getId()) {
		case R.id.fab_post_request:
			if (view.getAlpha() == 1) {
				Intent intent = new Intent(Home.this, CreatePromoScreen.class);
				startActivityForResult(intent, CommonLib.PROMO_CREATION);
				break;
			}
		}
	}

	private class GetVerificationPin extends AsyncTask<Object, Void, Object> {

		// execute the api
		private String key;

		public GetVerificationPin(String key) {
			this.key = key;
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "billing/verify?key=" + key;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.VERIFICATION_MESSAGE, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (destroyed)
				return;

			if (zProgressDialog != null && zProgressDialog.isShowing())
				zProgressDialog.dismiss();

			if (result != null) {
				if (result instanceof Object[]) {
					Object[] arr = (Object[]) result;
					CommonLib.hideKeyBoard(Home.this, promptsView.findViewById(R.id.key));
					if (arr[0] instanceof UserWish) {
						final UserWish userWish = (UserWish) arr[0];

						String message = "Pin verified\n "
								+ CommonLib.getStoreItemDescription(userWish.getStore().getStoreItem());

						final AlertDialog logoutDialog;
						logoutDialog = new AlertDialog.Builder(Home.this).setCancelable(false).setMessage(message)
								.setPositiveButton(getResources().getString(android.R.string.ok),
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										})
								.create();
						logoutDialog.show();
					} else {
						final AlertDialog logoutDialog;
						logoutDialog = new AlertDialog.Builder(Home.this)
								.setMessage("Invalid key" + String.valueOf(arr[1]))
								.setPositiveButton(getResources().getString(android.R.string.ok),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
											}
										})
								.create();
						logoutDialog.show();
					}
				}
			} else {
				if (CommonLib.isNetworkAvailable(Home.this)) {
					Toast.makeText(Home.this, getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(Home.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

				}
			}

		}
	}

	private class AppConfigAsync extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			mDrawerLayout.findViewById(R.id.support).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "appConfig/info";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.APP_CONFIG, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (result != null) {
				if (result instanceof AppConfig) {
					String contact = ((AppConfig) result).getContact();
					double version = ((AppConfig) result).getVersion();
					prefs.edit().putString("support_contact", contact).commit();
					System.out.println(contact + version);
					if (!destroyed)
						mDrawerLayout.findViewById(R.id.support).setVisibility(View.VISIBLE);
				}
			}
		}

	}

	public void showRestaurantSelectorDialog(Bundle args) {
		DialogFragment newFragment = MyAlertDialogFragment.newInstance(args);
		newFragment.show(getSupportFragmentManager(), "listPickerDialog");
	}

	public static class MyAlertDialogFragment extends DialogFragment {

		public static MyAlertDialogFragment newInstance(Bundle args) {
			MyAlertDialogFragment newFragment = new MyAlertDialogFragment();
			newFragment.setArguments(args);
			return newFragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog mDialog = null;
			final ArrayList<Store> nameList = (ArrayList<Store>) getArguments().get("list_items");
			AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
					.setTitle(getActivity().getResources().getString(R.string.select_stores))
					.setAdapter(((Home) getActivity()).new ListAdapter(getActivity(),
							R.layout.restaurant_picker_snippet, nameList), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									((Home) getActivity()).returnSelecteListItem(nameList.get(which), which);
									dialog.dismiss();
								}
							});
			mDialog = mDialogBuilder.create();
			return mDialog;
		}
	}

	private class ListAdapter extends ArrayAdapter<Store> {

		ArrayList<Store> items;

		public ListAdapter(Context context, int resourceId, ArrayList<Store> objects) {
			super(context, resourceId, objects);
			this.items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView.findViewById(R.id.restaurant_name) == null) {
				convertView = inflater.inflate(R.layout.restaurant_picker_snippet, parent, false);
				convertView.findViewById(R.id.restaurant_picker_icon).setVisibility(View.GONE);
				convertView.findViewById(R.id.restaurant_locality).setVisibility(View.GONE);
			}
			convertView.findViewById(R.id.res_snippet_container).setBackgroundResource(0);
			TextView itemName = ((TextView) convertView.findViewById(R.id.restaurant_name));
			itemName.setText(items.get(position).getStoreName());
			itemName.setTextColor(getResources().getColor(R.color.holo_white));
			convertView.findViewById(R.id.restaurant_name).setPadding(width / 20, width / 40, width / 20, width / 40);
			return convertView;
		}
	}

	private void returnSelecteListItem(Store restaurant, int which) {
		if (restaurant != null) {
			currentlySelectedRestaurant = restaurant;
			refreshViews(currentlySelectedRestaurant);
			// set values by passing a restaurant object and refres view
		}
	}

	protected void refreshViews(Store store) {
		if (store == null)
			return;

		currentlySelectedRestaurant = store;

		((TextView) findViewById(R.id.drawer_user_name)).setText(currentlySelectedRestaurant.getStoreName());

		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("selected_store_id", currentlySelectedRestaurant.getStoreId());
		editor.putInt("store_id", store.getStoreId());
		editor.putInt("store_type", store.getStoreType());
		editor.putFloat("store_lon", (float) store.getLongitude());
		editor.putFloat("store_lat", (float) store.getLatitude());
		editor.putString("store_name", store.getStoreName());
		editor.putString("store_address", store.getAddress());
		editor.putString("store_contact", store.getContactNumber());
		editor.putInt("availability", store.getAvailability());
		editor.putBoolean("accepts_reservation", store.isAcceptsReservation());
		editor.commit();

		// call the fragments
		if (fragments != null) {
			if (fragments.get(VIEWPAGER_INDEX_HOME_FRAGMENT) != null) {
				HomeFragment hf = (HomeFragment) fragments.get(VIEWPAGER_INDEX_HOME_FRAGMENT).get();
				if (hf != null) {
					hf.storeId = store.getStoreId();
					hf.refreshView();
				}
			}
			if (fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT) != null) {
				BookingFragment sf = (BookingFragment) fragments.get(VIEWPAGER_INDEX_BOOKING_FRAGMENT).get();
				if (sf != null) {
					sf.init();
				}
			}
		}
	}

	private class GetStoresList extends AsyncTask<Object, Void, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "store/all";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.STORES_LIST, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (destroyed)
				return;

			if (zProgressDialog != null && zProgressDialog.isShowing())
				zProgressDialog.dismiss();
			if (result != null) {
				if (result instanceof ArrayList<?>) {
					storesList = (ArrayList<Store>) result;

					// refresh activity views
				} else {
					String accessToken = prefs.getString("access_token", "");
					UploadManager.logout(accessToken);

					Editor editor = prefs.edit();
					editor.putInt("uid", 0);
					editor.putInt("merchant_id", 0);
					editor.putInt("availability", 0);
					editor.putInt("store_id", 0);
					editor.putString("thumbUrl", "");
					editor.putString("access_token", "");
					editor.remove("username");
					editor.remove("support_contact");
					editor.remove("profile_pic");
					editor.remove("HSLogin");
					editor.remove("INSTITUTION_NAME");
					editor.remove("STUDENT_ID");
					editor.putBoolean("facebook_post_permission", false);
					editor.putBoolean("post_to_facebook_flag", false);
					editor.putBoolean("facebook_connect_flag", false);
					editor.putBoolean("twitter_status", false);
					editor.remove("accepts_reservation");
					editor.commit();

					if (prefs.getInt("uid", 0) == 0) {
						Intent intent = new Intent(zapp, SplashScreen.class);
						startActivity(intent);
						finish();
					}
				}
			} else {
				if (CommonLib.isNetworkAvailable(Home.this)) {
					Toast.makeText(Home.this, Home.this.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(Home.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

	@Override
	public void onTabActionPerformed(UserWish tab, int action) {
	}

}
