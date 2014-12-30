package com.fordemobile.livepaintings;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;

/**
 * Created by dai on 14-12-26.
 */
public class LivePaintingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
        }

        Mint.initAndStartSession(LivePaintingApplication.this, "21f28559");
        HardwareManager.initialize(this);
        DataManager.getInstance().initialize(this);
    }
}
