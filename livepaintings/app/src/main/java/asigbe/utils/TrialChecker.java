package asigbe.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public class TrialChecker {

	private static final String IS_FIRST_TIME_LAUNCH = "is_first_time";
	public interface OnTrialCheckerListener {
		public void onTrialExpired();
	}
	
	private final Handler checkTrialHandler = new Handler();
	private final Runnable mCheckTrialRunnable = new Runnable() {
		public void run() {
			checkIfTrialHasExpired();
		}
	};
	private boolean trialHasExpired;
	private OnTrialCheckerListener listener;
	private final Context context;
	private final String preferenceName;

	public TrialChecker(Context context, String preferenceName, OnTrialCheckerListener listener) {
		this.context = context;
		this.preferenceName = preferenceName;
		this.listener = listener;
		this.trialHasExpired = false;

		SharedPreferences sharedPreferences = this.context.getSharedPreferences(
				preferenceName, Context.MODE_PRIVATE);
		boolean is_first_time = sharedPreferences.getBoolean(
				IS_FIRST_TIME_LAUNCH, true);

		if (is_first_time) {
			// the first time it's launched, we save into a file the date of the
			// first launch
			try {
				FileOutputStream fos = this.context.openFileOutput("as",
						Context.MODE_PRIVATE);
				DataOutputStream dos = new DataOutputStream(fos);
				dos.writeLong(Calendar.getInstance().getTimeInMillis());
				fos.close();

				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(IS_FIRST_TIME_LAUNCH, false);
				editor.commit();
			} catch (Exception e) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(IS_FIRST_TIME_LAUNCH, true);
				editor.commit();
			}
		}
		checkIfTrialHasExpired();
	}

	public void setListener(OnTrialCheckerListener listener) {
		this.listener = listener;
	}
	
	public void checkIfTrialHasExpired() {
		SharedPreferences sharedPreferences = this.context.getSharedPreferences(
				this.preferenceName, Context.MODE_PRIVATE);
		// then each time it's launched again we test if the trial has expired
		try {
			FileInputStream fos = this.context.openFileInput("as");
			DataInputStream dis = new DataInputStream(fos);
			long firstTimeInMillis;
			firstTimeInMillis = dis.readLong();
			long timeDifference = Calendar.getInstance().getTimeInMillis()
					- firstTimeInMillis;
			// if (timeDifference < 0
			// || timeDifference > (48 * 3600 * 1000)) {
			if (timeDifference < 0 || timeDifference > 30) {
				//trial has expired
				//this.trialHasExpired = true;
				this.listener.onTrialExpired();
			} else {
				// plan the next check
				// this.checkTrialHandler.postDelayed(mCheckTrialRunnable,
				// 24*3600*1000);
				this.checkTrialHandler.postDelayed(mCheckTrialRunnable, 10);
			}
			fos.close();
		} catch (Exception e) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(IS_FIRST_TIME_LAUNCH, true);
			editor.commit();
		}
	}
	
	public boolean hasTrialExpired() {
		return this.trialHasExpired;
	}

}
