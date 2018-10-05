package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;

/**
 * Created by TrungKD
 */
public class ViewPagerIndicator extends ViewPager {
    private LinearLayout pagerIndicator;
    /**
     * list ImageView indicator
     */
    private List<ImageView> ivIndis;
    /**
     * detect number of current selected page
     */
    private int currentSelectedPage = 0;
    private OnPageIndicatorChangeListener mOnPageIndicatorChangeListener;

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPagerIndicator(Context context) {
        super(context);
    }


    public void setPagerIndicator(LinearLayout pagerIndicator, int totalPage, OnPageIndicatorChangeListener onpageIndicatorChangeListener) {
        this.pagerIndicator = pagerIndicator;
        mOnPageIndicatorChangeListener = onpageIndicatorChangeListener;
        //setup layout for pager indicator to determine selected page
        setupPagerIndicator(totalPage);
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mOnPageIndicatorChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                updatePagerIndicator(position);
                mOnPageIndicatorChangeListener.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mOnPageIndicatorChangeListener.onPageScrollStateChanged(state);
            }
        });
    }

    /**
     * update pager indicator set image full for image at current position
     *
     * @param selectedPosition: current selected position
     */
    private void updatePagerIndicator(int selectedPosition) {
        ivIndis.get(currentSelectedPage).setImageResource(R.drawable.indicator_empty);
        ivIndis.get(selectedPosition).setImageResource(R.drawable.indicator_full);
        currentSelectedPage = selectedPosition;
    }

    private void setupPagerIndicator(int length) {
        pagerIndicator.removeAllViews();
        ivIndis = new ArrayList<>();
        currentSelectedPage = 0;
        for (int i = 0; i < length; i++) {
            ImageView imgIndicator = createImageIndicator(i);
            ivIndis.add(imgIndicator);
            pagerIndicator.addView(imgIndicator);
        }
    }

    private ImageView createImageIndicator(int pos) {
        ImageView imgIndicator = new ImageView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 0, 5, 0);
        imgIndicator.setLayoutParams(layoutParams);
        imgIndicator.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgIndicator.setScaleType(ImageView.ScaleType.CENTER);
        if (pos == 0) {
            imgIndicator.setImageResource(R.drawable.indicator_full);
        } else {
            imgIndicator.setImageResource(R.drawable.indicator_empty);
        }
        return imgIndicator;
    }

    public interface OnPageIndicatorChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

}
