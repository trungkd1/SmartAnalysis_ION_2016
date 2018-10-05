package jp.co.fujixerox.sa.ion.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.dialogs.ConfirmDialogFragment;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.services.UploadReportService;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadImageTask;
import jp.co.fujixerox.sa.ion.sync.DownloadSampleSoundTask;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AbstractItemView;
import jp.co.fujixerox.sa.ion.views.AxisImageView;
import jp.co.fujixerox.sa.ion.views.AxisViewController;
import jp.co.fujixerox.sa.ion.views.ItemViewGenerator;
import jp.co.fujixerox.sa.ion.views.MediaPlayerController;

import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PLAY;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.STOP;

/**
 * 異音報告画面
 *
 * @author FPT
 */
public class ReportDetailScreenActivity extends AbstractFragmentActivity implements
        OnTouchListener {
    private static InputMethodManager sInputMethodManager;
    protected final String TAG = ReportDetailScreenActivity.class
            .getSimpleName();
    private AssetManager assetManager;
    private Button btnFinish;
    private DatabaseHelper databaseHelper;
    private AudioData mAudioData;
    private ItemViewGenerator itemViewGenerator;
    private ProgressDialog progressDialog;
    private String audioFilePath, imageFilePath;
    /**
     * Is old report was get from cloud (don't save in local database)
     */
    private boolean isOldReport = false;
    /**
     * Audio file name
     */
    private TextView tvFileName;

    /**
     * List formIds screen
     */
    private List<String> formIds;
    /**
     * directory for saving sound and analysis image file
     */
    private File saveCatalogDirectory;

    private DownloadSampleSoundTask downloadSampleSoundTask;
    private DownloadImageTask downloadAnalysisImageTask;
    /**
     * directory for save catalog file
     */

    private AxisImageView axisImageView;
    private AxisViewController mAxisViewController;
    private int mCurrentAnalysisTime;
    private SharedPreferences mSharedPreferences;
    private ToggleButton btnPlayOriginalAudio;
    private MediaPlayerController mediaPlayerController;

    /**
     * Show/Hide soft keyboard
     *
     * @param v
     * @param hasFocus
     */
    public static void showHideSoftInput(View v, boolean hasFocus) {
        if (hasFocus) {
            sInputMethodManager.showSoftInput(v, 0);
        } else {
            sInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_screen);
        mSharedPreferences = getSharedPreferences(Utility.SHARE_PREFERENCES.MY_PREFERENCES, MODE_PRIVATE);
        databaseHelper = DatabaseHelper.getInstance(this);
        sInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assetManager = this.getAssets();
        Intent intent = getIntent();
        //get audios data from previous activity
        mAudioData = intent.getParcelableExtra(Utility.EXTRA_INTENT.AUDIO_REPORT);

        isOldReport = intent.getBooleanExtra(Utility.EXTRA_INTENT.IS_OLD_REPORT, false);
        if (isOldReport) {
            /* Set productGroup name if it's null,
               because case the report was get from cloud is always not include product group.
             */
            String productName = mAudioData.getValueByFormId(ICloudParams.productname);
            String productGroup = JsonParser.findProductgroup(productName, getAssets());
            mAudioData.addOrUpdateAudioFromData(ICloudParams.productgroup, productGroup, null, null);
        }
        //setup view
        setupView();
    }

    /**
     * {@inheritDoc}
     */
    public void onDestroy() {
        super.onDestroy();
        //cancel get catalog task
        if (mediaPlayerController != null) {
            mediaPlayerController.release();
        }
    }

    @Override
    public void onBackPressed() {
        saveAudioReportData();
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == Utility.EXTRA_INTENT.REQUEST_CODE_SCAN_BARCODE) {
                String name = data.getStringExtra(Utility.EXTRA_INTENT.SERIAL_NO);
                String number = data.getStringExtra(Utility.EXTRA_INTENT.PRODUCT_NAME);

                 List<AudioFormData> audioFormDataList = itemViewGenerator.getAudioFormData();

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

    /**
     * setup view for layout
     */
    private void setupView() {
        btnPlayOriginalAudio = (ToggleButton) findViewById(R.id.btn_play_original_audio);
        tvFileName = (TextView) findViewById(R.id.tvFileName);
        axisImageView = (AxisImageView) findViewById(R.id.axisImageView);
        btnFinish = (Button) findViewById(R.id.btn_finish);
        setCatalogResult();

        List<Item> itemList = JsonParser.getListItems(
                Utility.ASSETS_JSON_PATH,
                Utility.JSON_FILE_NAME.AUDIO_CONDITIONS, assetManager);

        LinearLayout inputLayout = (LinearLayout) ReportDetailScreenActivity.this
                .findViewById(R.id.inputLayoutSection2);
        LinearLayout areacodeLayout = (LinearLayout) ReportDetailScreenActivity.this
                .findViewById(R.id.areacells);
        itemViewGenerator = new ItemViewGenerator(this);

        List<Item> checkItems = new ArrayList<>(itemList);
        for (Item item : checkItems){
            if (item.getFormid().equals(ICloudParams.cause) && item.getPattern().equals(Utility.INPUT_PATTERN.SELECT.name())){
                itemList.remove(item);
            }
            if(Utility.IS_BARCODE) {
                if(ICloudParams.productgroup.equals(item.getFormid())){
                    itemList.remove(item);
                }
            }else {
                if(item.getFormid().equals(ICloudParams.productname) && !(item.getInputtype().equals(Utility.INPUT_PATTERN.SELECT.name()))){
                    itemList.remove(item);
                }
            }
        }

        itemViewGenerator.setSectionInputLayout(formIds,itemList, inputLayout, areacodeLayout, mAudioData,
                new AbstractItemView.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(String formId, Value value) {
                        btnFinish.setEnabled(itemViewGenerator.isValidated());
                    }
                });
        btnFinish.setEnabled(itemViewGenerator.isValidated());
        initAxisView();
    }

    /**
     * setOnCompletedListener axis view
     */
    private void initAxisView() {
        audioFilePath = mAudioData.getSound();
        imageFilePath = mAudioData.getPicture();
        if (isOldReport) {
            initCatalogFolder();
            downloadAudioCatalogFollowedByDownloadingImage();
        } else {
            // don't show analysis image and play audio when audio file is not exist
            if (TextUtils.isEmpty(audioFilePath)) {
                findViewById(R.id.llImageAnalysis).setVisibility(View.GONE);
                return;
            }
            // in case of remote url
            if (URLUtil.isValidUrl(audioFilePath)) {
                initCatalogFolder();
                downloadAudioCatalogFollowedByDownloadingImage();
                return;
            }

            // local path
            tvFileName = (TextView) findViewById(R.id.tvFileName);
            Log.v(TAG, "MFILEPATH: " + audioFilePath);
            tvFileName.setText(audioFilePath.substring(audioFilePath
                    .lastIndexOf(Utility.ESCAPE_FORWARD_SLASH) + 1));

            axisImageView = (AxisImageView) findViewById(R.id.axisImageView);
            mAxisViewController = new AxisViewController(this, axisImageView, tvFileName);
            mCurrentAnalysisTime = mSharedPreferences.getInt(Utility.SHARE_PREFERENCES.KEY_ANALYSIS_TIME,
                    Utility.ANALYSIS_TIME_VALUES[2]);
            mAxisViewController.setAxisHorizontalValues(mCurrentAnalysisTime);
            if (!TextUtils.isEmpty(imageFilePath)) {
                Bitmap analysisImageBitmap = BitmapFactory.decodeFile(imageFilePath);
                mAxisViewController.setBitmap(analysisImageBitmap);
            }
            initPlayAudioController();
            refreshSelectedArea();
        }
    }

    private void downloadAudioCatalogFollowedByDownloadingImage() {
        if (!TextUtils.isEmpty(audioFilePath)) {
            showProgressDialog();
            downloadAudioCatalog();
        } else {
            CommonUtils.showToast(ReportDetailScreenActivity.this, R.string.no_sound, Gravity.TOP);
            enablePlayButton(false);
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void downloadAudioCatalog() {
        cancelTask(downloadSampleSoundTask);
        downloadSampleSoundTask = new DownloadSampleSoundTask(saveCatalogDirectory, new AsyncTaskCallback() {
            @Override
            public void onPrepare(PROGRESS_TYPE type) {
                enablePlayButton(false);
            }

            @Override
            public void onSuccess(Object object) {
                String audioPath = (String) object;
                Toast.makeText(ReportDetailScreenActivity.this, "Retrieve audio catalog successful", Toast.LENGTH_SHORT).show();
                //enable button play catalog
                if (mediaPlayerController == null) {
                    mediaPlayerController = new MediaPlayerController(ReportDetailScreenActivity.this, audioPath);
                } else {
                    mediaPlayerController.changeAudio(audioPath);
                }

                if (!TextUtils.isEmpty(imageFilePath)) {
                    downloadAnalysisImage(imageFilePath);
                } else {
                    CommonUtils.showToast(ReportDetailScreenActivity.this, R.string.no_image, Gravity.TOP);
                    closeProgressDialog();
                }
            }

            @Override
            public void onFailed(int errorMessageId) {
                //do nothing
                closeProgressDialog();
            }

            @Override
            public void onFinish(PROGRESS_TYPE loadingType) {
                //do nothing
            }

        });
        downloadSampleSoundTask.execute(audioFilePath);
    }

    private void cancelTask(AsyncTask task) {
        if (task != null && !task.isCancelled()) {
            Log.d(TAG, "cancel download sample sound task");
            task.cancel(true);
        }
    }

    private void enablePlayButton(boolean enabled) {
        btnPlayOriginalAudio.setEnabled(enabled);
    }

    private void downloadAnalysisImage(String url) {
        cancelTask(downloadAnalysisImageTask);
        downloadAnalysisImageTask = new DownloadImageTask(saveCatalogDirectory, new AsyncTaskCallback() {
            @Override
            public void onPrepare(PROGRESS_TYPE type) {

            }
            @Override
            public void onSuccess(Object object) {
                String imageFilePath = (String) object;
                Log.v(TAG, "MFILEPATH: " + imageFilePath);
                tvFileName.setText(imageFilePath.substring(imageFilePath
                        .lastIndexOf(Utility.ESCAPE_FORWARD_SLASH) + 1));
                mAxisViewController = new AxisViewController(ReportDetailScreenActivity.this, axisImageView, tvFileName);
                mCurrentAnalysisTime = mSharedPreferences.getInt(Utility.SHARE_PREFERENCES.KEY_ANALYSIS_TIME,
                        Utility.ANALYSIS_TIME_VALUES[2]);
                mAxisViewController.setAxisHorizontalValues(mCurrentAnalysisTime);
                if (!TextUtils.isEmpty(imageFilePath)) {
                    Bitmap analysisImageBitmap = BitmapFactory.decodeFile(imageFilePath);
                    mAxisViewController.setBitmap(analysisImageBitmap);
                }
                initPlayAudioController();
                refreshSelectedArea();
                closeProgressDialog();
                enablePlayButton(true);
            }

            @Override
            public void onFailed(int errorMessageId) {
                closeProgressDialog();
            }

            @Override
            public void onFinish(PROGRESS_TYPE loadingType) {
                //do nothing
            }

        });
        downloadAnalysisImageTask.execute(url);
    }

    private void refreshSelectedArea() {
        //read json selected area
        String selectedRecF = mAudioData.getSelectPoints();
        if (selectedRecF != null) {
            PointF[] rectF = JsonParser.gson.fromJson(selectedRecF, PointF[].class);
            axisImageView.setSelectedAreaRect(rectF[0], rectF[1]);
            axisImageView.setDrawCircle(false);
        }
    }

    /**
     * setOnCompletedListener all view by resource id
     */
    private void initPlayAudioController() {
        //handle event for button
        btnPlayOriginalAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlayOriginalAudio.isChecked()) {
                    /* DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                            getString(R.string.action_button_press), getString(R.string.label_play)); //音声再生開始 160407 mit*/
                    //register media player
                    if (mediaPlayerController == null) {
                        mediaPlayerController = new MediaPlayerController(view.getContext(), mAudioData.getSound());
                    }
                    axisImageView.startSeek(Utility.AUDIO_DURATION, null);
                    mediaPlayerController.onClick(PLAY);
                    mediaPlayerController.setOnCompletedListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.v(TAG, "audio play finish");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnPlayOriginalAudio.setChecked(false); //Click for stop
                                }
                            });
                        }
                    });
                } else {
                    mediaPlayerController.onClick(STOP);
                    axisImageView.stopSeek();
                }
            }
        });
    }


    /**
     * setup cause parts and methods if exists
     */
    private void setCatalogResult() {
        Intent intent = getIntent();
        ArrayList<String> inputFormItemListCause;
        ArrayList<String> inputFormItemListMethod;
        formIds= new LinkedList<>(Arrays.asList(ICloudParams.reportFormIds));
        boolean isFromCompareScreen = intent.getBooleanExtra(Utility.EXTRA_INTENT.HAS_CATALOG_INFO, false);
        if (isFromCompareScreen) {
            //cause
            inputFormItemListCause = intent.getStringArrayListExtra(Utility.EXTRA_INTENT.CATALOG_CAUSE_PARTS);
            String inputFormJson = JsonParser.makeJsonStringArray(inputFormItemListCause);
            mAudioData.setCasuseJsonForm(inputFormJson);
            //method
            inputFormItemListMethod = intent.getStringArrayListExtra(Utility.EXTRA_INTENT.CATALOG_METHODS);
            inputFormJson = JsonParser.makeJsonStringArray(inputFormItemListMethod);
            mAudioData.setMethodJsonForm(inputFormJson);

        } else {
            inputFormItemListCause = JsonParser.makeArrayListOfString(mAudioData.getCasuseJsonForm());
            inputFormItemListMethod = JsonParser.makeArrayListOfString(mAudioData.getMethodJsonForm());
        }

        if(inputFormItemListCause != null && inputFormItemListCause.size() != 0  ) {
                formIds.add(ICloudParams.cause);
                AudioFormData audioFormData = new AudioFormData();
                audioFormData.setFormid(ICloudParams.cause);
                audioFormData.setValue(mAudioData.getCause());
//            mAudioData.getListAudioFormData().add(audioFormData);
        }
        if(inputFormItemListMethod != null && inputFormItemListMethod.size() != 0) {
                formIds.add(ICloudParams.method);
                AudioFormData audioFormData = new AudioFormData();
                audioFormData.setFormid(ICloudParams.method);
                audioFormData.setValue(mAudioData.getMethod());
//            mAudioData.getListAudioFormData().add(audioFormData);
        }

        AudioFormData audioFormData = new AudioFormData();
        audioFormData.setFormid(ICloudParams.result);
        audioFormData.setValue(mAudioData.getResult());
//        mAudioData.getListAudioFormData().add(audioFormData);

    }

    /**
     * Update audio data.
     * @return true if updated
     */
    private boolean updatedAudioData() {
        boolean result = false;
        List<AudioFormData> editAudioFromDataList = itemViewGenerator.getAudioFormData();
//        Collection<AudioFormData> diff = mAudioData.getDifference(editAudioFromDataList);
//        String editResult = getTreatmentResult();

        if (editAudioFromDataList == null) {
            //do nothing
        } else {
            for (AudioFormData dataDiff : editAudioFromDataList) {
                AudioFormData audioFormData = mAudioData.getAudioFormDataByFormId(dataDiff.getFormid());
                if (audioFormData != null) {
                    audioFormData.setValue(dataDiff.getValue());
                } else {
                    mAudioData.addOrUpdateAudioFromData(dataDiff.getFormid(), dataDiff.getValue(),dataDiff.getText(),dataDiff.getMimeType());
                }
            }
            result = true;
        }
        return result;
    }


    /**
     * check validate input data
     *
     * @return List<AudioFormData>
     */
    public List<AudioFormData> checkInputValidated() {
        if (itemViewGenerator.isValidated()) {
            btnFinish.setEnabled(true);
            return itemViewGenerator.getAudioFormData();
        } else {
            btnFinish.setEnabled(false);
            return null;
        }
    }

    /**
     * save audios data to database
     */
    private boolean saveAudioReportData() {
        if (isOldReport) {
            //don't save old report was get from cloud
            return true;
        }
        List<AudioFormData> dataList = checkInputValidated();
        if (dataList == null || dataList.isEmpty()) {
            return false;
        } else {
            setTreatmenResult(dataList);
            mAudioData.setListAudioFormData(dataList);
            databaseHelper.saveAudioData(mAudioData, false);
        }
        return true;
    }

    private void setTreatmenResult( List<AudioFormData> dataList){
        for (AudioFormData audioFormData : dataList){
            if(ICloudParams.result.equals(audioFormData.getFormid())){
                mAudioData.setResult(audioFormData.getValue());
            }else if(ICloudParams.method.equals(audioFormData.getFormid())){
                mAudioData.setMethod(audioFormData.getValue());
            }else if(ICloudParams.cause.equals(audioFormData.getFormid())){
                mAudioData.setCause(audioFormData.getValue());
            }
        }
    }

    private void setTreatmeCause( List<AudioFormData> dataList){
        for (AudioFormData audioFormData : dataList){
            if(ICloudParams.result.equals(audioFormData.getFormid())){
                mAudioData.setResult(audioFormData.getValue());
            }
        }
    }

    private String getTreatmentResult(){
        return null;
    }


    /**
     * send report data to cloud
     *
     * @param v
     */
    public void finishReport(View v) {
        sendTrackingButtonPress((Button) v);
        if (isOldReport) {
            checkOldReportHasChanged();
        } else {
            //finish report
            if (saveAudioReportData()) {
                if (CloudConnector
                        .isConnectingToInternet(ReportDetailScreenActivity.this)) {
                    Intent intent = getIntent();
                    intent.setClass(this, UploadReportService.class);
                    intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT,
                            mAudioData);
                    startService(intent);
                } else {
                    //put audios id to list audios in sharedPreference
                    CommonUtils.putAudiosIdToListAudiosPending(this, String.valueOf(mAudioData.getId()));
                }
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * Check old report has changed and send if user accepted.
     */
    private void checkOldReportHasChanged() {
        boolean hasChanged = updatedAudioData();
        if (hasChanged) {
            ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
            confirmDialogFragment.setMessage(getString(R.string.message_send_changed_data));
            confirmDialogFragment.setOnButtonClickListener(new ConfirmDialogFragment.OnButtonClickListener() {
                @Override
                public void onOK() {
                    if (CloudConnector
                            .isConnectingToInternet(ReportDetailScreenActivity.this)) {
                        Intent intent = getIntent();
                        intent.setClass(getApplicationContext(), UploadReportService.class);
                        intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT,
                                mAudioData);
                        startService(intent);
                    }
                    finish();
                    setResult(RESULT_OK);
                }

                @Override
                public void onCancel() {
                    finish();
                    setResult(RESULT_OK);
                }
            });
            confirmDialogFragment.show(getSupportFragmentManager().beginTransaction(), TAG);
        } else {
            finish();
            setResult(RESULT_OK);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            showHideSoftInput(v, false);
        }
        return false;
    }

    /**
     * make folder to store all data of catalog
     */
    private void initCatalogFolder() {
        try {
            FileUtils.deleteFileOrFolder(new File(
                    Utility.APP_REPORT_FOLDER_NAME_SDCARD));
            FileUtils.createFolder(ReportDetailScreenActivity.this.getExternalFilesDir(null)
                    + Utility.APP_REPORT_FOLDER_NAME_SDCARD);
            saveCatalogDirectory = new File(ReportDetailScreenActivity.this.getExternalFilesDir(null)
                    + Utility.APP_REPORT_FOLDER_NAME_SDCARD);
        } catch (Exception ex) {
            Log.e(TAG, "Error when create folder catalog", ex);
        }
    }

}
