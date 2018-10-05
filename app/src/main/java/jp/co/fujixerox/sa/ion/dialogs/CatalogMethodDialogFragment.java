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
import jp.co.fujixerox.sa.ion.adapters.MethodImagePagerAdapter;
import jp.co.fujixerox.sa.ion.imageloader.ImageLoader;
import jp.co.fujixerox.sa.ion.views.ViewPagerIndicator;

/**
 * Dialog to show method images of catalog
 */
public class CatalogMethodDialogFragment extends DialogFragment {
    public static final String TAG = CatalogMethodDialogFragment.class.getSimpleName();
    private View contentView;
    /**
     * view pager for analysis guide at top screen
     */
    private ViewPagerIndicator pagerMethod;
    /**
     * indicator for view pager top
     */
    private LinearLayout lntIndicatorMethod;
    /**
     * activity
     */
    private Activity mActivity;
    /**
     * ImageLoader for loading image
     */
    private ImageLoader imageLoader;
    /**
     * top analysis guide data
     */
    private List<String> mMethodImageUrlList;

    public void setData(List<String> methodImageUrlList) {
        mMethodImageUrlList = methodImageUrlList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        imageLoader = new ImageLoader(mActivity);
    }

    public static CatalogMethodDialogFragment newInstance(List<String> methodImageUrlList) {
        CatalogMethodDialogFragment fragment = new CatalogMethodDialogFragment();
        fragment.setData(methodImageUrlList);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.dialog_method_image, null);
        builder.setView(contentView);
        Dialog dialog = builder.create();
        initView(contentView);
        setDataToViewPagerIndicator(mMethodImageUrlList, pagerMethod, lntIndicatorMethod);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void initView(View contentView) {
        pagerMethod = (ViewPagerIndicator)contentView.findViewById(R.id.pager_method);
        lntIndicatorMethod = (LinearLayout)contentView.findViewById(R.id.lnt_indicator_method);
        contentView.findViewById(R.id.btn_oke_analysis_guide).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
    }


    private void setDataToViewPagerIndicator(List<String> methodImageUrlList,
                                             ViewPagerIndicator pagerAnalysisGuide, LinearLayout lntIndicatorGuideAnalysis) {
        MethodImagePagerAdapter pagerAdapter = new MethodImagePagerAdapter(mActivity, methodImageUrlList, imageLoader);
        pagerAnalysisGuide.setAdapter(pagerAdapter);
        pagerAnalysisGuide.setPagerIndicator(lntIndicatorGuideAnalysis, methodImageUrlList.size(),
                new ViewPagerIndicator.OnPageIndicatorChangeListener() {
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
