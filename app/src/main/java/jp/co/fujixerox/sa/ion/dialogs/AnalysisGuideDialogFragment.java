package jp.co.fujixerox.sa.ion.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.AnalysisHintPagerAdapter;
import jp.co.fujixerox.sa.ion.entities.AnalysisHintInfo;
import jp.co.fujixerox.sa.ion.entities.AnalysisHintItem;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadAnalysisHintInfoTask;
import jp.co.fujixerox.sa.ion.views.ViewPagerIndicator;

/**
 * Created by TrungKD._
 * Dialog show analysis hint
 */
public class AnalysisGuideDialogFragment extends DialogFragment {
    public static final String TAG = AnalysisGuideDialogFragment.class.getSimpleName();
    /**
     * view pager for analysis guide at top screen
     */
    private ViewPagerIndicator analysisHintInfoTop;
    /**
     * indicator for view pager top
     */
    private LinearLayout lntIndicatorAnalysisHintTop;
    /**
     * view pager for analysis guide at bottom screen
     */
    private ViewPagerIndicator analysisHintBottom;
    /**
     * indicator for view pager bottom
     */
    private LinearLayout lentIndicatorAnalysisHintBottom;
    /**
     * activity
     */
    private Activity mActivity;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    public static AnalysisGuideDialogFragment newInstance() {
        AnalysisGuideDialogFragment fragment = new AnalysisGuideDialogFragment();
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.dialog_analysis_guide, null);
        builder.setView(contentView);
        Dialog dialog = builder.create();
        initView(contentView);
        doShowHint();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void initView(View contentView) {
        analysisHintInfoTop = (ViewPagerIndicator)contentView.findViewById(R.id.pager_analysis_hint_top);
        lntIndicatorAnalysisHintTop = (LinearLayout)contentView.findViewById(R.id.lnt_indicator_analysis_hint_top);
        analysisHintBottom = (ViewPagerIndicator)contentView.findViewById(R.id.pager_analysis_hint_bottom);
        lentIndicatorAnalysisHintBottom = (LinearLayout)contentView.findViewById(R.id.lnt_indicator_analysis_hint_bottom);
        contentView.findViewById(R.id.btn_oke_analysis_guide).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
    }

    /**
     * Down load hint data from cloud and show in dialog
     */
    public void doShowHint() {
        DownloadAnalysisHintInfoTask getRecordHintInfoTask =
            new DownloadAnalysisHintInfoTask(getActivity(),
                new AsyncTaskCallback() {
                    @Override
                    public void onPrepare(PROGRESS_TYPE type) {
                        //do nothing
                    }

                    @Override
                    public void onSuccess(Object result) {
                        //handle onSuccess
                        AnalysisHintInfo AnalysisHintInfo = (AnalysisHintInfo) result;
                        setHintDataToViewPagerIndicator(AnalysisHintInfo.getDataListTop(), analysisHintInfoTop, lntIndicatorAnalysisHintTop);
                        setHintDataToViewPagerIndicator(AnalysisHintInfo.getDataListBottom(), analysisHintBottom, lentIndicatorAnalysisHintBottom);
                    }

                    @Override
                    public void onFailed(int messageId) {
                        //Do nothing
                    }

                    @Override
                    public void onFinish(PROGRESS_TYPE loadingType) {
                        //do nothing
                    }
                });
        getRecordHintInfoTask.execute();
    }

    /**
     * Set record hint information into Pager
     * @param data
     * @param pagerAnalysisGuide
     * @param lntIndicatorGuideAnalysis
     */
    private void setHintDataToViewPagerIndicator(List<AnalysisHintItem> data, ViewPagerIndicator pagerAnalysisGuide, LinearLayout lntIndicatorGuideAnalysis) {
        AnalysisHintPagerAdapter pagerAdapter = new AnalysisHintPagerAdapter(mActivity, data);
        pagerAnalysisGuide.setAdapter(pagerAdapter);
        pagerAnalysisGuide.setPagerIndicator(lntIndicatorGuideAnalysis, data.size(), new ViewPagerIndicator.OnPageIndicatorChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                //Do nothing
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });
    }
}
