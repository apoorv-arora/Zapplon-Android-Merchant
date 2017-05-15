package com.application.zapplonmerchant.views;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SearchFragment extends Fragment implements UploadManagerCallback {

	private ZApplication zapp;
	private Activity activity;
	private View getView;
	private SharedPreferences prefs;
	private int width, height;
	private LayoutInflater vi;
	ProgressDialog zProgressDialog;
	private boolean destroyed = false;
	public static final int REQUEST_CODE_MAP = 101;
	public static final int RESULT_CODE_OK = 102;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
		getView = getView();
		destroyed = false;
		prefs = activity.getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) activity.getApplication();
		width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
		vi = LayoutInflater.from(activity.getApplicationContext());

		UploadManager.addCallback(this);
		setUpHomeViews();

	}

	void setUpHomeViews() {

		((TextView) getView.findViewById(R.id.name)).setText("Name: " + prefs.getString("store_name", ""));
		((TextView) getView.findViewById(R.id.type))
				.setText("Type: " + (CommonLib.getStoreType(getActivity(), prefs.getInt("store_type", 1))));
		((TextView) getView.findViewById(R.id.address)).setText("Address: " + prefs.getString("store_address", ""));
		((TextView) getView.findViewById(R.id.contact)).setText("Contact: " + prefs.getString("store_contact", ""));

		((Switch) getView.findViewById(R.id.toggle_switch)).setChecked(prefs.getInt("availability", 0) == 1);

		getView.findViewById(R.id.toggle_switch).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (view instanceof Switch) {
					zProgressDialog = ProgressDialog.show(activity, "", "Altering availability. Please wait...");
					zProgressDialog.setCancelable(false);
					UploadManager.updateAvailability(((Switch) view).isChecked() ? 1 : 0);
				}
			}
		});

		getView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, StoreDetailsScreen.class);
				intent.putExtra("edit", true);
				startActivity(intent);
			}
		});

		displayAddressMap((ImageView) getView.findViewById(R.id.search_map), prefs.getFloat("store_lat", 0),
				prefs.getFloat("store_lon", 0));
	}

	public void scrollSearchToTop() {
		try {
			if (getView.findViewById(R.id.home_fragment_scroll_root) != null) {
				((ScrollView) getView.findViewById(R.id.home_fragment_scroll_root)).smoothScrollTo(0, 0);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.UPDATE_AVAILABILITY) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing()) {
					zProgressDialog.dismiss();
				}
				if (status)
					Toast.makeText(activity, "Status updated", Toast.LENGTH_LONG).show();

				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("availability", ((Switch) getView.findViewById(R.id.toggle_switch)).isChecked() ? 1 : 0);
				editor.commit();
			}
		} else if (requestType == CommonLib.STORE_ADD) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing())
					zProgressDialog.dismiss();

				if (data != null && status) {

				}
			}
		} else if (requestType == CommonLib.LOCATION_UPDATE) {
			if (!destroyed && status) {
				// update the prefs.
				SharedPreferences.Editor editor = prefs.edit();

				Store store = null;
				try {
					store = (Store) data;
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (store != null) {
					editor.putFloat("store_lat", (float) store.getLatitude());
					editor.putFloat("store_lon", (float) store.getLongitude());
				}

				editor.commit();

			}
		}

	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing()) {
			zProgressDialog.dismiss();
		}
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private void displayAddressMap(ImageView addressMap, final double lat, final double lon) {

		addressMap.getLayoutParams().width = width;

		// ((FrameLayout) addressMap.getParent()).getLayoutParams().height = 3 *
		// width / 10;
		((FrameLayout) addressMap.getParent()).getLayoutParams().width = width;
		addressMap.getLayoutParams().width = width;
		// addressMap.getLayoutParams().width = mapWidth;

		// ((RelativeLayout.LayoutParams) ((FrameLayout)
		// addressMap.getParent()).getLayoutParams()).setMargins(0, width / 40,
		// 0, 0);

		String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=14&size="
				+ width + "x" + getResources().getDimensionPixelSize(R.dimen.height125)
				+ "&maptype=roadmap&scale=2&markers=icon:http%3A%2F%2Fwww.zomato.com%2Fimages%2Fresprite_location%2Fpin_res2x.png|scale:2|"
				+ lat + "," + lon;

		// CommonLib.ZLog("displayAddressMap", mapUrl);url, imageView, type,
		// mapWidth, height, useDiskCache, fastBlur);
		setImageFromUrlOrDisk(mapUrl, addressMap, "static_map", width,
				getResources().getDimensionPixelSize(R.dimen.height125), false);

		// click
		((FrameLayout) addressMap.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(getActivity(), MapViewLayout.class);
//				intent.putExtra("edit", true);
//				intent.putExtra("lat", lat);
//				intent.putExtra("lon", lon);
//				getActivity().startActivityForResult(intent, SearchFragment.REQUEST_CODE_MAP);
//				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
	}

	private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
			int height, boolean useDiskCache) {

		if (cancelPotentialWork(url, imageView)) {

			GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url + type), task);
			imageView.setImageDrawable(asyncDrawable);
			if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
					&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
					&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
				((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
			}
			if (zapp.cache.get(url + type) == null) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1L);
			} else if (imageView != null && imageView.getDrawable() != null
					&& ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
				imageView.setBackgroundResource(0);
				if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
					((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
				}
			}
		}
	}

	private class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<GetImage> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<GetImage>(bitmapWorkerTask);
		}

		public GetImage getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public boolean cancelPotentialWork(String data, ImageView imageView) {
		final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.url;
			if (!bitmapData.equals(data)) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
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

	private class GetImage extends AsyncTask<Object, Void, Bitmap> {

		String url = "";
		private WeakReference<ImageView> imageViewReference;
		private int width;
		private int height;
		boolean useDiskCache;
		String type;
		Bitmap blurBitmap;

		public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type) {
			this.url = url;
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.width = width;
			this.height = height;
			this.useDiskCache = true;// useDiskCache;
			this.type = type;
		}

		@Override
		protected void onPreExecute() {
			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar)
					((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
			}
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
			try {

				String url2 = url + type;

				if (destroyed) {
					return null;
				}

				if (useDiskCache) {
					bitmap = CommonLib.getBitmapFromDisk(url2, activity.getApplicationContext());
				}

				if (bitmap == null) {
					try {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

						opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
						opts.inJustDecodeBounds = false;

						bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

						if (useDiskCache) {
							CommonLib.writeBitmapToDisk(url2, bitmap, activity.getApplicationContext(),
									Bitmap.CompressFormat.JPEG);
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Error e) {

					}
				}

				if (bitmap != null) {

//					bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);
					synchronized (zapp.cache) {
						zapp.cache.put(url2, bitmap);
					}
				}

			} catch (Exception e) {
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (!destroyed) {
				if (isCancelled()) {
					bitmap = null;
				}
				if (imageViewReference != null && bitmap != null) {
					final ImageView imageView = imageViewReference.get();
					if (imageView != null) {
						imageView.setImageBitmap(bitmap);
						if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
								&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
								&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
							((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
						}
					}
				}
			}
		}
	}

	protected void onFragmentResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_CODE_OK && data != null) {
			double latitude = 0, longitude = 0;

			if (data.hasExtra("latitude"))
				latitude = data.getDoubleExtra("latitude", 0.0);
			if (data.hasExtra("longitude"))
				longitude = data.getDoubleExtra("longitude", 0.0);

			if (latitude != 0.0 && longitude != 0.0) {
				displayAddressMap((ImageView) getView.findViewById(R.id.search_map), latitude, longitude);
				// update location on the backend
				UploadManager.updateStoreLocation(latitude, longitude);
			}
		}
	}

}
