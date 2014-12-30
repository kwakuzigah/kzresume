package com.fordemobile.billing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IMarketBillingService;
import com.fordemobile.billing.BillingRequest.CheckBillingSupported;
import com.fordemobile.billing.BillingRequest.ConfirmNotifications;
import com.fordemobile.billing.BillingRequest.GetPurchaseInformation;
import com.fordemobile.billing.BillingRequest.RequestPurchase;
import com.fordemobile.billing.BillingRequest.RestoreTransactions;
import com.fordemobile.billing.BillingRequest.Result;
import com.fordemobile.billing.Security.VerifiedPurchase;

public class BillingService extends Service implements ServiceConnection {

	/**
	 * The list of requests that are pending while we are waiting for the
	 * connection to the MarketBillingService to be established.
	 */
	protected static LinkedList<BillingRequest> mPendingRequests = new LinkedList<BillingRequest>();

	/**
	 * The list of requests that we have sent to Android Market but for which we
	 * have not yet received a response code. The HashMap is indexed by the
	 * request Id that each request receives when it executes.
	 */
	protected static HashMap<Long, BillingRequest> mSentRequests = new HashMap<Long, BillingRequest>();

	private static enum Service {
		MARKET_BILLING_SERVICE_ACTION(
				"com.android.vending.billing.MarketBillingService.BIND");
		private final String value;

		Service(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}

	public static enum Action {
		CONFIRM_NOTIFICATION("com.forde.kutiz.CONFIRM_NOTIFICATION"), GET_PURCHASE_INFORMATION(
				"com.forde.kutiz.GET_PURCHASE_INFORMATION"), RESTORE_TRANSACTIONS(
				"com.forde.kutiz.RESTORE_TRANSACTIONS"), NOTIFY(
				"com.android.vending.billing.IN_APP_NOTIFY"), RESPONSE_CODE(
				"com.android.vending.billing.RESPONSE_CODE"), PURCHASE_STATE_CHANGED(
				"com.android.vending.billing.PURCHASE_STATE_CHANGED");
		private final String value;

		Action(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}

		public boolean equalsTo(String string) {
			return this.value.equals(string);
		}
	}

	public enum Extra {
		NOTIFICATION_ID("notification_id"), INAPP_SIGNED_DATA(
				"inapp_signed_data"), INAPP_SIGNATURE("inapp_signature"), INAPP_REQUEST_ID(
				"request_id"), INAPP_RESPONSE_CODE("response_code");
		private final String value;

		Extra(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}

	private IMarketBillingService mService;

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = IMarketBillingService.Stub.asInterface(service);
		runPendingRequests();
	}

	/**
	 * Runs any pending requests that are waiting for a connection to the
	 * service to be established. This runs in the main UI thread.
	 */
	private void runPendingRequests() {
		int maxStartId = -1;
		BillingRequest request;
		while ((request = mPendingRequests.peek()) != null) {
			if (request.runIfConnected()) {
				// Remove the request
				mPendingRequests.remove();

				// Remember the largest startId, which is the most recent
				// request to start this service.
				if (maxStartId < request.getStartId()) {
					maxStartId = request.getStartId();
				}
			} else {
				// The service crashed, so restart it. Note that this leaves
				// the current request on the queue.
				bindToMarketBillingService();
				return;
			}
		}

		// If we get here then all the requests ran successfully. If maxStartId
		// is not -1, then one of the requests started the service, so we can
		// stop it now.
		if (maxStartId >= 0) {
			stopSelf(maxStartId);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
	}

	/**
	 * We don't support binding to this service, only starting the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent != null) {
			String action = intent.getAction();

			if (Action.CONFIRM_NOTIFICATION.equalsTo(action)) {
				String[] notifyIds = intent
						.getStringArrayExtra(Extra.NOTIFICATION_ID.toString());
				new ConfirmNotifications(this, startId, notifyIds).runRequest();
			} else if (Action.GET_PURCHASE_INFORMATION.equalsTo(action)) {
				String notifyId = intent.getStringExtra(Extra.NOTIFICATION_ID
						.toString());
				new GetPurchaseInformation(this, startId,
						new String[] { notifyId }).runRequest();
			} else if (Action.PURCHASE_STATE_CHANGED.equalsTo(action)) {
				String signedData = intent
						.getStringExtra(Extra.INAPP_SIGNED_DATA.toString());
				String signature = intent.getStringExtra(Extra.INAPP_SIGNATURE
						.toString());
				ArrayList<Security.VerifiedPurchase> purchases;
				purchases = Security.verifyPurchase(signedData, signature);
				if (purchases == null) {
					return;
				}

				ArrayList<String> notifyList = new ArrayList<String>();
				for (VerifiedPurchase vp : purchases) {
					if (vp.notificationId != null) {
						notifyList.add(vp.notificationId);
					}
					ResponseHandler.purchaseResponse(this, vp.purchaseState,
							vp.productId, vp.orderId, vp.purchaseTime,
							vp.developerPayload);
				}
				if (!notifyList.isEmpty()) {
					String[] notifyIds = notifyList
							.toArray(new String[notifyList.size()]);
					new ConfirmNotifications(this, startId, notifyIds)
							.runRequest();
				}
			} else if (Action.RESPONSE_CODE.equalsTo(action)) {
				long requestId = intent.getLongExtra(
						Extra.INAPP_REQUEST_ID.toString(), -1);
				int responseCodeIndex = intent.getIntExtra(
						Extra.INAPP_RESPONSE_CODE.toString(),
						BillingRequest.Result.ERROR.ordinal());
				Result responseCode = Result.valueOf(responseCodeIndex);
				BillingRequest request = mSentRequests.get(requestId);
				if (request != null) {
					request.responseCodeReceived(responseCode);
				}
				mSentRequests.remove(requestId);
			} else if (Action.RESTORE_TRANSACTIONS.equalsTo(action)) {
				new RestoreTransactions(this).runRequest();
			}
		}
	}

	public boolean requestPurchase(String productId, String developerPayload) {
		return new RequestPurchase(this, productId, developerPayload)
				.runRequest();
	}

	/**
	 * Unbinds from the MarketBillingService. Call this when the application
	 * terminates to avoid leaking a ServiceConnection.
	 */
	public void unbind() {
		try {
			unbindService(this);
		} catch (IllegalArgumentException e) {
			// This might happen if the service was disconnected
		}
	}

	/**
	 * Binds to the MarketBillingService and returns true if the bind succeeded.
	 * 
	 * @return true if the bind succeeded; false otherwise
	 */
	protected boolean bindToMarketBillingService() {
		try {
			boolean bindResult = bindService(new Intent(
					Service.MARKET_BILLING_SERVICE_ACTION.toString()), this, // ServiceConnection.
					Context.BIND_AUTO_CREATE);

			if (bindResult) {
				return true;
			} else {
				// TODO handle interface
			}
		} catch (SecurityException e) {
			// TODO handle interface
		}
		return false;
	}

	public Bundle sendBillingRequest(Bundle request) throws RemoteException {
		return mService.sendBillingRequest(request);
	}

	/**
	 * Checks if in-app billing is supported.
	 * 
	 * @return true if supported; false otherwise
	 */
	public boolean checkBillingSupported() {
		return new CheckBillingSupported(this).runRequest();
	}

	public void setContext(Context context) {
		attachBaseContext(context);
	}

	public boolean restoreTransactions() {
		return new RestoreTransactions(this).runRequest();
	}

	public boolean isConnected() {
		return (mService != null);
	}
}
