package jp.co.fujixerox.sa.ion.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.AudiosListAdapter;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.dialogs.DeleteConfirmDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.SearchSNDialogFragment;
import jp.co.fujixerox.sa.ion.entities.Report;
import jp.co.fujixerox.sa.ion.services.UploadReportService;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadReportListTask;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.UIHelper;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.SelectAudioHeadingView;

import static jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback.PROGRESS_TYPE;

/**
 * 録音した情報一覧画面
 */
public class RecordListScreenActivity extends AbstractFragmentActivity implements SearchSNDialogFragment.OnSearchSNListener{
    private String TAG = RecordListScreenActivity.class.getSimpleName();
    private AudiosListAdapter mAdapter;
    private View mHeader;
    private boolean isCatalog;
    private DatabaseHelper mDatabaseHelper;
    private RecyclerView mRecyclerView;
    private String mUserId;
    private DownloadReportListTask getReportListTask;
    private boolean loading = false; //use to detect  is or isn't loading from cloud
    private int visibleThreshold = 1; // use threshold to detect end has been reached
    private boolean isOldReport = false;
    private String productName = null;
    private String serialId = null;

    private AudiosListAdapter.AdapterCallback onClickListener = new AudiosListAdapter.AdapterCallback() {
        @Override
        public void onMethodCallback(AudioData audioData) {
            if (UploadReportService.checkSameAudioDataIsSending(audioData)) {
                Toast.makeText(getBaseContext(), R.string.msg_report_is_sending, Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = getIntent();
            i.setClass(getBaseContext(),
                    ReportDetailScreenActivity.class);
            if (audioData != null) {
                i.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, audioData);
            }
            i.putExtra(Utility.EXTRA_INTENT.IS_OLD_REPORT, isOldReport);
            startActivityForResult(i, 1);
            //finish();  //Back to ReportList From ReportDetail 160408 mit
        }

        @Override
        public void onDeleteCallback(final int position, final AudioData audioData) {
            // show dialog confirm
            final DeleteConfirmDialogFragment dialog = new DeleteConfirmDialogFragment();
            dialog.setAudioData(audioData);
            dialog.setOnButtonClickListener(new DeleteConfirmDialogFragment.OnButtonClickListener() {
                @Override
                public void onDelete() {
                    mDatabaseHelper.deleteAudioData(audioData.getId());
                    String audioFile = audioData.getSound();
                    if (audioFile != null && !TextUtils.isEmpty(audioFile)) {
                        FileUtils.deleteFileOrFolder(new File(audioFile));
                    }
                    String imageFile = audioData.getPicture();
                    if (imageFile != null && !TextUtils.isEmpty(imageFile)) {
                        FileUtils.deleteFileOrFolder(new File(imageFile));
                    }
                    mAdapter.removeItemAt(position);
                }

                @Override
                public void onCancel() {
                    dialog.dismiss();
                }
            });
            dialog.show(getSupportFragmentManager(), "delete confirm");
            //Tracking Event
            /* DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                    getString(R.string.action_show_dialog), getString(R.string.delete_report_alert)); レポート削除ダイアログ　160407 mit*/
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list_screen);
        initData();
        setupRecyclerView();
        setupHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Utility.EXTRA_INTENT.AUDIO_REPORT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Init data
     */
    private void initData() {
         mDatabaseHelper = DatabaseHelper.getInstance(this);
        isCatalog = getIntent().getBooleanExtra(
                Utility.EXTRA_INTENT.CATALOG_SCREEN, false);
        mUserId = CommonUtils.getStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_ACCOUNT_AUTHEN);
    }

    /**
     * Setup list to view report list
     */
    private void setupRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setupAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.v(TAG, "@@@onScrolled");
                // load more is only show old report, them get from cloud.
                if (isOldReport && isCanLoadMore(mLayoutManager)) {
                    Log.v(TAG, "@@@Load more");
                    // End has been reached
                    // Do something
                    doGetReportListFromCloud(PROGRESS_TYPE.LOADING_MORE);
                }
            }
        });

    }

    /**
     * check can or not load more
     * @param layoutManager
     * @return
     */
    private boolean isCanLoadMore(LinearLayoutManager layoutManager) {
        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        Log.v(TAG, "@@@Check Load More: " + loading + "|" + totalItemCount + "|" + lastVisibleItem + "|" + visibleThreshold);
        return (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold));
    }

    /**
     * get audios data an set adapter
     */
    private void setupAdapter() {
        List<AudioData> audios;
        if (isCatalog) {
            audios = mDatabaseHelper.getAudiosData(
                    AudioData.COLUMN_RECORD_DATE, -1, false, true);
            // specify an mAdapter
            mAdapter = new AudiosListAdapter(audios, onClickListener, R.layout.row_catalog_list);
            Log.i(TAG, "audios" + audios.toString());
        } else {
            audios = mDatabaseHelper.getAudiosData(
                    AudioData.COLUMN_RECORD_DATE, -1, true, true);
            // specify an mAdapter
            mAdapter = new AudiosListAdapter(audios, onClickListener, R.layout.row_report_list);
            Log.i(TAG, "audios" + audios.toString());
        }
    }

    /**
     * Setup header for data table
     */
    private void setupHeader() {
        mHeader = findViewById(R.id.header);
        if (isCatalog) {
            mHeader.findViewById(R.id.heading_delete_report).setVisibility(View.GONE);
        }
        // set view for order audios
        View.OnClickListener onClickListener = new View.OnClickListener() {
            int id = 0;
            boolean isDesc = true;

            @Override
            public void onClick(View view) {
                id = view.getId();
                Object tag = view.getTag(id);
                if (tag != null) {
                    isDesc = (boolean) tag;
                    isDesc = !isDesc;
                }
                view.setTag(id, isDesc);
                if (id == R.id.heading_audio_file_name) {
                    mAdapter.sortByFormId(ICloudParams.sound, isDesc);
                } else if (id == R.id.heading_image_analysis) {
                    mAdapter.sortByFormId(ICloudParams.picture, isDesc);
                } else if (id == R.id.heading_productname) {
                    mAdapter.sortByFormId(ICloudParams.productname, isDesc);
                } else if (id == R.id.heading_recording_date) {
                    mAdapter.sortByFormId(ICloudParams.record_date, isDesc);
                } else if (id == R.id.heading_serial_number) {
                    mAdapter.sortByFormId(ICloudParams.serialid, isDesc);
                } else if (id == R.id.heading_report_fixresult) {
                    mAdapter.sortByFormId(ICloudParams.result, isDesc);
                }
                setSortIconForHeader((SelectAudioHeadingView) view);
            }
        };
        mHeader.findViewById(R.id.heading_recording_date).setOnClickListener(onClickListener);
        mHeader.findViewById(R.id.heading_serial_number).setOnClickListener(onClickListener);
        mHeader.findViewById(R.id.heading_productname).setOnClickListener(onClickListener);
        mHeader.findViewById(R.id.heading_image_analysis).setOnClickListener(onClickListener);
        mHeader.findViewById(R.id.heading_audio_file_name).setOnClickListener(onClickListener);
        mHeader.findViewById(R.id.heading_report_fixresult).setOnClickListener(onClickListener);
    }

    /**
     * Change sort icon for selected header
     *
     * @param headingModelName SelectAudioHeadingView
     */
    private void setSortIconForHeader(SelectAudioHeadingView headingModelName) {
        deActiveHeadingSelectAudio();
        headingModelName.setVisibility(View.VISIBLE);
        headingModelName.changeIconOrder();
    }

    /**
     * Reset all sort icon view
     */
    private void deActiveHeadingSelectAudio() {
        mHeader.findViewById(R.id.heading_recording_date).setVisibility(View.INVISIBLE);
        mHeader.findViewById(R.id.heading_serial_number).setVisibility(View.INVISIBLE);
        mHeader.findViewById(R.id.heading_productname).setVisibility(View.INVISIBLE);
        mHeader.findViewById(R.id.heading_image_analysis).setVisibility(View.INVISIBLE);
        mHeader.findViewById(R.id.heading_audio_file_name).setVisibility(View.INVISIBLE);
        mHeader.findViewById(R.id.heading_report_fixresult).setVisibility(View.INVISIBLE);
    }

    /**
     * handle event click to view past reports
     *
     * @param v
     */
    public void viewPastReports(View v) {
        int id = v.getId();
        if (id == R.id.btn_get_past_reports) {
            // load all old report with none conditions
            productName = null;
            serialId = null;

            ToggleButton toggleButton = (ToggleButton) v;
            isOldReport = toggleButton.isChecked();
            if (isOldReport) {
                //Tracking Event
                DefaultApplication.getInstance().trackEvent(
                        getString(R.string.action_button_press),
                        getString(R.string.show_past_reports),
                        CommonUtils.getStringPreferences(RecordListScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
                //show cloud report
                if (UIHelper.canExecuteClickEvent()) {
                    doGetReportListFromCloud(PROGRESS_TYPE.FIRST_LOADING);
                }
            } else {
                //show local report
                //Tracking Event
                DefaultApplication.getInstance().trackEvent(
                        getString(R.string.action_button_press),
                        getString(R.string.show_local_reports),
                        CommonUtils.getStringPreferences(RecordListScreenActivity.this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
                setupAdapter();
                mRecyclerView.setAdapter(mAdapter);
            }
        } else {
            Log.e(TAG, "button must be a toggle button");
        }
    }

    /**
     * perform retrieve past reports from cloud
     */
    private void doGetReportListFromCloud(final PROGRESS_TYPE loadingType) {
        //cancel DownloadReportListTask if it's running
        cancelGetReportListTask();
        //create new task
        getReportListTask = new DownloadReportListTask(this, new AsyncTaskCallback() {
            @Override
            public void onPrepare(PROGRESS_TYPE loadingType) {
                //enable loading = true: you're loading from cloud
                loading = true;
                if (loadingType == PROGRESS_TYPE.FIRST_LOADING) {
                    showProcessingDialog();
                } else if (loadingType == PROGRESS_TYPE.LOADING_MORE) {
                    mAdapter.addFooter();
                }
            }

            @Override
            public void onSuccess(Object object) {
                // display data on screen
                List<Report> reportList = (List<Report>)object;
                List<AudioData> audioDataList = convertReportToAudiData(reportList);
                displayOldReports(audioDataList, loadingType);
                isOldReport = true;
            }

            @Override
            public void onFailed(int errorMessageId) {
                // show error message
//                Toast.makeText(RecordListScreenActivity.this, errorMessageId, Toast.LENGTH_LONG).show();
                // show empty list
//                displayOldReports(new ArrayList<AudioData>(), loadingType);
            }

            @Override
            public void onFinish(PROGRESS_TYPE loadingType) {
                //turn of flag set loading = false: you has finish loading from cloud
                loading = false;
                if (loadingType == PROGRESS_TYPE.FIRST_LOADING) {
                    dismissDialog();
                } else if (loadingType == PROGRESS_TYPE.LOADING_MORE) {
                    //remove progress bar
                    mAdapter.removeFooter();
                }

            }
        }, productName, serialId, loadingType);
        //execute task
        getReportListTask.execute(mUserId);

    }

    /**
     * Create audio data list from report list
     * @param reportList
     * @return
     */
    private List<AudioData> convertReportToAudiData(List<Report> reportList) {
        List<AudioData> lstAudio = new ArrayList<>();
        for (Report report :
                reportList) {
            AudioData audioData = new AudioData();
            audioData.setMethod(report.getMethod());
            audioData.setCause(report.getCause());
            audioData.setCasuseJsonForm(report.getCause()); //form view
            audioData.setMethod(report.getMethod());
            audioData.setMethodJsonForm(report.getMethod()); // form view
            audioData.setMethodDetail(report.getMethod_detail());
            audioData.setResult(report.getResult());
//            audioData.setDummy_sound(report.getDummy_sound());
            audioData.setComment(report.getComment());
            audioData.setAverageFrequency(report.getFrequency());
            audioData.setAveragePeriod(report.getPeriod());
            audioData.setRecordDate(report.getRecord_date());
            audioData.setPicture(report.getPicture());
            audioData.setSound(report.getSound());
            audioData.setReportid(report.getReportid());
            List<AudioFormData> audioFormData = new ArrayList<>();
            if (report.getSerialid() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.serialid, report.getSerialid(), null));
            }
            if (report.getProductname() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.productname, report.getProductname(), null));
                String projectGroupName = JsonParser.findProductgroup(report.getProductname(), getAssets());
                if (projectGroupName != null) {
                    audioFormData.add(new AudioFormData(ICloudParams.productgroup, projectGroupName, null));
                }
            }
            if (report.getCondition() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.condition, report.getCondition(), null));
            }
            if (report.getColor() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.color, report.getColor(), null));
            }
            if (report.getOutput_size() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.output_size, report.getOutput_size(), null));
            }
            if (report.getOutput_type() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.output_type, report.getOutput_type(), null));
            }
            if (report.getOutput() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.output, report.getOutput(), null));
            }
            if (report.getAreacode() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.areacode, report.getAreacode(), null));
            }
            if (report.getOriginal_size() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.original_size, report.getOriginal_size(), null));
            }
            if (report.getOriginal_type() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.original_type, report.getOriginal_type(), null));
            }
            if (report.getStart() != null) {
                audioFormData.add(new AudioFormData(ICloudParams.start, report.getStart(), null));
            }
            if (report.getSelect_point() != null) {
                String select_point = JsonParser.gson.toJson(report.getSelect_point());
                audioData.setSelectPoints(select_point);
                audioFormData.add(new AudioFormData(ICloudParams.select_point, select_point, null));
            }
            audioData.setListAudioFormData(audioFormData);
            lstAudio.add(audioData);
        }
        return lstAudio;
    }

    /**
     * cancel DownloadReportListTask if it's running
     */
    private void cancelGetReportListTask() {
        if (getReportListTask != null && getReportListTask.getStatus() == AsyncTask.Status.RUNNING) {
            getReportListTask.cancel(true);
        }
    }

    /**
     * Display old report which was get from cloud
     * @param audios
     * @param loadingType
     */
    private void displayOldReports(List<AudioData> audios, PROGRESS_TYPE loadingType) {
        // display Past Reports
        if (loadingType == PROGRESS_TYPE.FIRST_LOADING) {
            mAdapter = new AudiosListAdapter(audios, onClickListener, R.layout.row_report_list, true);
            mRecyclerView.setAdapter(mAdapter);
        } else if (loadingType == PROGRESS_TYPE.LOADING_MORE){
            mAdapter.addItems(audios);
        }
    }
    private ProgressDialog dialog;
    /**
     * show precessing dialog
     */
    private void showProcessingDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.processing));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * dismiss dialog
     */
    private void dismissDialog() {
        if (dialog != null){
            dialog.dismiss();
        }
    }

    /**
     * show search by serial number dialog, fired by clicking
     * @param view: serial number search button
     */
    public void showSearchSNDialog(View view) {
        SearchSNDialogFragment searchSNDialog = new SearchSNDialogFragment();
        searchSNDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSearchReportBySerialId(String serialId, String productName) {
        this.serialId = serialId;
        this.productName = productName;
        doGetReportListFromCloud( PROGRESS_TYPE.FIRST_LOADING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (!isCatalog) { //report list only
                viewPastReports(findViewById(R.id.btn_get_past_reports));
            }
        }
    }

    /**
     * Receive broadcast and update report list
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utility.EXTRA_INTENT.AUDIO_REPORT.equals(intent.getAction())) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };
}
