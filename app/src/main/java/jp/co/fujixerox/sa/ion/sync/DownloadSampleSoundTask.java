package jp.co.fujixerox.sa.ion.sync;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.utils.FileUtils;

import static jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback.PROGRESS_TYPE.NONE;

/**
 * Created by TrungKD
 * AsyncTask download catalog's sample sound to temporary folder
 */
public class DownloadSampleSoundTask extends AsyncTask<String, Void, String> {

    public static final String TAG = DownloadSampleSoundTask.class.getSimpleName();

    private AsyncTaskCallback asyncTaskCallback;

    private File saveAudioCatalogDirectory;

    /**
     * Constructor
     *
     * @param saveAudioCatalogDirectory: file to save file audio catalog
     * @param asyncTaskCallback:         event handle asyncTaskCallback
     */
    public DownloadSampleSoundTask(File saveAudioCatalogDirectory, AsyncTaskCallback asyncTaskCallback) {
        this.asyncTaskCallback = asyncTaskCallback;
        this.saveAudioCatalogDirectory = saveAudioCatalogDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected void onPreExecute() {
        try {
            Log.d(TAG, "DownloadSampleSoundTask start onPreExecute()");
        } finally {
            Log.d(TAG, "DownloadSampleSoundTask end onPreExecute()");
        }
        if (asyncTaskCallback != null) {
            asyncTaskCallback.onPrepare(NONE);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String doInBackground(String... params) {
        if (!isSatisfyConditionToExecute(params)) {
            return null;
        }
        return FileUtils.downloadFileFromUrl(saveAudioCatalogDirectory, params[0]);
    }

    /**
     * {@inheritDoc}
     */
    protected void onPostExecute(String result) {
        Log.d(TAG, "DownloadSampleSoundTask start onPostExecute()");
        try {
            if (asyncTaskCallback != null) {
                if (!TextUtils.isEmpty(result)) {
                    asyncTaskCallback.onSuccess(result);
                } else {
                    asyncTaskCallback.onFailed(R.string.error);
                }
            }
        } finally {
            Log.d(TAG, "DownloadSampleSoundTask end onPostExecute()");
        }
    }

    private boolean isSatisfyConditionToExecute(String... params) {
        if (params == null || params.length == 0) {
            return false;
        }
        if (!URLUtil.isValidUrl(params[0])) {
            return false;
        }
        return true;

    }
}
