package jp.co.fujixerox.sa.ion.receivers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import jp.co.fujixerox.sa.ion.services.CheckReportSchedulingService;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
public class AlarmBootReceiver extends BroadcastReceiver {
    public static final String TAG = AlarmBootReceiver.class.getSimpleName();
    AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "@@AlarmBootReceiver: onReceive");
        alarm.setAlarm(context);
        startCheckReportService(context);
    }

    /**
     * call CheckReportSchedulingService
     * @param context: Context
     */
    private void startCheckReportService(Context context) {
        Log.i(TAG, "@@startCheckReportService");
        Intent service = new Intent(context, CheckReportSchedulingService.class);

        // Start the service, keeping the device awake while it is launching.
        context.startService(service);
    }
}
