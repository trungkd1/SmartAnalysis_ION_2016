package jp.co.fujixerox.sa.ion.sync;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.AnalysisGuide;
import jp.co.fujixerox.sa.ion.entities.AnalysisHintInfo;
import jp.co.fujixerox.sa.ion.entities.AnalysisHintItem;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

import static jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback.PROGRESS_TYPE.NONE;

/**
 * Created by TrungKD
 * AsyncTask using to get analysis guide
 */
public class DownloadAnalysisHintInfoTask extends AsyncTask<Void, Void, Boolean> {
    protected static final String TAG = DownloadAnalysisHintInfoTask.class.getSimpleName();
    protected static final String ASSET_PATH = "analysis_guide";
    private final AsyncTaskCallback asyncTaskCallback;
    private AnalysisHintInfo mData;
    private Activity mActivity;
    private AssetManager assetManager;
    private File mAnalysisGuideSaveDirectory;


    /**
     * Constructor
     *
     * @param asyncTaskCallback: asyncTaskCallback
     */
    public DownloadAnalysisHintInfoTask(Activity activity, AsyncTaskCallback asyncTaskCallback) {
        this.asyncTaskCallback = asyncTaskCallback;
        this.mActivity = activity;
        this.assetManager = activity.getAssets();
    }

    protected void onPreExecute() {
        try {
            Log.d(TAG, "DownloadAnalysisHintInfoTask start onPreExecute()");
        } finally {
            Log.d(TAG, "DownloadAnalysisHintInfoTask end onPreExecute()");
        }
        if (asyncTaskCallback != null) {
            asyncTaskCallback.onPrepare(NONE);
        }
        createAnalysisGuideSaveDirectory();
    }

    protected Boolean doInBackground(Void... params) {
        FileUtils.copyAssets(assetManager, ASSET_PATH, mAnalysisGuideSaveDirectory);
        AnalysisGuide analysisGuide = readAnalysisGuideJsonFromSaveDirectory();

        if (analysisGuide == null) {
            return Boolean.FALSE;
        }
        mData = returnAnalysisGuideInfo(analysisGuide);
        return Boolean.TRUE;
    }

    protected void onPostExecute(Boolean result) {
        Log.d(TAG, "DownloadAnalysisHintInfoTask start onPostExecute()");
        try {
            if (asyncTaskCallback != null) {
                if (result) {
                    asyncTaskCallback.onSuccess(mData);
                } else {
                    asyncTaskCallback.onFailed(R.string.no_data);
                }
            }
        } finally {
            Log.d(TAG, "DownloadAnalysisHintInfoTask end onPostExecute()");
        }

    }

    private AnalysisHintInfo returnAnalysisGuideInfo(AnalysisGuide analysisGuide) {
        if (analysisGuide == null) {
            return null;
        }
        List<AnalysisHintItem> lstDataTop = analysisGuide.getTop();
        setAnalysisGuideItemFilePath(lstDataTop);
        List<AnalysisHintItem> lstDataBottom = analysisGuide.getBottom();
        setAnalysisGuideItemFilePath(lstDataBottom);
        return new AnalysisHintInfo(lstDataTop, lstDataBottom);
    }

    /**
     * set file path for List AnalysisHintItem
     *
     * @param lstData: list AnalysisHintItem data
     */
    private void setAnalysisGuideItemFilePath(List<AnalysisHintItem> lstData) {
        if (lstData != null) {
            for (AnalysisHintItem item : lstData) {
                File checkFile = new File(mAnalysisGuideSaveDirectory, item.getReferenceImage());
                if (checkFile.exists()) {
                    item.setReferenceImageFilePath(checkFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * create folder to save analysis guide json
     */
    private void createAnalysisGuideSaveDirectory() {
        try {
            String analysisGuideFolderName = mActivity.getExternalFilesDir(null)
                    + Utility.APP_ANALYSIS_GUIDE_FOLDER_NAME;
            FileUtils.createFolder(analysisGuideFolderName);
            mAnalysisGuideSaveDirectory = new File(analysisGuideFolderName);
        } catch (Exception ex) {
            Log.e(TAG, "Error when create analysis guide save directory", ex);
        }
    }

    /**
     * read file json from save directory
     *
     * @return
     */
    private AnalysisGuide readAnalysisGuideJsonFromSaveDirectory() {
        if (mAnalysisGuideSaveDirectory.exists()) {
            String jsonFileName = findAnalysisJsonFile(mAnalysisGuideSaveDirectory);
            if (!TextUtils.isEmpty(jsonFileName)) {
                File jsonFile = new File(mAnalysisGuideSaveDirectory, jsonFileName);
                return parseAnalysisGuideJson(jsonFile);
            }
        }
        return null;
    }

    /**
     * find file json
     *
     * @param directory
     * @return
     */
    private String findAnalysisJsonFile(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }
        String[] files = directory.list();
        if (files == null || files.length == 0) {
            return null;
        }
        for (String file : files) {
            if (file.endsWith(".json")) {
                Log.d(TAG, "@@FINDED JSON: " + file);
                return file;
            }
        }
        return null;
    }

    /*private List<AnalysisHintItem> parseAnalysisGuideJson(File source) {
        InputStream content = null;
        Gson gson = new Gson();
        try {
            content = new FileInputStream(source);
            Type type = new TypeToken<ArrayList<AnalysisHintItem>>() {
            }.getType();
            List<AnalysisHintItem> items = gson.fromJson(FileUtils.readFully(content, Utility.ENCODING), type);
            return items;
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
        } finally {
            FileUtils.closeStream(content);
        }
        return null;
    }*/

    /**
     * parse file json to object
     *
     * @param source: file source
     * @return AnalysisGuide object
     */
    private AnalysisGuide parseAnalysisGuideJson(File source) {
        InputStream content = null;
        Gson gson = new Gson();
        try {
            content = new FileInputStream(source);
            return gson.fromJson(FileUtils.readFully(content, Utility.ENCODING), AnalysisGuide.class);
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
        } finally {
            FileUtils.closeStream(content);
        }
        return null;
    }

}
