package jp.co.fujixerox.sa.ion.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.Report;
import jp.co.fujixerox.sa.ion.entities.ReportData;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;

import static jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback.PROGRESS_TYPE;

/**
 * Created by TrungKD
 */
public class DownloadReportListTask extends AsyncTask<String, Void, Boolean> {
    public static final String TAG = DownloadCatalogTask.class.getSimpleName();
    private static int COUNT = 20;
    private Context mContext;
    private AsyncTaskCallback asyncTaskCallback;
    private List<Report> audiosReport;
    private PROGRESS_TYPE loadingType;
    private String productNanme;
    private String serialId;
    private static boolean isPaging;

    /**
     * Constructor
     */
    public DownloadReportListTask(Context context, AsyncTaskCallback asyncTaskCallback,
                                  String productNanme, String serialId, PROGRESS_TYPE loading_type) {
        this.mContext = context;
        this.asyncTaskCallback = asyncTaskCallback;
        this.loadingType = loading_type;
        this.productNanme = productNanme;
        this.serialId = serialId;
    }

    protected void onPreExecute() {
        if (asyncTaskCallback != null) {
            asyncTaskCallback.onPrepare(loadingType);
        }
    }

    protected Boolean doInBackground(String... params) {
        //String cookieAuth = CommonUtils.getStringPreferences(mContext, Utility.SHARE_PREFERENCES.KEY_COOKIE_AUTHEN);
        String cookieAuth = null;
        if (serialId == null && productNanme == null) { // only use accountId when serialId and productName is null
            cookieAuth = CommonUtils.getStringPreferences(mContext, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);   // hashに変更 160406 mit
        }
        //Get past reports from cloud
        //InputStream inputStream = CloudConnector.getOldReports(cookieAuth, null, 20, false);
        if(loadingType == PROGRESS_TYPE.FIRST_LOADING) {
            isPaging = false;
        } else if (isPaging == false) {
            return false;
        }
        InputStream inputStream = CloudConnector.getOldReports(cookieAuth, productNanme, serialId, COUNT, isPaging);
        //InputStream inputStream = null;
        if (inputStream == null) {
            Log.e(TAG, "Old report is empty");
            //dummy data
            //inputStream = FileUtils.readStreamFromAsset(Utility.ASSETS_JSON_PATH, "oldreportresponse.json", mContext.getAssets());
            //audiosReport = JsonParser.parseToReportListFromJsonStream(inputStream);
        } else {
            ReportData reportData = JsonParser.gson.fromJson(FileUtils.readFully(inputStream, Utility.ENCODING), ReportData.class);
            if (reportData !=null) {
                isPaging = (reportData.getPaging() == 1);
                audiosReport = reportData.getReport();
            }
        }
        if (audiosReport != null) {
            return true;
        } else {
            Log.e(TAG, "response is null");
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        if (asyncTaskCallback != null) {
            asyncTaskCallback.onFinish(loadingType);
        }
        if (result) {
            if (asyncTaskCallback != null ) {
                asyncTaskCallback.onSuccess(audiosReport);
            }
        } else {
            if (asyncTaskCallback != null) {
                asyncTaskCallback.onFailed(R.string.no_data);
            }
        }
    }

    /**
     * Class: the event listener for Get ReportData List
     */
    public interface GetReportListTaskEventListener {
        void onPrepare(PROGRESS_TYPE loadingType);

        void onSuccess(List<Report> audios);

        void onFailed(int errorMessageId);

        void onFinish(PROGRESS_TYPE loadingType);
    }
}
