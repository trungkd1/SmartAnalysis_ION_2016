package jp.co.fujixerox.sa.ion;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import jp.co.fujixerox.sa.ion.analytics.AnalyticsTrackers;
import jp.co.fujixerox.sa.ion.receivers.AlarmReceiver;

/**
 * Created by FPT on 12/15/2015.
 */
public class DefaultApplication extends Application{
    public static final String TAG = DefaultApplication.class.getSimpleName();
    private static DefaultApplication mInstance;
    private AlarmReceiver alarm = new AlarmReceiver();

    public void onCreate() {
        super.onCreate();
        alarm.setAlarm(getBaseContext());
        mInstance = this;
        onCreateGoogleAnalytics();
    }

    private void onCreateGoogleAnalytics() {
        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().getTrackerFromGoogleAnalytics(AnalyticsTrackers.Target.APP);
    }

    public static synchronized DefaultApplication getInstance() {
        return mInstance;
    }

    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.getTrackerFromGoogleAnalytics(AnalyticsTrackers.Target.APP);
    }

    /***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    /***
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @param label    label
     */
    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label).build());
    }


}
