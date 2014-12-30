/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fordemobile.livepaintings;

import android.support.v4.app.FragmentActivity;

public class FragmentPagerSupport extends FragmentActivity {

//	private final static String TAG = "FragmentPagerSupport";
//	private final static int DIALOG_PURCHASE_CONFIRMATION = 0;
//	private static final int REQUEST_CODE = 0;
//
//	private Handler mHandler;
//	private Set<String> mOwnedItems = new HashSet<String>();
//	private UserPurchaseObserver mUserPurchaseObserver;
//	private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 0;
//	private boolean purchaseEnable;
//	private boolean hasBought;
//
//	/**
//	 * A {@link PurchaseObserver} is used to get callbacks when Android Market
//	 * sends messages to this application so that we can update the UI.
//	 */
//	private class UserPurchaseObserver extends PurchaseObserver {
//
//		public UserPurchaseObserver(Handler handler) {
//			super(FragmentPagerSupport.this, handler);
//		}
//
//		@Override
//		public void onBillingSupported(boolean supported) {
//			updatePurchaseview(supported);
//		}
//
//		@Override
//		public void onPurchaseStateChange(PurchaseState purchaseState,
//				String itemId, int quantity, long purchaseTime,
//				String developerPayload) {
//			if (purchaseState == PurchaseState.PURCHASED) {
//				FragmentPagerSupport.this.mOwnedItems.add(itemId);
//				notifyUser(R.string.purchase_status,
//						R.string.purchase_successful);
//			} else if (purchaseState == PurchaseState.REFUNDED) {
//				notifyUser(R.string.purchase_status, R.string.purchase_refunded);
//			}
//			doInitializeOwnedItems();
//		}
//
//		@Override
//		public void onRequestPurchaseResponse(RequestPurchase request,
//				Result result) {
//			if (result == Result.OK) {
//				notifyUser(R.string.purchase_status, R.string.purchase_pending);
//			} else if (result == Result.USER_CANCELED) {
//				notifyUser(R.string.purchase_status, R.string.purchase_canceled);
//			} else if (result == Result.ERROR) {
//				notifyUser(R.string.purchase_status, R.string.purchase_error);
//			} else if (result == Result.DEVELOPER_ERROR) {
//				notifyUser(R.string.purchase_status, R.string.purchase_problem);
//			} else if (result == Result.SERVICE_UNAVAILABLE) {
//				notifyUser(R.string.purchase_status, R.string.purchase_problem);
//			} else if (result == Result.ITEM_UNAVAILABLE) {
//				notifyUser(R.string.purchase_status, R.string.item_unavailable);
//			} else if (result == Result.BILLING_UNAVAILABLE) {
//				notifyUser(R.string.purchase_status,
//						R.string.billing_unavailable);
//			}
//		}
//
//		@Override
//		public void onRestoreTransactionsResponse(RestoreTransactions request,
//				Result result) {
//		}
//	}
//
//	public void updatePurchaseview(boolean purchaseEnable) {
//		this.purchaseEnable = purchaseEnable;
//	}
//
//	/**
//	 * Creates a background thread that reads the database and initializes the
//	 * set of owned items.
//	 */
//	private void initializeOwnedItems() {
//		new Thread(new Runnable() {
//			public void run() {
//				doInitializeOwnedItems();
//			}
//		}).start();
//	}
//
//	/**
//	 * Reads the set of purchased items from the database in a background thread
//	 * and then adds those items to the set of owned items in the main UI
//	 * thread.
//	 */
//	private void doInitializeOwnedItems() {
//		Cursor cursor = this.mPurchaseDatabase.queryAllPurchasedItems();
//		if (cursor == null) {
//			return;
//		}
//
//		final Set<String> ownedItems = new HashSet<String>();
//		try {
//			int productIdCol = cursor
//					.getColumnIndexOrThrow(PurchaseDatabase.PURCHASED_PRODUCT_ID_COL);
//			while (cursor.moveToNext()) {
//				String productId = cursor.getString(productIdCol);
//				ownedItems.add(productId);
//			}
//		} finally {
//			cursor.close();
//		}
//
//		// We will add the set of owned items in a new Runnable that runs on
//		// the UI thread so that we don't need to synchronize access to
//		// mOwnedItems.
//		this.mHandler.post(new Runnable() {
//			public void run() {
//				FragmentPagerSupport.this.mOwnedItems.addAll(ownedItems);
//				updateDisplay();
//			}
//		});
//	}
//
//	private void updateDisplay() {
//
//		if (mOwnedItems.contains(Consts.PACK_001)) {
//			this.hasBought = true;
//			this.pager.getAdapter().notifyDataSetChanged();
//		}
//	}
//
//	public void notifyUser(int titleId, int messageId) {
//		String ns = Context.NOTIFICATION_SERVICE;
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
//
//		int icon = R.drawable.icon;
//		long when = System.currentTimeMillis();
//
//		Notification notification = new Notification(icon, getString(titleId),
//				when);
//
//		Context context = getApplicationContext();
//		CharSequence contentTitle = getString(titleId);
//		CharSequence contentText = getString(messageId);
//		Intent notificationIntent = new Intent(this, FragmentPagerSupport.class);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				notificationIntent, 0);
//
//		notification.setLatestEventInfo(context, contentTitle, contentText,
//				contentIntent);
//		mNotificationManager.notify(0, notification);
//	}
//
//	/**
//	 * Called when this activity is no longer visible.
//	 */
//	@Override
//	protected void onStop() {
//		super.onStop();
//		ResponseHandler.unregister(this.mUserPurchaseObserver);
////		((ViewPagerAdapter)this.pager.getAdapter()).recycle();
//	}
//
//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//		case DIALOG_BILLING_NOT_SUPPORTED_ID:
//			return createDialog(R.string.billing_not_supported_title,
//					R.string.billing_not_supported_message);
//		default:
//			return null;
//		}
//	}
//
//	private Dialog createDialog(int titleId, int messageId) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(titleId).setIcon(android.R.drawable.stat_sys_warning)
//				.setMessage(messageId).setCancelable(false)
//				.setPositiveButton(android.R.string.ok, null);
//		return builder.create();
//	}
//
//	public class ViewPagerAdapter extends PagerAdapter implements
//			OnTouchListener, OnClickListener {
//		private Context context;
//		private List<Painting> paintings;
//		private Typeface tf;
//
//		public ViewPagerAdapter(Context context) {
//			tf = Typeface
//					.createFromAsset(getAssets(), "fonts/Poly-Regular.ttf");
//			this.context = context;
//			this.paintings = new ArrayList<Painting>();
//
//			try {
//				String lists[] = getResources().getAssets().list("paintings");
//				for (String paint : lists) {
//					Painting painting = new Painting(
//							"paintings/" + paint + "/", 0, 0);
//					this.paintings.add(painting);
//				}
//				Collections.sort(this.paintings, new Comparator<Painting>() {
//					public int compare(Painting lhs, Painting rhs) {
//						return lhs.getDate().compareTo(rhs.getDate());
//					};
//				});
//				Log.i(TAG, "List:" + this.paintings.size());
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void destroyItem(View pager, int position, Object view) {
//			((ViewPager) pager).removeView((View) view);
//			Painting painting = paintings.get(position);
//			painting.recycle();
//		}
//
//		public void recycle() {
//			int currentItem = pager.getCurrentItem();
//			Painting painting = paintings.get(currentItem);
//			painting.recycle();
//		}
//
//		@Override
//		public void finishUpdate(View view) {
//		}
//
//		@Override
//		public int getCount() {
//			return this.paintings.size();
//		}
//
//		@Override
//		public Object instantiateItem(View pager, int position) {
//			LayoutInflater layoutInflater = (LayoutInflater) this.context
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			Painting painting = paintings.get(position);
//			painting.loadPreview();
//			View v = null;
//			if (painting.isLandscape()) {
//				v = layoutInflater.inflate(R.layout.fragment_pager_list, null);
//			} else {
//				v = layoutInflater.inflate(
//						R.layout.fragment_pager_list_portrait, null);
//			}
//			TextView descriptionTextView = (TextView) v
//					.findViewById(R.id.descriptionTextView);
//			descriptionTextView.setTypeface(tf);
//			ImageView previewImageView = (ImageView) v
//					.findViewById(R.id.previewImageView);
//			if (painting.getArtistName().trim().isEmpty()) {
//				descriptionTextView
//						.setText(getString(R.string.request_description));
//			} else {
//				descriptionTextView.setText(Html.fromHtml(painting
//						.getDescription()));
//			}
//			previewImageView.setImageBitmap(painting.getPreviewImage());
//			((ViewPager) pager).addView(v, 0);
//
//			ImageView buyFullVersionButton = (ImageView) v
//					.findViewById(R.id.buyFullVersionButton);
//			buyFullVersionButton.setOnClickListener(this);
//
//			View forsaleImage = v.findViewById(R.id.forSaleView);
//			if (FragmentPagerSupport.this.hasBought
//					|| (!Consts.DEBUG && painting.isForSale())) {
//				if (purchaseEnable) {
//					// display sale views
//					buyFullVersionButton.setVisibility(View.VISIBLE);
//					forsaleImage.setVisibility(View.VISIBLE);
//					ColorMatrix cm = new ColorMatrix();
//					cm.setSaturation(0);
//					ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
//							cm);
//					previewImageView.setColorFilter(filter);
//				} else {
//					// TODO display purchase impossible
//					buyFullVersionButton.setVisibility(View.GONE);
//				}
//			} else {
//				// remove sale views
//				previewImageView.setOnTouchListener(this);
//				forsaleImage.setVisibility(View.GONE);
//				buyFullVersionButton.setVisibility(View.GONE);
//			}
//
//			ImageView setAsWallpaperButton = (ImageView) v
//					.findViewById(R.id.setAsWallpaperButton);
//			setAsWallpaperButton.setOnClickListener(this);
//			Log.i(TAG, "Inflate");
//
//			return v;
//		}
//
//		@Override
//		public boolean isViewFromObject(View view, Object object) {
//			return view.equals(object);
//		}
//
//		@Override
//		public void restoreState(Parcelable p, ClassLoader c) {
//		}
//
//		@Override
//		public Parcelable saveState() {
//			return null;
//		}
//
//		@Override
//		public void startUpdate(View view) {
//		}
//
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			ImageView previewImageView = (ImageView) v;
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				previewImageView.setColorFilter(new LightingColorFilter(
//						0xFFF1C40E, 0x00000000));
//				break;
//			case MotionEvent.ACTION_UP:
//				int currentItem = pager.getCurrentItem();
//				Painting painting = paintings.get(currentItem);
//				if (painting.getArtistName().trim().isEmpty()) {
//					final Intent emailIntent = new Intent(
//							Intent.ACTION_SEND);
//
//					/* Fill it with Data */
//					emailIntent.setType("plain/text");
//					emailIntent.putExtra(Intent.EXTRA_EMAIL,
//							new String[] { "livepaintings@fordemobile.com" });
//					emailIntent.putExtra(Intent.EXTRA_SUBJECT,
//							getString(R.string.request_subject));
//					emailIntent.putExtra(Intent.EXTRA_TEXT,
//							getString(R.string.request_message));
//
//					/* Send it off to the Activity-Chooser */
//					this.context.startActivity(Intent.createChooser(emailIntent,
//							getString(R.string.request_send)));
//				} else {
//					previewImageView.setColorFilter(new LightingColorFilter(
//							0xFFFFFFFF, 0x00000000));
//					SharedPreferences prefs = getSharedPreferences(
//							Consts.SHARED_PREFS_NAME, 0);
//					SharedPreferences.Editor edit = prefs.edit();
//					edit.putString(Consts.PREF_PAINTING,
//							painting.getDirectoryPath());
//					edit.commit();
//
//					Toast.makeText(
//							this.context,
//							painting.getName() + " "
//									+ getString(R.string.set_as_painting),
//							Toast.LENGTH_SHORT).show();
//				}
//				break;
//			}
//			return true;
//		}
//
//		@Override
//		public void onClick(View v) {
//
//			switch (v.getId()) {
//			case R.id.setAsWallpaperButton:
//				// install button
//				Toast toast = Toast.makeText(this.context,
//						R.string.choose_live_paintings, Toast.LENGTH_LONG);
//				toast.show();
//
//				Intent intent = new Intent();
//				intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
//				startActivityForResult(intent, REQUEST_CODE);
//				break;
//			case R.id.buyFullVersionButton:
//				// buy full version
//				try {
//					if (!FragmentPagerSupport.this.mBillingService
//							.requestPurchase(Consts.PACK_001, "Full Version")) {
//						showDialog(FragmentPagerSupport.DIALOG_BILLING_NOT_SUPPORTED_ID);
//					}
//				} catch (NullPointerException e) {
//					showDialog(FragmentPagerSupport.DIALOG_BILLING_NOT_SUPPORTED_ID);
//				}
//				break;
//			default:
//				break;
//			}
//		}
//	}
//
//	private ViewPager pager;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.fragment_pager);
//		HardwareManager.initialize(this);
//
//		this.pager = (ViewPager) findViewById(R.id.pager);
//		this.pager.setAdapter(new ViewPagerAdapter(this));
//
//		this.mHandler = new Handler();
//		this.mUserPurchaseObserver = new UserPurchaseObserver(this.mHandler);
//
//		this.mBillingService = new BillingService();
//		this.mBillingService.setContext(this);
//
//		this.mPurchaseDatabase = new PurchaseDatabase(this);
//
//		// Check if billing is supported.
//		ResponseHandler.register(this.mUserPurchaseObserver);
//		if (this.mBillingService.checkBillingSupported()) {
//			updatePurchaseview(false);
//		}
//	}
//
//	@Override
//	protected void onStart() {
//		super.onStart();
//
//		ResponseHandler.register(this.mUserPurchaseObserver);
//		initializeOwnedItems();
//
//		Toast.makeText(this, R.string.choose_painting, Toast.LENGTH_SHORT)
//				.show();
//	}

	// @Override
	// protected Dialog onCreateDialog(int id) {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setTitle(R.string.purchase).setMessage(R.string.purchase_question)
	// .setPositiveButton(R.string., new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// MyActivity.this.finish();
	// }
	// })
	// .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
	// {
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.cancel();
	// });
	//
	// return super.onCreateDialog(id);
	// }
}