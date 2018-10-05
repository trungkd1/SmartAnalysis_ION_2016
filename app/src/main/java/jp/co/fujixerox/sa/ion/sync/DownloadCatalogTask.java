package jp.co.fujixerox.sa.ion.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.entities.CatalogList;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;

import static jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback.PROGRESS_TYPE.NONE;

/**
 * Created by TrungKD
 * AsyncTask using to get catalog task
 */
public class DownloadCatalogTask extends AsyncTask<String, Void, Boolean> {
    public static final String TAG = DownloadCatalogTask.class.getSimpleName();
    private Context mContext;
    public int isCancelled=0;
    /**
     * {@link AsyncTaskCallback}
     */
    private AsyncTaskCallback callback;
    /**
     * {@link AudioData}
     */
    private List<AudioFormData> mAudioFormDataList;
    /**
     * {@link CatalogList}
     */
    private CatalogList catalogList;
    /**
     * message when get catalog failed
     */
    private int errorMessage = R.string.no_data;

    /**
     * Constructor
     *
     * @param context:  Context
     * @param audioFormDataList: audio data
     * @param listener:  event listener
     */
    public DownloadCatalogTask(Context context, List<AudioFormData> audioFormDataList, AsyncTaskCallback listener) {
        this.mContext = context;
        this.mAudioFormDataList = audioFormDataList;
        callback = listener;
    }

    /**
     * {@inheritDoc}
     */
    protected void onPreExecute() {
        try {
            Log.d(TAG, "DownloadCatalogTask start onPreExecute()");
        } finally {
            Log.d(TAG, "DownloadCatalogTask end onPreExecute()");
        }
        if (callback != null) {
            callback.onPrepare(NONE);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Boolean doInBackground(String... params) {
        //check it isn't satisfy when return false
        if (mAudioFormDataList == null || mAudioFormDataList.size() <= 0) {
            Log.e(TAG, "AudioFormDataList is empty");
            return false;
        }
        //check it has no network when return failed set error message to "no connect internet"
        if (!CloudConnector
                .isConnectingToInternet(mContext)) {
            errorMessage = R.string.no_connect_internet;
            return Boolean.FALSE;
        }
        InputStream inputStream = CloudConnector.getCatalogList(mAudioFormDataList);
        if (inputStream != null) {
            CatalogList catalogList = JsonParser
                    .getCatalogList(inputStream);
            if (catalogList != null) {
                Log.v(TAG, "GET CATALOG_SCREEN LIST: " + catalogList.toString());
                this.catalogList = catalogList;
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorMessage = R.string.no_data;
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    protected void onPostExecute(Boolean result) {
        Log.d(TAG, "DownloadCatalogTask start onPostExecute()");
        try {
            if (callback != null) {
                if (result) {
                    callback.onSuccess(catalogList);
                    //Tracking Event
                    DefaultApplication.getInstance().trackEvent(
                            this.mContext.getString(R.string.category_background),
                            this.mContext.getString(R.string.action_get_catalog_list),
                            CommonUtils.getStringPreferences(mContext, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
                } else {
                    callback.onFailed(errorMessage);
                }
            }
        } finally {
            Log.d(TAG, "DownloadCatalogTask end onPostExecute()");
        }

    }


    @Override
    protected void onCancelled(){

    }

}
