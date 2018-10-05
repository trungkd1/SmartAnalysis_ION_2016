package jp.co.fujixerox.sa.ion.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.dialogs.CatalogSelectDialog;
import jp.co.fujixerox.sa.ion.dialogs.ConfirmDialogFragment;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.URL;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * 起動画面
 *
 * @author TrungKD
 */
public class StartScreenActivity extends AbstractFragmentActivity {
    public final String TAG = StartScreenActivity.class.getSimpleName();
    /**
     * boolean check is downloading template
     */
    private boolean isDownloadingTemplate = false;
    /**
     * task download template
     */
    DownloadTemplateAsyncTask downloadTemplateAsyncTask;

//    private AlarmReceiver alarm = new AlarmReceiver();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        alarm.setAlarm(this);
        setContentView(R.layout.activity_start_screen);
        // BACKUP DB テスト用
        DatabaseHelper.getInstance(getBaseContext()).backupDatabase();
        //TODO Download template from cloud
//        downloadTemplate();
        // account 160404 mit
        if(TextUtils.isEmpty(getCurrentSelectAccount(this)) && isTablet()){
            ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
            confirmDialogFragment.setMessage(getString(R.string.first_setting_confirm));
            confirmDialogFragment.setOnButtonClickListener(new ConfirmDialogFragment.OnButtonClickListener() {
                @Override
                public void onOK(){
                    Intent i = new Intent(getBaseContext(), SettingScreenActivity.class);
                    startActivity(i);
                }

                @Override
                public void onCancel() {
                    //do nothing, close dialog
                }
            });
            confirmDialogFragment.show(getSupportFragmentManager(), TAG);
        }
        sendTrackingStartUP(this);
        sendTrackingDevice(this);  //デバイス名とアプリVer.を送信 160617 mit
    }

    /**
     * Translate to Record Screen
     * @param v
     */
    public void gotoRecordingScreen(final View v) {
        ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
        confirmDialogFragment.setMessage(getString(R.string.record_confirm_message));
        confirmDialogFragment.setOnButtonClickListener(new ConfirmDialogFragment.OnButtonClickListener() {
            @Override
            public void onOK() {
                Intent i = new Intent(getBaseContext(), RecordingScreenActivity.class);
                startActivity(i);
            }

            @Override
            public void onCancel() {
                //do nothing, close dialog
            }
        });
        confirmDialogFragment.show(getSupportFragmentManager(), TAG);
        //sendTrackingButtonPress((Button) v);
    }

    /**
     * Translate to Analysis Screen
     * @param v
     */
    public void gotoAnalysisScreen(View v) {
        Intent i = new Intent(getBaseContext(), AnalysisScreenActivity.class);
        i.putExtra(Utility.EXTRA_INTENT.FROM_START_SCREEN, true);
        startActivity(i);
        //sendTrackingButtonPress((Button) v);
    }

    /**
     * Translate to Catalog screen
     * @param v
     */
    public void gotoCatalogScreen(View v) {
        final CatalogSelectDialog catalogSelectDialog = new CatalogSelectDialog();
        catalogSelectDialog.show(getSupportFragmentManager(), TAG);
        catalogSelectDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.catalog_list_btn) {
                    Intent i = new Intent(getBaseContext(), CatalogScreenActivity.class);
                    i.putExtra(Utility.EXTRA_INTENT.CATALOG_SCREEN, true);
                    i.putExtra(Utility.EXTRA_INTENT.CATALOG_SCREEN_LISTMODE, true);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getBaseContext(), CatalogScreenActivity.class);
                    i.putExtra(Utility.EXTRA_INTENT.CATALOG_SCREEN, true);
                    i.putExtra(Utility.EXTRA_INTENT.CATALOG_SCREEN_LISTMODE, false);
                    startActivity(i);
                }
                catalogSelectDialog.dismiss();
            }
        });
        //sendTrackingButtonPress((Button) v);
    }

    /**
     * Translate to Report list screen
     * @param v
     */
    public void gotoReportScreen(View v) {
        Intent i = new Intent(getBaseContext(), RecordListScreenActivity.class);
        startActivity(i);
        //sendTrackingButtonPress((Button) v);
    }

    /**
     * Translate to Setting Screen
     * @param v
     */
    public void gotoSettingScreen(View v){
        Intent i = new Intent(getBaseContext(), SettingScreenActivity.class);
        startActivity(i);
        //sendTrackingButtonPress((Button) v);
    }

    @Override
    public void onBackPressed() {
        //cancel download template task if it is running
        cancelDownloadTaskStillRunning();
        //finish activity
        moveTaskToBack(true);
        finish();
    }

    /**
     * cancel asyncTask
     */
    private void cancelDownloadTaskStillRunning() {
        if (downloadTemplateAsyncTask != null && downloadTemplateAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            downloadTemplateAsyncTask.cancel(true);
        }
    }

    private void downloadTemplate() {
        downloadTemplateAsyncTask = new DownloadTemplateAsyncTask();
        downloadTemplateAsyncTask.execute();
    }

    /**
     * AsyncTask download template
     */
    class DownloadTemplateAsyncTask extends AsyncTask<String, String, String> {
        String tempFileName = "template_temp.json";
        String templateFileName = "report_template.json";

        File tempFile;
        File templateFile;
        protected void onPreExecute() {
            showProgressLoadingTemplate();
            String tempFilePath = FileUtils.getReportTemplateFilename(StartScreenActivity.this, tempFileName);
            tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                FileUtils.deleteFileOrFolder(tempFile);
            }
            String templateFilePath = FileUtils.getReportTemplateFilename(StartScreenActivity.this, templateFileName);
            templateFile = new File(templateFilePath);
        }
        protected String doInBackground(String... params) {
            //1. connect to cloud get template streaming
            //2. write stream to temp file
            //3. check if file is newest when overwrite old file
            String templateUrl = URL.getTemplateUrl();
            InputStream templateStream = CloudConnector.downloadFileTemplate(templateUrl);
            //save template stream to file
            try {
                FileUtils.copyFileUsingFileStreams(templateStream, tempFile);
            } catch (IOException ex) {
                Log.e(TAG, "@@@Error when write stream template to temp file");
            }
            //check if temp file has exist when when write to template file
            if (tempFile.exists()) {
                try {
                    //overwrite template file by temp file
                    FileUtils.copyFile(tempFile, templateFile);
                    //delete temp file
                    FileUtils.deleteFileOrFolder(tempFile);
                } catch (IOException ex) {
                    Log.e(TAG, "@@@Error when copy temp file overwrite template file");
                }
            }

            return Utility.EMPTY_STRING;
        }
        protected void onPostExecute(String result) {
            dismissProgressLoadingTemplate();
        }
    }

    private void showProgressLoadingTemplate() {
        //TODO
    }

    private void dismissProgressLoadingTemplate() {
        //TODO
    }

}
