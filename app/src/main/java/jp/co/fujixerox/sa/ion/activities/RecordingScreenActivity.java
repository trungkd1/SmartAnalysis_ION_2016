package jp.co.fujixerox.sa.ion.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.dialogs.RecordConfirmDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.CompareGuideDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.VolumeDialogFragment;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.gps.GPSTracker;
import jp.co.fujixerox.sa.ion.services.UploadReportService;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.RecordUtils;
import jp.co.fujixerox.sa.ion.utils.UIHelper;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AbstractItemView;
import jp.co.fujixerox.sa.ion.views.ItemViewGenerator;

/**
 * 録音画面
 *
 * @author TrungKD
 */
public class RecordingScreenActivity extends AbstractFragmentActivity implements
        OnTouchListener {

    private static InputMethodManager sInputMethodManager;
    protected final String TAG = RecordingScreenActivity.class.getSimpleName();
    private AudioData mAudioData;
    double latitude;
    double longitude;
    private Button btnStartRecording;
    private DatabaseHelper databaseHelper;
    private String mAudioFileName;
    private List<AudioFormData> audioFormDataList = null;
    private AssetManager assetManager;
    private String recordError = null;
    private boolean reRecordFlag = false;
    /**
     * Show dialog report
     */
    private boolean isOther = false;
    private Dialog dialog = null;
    /**
     * volume controls
     */
    private VolumeDialogFragment volumeControls;
    /**
     * record task: implement recording audio
     */
    RecordAsyncTask recordAudioTask;
    private ItemViewGenerator itemViewGenerator;
    private TimerTask firtHaftTimerTask;
    private TimerTask lastHaftTimerTask;

    public static void showHideSoftInput(View v, boolean hasFocus) {
        if (hasFocus) {
            sInputMethodManager.showSoftInput(v, 0);
        } else {
            sInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_screen);
        mAudioData = getIntent().getParcelableExtra(Utility.EXTRA_INTENT.AUDIO_REPORT);
        assetManager = this.getAssets();
        databaseHelper = DatabaseHelper.getInstance(this);
        if (mAudioData != null) {
            mAudioFileName = mAudioData.getSound();
            deleteAnalysisImage(mAudioData.getPicture());  //再録音時は過去の音声を削除 160408 mit
            deleteAudioRecord(); //音声も削除 160408 mit
            mAudioData.setPicture(null);
            mAudioData.setSound(null);
            Log.d(TAG, "Rerec:" + mAudioData.getSound());
            databaseHelper.saveAudioData(mAudioData, false); //ファイルを削除した情報を更新 160411 mit
            reRecordFlag = true;  //再録音フラグ 160411 mit
            sendTrackingRecording(this, true);
        } else sendTrackingRecording(this, false);

        setupView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == Utility.EXTRA_INTENT.REQUEST_CODE_SCAN_BARCODE) {
                String name = data.getStringExtra(Utility.EXTRA_INTENT.SERIAL_NO);
                String number = data.getStringExtra(Utility.EXTRA_INTENT.PRODUCT_NAME);
                if (audioFormDataList == null) {
                    audioFormDataList = itemViewGenerator.getAudioFormData();
                }
                for ( AbstractItemView itemView :itemViewGenerator.geAllItemViews()){
                    if(ICloudParams.serialid.equals(itemView.getFormId())){
                       itemView.setValue(data.getStringExtra(Utility.EXTRA_INTENT.SERIAL_NO));
                    }
                    if(ICloudParams.productname.equals(itemView.getFormId())){
                        itemView.setValue(data.getStringExtra(Utility.EXTRA_INTENT.PRODUCT_NAME));
                    }
                }
            }else {
                setResult(RESULT_OK);
                finish(); // back to front activity
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        //check if record task is running when cancel task
        if (checkRecordTaskIsRunning()) {
//            recordAudioTask.cancel(true);
            recordAudioTask.cancelRecord();
        }

        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
            //Reset audio data
            mAudioData = null;
        }
    }

    private boolean checkRecordTaskIsRunning() {
        return recordAudioTask != null && recordAudioTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /**
     * setup view
     */
    private void setupView() {
        // set record input layout
        sInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        btnStartRecording = (Button) findViewById(R.id.btn_start_recording);
        setupInputsArea();
    }

    public void startRecording(View v) {
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.action_button_press),
                TAG + ":" + getString(R.string.start_recording),
                CommonUtils.getStringPreferences(RecordingScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
        cancelAllSendingAudioData();
        //get data and start recording
        audioFormDataList = itemViewGenerator.getAudioFormData();
        //start recording
        startRecord();
    }

    /**
     * Cancel all sending report when record audio start
     */
    private void cancelAllSendingAudioData() {
        Intent intent = new Intent();
        intent.setClass(this, UploadReportService.class);
        intent.putExtra(Utility.EXTRA_INTENT.CANCEL_SENDING_REPORT,
                true);
        startService(intent);
    }

    /**
     * Resume send report task when record audio finish
     */
    private void resumeAllSendingAudioData() {
        Intent intent = new Intent();
        intent.setClass(this, UploadReportService.class);
        intent.putExtra(Utility.EXTRA_INTENT.RESUME_SENDING_REPORT,
                true);
        startService(intent);
    }

    /**
     * change status of button start recoding
     *
     * @param enable: true when enable button else disable button
     */
    private void enableButtonStartRecording(boolean enable) {
        btnStartRecording.setEnabled(enable);
    }

    private void setupInputsArea() {
        List<Item> mItems = JsonParser.getListItems(
                Utility.ASSETS_JSON_PATH,
                Utility.JSON_FILE_NAME.AUDIO_CONDITIONS, assetManager);
        if (mItems == null) {
            Log.e(TAG, "@@Items is null!!");
            return;
        }
        List<Item> checkItems = new ArrayList<>(mItems);

            for (Item item:checkItems) {
                if(Utility.IS_BARCODE) {
                    if(ICloudParams.productgroup.equals(item.getFormid())){
                        mItems.remove(item);
                    }
                }else {
                    if(item.getFormid().equals(ICloudParams.productname) && !(item.getInputtype().equals(Utility.INPUT_PATTERN.SELECT.name()))){
                        mItems.remove(item);
                    }
                }
            }


        GPSTracker gps = new GPSTracker(RecordingScreenActivity.this);

        // check if GPS enabled
        if (gps.canGetLocation) {
            latitude = gps.getLatitude();
            latitude = Math.round(latitude * 10000.0) / 10000.0;
            Log.i(TAG, "Latitude: " + latitude);
            longitude = gps.getLongitude();
            longitude = Math.round(longitude * 10000.0) / 10000.0;
            Log.i(TAG, "Longitude: " + longitude);

        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        LinearLayout inputSectionLayout = (LinearLayout) RecordingScreenActivity.this
                .findViewById(R.id.inputLayoutSection);
        LinearLayout areaSectionLayout = (LinearLayout) RecordingScreenActivity.this
                .findViewById(R.id.areacells);
        itemViewGenerator = new ItemViewGenerator(this);

        AbstractItemView.OnValueChangedListener listener = new AbstractItemView.OnValueChangedListener() {
            @Override
            public void onValueChanged(String formId, Value value) {
                if(ICloudParams.productgroup.equals(formId)){
                    if(value.getValue().equals(ICloudParams.other)){
                        isOther = true;
                    }else {
                        isOther = false;
                    }
                }
                boolean isAllValidated = itemViewGenerator.isValidated();
                enableButtonStartRecording(isAllValidated);
            }
        };
        itemViewGenerator.setSectionInputLayout(Arrays.asList(ICloudParams.recordFormIds),mItems, inputSectionLayout, areaSectionLayout, mAudioData, listener);
        if (reRecordFlag == true) { //再録音フラグtrueの時は起動UI更新後にデータ削除 フラグfalse 160411 mit
            databaseHelper.deleteAudioData(mAudioData.getId());
            reRecordFlag = false;
            mAudioData = null;
        }
    }


    /**
     * Begin record
     */
    private void startRecord() {
        recordAudioTask = new RecordAsyncTask();
        recordAudioTask.execute();
    }

    /**
     * insert information of audio in database
     */
    private void onRecordingSuccessful(String audioFileName) {
        //save audio data
        saveAudioData(audioFileName);
        // clean folder store file report: delete audio, analysis image file out of file need keep
        cleanFolderStoreFileReport();
    }

    /**
     * save audio data
     *
     * @param audioFileName: audio file name
     */
    private void saveAudioData(String audioFileName) {
        if (mAudioData == null) {
            mAudioData = new AudioData();
        }
        mAudioData.setRecordDate(System.currentTimeMillis());
        mAudioData.setLatitude(String.valueOf(latitude));
        mAudioData.setLongitude(String.valueOf(longitude));
        mAudioData.setSound(audioFileName);

        if (audioFormDataList == null || audioFormDataList.isEmpty()) {
            return;
        } else {
            mAudioData.setListAudioFormData(audioFormDataList);
        }
        //save and setup max size audio data
        databaseHelper.saveAudioData(mAudioData, true);
    }

    /**
     * clean folder store file report: delete file out of range
     */
    private void cleanFolderStoreFileReport() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //delete oldest audio record
                deleteOldestAudioRecordOverMax();
                //delete oldest analysis image
                deleteOldestAnalysisImageOverMax();
            }
        });
        thread.start();
    }

    /**
     * delete oldest analysis image over max
     */
    private void deleteOldestAnalysisImageOverMax() {
        String lstAnalysisImageKeptStr = getAnalysisImageFileNameKept();
        deleteFileOutOfListKept(lstAnalysisImageKeptStr, false);
    }

    /**
     * get list analysis image file name that need keeping
     *
     * @return list analysis image file name that need keeping
     */
    private String getAnalysisImageFileNameKept() {
        StringBuilder lstAnalysisImageKept = new StringBuilder();
        List<AudioData> lstAudioData = databaseHelper.getAudiosData(
                AudioData.COLUMN_RECORD_DATE, -1, true, true);
        if (lstAudioData == null || lstAudioData.size() == 0) {
            return Utility.EMPTY_STRING;
        }
        for (AudioData audioData : lstAudioData) {
            //get analysis image file name that need keeping in a string builder
            String analysisImageFilePath = audioData.getPicture();
            if (!TextUtils.isEmpty(analysisImageFilePath)) {
                String analysisImageFileName = analysisImageFilePath.substring(analysisImageFilePath.lastIndexOf(File.separator) + File.separator.length());
                Log.i(TAG, "ANALYSIS_SCREEN IMAGE NAME EXTRACTED: " + analysisImageFilePath + "|" + analysisImageFileName);
                lstAnalysisImageKept.append(analysisImageFileName).append(Utility.CHARACTERS_SEPARATE);
            }
        }
        return lstAnalysisImageKept.toString();
    }

    /**
     * delete audio file that isn't belong list audios file name
     *
     * @param lstFileNameKeptStr: String list file name need keeping
     */
    private void deleteFileOutOfListKept(String lstFileNameKeptStr, boolean isAudioFile) {
        //list file name in store folder
        String audioRecorderFolder = RecordUtils.getAudioRecorderFolder(RecordingScreenActivity.this);
        File audioRecorderFile = new File(audioRecorderFolder);
        if (audioRecorderFile.exists() && audioRecorderFile.isDirectory()) {
            File[] files = audioRecorderFile.listFiles();
            if (checkAudioFilesIsInLimited(files, isAudioFile)) {
                return;
            }
            for (File file : files) {
                String filePath = file.getAbsolutePath();
                String fileName = file.getName();
                //check is correct file type: is not correct file type when continue next loop
                if (!isCorrectFileType(fileName, isAudioFile)) {
                    continue;
                }
                Log.i(TAG, "@@@FILE IN AUDIO RECORDER FOLDER: " + fileName + "|" + filePath);
                if (isNotKeptFile(lstFileNameKeptStr, fileName)) {
                    Log.i(TAG, "@@@FILE DELETED " + fileName + "|" + lstFileNameKeptStr);
                    FileUtils.deleteFileOrFolder(file);
                    databaseHelper.clearAudioFileName(fileName);
                }
            }
        }
    }

    /**
     * check file is not kept file
     *
     * @param lstFileNameKeptStr: list file name need keep
     * @param fileName:           file name to check
     * @return true if is not skipped file
     */
    private boolean isNotKeptFile(String lstFileNameKeptStr, String fileName) {
        return TextUtils.isEmpty(lstFileNameKeptStr) || !lstFileNameKeptStr.contains(fileName);
    }

    /**
     * check file is correct file type (wav or png)_
     *
     * @param fileName:    File Name
     * @param isAudioFile: boolean detect is audio file
     * @return true if is correct file type
     */
    private boolean isCorrectFileType(String fileName, boolean isAudioFile) {
        if (isAudioFile) {
            return fileName.endsWith(Utility.FILE_EXT_WAV);
        } else {
            return fileName.endsWith(Utility.FILE_EXT_PNG);
        }
    }

    private boolean checkAudioFilesIsInLimited(File[] files, boolean isAudioFile) {
        if (isAudioFile) {
            return (files == null || files.length <= Utility.AUDIO_MAX_NUMBER_2);
        } else {
            return (files == null || files.length <= Utility.REPORTS_MAX_NUMBER);
        }
    }

    /**
     * handling when barcode
     */
    public void gotoScanBarCodeScreen() {
            Intent intent = getIntent();
            intent.setClass(getBaseContext(), ScanBarCodeActivity2.class);
            startActivityForResult(intent,Utility.EXTRA_INTENT.REQUEST_CODE_SCAN_BARCODE);
    }

    /**
     * handling when recording done
     */
    private void gotoAnalysisScreen() {
        Intent intent = getIntent();
        intent.setClass(getBaseContext(),
                AnalysisScreenActivity.class);
        intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, mAudioData);
        startActivityForResult(intent, 1);
    }

    /**
     * handling when recording done (phone only)
     */
    private void gotoReportScreen() {
        Intent intent = getIntent();
        intent.setClass(getBaseContext(),
                ReportDetailScreenActivity.class);
        intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, mAudioData);
        startActivityForResult(intent, 1);
    }

    /**
     * show dialog Compare Catalog guide
     *
     * @param v: View
     */
    public void onClickShowHint(View v) {
        sendTrackingButtonPress(getString(R.string.label_hint));
        if (UIHelper.canExecuteClickEvent()) {
            doShowHint();
        }
    }

    private void doShowHint() {
        AssetManager assetManager = getAssets();
        List<String> listCatalogHtml = listCatalogHtml(assetManager, Utility.ASSETS_COMPARE_GUIDE_HTML_PATH);
        //機種対応 160306 mitsuha
        if (listCatalogHtml == null) {
            Toast.makeText(RecordingScreenActivity.this, R.string.not_support_device, Toast.LENGTH_LONG).show();
            return;
        }
        CompareGuideDialogFragment dialogFragment = CompareGuideDialogFragment.newInstance(listCatalogHtml);
        dialogFragment.show(getSupportFragmentManager(), CompareGuideDialogFragment.TAG);
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.action_show_dialog),
                getString(R.string.label_hint_dialog),
                CommonUtils.getStringPreferences(RecordingScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            showHideSoftInput(v, false);
        }
        return false;
    }

    /**
     * list all catalog html in asset
     *
     * @param assetManager: AssetManager
     * @param assetPath:    html asset path
     * @return list catalog html
     */
    private List<String> listCatalogHtml(AssetManager assetManager, String assetPath) {
        String[] listHtml = null;
        try {
            //機種対応 160307 mitsuha
            assetPath = assetPath + "/" + Build.MANUFACTURER + "/" + Build.MODEL;
            Log.d("TAG", assetPath);
            listHtml = assetManager.list(assetPath);
        } catch (IOException ex) {
            Log.e(TAG, "@@Error when list file in asset path: " + assetPath, ex);
        }
        if (listHtml == null || listHtml.length == 0) {
            return null;
        }
        List<String> htmls = new ArrayList<>();
        Collections.addAll(htmls, listHtml);
        return htmls;
    }

    /**
     * Record pcm audio asynctask
     */
    private class RecordAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private static final int secondWaitingStopRecord = 9; // max count
        private static final int secondMinimumStopRecord = 4; // minimum count
        Button btnCancelRecord;
        Button btnStopRecord;
        ProgressBar progressBar;
        TextView txtProgress;
        TextView txtExplainRecording;
        private boolean isStopError = false;
        private int count = 1;
        private boolean isCancelRecord = false;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(RecordingScreenActivity.this);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_record);

            btnCancelRecord = (Button) dialog
                    .findViewById(R.id.btn_cancelRecord);
            btnStopRecord = (Button) dialog.findViewById(R.id.btn_stopRecord);
            progressBar = (ProgressBar) dialog
                    .findViewById(R.id.progressRecording);
            txtProgress = (TextView) dialog.findViewById(R.id.txtProgress);
            txtExplainRecording = (TextView) dialog.findViewById(R.id.txt_explain_recording);
            progressBar.setIndeterminate(true);
            btnCancelRecord.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    RecordUtils.stopRecording();
                    isCancelRecord = true;
                    dialog.dismiss();
                    if (firtHaftTimerTask != null) {
                        firtHaftTimerTask.cancel();
                    }
                    if (lastHaftTimerTask != null) {
                        lastHaftTimerTask.cancel();
                    }

                }
            });
            btnStopRecord.setSoundEffectsEnabled(false);
            btnStopRecord.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(secondWaitingStopRecord);
                    btnStopRecord.setClickable(false);
                    btnStopRecord.setEnabled(false);
                    btnStopRecord.setTextColor(getResources().getColor(R.color.rgb_grey));
//                    count = secondMinimumStopRecord;
                    firtHaftTimerTask.cancel();
                    progressBar.setProgress(count);
                    Log.d(TAG, "@@@ STOP BUTTON PRESSED");
                    lastHaftTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Log.d(TAG, "@@@ STOP COUNT " + count);
                            publishProgress(count);
                            if (count >= secondWaitingStopRecord) {
                                cancel();
                                RecordUtils.stopRecording();
                            }
                            count++;
                        }
                    };
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(lastHaftTimerTask, 1000, 1000);
                }
            });
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = RecordUtils.startRecording();
            Log.i(TAG, "result callback: " + result);
            if (result) {
                firtHaftTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Log.v(TAG, "@@COUNT DIALOG: " + count);
                        if (count <= secondMinimumStopRecord) {
                            publishProgress(count++);
                        } else {
                            cancel();
                        }
                    }
                };
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(firtHaftTimerTask, 1000, 1000);
                Log.v(TAG, "start to record");
                // loop to read data
                RecordUtils.readRecordStream();
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values == null || values.length <= 0)
                return;
            int count = values[0];
            txtProgress.setText(String.format("%d/9", count));
            progressBar.setProgress(count);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            resumeAllSendingAudioData();
            dialog.dismiss();
            if (isCancelRecord) {
                return;
            }
            if (isStopError) {
                Toast.makeText(RecordingScreenActivity.this,
                        "Need minimum 4 seconds recording before click Stop",
                        Toast.LENGTH_SHORT).show();
            }
            if (recordError != null) {
                RecordUtils.stopRecording();
                dialog.dismiss();
                Toast.makeText(RecordingScreenActivity.this, recordError,
                        Toast.LENGTH_SHORT).show();
                startRecord();
                return;
            }
            // write audio stream to file
            String audioFileName = RecordUtils
                    .writeFile(RecordingScreenActivity.this);
            // 1. Check has recording successful, if has record successful goto 2
            // 2. Check audio record has exist before, if exist goto 3
            // 3. Delete audio record before. Write new audio record
            if (checkHasRecordingSuccess(audioFileName)) {
                //check audio exist, if audio is exist when overwrite
                if (checkAudioRecordExist()) {
                    deleteAudioRecord();
                }
                mAudioFileName = audioFileName;
                Log.d(TAG, "@@@ record filename:" + mAudioFileName);
                //handle on recording successful
                onRecordingSuccessful(audioFileName);
                //show fragment dialog after recording
                showAfterRecordingDialog();
            }

        }

        public void cancelRecord() {
            btnCancelRecord.performClick();
            cancel(true);
        }
    }

    private boolean checkHasRecordingSuccess(String audioFileName) {
        return !(TextUtils.isEmpty(audioFileName) || audioFileName.length() < 4);
    }

    private void deleteAudioRecord() {
        FileUtils.deleteFileOrFolder(new File(mAudioFileName));
    }

    private void deleteAnalysisImage(String fname) {
        FileUtils.deleteFileOrFolder(new File(fname));
    }

    /**
     * delete oldest audio record over max
     */
    private void deleteOldestAudioRecordOverMax() {
        String lstAudioFileNamesKeep = databaseHelper.getListKeptAudioFileName();
        lstAudioFileNamesKeep = addNewestAudioFileName(lstAudioFileNamesKeep);
        deleteFileOutOfListKept(lstAudioFileNamesKeep, true);
    }

    /**
     * add newest audio file name to list audio file name need keep
     *
     * @param lstAudioFileNamesKeep: string list audio file naem keep
     * @return new list audio file name keep include newest audio file name
     */
    private String addNewestAudioFileName(String lstAudioFileNamesKeep) {
        if (mAudioData == null) {
            return lstAudioFileNamesKeep;
        }
        return new StringBuilder(lstAudioFileNamesKeep).append(Utility.CHARACTERS_SEPARATE).append(mAudioData.getSound()).toString();

    }

    private boolean checkAudioRecordExist() {
        return mAudioData != null && mAudioData.getId() > 0;
    }

    /**
     *
     */
    private void showVolumeDialog() {
        if (volumeControls == null) {
            volumeControls = new VolumeDialogFragment();
        }
        volumeControls.show(getSupportFragmentManager(), VolumeDialogFragment.TAG);
        //Tracking Event
        /*DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                getString(R.string.action_show_dialog), getString(R.string.volume)); 160407 mit */
    }

    /**
     * show after recording dialog
     */
    private void showAfterRecordingDialog() {
        //create after recording dialog
        RecordConfirmDialogFragment dialogFragment = new RecordConfirmDialogFragment();
        //set parameter for Dialog Fragment
        Bundle b = new Bundle();
        b.putBoolean(ICloudParams.other,isOther);
        dialogFragment.setArguments(b);
        //important: need set audio file path for fragment to play audio
        dialogFragment.setAudioFilePath(mAudioFileName);
        dialogFragment.setListener(new RecordConfirmDialogFragment.HandleEventAfterFinishDialogListener() {
            @Override
            public void onClickBeginAnalysis() {
                DefaultApplication.getInstance().trackEvent(
                        getString(R.string.action_button_press),
                        TAG + ":" + getString(R.string.begin_analysis),
                        CommonUtils.getStringPreferences(RecordingScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
                //goto analysis screen
                gotoAnalysisScreen();
            }

            @Override
            public void onClickVolume() {
                showVolumeDialog();
            }

            @Override
            public void onClickRecordAgain() {
                DefaultApplication.getInstance().trackEvent(
                        getString(R.string.action_button_press),
                        TAG + ":" + getString(R.string.record_again),
                        CommonUtils.getStringPreferences(RecordingScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
                //start recording
                startRecording(null);
            }

            @Override
            public void onClickCreateReport() {
                DefaultApplication.getInstance().trackEvent(
                        getString(R.string.action_button_press),
                        TAG + ":" + getString(R.string.create_report),
                        CommonUtils.getStringPreferences(RecordingScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
                gotoReportScreen();
            }
        });

        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), RecordConfirmDialogFragment.TAG);

        //Tracking Event
        /*DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                getString(R.string.action_show_dialog), getString(R.string.title_dialog_after_recording)); 160407 mit*/
    }

}
