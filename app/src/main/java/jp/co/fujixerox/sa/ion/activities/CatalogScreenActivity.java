package jp.co.fujixerox.sa.ion.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.dialogs.ProcessDetailDialogFragment;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.fragments.CatalogListFragment;
import jp.co.fujixerox.sa.ion.fragments.CatalogSelectFragment;
import jp.co.fujixerox.sa.ion.fragments.CatalogViewFragment;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogScreenActivity;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD.
 * カタログを取得、表示する画面
 */
public class CatalogScreenActivity extends AbstractFragmentActivity implements ICatalogScreenActivity {
    private static final String TAG = CatalogScreenActivity.class.getSimpleName();
    private boolean isSelectMode;
    private CatalogViewFragment catalogViewFragment;
    private CatalogListFragment catalogListFragment;
    private CatalogSelectFragment catalogSearchFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_screen);
        setFixedOritentationIfNeed();
        findViewById(R.id.bottom_layout).setVisibility(View.GONE);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            Intent intent = getIntent();
            isSelectMode = intent.getBooleanExtra(Utility.EXTRA_INTENT.CATALOG_SCREEN_LISTMODE, false);
            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Create a new Fragment to be placed in the activity layout
            if (isSelectMode) {
                sendTrackingCatalog(this, true); //Track Event 160405 mit
                catalogListFragment = CatalogListFragment.newInstance();
                catalogListFragment.setArguments(intent.getExtras());

                transaction.add(R.id.fragment_container, catalogListFragment);
            } else {
                sendTrackingCatalog(this,false); //Track Event 160405 mit
                catalogSearchFragment = CatalogSelectFragment.newInstance();
                catalogSearchFragment.setArguments(intent.getExtras());
                transaction.add(R.id.fragment_container, catalogSearchFragment);
            }
            transaction.commit();
        }
    }

    private void setFixedOritentationIfNeed(){
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (!isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Search catalog or Close catalog
     *
     * @param view View
     */
    public void onButtonClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_search_catalog) {
            if (isSelectMode) {
                catalogListFragment.searchCatalogs();
            } else {
                catalogSearchFragment.searchCatalogs();
            }
        } else if (id == R.id.btn_finish) {
            finish();
        } else if (id == R.id.btn_method_confirm) {
            showProcessingConfirm();
        } else if (id == R.id.btn_method_detail) {
            showProcessingDetail();
        } else if (id == R.id.btn_create_report) {
            gotoReportInformation();
        }

    }

    /**
     * Show report information to send
     *
     * click to go to ReportInformation
     */
    public void gotoReportInformation() {
        Catalog catalog = catalogViewFragment.getCurrentCatalog();
        if (catalog == null) {
            Log.e(TAG, "Catalog is null");
            return;
        }
        List<AudioFormData> mAudioFormDataList;
        if (isSelectMode) {
            mAudioFormDataList = catalogListFragment.getAudioFormDataList();
        } else {
            mAudioFormDataList = catalogSearchFragment.getAudioFormDataList();
        }
        AudioData audioData = new AudioData();
        audioData.setRecordDate(System.currentTimeMillis());
        audioData.setListAudioFormData(mAudioFormDataList);
        audioData.setCasuseJsonForm(JsonParser.makeJsonStringArray(catalog.getCause()));
        audioData.setCause(JsonParser.makeJsonStringArray(catalog.getCause()));
        audioData.setMethodJsonForm(JsonParser.makeJsonStringArray(catalog.getMethod()));
        audioData.setMethod(JsonParser.makeJsonStringArray(catalog.getMethod()));
        Intent intent = getIntent();
        intent.setClass(this, ReportDetailScreenActivity.class);
        intent.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, audioData);
        startActivityForResult(intent, 1);
        //finish all activity screen after start ReportData screen
        setResult(RESULT_OK);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        findViewById(R.id.bottom_layout).setVisibility(View.GONE);
    }


    /**
     * Show catalog list
     */
    @Override
    public void showCatalogView(List<Catalog> catalogList) {
        if (catalogList == null || catalogList.isEmpty()) {
            Toast.makeText(this, R.string.catalog_not_found, Toast.LENGTH_LONG).show();
        } else {
            // Create fragment and give it an argument specifying the article it should show
            catalogViewFragment = CatalogViewFragment.newInstance(catalogList);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.add(R.id.fragment_container, catalogViewFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
            findViewById(R.id.bottom_layout).setVisibility(View.VISIBLE);
        }
    }

    /**
     * show processing dialog with detail info
     * button to click on to show dialog
     */
    public void showProcessingDetail() {
        ProcessDetailDialogFragment dialog = new ProcessDetailDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Utility.EXTRA_INTENT.CATALOG_METHOD_DETAIL, catalogViewFragment.getCurrentCatalog());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * show processing dialog with confirm type
     * butotn to click on to show dialog
     */

    public void showProcessingConfirm() {
        ProcessDetailDialogFragment dialog = new ProcessDetailDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Utility.EXTRA_INTENT.CATALOG_METHOD_CONFIRM, catalogViewFragment.getCurrentCatalog());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), null);
    }
}