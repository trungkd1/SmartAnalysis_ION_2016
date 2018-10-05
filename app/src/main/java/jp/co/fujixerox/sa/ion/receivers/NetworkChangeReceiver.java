package jp.co.fujixerox.sa.ion.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.services.UploadReportService;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    public static final String TAG = NetworkChangeReceiver.class.getSimpleName();
    private DatabaseHelper databaseHelper;
    private static long lastUpload = 0;
    private static int period = 30*1000; //one minutes
    @Override
    public void onReceive(final Context context, final Intent intent) {
        databaseHelper = DatabaseHelper.getInstance(context);
        Log.v(TAG, intent.getAction());
        if (CloudConnector
                .isConnectingToInternet(context)) {
            if ((System.currentTimeMillis() - lastUpload) > period) {
                lastUpload = System.currentTimeMillis();
                Log.v(TAG, "@@@Upload all report pending again");
                uploadAllAudioReportPending(context);
            }
        }
    }


    private void uploadReport(Context context, AudioData audioData) {
        Intent intent = new Intent(context, UploadReportService.class);
        intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT,
                audioData);
        context.startService(intent);
    }

    /**
     * upload all audio report pending
     *
     * @param context: Context
     */
    private void uploadAllAudioReportPending(Context context) {
        String[] lstAudiosId = getListAudiosIdPending(context);
        List<AudioData> audioDataList = databaseHelper.getAudiosPending(lstAudiosId);
        if (audioDataList == null) {
            return;
        }
        for (AudioData audioData : audioDataList) {
            Log.v(TAG, "@@@Audio Pending: " + audioData.getId() + "|" + audioData.getSound());
            uploadReport(context, audioData);
        }

    }

    private String[] getListAudiosIdPending(Context context) {
        String lstAudiosIdStr = CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_REPORTS_UPLOAD_PENDING);
        Log.v(TAG, "@@lstAudiosIdStr: " + lstAudiosIdStr);
        if (TextUtils.isEmpty(lstAudiosIdStr)) {
            return null;
        }
        return lstAudiosIdStr.split(Utility.CHARACTERS_SEPARATE);
    }


}
