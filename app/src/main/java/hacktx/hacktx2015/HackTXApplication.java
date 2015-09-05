package hacktx.hacktx2015;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.estimote.sdk.EstimoteSDK;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import hacktx.hacktx2015.services.BeaconService;

public class HackTXApplication extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        initParse();
        initBeacons();

    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(BuildConfig.GOOG_ANALYTICS_ID);

            // Provide unhandled exceptions reports. Do that first after creating the tracker
            mTracker.enableExceptionReporting(true);

            // Enable automatic activity tracking for your app
            mTracker.enableAutoActivityTracking(true);
        }
        return mTracker;
    }

    private void initParse() {
        Parse.initialize(this, BuildConfig.PARSE_APP_ID, BuildConfig.PARSE_CLIENT_ID);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("announcements", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }

    private void initBeacons() {
        EstimoteSDK.initialize(this, BuildConfig.ESTIMOTE_APP_ID, BuildConfig.ESTIMOTE_APP_TOKEN);
        EstimoteSDK.enableDebugLogging(true);

        Intent serviceIntent = new Intent(getApplicationContext(), BeaconService.class);
        startService(serviceIntent);
    }
}