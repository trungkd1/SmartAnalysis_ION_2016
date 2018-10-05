package jp.co.fujixerox.sa.ion.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.activities.RecordListScreenActivity;
import jp.co.fujixerox.sa.ion.activities.ReportDetailScreenActivity;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.receivers.AlarmReceiver;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 */
public class CheckReportSchedulingService extends IntentService {
    protected static final String TAG = CheckReportSchedulingService.class.getSimpleName();

    public CheckReportSchedulingService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "@@CheckReportSchedulingService: onHandleIntent");
        sendNotification(getReportsInputNotComplete());
        AlarmReceiver.completeWakefulIntent(intent);
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(List<AudioData> audioDataList) {
        if (audioDataList == null || audioDataList.isEmpty()) return;
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        /* 通知を1回に変更　160301 mitsuha
        for (AudioData audioData :
                audioDataList) {

            Intent intent = new Intent(this, ReportDetailScreenActivity.class);
            intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, audioData);
            PendingIntent contentIntent = PendingIntent.getActivity(this, (int)audioData.getId(),
                    intent, 0);
         */
            Intent intent = new Intent(this, RecordListScreenActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);
            //String msg = getString(R.string.message_notify_has_report_miss_input, audioData.getId());
            String msg = getString(R.string.message_notify_has_report_miss_input);
            Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg))
                    .setContentText(msg)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);
            //notificationManager.notify((int) audioData.getId(), builder.build());
            notificationManager.notify(0,builder.build());
        //} forの終わり

    }

    /**
     * check has report with item hasn't inputted (cause or method or result)
     *
     * @return boolean
     */
    private List<AudioData> getReportsInputNotComplete() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        return databaseHelper.getReportMissInput();
    }


}
