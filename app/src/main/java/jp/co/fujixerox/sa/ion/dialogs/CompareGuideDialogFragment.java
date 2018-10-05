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
import jp.co.fujixerox.sa.ion.adapters.CompareCatalogGuidePagerAdapter;
import jp.co.fujixerox.sa.ion.views.ViewPagerIndicator;

/**
 * Created by TrungKD
 */
public class CompareGuideDialogFragment extends DialogFragment {
    public static final String TAG = CompareGuideDialogFragment.class.getSimpleName();
    private Activity mActivity;
    /**
     * {@link ViewPagerIndicator}
     */
    private ViewPagerIndicator viewPagerIndicator;
    /**
     * indicator layout
     */
    private LinearLayout lntIndicator;

    private List<String> mCatalogHtmls;

    /**
     * setOnCompletedListener new instance of CompareGuideDialogFragment
     *
     * @param catalogHtmls: list html catalog
     * @return {@link CompareGuideDialogFragment}
     */
    public static CompareGuideDialogFragment newInstance(List<String> catalogHtmls) {
        CompareGuideDialogFragment dialogFragment = new CompareGuideDialogFragment();
        dialogFragment.setData(catalogHtmls);
        return dialogFragment;
    }

    private void setData(List<String> catalogHtmls) {
        mCatalogHtmls = catalogHtmls;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.dialog_catalog_guide, null);
        builder.setView(contentView);
        Dialog dialog = builder.create();
        initView(contentView);
        setAdapterForViewPager();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * setOnCompletedListener view
     *
     * @param contentView: content view
     */
    private void initView(View contentView) {
        viewPagerIndicator = (ViewPagerIndicator) contentView.findViewById(R.id.pager_catalog_guide);
        lntIndicator = (LinearLayout) contentView.findViewById(R.id.lnt_indicator_catalog_guide);
        contentView.findViewById(R.id.btn_oke_catalog_guide)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    /**
     * set adapter for viewPager
     */
    private void setAdapterForViewPager() {
        CompareCatalogGuidePagerAdapter pagerAdapter = new CompareCatalogGuidePagerAdapter(mActivity, mCatalogHtmls);
        viewPagerIndicator.setAdapter(pagerAdapter);
        viewPagerIndicator.setPagerIndicator(lntIndicator, mCatalogHtmls.size(),
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
