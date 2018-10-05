package jp.co.fujixerox.sa.ion.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.AnalysisHintItem;
import jp.co.fujixerox.sa.ion.imageloader.ImageLoader;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.views.TouchImageView;

/**
 * Created by TrungKD.
 */
public class AnalysisHintPagerAdapter extends PagerAdapter {
    private static final String TAG = AnalysisHintPagerAdapter.class.getSimpleName();

    /**
     * list analysis guide
     */
    private List<AnalysisHintItem> analysisGuideItemList;
    /**
     * ImageLoader for loading image from local or internet
     */
    private ImageLoader imageLoader;
    private Context context;

    public AnalysisHintPagerAdapter(Context context, List<AnalysisHintItem> analysisGuideItemList) {
        this.context = context;
        this.analysisGuideItemList = analysisGuideItemList;
        this.imageLoader =  new ImageLoader(context);
    }

    public int getCount() {
        return analysisGuideItemList == null ? 0 : analysisGuideItemList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View parentView = layoutInflater.inflate(R.layout.page_analysis_guide, null);
        TextView tvHintText = (TextView) parentView.findViewById(R.id.txt_hint_text);
        TouchImageView ivHintImage = (TouchImageView) parentView.findViewById(R.id.img_reference_analysis);
        AnalysisHintItem item = analysisGuideItemList.get(position);
        if (item != null) {
            Log.v(TAG, "@@InstantiateItem: " + item.toString());
            tvHintText.setText(item.getKnowhow());
            //TODO  display hint image from url (waiting for cloud API)
//            imageLoader.displayImage(item.getReferenceImageFilePath(), imgReferenceAnalysis);
            //Provisional show hint image from asset image
            String imageFilePath = item.getReferenceImageFilePath();
            Log.v(TAG, "@@ReferenceImageFilePath: " + imageFilePath);
            Bitmap bmpAnalysis = CommonUtils.decodeFile(new File(imageFilePath));
            ivHintImage.setImageBitmap(bmpAnalysis);
        }
        container.addView(parentView, 0);
        return parentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

}
