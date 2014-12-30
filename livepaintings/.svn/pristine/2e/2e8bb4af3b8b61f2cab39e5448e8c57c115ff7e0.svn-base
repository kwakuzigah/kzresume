package com.fordemobile.billing;

import java.util.HashMap;
import java.util.LinkedList;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.RemoteException;

/**
 * The base class for all requests that use the MarketBillingService. Each
 * derived class overrides the run() method to call the appropriate service
 * interface. If we are already connected to the MarketBillingService, then we
 * call the run() method directly. Otherwise, we bind to the service and save
 * the request on a queue to be run later when the service is connected.
 */
public abstract class BillingRequest {

	// These are the names of the fields in the request bundle.
	public enum Transaction {
		CHECK_BILLING_SUPPORTED, CONFIRM_NOTIFICATIONS, GET_PURCHASE_INFORMATION, REQUEST_PURCHASE
	}

	// These are the names of the fields in the request bundle.
	public enum Request {
		BILLING_REQUEST, API_VERSION, PACKAGE_NAME, ITEM_ID, DEVELOPER_PAYLOAD, NOTIFY_IDS, NONCE
	}

	public enum Response {
		RESPONSE_CODE, PURCHASE_INTENT, REQUEST_ID
	}

	// The response codes for a request, defined by Android Market.
	public enum Result {
		OK, USER_CANCELED, SERVICE_UNAVAILABLE, BILLING_UNAVAILABLE, ITEM_UNAVAILABLE, DEVELOPER_ERROR, ERROR;

		// Converts from an ordinal value to the ResponseCode
		public static Result valueOf(int index) {
			Result[] values = Result.values();
			if (index < 0 || index >= values.length) {
				return ERROR;
			}
			return values[index];
		}
	}

	private final int mStartId;
	protected long mRequestId;
	public static long BILLING_RESPONSE_INVALID_REQUEST_ID = -1;

	protected BillingService mService;

	public BillingRequest(BillingService service, int startId) {
		mStartId = startId;
		mService = service;
	}

	public int getStartId() {
		return mStartId;
	}

	/**
	 * Run the request, starting the connection if necessary.
	 * 
	 * @return true if the request was executed or queued; false if there was an
	 *         error starting the connection
	 */
	public boolean runRequest() {
		if (runIfConnected()) {
			return true;
		}

		if (mService.bindToMarketBillingService()) {
			// Add a pending request to run when the service is connected.
			BillingService.mPendingRequests.add(this);
			return true;
		}
		return false;
	}

	/**
	 * Try running the request directly if the service is already connected.
	 * 
	 * @return true if the request ran successfully; false if the service is not
	 *         connected or there was an error when trying to use it
	 */
	public boolean runIfConnected() {
		if (mService.isConnected()) {
			try {
				mRequestId = run();
				if (mRequestId >= 0) {
					BillingService.mSentRequests.put(mRequestId, this);
				}
				return true;
			} catch (RemoteException e) {
				onRemoteException(e);
			}
		}
		return false;
	}

	/**
	 * Called when a remote exception occurs while trying to execute the
	 * {@link #run()} method. The derived class can override this to execute
	 * exception-handling code.
	 * 
	 * @param e
	 *            the exception
	 */
	protected void onRemoteException(RemoteException e) {
		mService = null;
	}

	/**
	 * The derived class must implement this method.
	 * 
	 * @throws RemoteException
	 */
	abstract protected long run() throws RemoteException;

	/**
	 * This is called when Android Market sends a response code for this
	 * request.
	 * 
	 * @param responseCode
	 *            the response code
	 */
	protected void responseCodeReceived(Result result) {
	}

	protected Bundle makeRequestBundle(String method) {
		Bundle request = new Bundle();
		request.putString(Request.BILLING_REQUEST.toString(), method);
		request.putInt(Request.API_VERSION.toString(), 1);
		request.putString(Request.PACKAGE_NAME.toString(), mService
				.getPackageName());
		return request;
	}

	/**
	 * Wrapper class that checks if in-app billing is supported.
	 */
	static class CheckBillingSupported extends BillingRequest {
		public CheckBillingSupported(BillingService service) {
			// This object is never created as a side effect of starting this
			// service so we pass -1 as the startId to indicate that we should
			// not stop this service after executing this request.
			super(service, -1);
		}

		@Override
		protected long run() throws RemoteException {
			Bundle request = makeRequestBundle(Transaction.CHECK_BILLING_SUPPORTED
					.toString());
			Bundle response = mService.sendBillingRequest(request);
			int responseCode = response
					.getInt(BillingService.Action.RESPONSE_CODE.toString());

			boolean billingSupported = (responseCode == Result.OK.ordinal());

			ResponseHandler.checkBillingSupportedResponse(billingSupported);
			return BILLING_RESPONSE_INVALID_REQUEST_ID;
		}
	}

	/**
	 * Wrapper class that requests a purchase.
	 */
	public static class RequestPurchase extends BillingRequest {
		public final String mProductId;
		public final String mDeveloperPayload;

		public RequestPurchase(BillingService service, String itemId) {
			this(service, itemId, null);
		}

		public RequestPurchase(BillingService service, String itemId,
				String developerPayload) {
			// This object is never created as a side effect of starting this
			// service so we pass -1 as the startId to indicate that we should
			// not stop this service after executing this request.
			super(service, -1);
			mProductId = itemId;
			mDeveloperPayload = developerPayload;
		}

		@Override
		protected long run() throws RemoteException {
			Bundle request = makeRequestBundle(Transaction.REQUEST_PURCHASE
					.toString());
			request.putString(Request.ITEM_ID.toString(), mProductId);
			// Note that the developer payload is optional.
			if (mDeveloperPayload != null) {
				request.putString(Request.DEVELOPER_PAYLOAD.toString(),
						mDeveloperPayload);
			}
			Bundle response = mService.sendBillingRequest(request);
			PendingIntent pendingIntent = response
					.getParcelable(Response.PURCHASE_INTENT.toString());
			if (pendingIntent == null) {
				return BILLING_RESPONSE_INVALID_REQUEST_ID;
			}

			Intent intent = new Intent();

			ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
			return response.getLong(Response.REQUEST_ID.toString(),
					BILLING_RESPONSE_INVALID_REQUEST_ID);
		}

		@Override
		protected void responseCodeReceived(Result result) {
			ResponseHandler.responseCodeReceived(this, result);
		}
	}

	/**
	 * Wrapper class that confirms a list of notifications to the server.
	 */
	static class ConfirmNotifications extends BillingRequest {
		final String[] mNotifyIds;

		public ConfirmNotifications(BillingService service, int startId,
				String[] notifyIds) {
			super(service, startId);
			mNotifyIds = notifyIds;
		}

		@Override
		protected long run() throws RemoteException {
			Bundle request = makeRequestBundle(Transaction.CONFIRM_NOTIFICATIONS
					.toString());
			request.putStringArray(Request.NOTIFY_IDS.toString(), mNotifyIds);
			Bundle response = mService.sendBillingRequest(request);
			return response.getLong(Response.REQUEST_ID.toString(),
					BILLING_RESPONSE_INVALID_REQUEST_ID);
		}
	}

	/**
	 * Wrapper class that sends a GET_PURCHASE_INFORMATION message to the
	 * server.
	 */
	static class GetPurchaseInformation extends BillingRequest {
		long mNonce;
		final String[] mNotifyIds;

		public GetPurchaseInformation(BillingService service, int startId,
				String[] notifyIds) {
			super(service, startId);
			mNotifyIds = notifyIds;
		}

		@Override
		protected long run() throws RemoteException {
			mNonce = Security.generateNonce();

			Bundle request = makeRequestBundle(Transaction.GET_PURCHASE_INFORMATION
					.toString());
			request.putLong(Request.NONCE.toString(), mNonce);
			request.putStringArray(Request.NOTIFY_IDS.toString(), mNotifyIds);
			Bundle response = mService.sendBillingRequest(request);
			return response.getLong(Response.REQUEST_ID.toString(),
					BILLING_RESPONSE_INVALID_REQUEST_ID);
		}

		@Override
		protected void onRemoteException(RemoteException e) {
			super.onRemoteException(e);
			Security.removeNonce(mNonce);
		}
	}

	/**
	 * Wrapper class that sends a RESTORE_TRANSACTIONS message to the server.
	 */
	public static class RestoreTransactions extends BillingRequest {
		long mNonce;

		public RestoreTransactions(BillingService service) {
			// This object is never created as a side effect of starting
			// this service so we pass -1 as the startId to indicate that we
			// should not stop this service after executing this request.
			super(service, -1);
		}

		@Override
		protected long run() throws RemoteException {
			mNonce = Security.generateNonce();

			Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
			request.putLong(Request.NONCE.toString(), mNonce);
			Bundle response = mService.sendBillingRequest(request);

			return response.getLong(Response.REQUEST_ID.toString(),
					BILLING_RESPONSE_INVALID_REQUEST_ID);
		}

		@Override
		protected void onRemoteException(RemoteException e) {
			super.onRemoteException(e);
			Security.removeNonce(mNonce);
		}

		@Override
		protected void responseCodeReceived(Result result) {
			ResponseHandler.responseCodeReceived(this, result);
		}
	}
}
