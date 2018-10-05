package jp.co.fujixerox.sa.ion.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.entities.UploadResponseJson;
import jp.co.fujixerox.sa.ion.utils.CloudConnector2;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.ICloudInterface;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.ProgressRequestBody;
import jp.co.fujixerox.sa.ion.utils.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Background upload report service class
 *
 * Created by TrungKD
 */
public class UploadReportService extends IntentService {
    private static final String TAG = UploadReportService.class.getSimpleName();
    private static final int MAX = 100;
    private static final int NOTIFY_ID = 1;
    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    /**
     * Use two list waitingAudioDataList and sendingAudioDataList,
     * to prevent sending a report at the same time.
     * So check the report before sending,
     * if the same report, it must submit a report each.
     */
    private static List<AudioData> waitingAudioDataList = new ArrayList<>();
    private static List<AudioData> sendingAudioDataList = new ArrayList<>();
    /**
     * Use sendingCallList
     * To be able to cancel a process of sending the report and recover from the outside.
     */
    private static List<Call> sendingCallList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private String accountId;
    public UploadReportService(String name) {
        super(name);
        databaseHelper = DatabaseHelper
                .getInstance(UploadReportService.this);

    }

    public UploadReportService() {
        this(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        accountId = CommonUtils.getStringPreferences(getBaseContext(),Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        boolean isCancelReport = intent.getBooleanExtra(Utility.EXTRA_INTENT.CANCEL_SENDING_REPORT, false);
        boolean isResumelReport = intent.getBooleanExtra(Utility.EXTRA_INTENT.RESUME_SENDING_REPORT, false);
        if (isCancelReport) {
            cancelSendingAudioData();
            return;
        } else if (isResumelReport) {
            resumeSendingAudioData();
            return;
        }
        final AudioData audioData = intent.getParcelableExtra(Utility.EXTRA_INTENT.AUDIO_REPORT);
        Log.i(TAG, "@@@ onHandleIntent report id="+audioData.getId());
        boolean isSending = checkSameAudioDataIsSending(audioData);
        if (isSending) {
            waitingAudioDataList.add(audioData);
            //wait to data send completed
            return;
        }
        sendingAudioDataList.add(audioData);
        if (audioData != null) {
            sendAudioData(audioData);
        }
    }

    /**
     * Send report data
     */
    private void sendAudioData(final AudioData audioData) {
        startSendReportNotification();
        Call callGet = CloudConnector2.get(ICloudInterface.API.GET_REPORT_URL, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "can't get upload report url", e);
                sendingCallList.remove(call);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                sendingCallList.remove(call);
                int status = response.code();
                boolean result = response.isSuccessful();
                String uploadUrl = response.body().string();
                response.body().close();
                if (result) {
                    List<AudioFormData> data = createUploadData(audioData);
                    //only send account id when new report
                    String newAccountId = null;
                    if (audioData.getReportid() == null) {
                        newAccountId = accountId;
                    }
                    Call callPost = CloudConnector2.postReport(uploadUrl, data, newAccountId, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "can't upload report data", e);
                            sendingCallList.remove(call);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            sendingCallList.remove(call);
                            int status = response.code();
                            boolean result = response.isSuccessful();
                            String responseJson = response.body().string();
                            response.body().close();
                            if (result) {
                                UploadResponseJson uploadResponse = JsonParser.gson.fromJson(
                                        responseJson, UploadResponseJson.class);
                                String reportid = uploadResponse.getReportid();
                                Log.i(TAG, "@@@ reportid " + reportid);
                                if (!TextUtils.isEmpty(reportid)) {
                                    audioData.setReportid(reportid);
                                    int updatedRows = databaseHelper.updateAudioData(audioData);
                                    Log.i(TAG, "@@@ UPLOAD success rows: " + updatedRows);
                                    finishSendReportNotification(audioData, uploadResponse);
                                } else {
                                    Log.e(TAG, "@@@ UPLOAD FAIL CLOUD REPORT_SCREEN ID IS NULL");
                                    finishSendReportNotification(audioData, null);
                                }
                            } else {
                                finishSendReportNotification(audioData, null);
                                Log.e(TAG, "can't upload report data, reponse code" + status);
                            }
                            sendNextAudioData(audioData);
                        }
                    }, new ProgressRequestBody.Listener() {
                        @Override
                        public void onRequestProgress(long bytesWritten, long contentLength) {
                            mBuilder.setProgress((int)contentLength, (int)bytesWritten, false);
                            mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
                        }
                    });
                    // add callPost to list sending
                    sendingCallList.add(callPost);
                } else {
                    Log.e(TAG, "can't get upload url, response code"+status);
                }
            }
        });
        sendingCallList.add(callGet);
    }

    /**
     *  1. remove completed audiodata from sendinglist
     /* 2. Find the same audiodata from waitinglist
     /* 3. If exist then send audiodata, add it to sendinglist, and remove from waitinglist
     * @param completedAudioData
     */
    private void sendNextAudioData(AudioData completedAudioData) {
        // 1. remove completed audiodata from sendinglist
        // 2. Find the same audiodata from waitinglist
        // 3. If exist then send audiodata, add it to sendinglist, and remove from waitinglist
        sendingAudioDataList.remove(completedAudioData);
        AudioData waitingAudioData = null;
        for (AudioData data :
                waitingAudioDataList) {
            if (data.getId() == completedAudioData.getId()) {
                waitingAudioData = data;
                break;
            } else {
                if (completedAudioData.getReportid() != null &&
                        completedAudioData.getReportid().equals(data.getReportid())) {
                    waitingAudioData = data;
                    break;
                }
            }
        }
        if (waitingAudioData != null) {
            waitingAudioDataList.remove(waitingAudioData);
            sendingAudioDataList.add(waitingAudioData);
            sendAudioData(waitingAudioData);
            Log.v(TAG, "@@@ send waiting data" + waitingAudioData.getId());
            Log.v(TAG, "@@@ waiting list size" + waitingAudioDataList.size());
        }
    }

    private List<AudioFormData> createUploadData(AudioData audioData) {
        List<AudioFormData> data = new ArrayList<>();
        data.addAll(audioData.getListAudioFormData());
        return data;
    }

    /**
     * Cancel sending report
     */
    private void cancelSendingAudioData() {
        Log.d(TAG, "@@@cancleSendingAudioData");
        for (Call call:
             sendingCallList) {
            if (!call.isCanceled()) {
                call.cancel();
            }
        }
        sendingCallList.clear();
    }

    /**
     * Resume sending audio data
     */
    private void resumeSendingAudioData() {
        Log.d(TAG, "@@@resumeSendingAudioData");
        if (!sendingAudioDataList.isEmpty()) {
            for (AudioData audioData :
                    sendingAudioDataList) {
                sendAudioData(audioData);
            }
        }
    }

    /**
     * Check audioData ID is existing in sending list
     * @param audioData
     * @return true if exist or false if none
     */
    public static boolean checkSameAudioDataIsSending(AudioData audioData) {
        for (AudioData data:
                sendingAudioDataList) {
            if (data.getId() == audioData.getId()) {
                return true;
            } else if (audioData.getReportid() != null &&
                    audioData.getReportid().equals(data.getReportid())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove sent report from sending list,
     * And send report in waiting if it's same.
     * @param audioData if waiting data exist or null if not.
     */
    private AudioData updateSendingAudioDataList(AudioData audioData) {
        sendingAudioDataList.remove(audioData);
        for (AudioData data :
                waitingAudioDataList) {
            if (data.getId() == audioData.getId()) {
                return data;
            }
        }
        return null;
    }

    /**
     * Start show data sending notification
     */
    private void startSendReportNotification() {
        mBuilder = new NotificationCompat.Builder(UploadReportService.this);
        mBuilder.setContentTitle(getText(R.string.notify_upload_title))
                .setSmallIcon(android.R.drawable.stat_sys_upload);
        mBuilder.setProgress(MAX, 0, false);
        mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * Show finish data sending notification
     * @param audioData
     * @param uploadResponse
     */
    private void finishSendReportNotification(AudioData audioData, UploadResponseJson uploadResponse) {
        // When the loop is finished, updates the notification
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
        mBuilder.setProgress(MAX, MAX, false);
        if (uploadResponse != null) {
            mBuilder.setContentText(getText(R.string.notify_upload_complate));
            //remove audios id out of list audios id pending
            CommonUtils.removeAudiosIdOutListAudiosPending(UploadReportService.this, String.valueOf(audioData.getId()));
            //Tracking Event
            DefaultApplication.getInstance().trackEvent(getString(R.string.action_send_report),
                    uploadResponse.getReportid(),
                    CommonUtils.getStringPreferences(getBaseContext(), Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
        } else {
            Log.i(TAG, "result == null");
            mBuilder.setContentText(getText(R.string.notify_upload_fail));
            //put audios id in list audios id pending
            CommonUtils.putAudiosIdToListAudiosPending(UploadReportService.this, String.valueOf(audioData.getId()));
            //Tracking Event
            DefaultApplication.getInstance().trackEvent(getString(R.string.action_send_report),
                    getString(R.string.action_send_report_err),
                    CommonUtils.getStringPreferences(getBaseContext(), Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
        }
        mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
        sendBroadcast(new Intent(Utility.EXTRA_INTENT.AUDIO_REPORT));
    }

}
