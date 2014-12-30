/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.fordemobile.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class implements the broadcast receiver for in-app billing. All
 * asynchronous messages from Android Market come to this app through this
 * receiver. This class forwards all messages to the {@link BillingService},
 * which can start background threads, if necessary, to process the messages.
 * This class runs on the UI thread and must not do any network I/O, database
 * updates, or any tasks that might take a long time to complete. It also must
 * not start a background thread because that may be killed as soon as
 * {@link #onReceive(Context, Intent)} returns.
 * 
 * You should modify and obfuscate this code before using it.
 */
public class BillingReceiver extends BroadcastReceiver {
	private static final String TAG = "BillingReceiver";

	/**
	 * This is the entry point for all asynchronous messages sent from Android
	 * Market to the application. This method forwards the messages on to the
	 * {@link BillingService}, which handles the communication back to Android
	 * Market. The {@link BillingService} also reports state changes back to the
	 * application through the {@link ResponseHandler}.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (BillingService.Action.PURCHASE_STATE_CHANGED.equalsTo(action)) {
			String signedData = intent
					.getStringExtra(BillingService.Extra.INAPP_SIGNED_DATA
							.toString());
			String signature = intent
					.getStringExtra(BillingService.Extra.INAPP_SIGNATURE
							.toString());
			intent = new Intent(BillingService.Action.PURCHASE_STATE_CHANGED
					.toString());
			intent.setClass(context, BillingService.class);
			intent.putExtra(BillingService.Extra.INAPP_SIGNED_DATA.toString(),
					signedData);
			intent.putExtra(BillingService.Extra.INAPP_SIGNATURE.toString(),
					signature);
			context.startService(intent);
		} else if (BillingService.Action.NOTIFY.equalsTo(action)) {
			String notifyId = intent
					.getStringExtra(BillingService.Extra.NOTIFICATION_ID
							.toString());
			intent = new Intent(BillingService.Action.GET_PURCHASE_INFORMATION
					.toString());
			intent.setClass(context, BillingService.class);
			intent.putExtra(BillingService.Extra.NOTIFICATION_ID.toString(),
					notifyId);
			context.startService(intent);
		} else if (BillingService.Action.RESPONSE_CODE.equalsTo(action)) {
			long requestId = intent.getLongExtra(
					BillingService.Extra.INAPP_REQUEST_ID.toString(), -1);
			int responseCodeIndex = intent.getIntExtra(
					BillingService.Extra.INAPP_RESPONSE_CODE.toString(),
					BillingRequest.Result.ERROR.ordinal());
			intent = new Intent(BillingService.Action.RESPONSE_CODE.toString());
			intent.setClass(context, BillingService.class);
			intent.putExtra(BillingService.Extra.INAPP_REQUEST_ID.toString(),
					requestId);
			intent.putExtra(
					BillingService.Extra.INAPP_RESPONSE_CODE.toString(),
					responseCodeIndex);
			context.startService(intent);
		}
	}

}
