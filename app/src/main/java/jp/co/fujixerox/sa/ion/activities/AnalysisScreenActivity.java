package jp.co.fujixerox.sa.ion.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ionanalyzelib.IonAnalyzeLib;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.dialogs.AnalysisGuideDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.AudiosListDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.ConfirmDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.SettingDialogFragment;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.UIHelper;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AxisImageView;
import jp.co.fujixerox.sa.ion.views.AxisViewController;
import jp.co.fujixerox.sa.ion.views.MediaPlayerController;
import jp.co.fujixerox.sa.ion.views.MediaPlayerController.PlayCallback;

import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PAUSE;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PLAY;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.STOP;

/**
 * 異音解析画面<br>
 * 音声ファイルを解析し、解析画像を表示します。<br>
 * 録音ファイルを再生しながら解析画像に沿ってシークバー表示します。<br>
 * 縦軸の表示単位はKHzです。(0, 5, 10, 15, 20, 22)
 * 横軸の表示単位は秒です。(0, 1, 2, 3, 4, 5)
 *
 * @author TrungKD
 */
public class AnalysisScreenActivity extends AbstractFragmentActivity {

    protected static final String TAG = AnalysisScreenActivity.class
            .getSimpleName();
    private int mCurrentFFT, mCurrentAnalysisTime, mCurrentStepWidth;
    private ToggleButton ib_play;
    private ImageButton ib_stop;
    private AxisViewController mAxisViewController = null;
    private TextView tvFileName;
    private DatabaseHelper databaseHelper;
    private IonAnalyzeLib ionAnalyzeLib;
    private boolean hasAnalysisAudio = false;
    private SharedPreferences mSharedPreferences;
    private Gson mGon = new Gson();  //utility gson
    private AudioData mAudioData;
    private AxisImageView axisImageView;
    private MediaPlayerController mMediaPlayerController;
    private AudiosListDialogFragment audioListDialog;
    private Handler showingDialogHandler;
    /**
     * boolean detect start activity from start screen
     */
    private boolean isFromStartScreen = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showingDialogHandler = new Handler();
        mSharedPreferences = getSharedPreferences(Utility.SHARE_PREFERENCES.MY_PREFERENCES, MODE_PRIVATE);
        databaseHelper = DatabaseHelper.getInstance(this);
        ionAnalyzeLib = new IonAnalyzeLib(this);
        setContentView(R.layout.activity_analysis_screen);
        readAudioData();
        readSettingParameters();
        initAxisView();
        if (!isTablet()) {
            // hide compare button in phone device
            findViewById(R.id.ib_compare).setVisibility(View.GONE);
        }

        //tracking event 160405 mit
        sendTrackingAnalysis(this, getIntent().getBooleanExtra(
                Utility.EXTRA_INTENT.FROM_START_SCREEN, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayerController != null) {
            mMediaPlayerController.onClick(STOP);
        }
    }

    /**
     * Check null, audio data is exist
     *
     * @return true if NULL or otherwise
     */
    private boolean checkAudioDataExist() {
        return mAudioData != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(resultCode, data);
            finish();
        } else if (resultCode == RESULT_CANCELED) {
            if (isFromStartScreen) {
                showingDialogHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        audioListDialog.show(getSupportFragmentManager(), "record list");
                    }
                }, 200);
            }
        }
    }

    /**
     * Read saved audio data from database
     */
    private void readAudioData() {
        mAudioData = getIntent().getParcelableExtra(Utility.EXTRA_INTENT.AUDIO_REPORT);
        if (mAudioData != null) {
            String analysisImage = mAudioData.getPicture();
            Log.v(TAG, "@@@AnalysisImage: " + analysisImage);
            if (analysisImage == null) {
                showAnalysisImage(mAudioData.getSound());
            }
            setupMediaPlayerView(mAudioData);
        } else {
            isFromStartScreen = getIntent().getBooleanExtra(
                    Utility.EXTRA_INTENT.FROM_START_SCREEN, false);
            if (isFromStartScreen) {
                onClickShowAudiosList(null);
                findViewById(R.id.ib_record_again).setVisibility(View.GONE);
            } else {
                Log.e(TAG, "AudioData IS NULL");
                finish();
            }
        }
    }

    /**
     * setup media player view controller
     */
    private void setupMediaPlayerView(AudioData audioData) {
        ib_play = (ToggleButton) findViewById(R.id.ib_play);
        ib_stop = (ImageButton) findViewById(R.id.ib_stop);
        String audioFileName = mAudioData.getSound();
        File audioFile = new File(audioFileName);
        if (!audioFile.exists()) {
            ib_play.setEnabled(false);
            ib_stop.setEnabled(false);
            Toast.makeText(AnalysisScreenActivity.this, R.string.file_not_exist, Toast.LENGTH_LONG).show();
            if (mAxisViewController != null) {
                mAxisViewController.resetAnalysisImage();
            }
            return;
        }
        final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMediaPlayerController.onClick(STOP);
                        ib_play.setChecked(false);
                        mAxisViewController.stopSeekbar();
                    }
                });
            }
        };
        PlayCallback playPauseCallback = new PlayCallback() {
            @Override
            public void onPlaying(MediaPlayer mediaPlayer) {
                int duration = mediaPlayer.getDuration();
                Log.d(TAG, "duration=" + duration);
                if (mediaPlayer.isPlaying()) {
                    mAxisViewController.startSeekbar(mCurrentAnalysisTime * 1000, onCompletionListener);
                }
            }

            @Override
            public void onPausing(MediaPlayer mediaPlayer) {
                mAxisViewController.pauseSeekbar();
            }

            @Override
            public void onStop(MediaPlayer mediaPlayer) {
                mAxisViewController.stopSeekbar();
                ib_play.setChecked(false);
            }

        };
        mMediaPlayerController = new MediaPlayerController(this, audioData.getSound());
        mMediaPlayerController.setOnCompletedListener(playPauseCallback, onCompletionListener);
    }

    /**
     * get setting info has store in preferences
     */
    public void readSettingParameters() {
        mCurrentFFT = mSharedPreferences.getInt(Utility.SHARE_PREFERENCES.KEY_FFT_SIZE,
                Utility.FFT_VALUES[0]);
        mCurrentStepWidth = mSharedPreferences.getInt(Utility.SHARE_PREFERENCES.KEY_STEP_WIDTH,
                Utility.STEP_WIDTH_VALUES[0]);
        mCurrentAnalysisTime = mSharedPreferences.getInt(Utility.SHARE_PREFERENCES.KEY_ANALYSIS_TIME,
                Utility.ANALYSIS_TIME_VALUES[2]);
    }

    /**
     * change analysis audio
     *
     * @param audioData AudioData
     */
    public void changeAnalyseAudioFile(AudioData audioData) {
        mAudioData = audioData;
        Bitmap analysisImageBitmap = BitmapFactory.decodeFile(audioData.getPicture());
        String audioFilePath = audioData.getSound();
        mAxisViewController.setBitmap(analysisImageBitmap);
        // refresh analysis layout when select another audio
        tvFileName.setText(audioFilePath.substring(
                audioFilePath.lastIndexOf('/') + 1, audioFilePath.length()));
    }

    /**
     * Button Analysis click event
     *
     * @param v View
     */
    public void onClickAnalysis(View v) {
        if (hasAnalysisAudio) {
            return;
        }
        if (!TextUtils.isEmpty(mAudioData.getSound())) {
            mAxisViewController.resetAnalysisImage();
            showAnalysisImage(mAudioData.getSound());
        }
    }

    /**
     * show analysis image
     */

    private void showAnalysisImage(String filePath) {
        hasAnalysisAudio = true;
        AnalysisAudioTask analysisAudioTask = new AnalysisAudioTask();
        analysisAudioTask.execute(filePath);
    }

    /**
     * save analysis image in folder image
     */
    private void saveAnalysisImageSerial(long audioId, Bitmap bitmap) {
        try {
            String mAudioName = mAudioData.getSound();
            String imageName = mAudioName.substring(0, mAudioName.lastIndexOf('.')) + Utility.FILE_EXT_PNG;
            File imageFile = new File(imageName);
            String analysisImageFilePath = FileUtils.saveAnalysisBitmapToFile(
                    imageFile, bitmap);
            if (analysisImageFilePath != null) {
                int result = databaseHelper.updateImageFileName(audioId,
                        analysisImageFilePath);
                if (result > 0) { //update image file for mAudioData variable
                    mAudioData.setPicture(analysisImageFilePath);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error when create image analysis from bitmap", ex);
        }
    }

    /**
     * Button setting click event
     *
     * @param v View
     */
    public void onClickSetting(View v) {
        SettingDialogFragment dialogFragment = new SettingDialogFragment();
        dialogFragment.setActivityCallback(new SettingDialogFragment.ActivityCallback() {
            @Override
            public void onSettingChanged() {
                readSettingParameters();
            }

            @Override
            public void onAnalysisTimeChanged() {
                //check last analysed bitmap and current analysis time to show correct scale image
                updateAnalysisImageAndTags();

            }
        });
        dialogFragment.setCurrentSetting(mCurrentFFT, mCurrentStepWidth,
                mCurrentAnalysisTime, mSharedPreferences.edit());
        dialogFragment.show(getSupportFragmentManager(), "setting dialog");
    }

    /**
     * Button play or pause click event
     *
     * @param v View
     */
    public void onClickPlayOrPause(View v) {
        //Handle onClick play
        if (mMediaPlayerController != null) {
            if (ib_play.isChecked()) {
                mMediaPlayerController.onClick(PLAY);
            } else {
                mMediaPlayerController.onClick(PAUSE);
            }
        }

    }

    /**
     * Button stop click event
     *
     * @param v View
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalStateException
     * @throws IOException
     */
    public void stopWAV(View v) {
        if (mMediaPlayerController != null) {
            mMediaPlayerController.onClick(STOP);
        }
    }

    /**
     * Button click event to show record audio list
     *
     * @param v View
     */
    public void onClickShowAudiosList(View v) {
        audioListDialog = new AudiosListDialogFragment();
        audioListDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                }
                return true;
            }
        });
        audioListDialog.setOnAudioFileSelectedListener(new AudiosListDialogFragment.OnAudioFileSelectedListener() {
            @Override
            public void onAudioFileSelected(AudioData audioData) {
                mAudioData = audioData;
                if (TextUtils.isEmpty(audioData.getSound())) {
                    Toast.makeText(AnalysisScreenActivity.this, R.string.file_not_exist, Toast.LENGTH_LONG).show();
                    //do nothing
                    findViewById(R.id.ib_play).setEnabled(false);
                    if (mAxisViewController != null) {
                        mAxisViewController.resetAnalysisImage();
                    }
                    return;
                } else {
                    setupMediaPlayerView(mAudioData);
                    if (!TextUtils.isEmpty(audioData.getPicture())) {
                        changeAnalyseAudioFile(audioData);
                        refreshSelectedArea();
                    } else {
                        showAnalysisImage(audioData.getSound());
                    }
                }
            }
        });
        audioListDialog.setCancelable(false);
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.action_show_dialog),
                getString(R.string.label_record_list_dialog),
                CommonUtils.getStringPreferences(AnalysisScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
        audioListDialog.show(getSupportFragmentManager(), "record list");
    }

    /**
     * Button compare click event save average tags in database and go to
     * analysis screen
     *
     * @param v View
     */
    public void onClickCompare(View v) {
        if (mAudioData == null) {
            Log.e(TAG, "onClickCompare audio data is null");
            return;
        }
        if (!TextUtils.isEmpty(mAudioData.getSound())) {
            Intent i = new Intent(getBaseContext(), CompareScreenActivity.class);
            //周波数と周期をAudioFormListに追加して比較画面に遷移 160502 mit
            //i.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, mAudioData);
            i.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, mAudioData);
            i.putExtra(Utility.EXTRA_INTENT.FROM_START_SCREEN, isFromStartScreen);
            startActivityForResult(i, 1);
        }
    }

    /**
     * update analysis image average in table audio_data
     */
    private void updateAudioAverage(long audioId, float avergageHz, float avergageTs, String selectedRect) {
        // update select area into database
        databaseHelper.updateAudioAverages(audioId,
                avergageHz, avergageTs, selectedRect);
    }

    public void onClickShowHint(View v) {
        sendTrackingButtonPress(getString(R.string.label_hint));
        if (UIHelper.canExecuteClickEvent()) {
            //show analysis guide dialog
            AnalysisGuideDialogFragment analysisGuideDialogFragment = AnalysisGuideDialogFragment.newInstance();
            analysisGuideDialogFragment.show(getSupportFragmentManager(), AnalysisGuideDialogFragment.TAG);
            //Tracking Event
            DefaultApplication.getInstance().trackEvent(
                    getString(R.string.action_show_dialog),
                    getString(R.string.label_hint_dialog),
                    CommonUtils.getStringPreferences(AnalysisScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
        }
    }

    /**
     * setOnCompletedListener axis view
     */
    private void initAxisView() {
        tvFileName = (TextView) findViewById(R.id.tvFileName);
        String audioFilePath = "";
        if (checkAudioDataExist()) {
            audioFilePath = mAudioData.getSound();
        }
        if (!TextUtils.isEmpty(audioFilePath)) {
            Log.v(TAG, "MFILEPATH: " + audioFilePath);
            tvFileName.setText(audioFilePath.substring(audioFilePath
                    .lastIndexOf(Utility.ESCAPE_FORWARD_SLASH) + 1));
        }
        axisImageView = (AxisImageView) findViewById(R.id.axisImageView);
        mAxisViewController = new AxisViewController(this, axisImageView, tvFileName);
        mAxisViewController.setAxisHorizontalValues(mCurrentAnalysisTime);
        axisImageView.setOnSelectedAreaListener(new AxisImageView.OnSelectedAreaListener() {
            @Override
            public void updateSelectedArea(PointF point1, PointF point2) {
                // call library to compute average Hz and Times from point1, point2
                float[] result = ionAnalyzeLib.AnalyzeFleqBitmap(axisImageView.getBitmap(), point1.x, 1 - point1.y, point2.x, 1 - point2.y);
                //float[] result = ionAnalyzeLib.AnalyzeFleqBitmap(axisImageView.getBitmap(), 0, 0, 1, 1);
                float averageHz = result[0];
                float averageTs = result[1];
                // save point1, point2 to database
                PointF[] twoPoints = new PointF[]{point1, point2};
                String jsonResult = mGon.toJson(twoPoints, PointF[].class);
                Log.v(TAG, "JSON RESULT: " + jsonResult);
                mAudioData.setAveragePeriod(averageTs);
                mAudioData.setAverageFrequency(averageHz);
                mAudioData.setSelectPoints(jsonResult);
                updateAudioAverage(mAudioData.getId(), averageHz, averageTs, jsonResult);
                DefaultApplication.getInstance().trackEvent(
                        getString(R.string.category_analysis),
                        String.format("x1=%.3f;y1=%.3f;x2=%.3f;y2=%.3f", point1.x, 1 - point1.y, point2.x, 1 - point2.y),
                        CommonUtils.getStringPreferences(AnalysisScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED)); //160407 mit
            }
        });
        refreshSelectedArea();
    }


    /**
     * Re draw selected area if exist data
     */
    private void refreshSelectedArea() {
        if (mAudioData != null) {
            //read json selected area
            String selectedRecF = mAudioData.getSelectPoints();
            if (selectedRecF != null) {
                PointF[] rectF = mGon.fromJson(selectedRecF, PointF[].class);
                axisImageView.setDrawCircle(true);
                axisImageView.setSelectedAreaRect(rectF[0], rectF[1]);
            }
        }
    }

    /**
     * update analysis image and tag when changing setting
     */
    public void updateAnalysisImageAndTags() {
        if (mAxisViewController != null) {
            // update axis view
            mAxisViewController.setAxisHorizontalValues(mCurrentAnalysisTime);
        }
    }

    /**
     * finish activity and back to record screen
     *
     * @param v
     */
    public void onClickRecordAgain(View v) {
        //create dialog confirm record again
        final ConfirmDialogFragment dialogFragment = new ConfirmDialogFragment();
        dialogFragment.setTitle(getString(R.string.record_again_confirm));
        dialogFragment.setMessage(getString(R.string.record_again_message));
        dialogFragment.setOnButtonClickListener(new ConfirmDialogFragment.OnButtonClickListener() {
            @Override
            public void onOK() {
                //Tracking Event
                DefaultApplication.getInstance().trackEvent(getString(R.string.action_button_press),
                        getString(R.string.record_again), CommonUtils.getStringPreferences(AnalysisScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));

                //Start AnalysisScreenActivity activity
                Intent i = new Intent(AnalysisScreenActivity.this, RecordingScreenActivity.class);
                i.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, mAudioData);
                startActivity(i);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onCancel() {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getSupportFragmentManager(), TAG);
        //Tracking Event
        /*DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                getString(R.string.action_show_dialog), getString(R.string.label_record_again_confirm)); */
    }

    //backボタンの禁止 160408 mit
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && !isFromStartScreen) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

//    //AudioFormDataに周波数周期を追加 160502 mit
//    private AudioData addFreqPeriodToAudioData(AudioData aData) {
//        List<AudioFormData> AfDataList = new ArrayList<>();
//        AudioFormData AfFreqData = new AudioFormData();
//        AudioFormData AfPerdData = new AudioFormData();
//        long afdata_id;
//        int freq_num = -1;
//        int period_num = -1;
//        for (int i = 0; i < aData.getListAudioFormData().size(); i++) {
//            if (aData.getListAudioFormData().get(i).getFormid().equals(ICloudParams.frequency))
//                freq_num = i;
//            if (aData.getListAudioFormData().get(i).getFormid().equals(ICloudParams.period))
//                period_num = i;
//        }
//
//        AfDataList = aData.getListAudioFormData();
//        afdata_id = AfDataList.get(0).getAudioData_id();
//        AfFreqData.setAudioData_id(afdata_id);
//        AfFreqData.setFormid(ICloudParams.frequency);
//        AfFreqData.setValue(Float.toString(aData.getAverageFrequency()));
//        if (freq_num < 0) AfDataList.add(AfFreqData);
//        else AfDataList.set(freq_num, AfFreqData);
//
//
//        AfPerdData.setAudioData_id(afdata_id);
//        AfPerdData.setFormid(ICloudParams.period);
//        AfPerdData.setValue(Float.toString(aData.getAveragePeriod()));
//        if (period_num < 0) AfDataList.add(AfPerdData);
//        else AfDataList.set(period_num, AfPerdData);
//
//        aData.setListAudioFormData(AfDataList);
//        return aData;
//    }

    /**
     * Async task process analysis audio and generate analysis bitmap
     */
    public class AnalysisAudioTask extends AsyncTask<String, Integer, Bitmap> {
        int MAX_PROGRESS = 100;
        ProgressDialog dialog;
        String audioFileName = "";

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AnalysisScreenActivity.this);
            dialog.setIndeterminate(false);
            dialog.setMax(MAX_PROGRESS);
            dialog.setProgress(0);
            dialog.setMessage(getResources().getString(R.string.analysing));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
            ionAnalyzeLib.setAnalyzeProgressLister(new IonAnalyzeLib.AnalyzeProgressListener() {
                @Override
                public void getProgress(double v) {
                    int value = (int) (v * MAX_PROGRESS);
                    onProgressUpdate(value);
                }
            });
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (params != null && params.length > 0) {
                String audioFilePath = params[0];
                File audioFile = new File(audioFilePath); // input recorded audio
                // file
                // to here
                if (!(audioFile).exists()) {
                    return null;
                }
                // get setting parameters
                readSettingParameters();
                audioFileName = audioFile.getName();
//                String imageFilePath = audioFile.getParent();
                // using lib to analysis audio and save in sdcard
                Log.v(TAG, "@@@ANALYSIS_SCREEN AUDIO WITH API: " + audioFileName + "|"
                        + mCurrentFFT + "|" + mCurrentAnalysisTime + "|"
                        + mCurrentStepWidth);

                Bitmap analysisBitmap = CommonUtils.analysisAudio(audioFilePath,
                        ionAnalyzeLib, mCurrentFFT, mCurrentAnalysisTime,
                        mCurrentStepWidth);

                if (analysisBitmap != null && isTablet()) { //only tablet save image
                    saveAnalysisImageSerial(mAudioData.getId(), analysisBitmap);
                }
                return analysisBitmap;
            } else {
                Log.e(TAG, "file path is null");
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // dismiss dialog and show analysis image
            Log.i(TAG, "onPostExecute");
            dialog.dismiss();
            if (result != null) {
                mAxisViewController.setAxisHorizontalValues(mCurrentAnalysisTime);
                //解析終了後に周波数/周期を前面にかけた値をセットする 160411 mit
                PointF point1 = new PointF(0, 1);
                PointF point2 = new PointF(1, 0);
                //float[] AnaFlqRes = ionAnalyzeLib.AnalyzeFleqBitmap(axisImageView.getBitmap(), point1.x, 1 - point1.y, point2.x, 1 - point2.y);
                float[] AnaFlqRes = ionAnalyzeLib.getFirstAnalyzevalue();
                float averageHz = AnaFlqRes[0];
                float averageTs = AnaFlqRes[1];
                // save point1, point2 to database
                PointF[] twoPoints = new PointF[]{point1, point2};
                String jsonResult = mGon.toJson(twoPoints, PointF[].class);
                Log.v(TAG, "JSON RESULT: " + jsonResult);
                mAudioData.setAveragePeriod(averageTs);
                mAudioData.setAverageFrequency(averageHz);
                //mAudioData.setSelectPoints(jsonResult);
                mAudioData.setSelectPoints(jsonResult);
                updateAudioAverage(mAudioData.getId(), averageHz, averageTs, jsonResult);
                mAxisViewController.setBitmap(result);
                // Set filename
                mAxisViewController.setFileName(audioFileName);
                axisImageView.setDrawCircle(true);
                axisImageView.setSelectedAreaRect(point1,point2);
            }

        }
    }


}
