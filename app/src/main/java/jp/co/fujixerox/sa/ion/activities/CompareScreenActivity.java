package jp.co.fujixerox.sa.ion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.dialogs.ProcessDetailDialogFragment;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.entities.CatalogList;
import jp.co.fujixerox.sa.ion.fragments.AnalysisViewFragment;
import jp.co.fujixerox.sa.ion.fragments.CatalogViewFragment;
import jp.co.fujixerox.sa.ion.interfaces.ICompareScreenActivity;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadCatalogTask;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * 解析結果とクラウドカテゴリの比較画面
 *
 * @author TrungKD
 */
public class CompareScreenActivity extends AbstractFragmentActivity implements ICompareScreenActivity {
    private static final String TAG = CompareScreenActivity.class.getSimpleName();
    /**
     * {@link CatalogViewFragment}
     */
    private CatalogViewFragment catalogViewFragment;
    /**
     * {@link AnalysisViewFragment}
     */
    private AnalysisViewFragment analysisViewFragment;

    private DownloadCatalogTask downloadCatalogTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "@@@onCreate");
        setContentView(R.layout.activity_compare_screen);
        Intent fromIntent = getIntent();
        //pick audios data from database
        AudioData audioData = fromIntent.getParcelableExtra(Utility.EXTRA_INTENT.AUDIO_REPORT);
        Log.v(TAG, "@@@AnalysisImage: " + audioData.getPicture());
        showAnalysisViewFragment(audioData);
        sendTrackingCompare(this,fromIntent.getBooleanExtra(Utility.EXTRA_INTENT.FROM_START_SCREEN,false));
        //カタログ取得用データ内容コンバート 160419 mit
        audioData = convAudioDataForCatalog(audioData);
        showCatalogViewFragment(audioData);
    }

    @Override
    public void cancelAllBackgroundTask() {
        //cancel get catalog task
        if (downloadCatalogTask != null && !downloadCatalogTask.isCancelled()) {
            downloadCatalogTask.cancel(true);
            Log.e(TAG, "cancelAllBackGroundTask");
            //finish(); //暫定策 160407 mit
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Switch to CatalogViewFragment to show catalogs list
     */
    private void showCatalogViewFragment(AudioData audioData) {
        List<AudioFormData> audioFormDataList = createListParams(audioData.getListAudioFormData());

        downloadCatalogTask = new DownloadCatalogTask(this, audioFormDataList, new AsyncTaskCallback() {
            @Override
            public void onPrepare(PROGRESS_TYPE type) {

            }

            @Override
            public void onSuccess(Object object) {
                CatalogList catalogList = (CatalogList)object;
                List<Catalog> listOfCatalog = catalogList.getCatalogs();
                if (listOfCatalog != null && !listOfCatalog.isEmpty()) {
                    // Create fragment and give it an argument specifying the article it should show
                    catalogViewFragment = CatalogViewFragment.newInstance(listOfCatalog);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    try {
                        transaction.replace(R.id.fragment_catalog, catalogViewFragment);
                        transaction.addToBackStack(null);
                        // Commit the transaction
                        transaction.commit();
                    } catch (Exception e){
                        Log.w(TAG,"Catalog ="+e);
                    }

                } else {
                    Log.d(TAG, "catalog list is empty");
                    findViewById(R.id.prb_loading).setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.tv_load_message)).setText(R.string.catalog_not_found);
                }
            }

            @Override
            public void onFailed(int errorMessageId) {
                CommonUtils.showToast(CompareScreenActivity.this, errorMessageId);
                findViewById(R.id.prb_loading).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.tv_load_message)).setText(R.string.catalog_not_found);
            }

            @Override
            public void onFinish(PROGRESS_TYPE loadingType) {

            }

        });
        downloadCatalogTask.execute();
    }

    /**
     * Create request params list data
     * @param listAudioFormData
     * @return
     */
    private List<AudioFormData> createListParams(List<AudioFormData> listAudioFormData) {
        List<AudioFormData> audioFormData = new ArrayList<>();
        List<String> listParams = Arrays.asList(ICloudParams.catalogParams2);
        for (AudioFormData data :
                listAudioFormData) {
            if (listParams.contains(data.getFormid())) {
                audioFormData.add(data);
            }
        }
        return audioFormData;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return false;
    }

    /**
     * show analysis fragment
     * @param audioData
     */
    private void showAnalysisViewFragment(AudioData audioData) {
        // Create fragment and give it an argument specifying the article it should show
        /*
      {@link AnalysisViewFragment}
     */
        analysisViewFragment = AnalysisViewFragment.newInstance(audioData);
        analysisViewFragment.setFromTopScreenCompare(getIntent().getBooleanExtra(Utility.EXTRA_INTENT.FROM_START_SCREEN,false));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_analysis, analysisViewFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();


    }

    /**
     * Show report information to send
     */
    public void gotoReportInformation() {
        //cancelAllBackgroundTask();  //即レポート作成ボタンを押したときに落ちる回避暫定策 160408 mit
        Intent intent = getIntent();
        intent.setClass(this, ReportDetailScreenActivity.class);
        intent.putExtra(Utility.EXTRA_INTENT.HAS_CATALOG_INFO, true);
        if (catalogViewFragment != null) {
            intent.putStringArrayListExtra(Utility.EXTRA_INTENT.CATALOG_CAUSE_PARTS, catalogViewFragment.getAllCatalogCauseParts());
            intent.putStringArrayListExtra(Utility.EXTRA_INTENT.CATALOG_METHODS, catalogViewFragment.getAllCatalogMethods());
        } else {
            Log.e(TAG, "catalogViewFragment is null, can't get cause parts information");
        }
        startActivityForResult(intent, 1);
    }

    private AudioData convAudioDataForCatalog(AudioData data){
        //機種名


        //用紙サイズ

        //

        return data;
    }

    @Override
    public void onRecordAudioPlaying() {
        if (catalogViewFragment != null) {
            catalogViewFragment.enableButtonPlayCatalog(false);
        }
    }

    @Override
    public void onCatalogAudioPlaying() {
        analysisViewFragment.setEnablePlayButton(false);
    }

    @Override
    public void onAudioPlayingFinish() {
        if (catalogViewFragment != null && catalogViewFragment.hasSound()) {
            catalogViewFragment.enableButtonPlayCatalog(true);
        }
        analysisViewFragment.setEnablePlayButton(true);
    }


    /**
     * Search catalog or Close catalog
     *
     * @param view View
     */
    public void onButtonClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_report) {
            gotoReportInformation();
        } else if (id == R.id.btn_method_detail) {
            showProcessingDetail();
        } else if (id == R.id.btn_method_confirm) {
            showProcessingConfirm();
        }

    }

    /**
     * show processing dialog with detail info
     * button to click on to show dialog
     */
    public void showProcessingDetail() {
        if (catalogViewFragment != null) {
            ProcessDetailDialogFragment dialog = new ProcessDetailDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Utility.EXTRA_INTENT.CATALOG_METHOD_DETAIL, catalogViewFragment.getCurrentCatalog());
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), null);
        } else {
            Log.e(TAG, "catalog not found");
        }
    }

    /**
     * show processing dialog with confirm type
     * button to click on to show dialog
     */

    public void showProcessingConfirm() {
        if (catalogViewFragment != null) {
            ProcessDetailDialogFragment dialog = new ProcessDetailDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Utility.EXTRA_INTENT.CATALOG_METHOD_CONFIRM, catalogViewFragment.getCurrentCatalog());
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), null);
        } else {
            Log.e(TAG, "catalog not found");
        }
    }

    @Override
    public boolean isRecordAudioPlaying() {
        return analysisViewFragment.isPlayingAudio();
    }
}
