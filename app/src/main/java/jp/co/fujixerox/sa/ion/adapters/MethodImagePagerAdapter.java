package jp.co.fujixerox.sa.ion.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.imageloader.ImageLoader;
import jp.co.fujixerox.sa.ion.interfaces.IBitmapDisplay;

/**
 * Created by TrungKD
 */
public class MethodImagePagerAdapter extends PagerAdapter {
    public static final String TAG = MethodImagePagerAdapter.class.getSimpleName();

    /**
     * list of method image url
     */
    private List<String> methodImageUrlList;
    /**
     * ImageLoader for loading image from local or internet
     */
    private ImageLoader imageLoader;
    private Context context;

    public MethodImagePagerAdapter(Context context, List<String> methodImageUrlList, ImageLoader imageLoader) {
        this.context = context;
        this.methodImageUrlList = methodImageUrlList;
        this.imageLoader = imageLoader;
    }

    public int getCount() {
        return methodImageUrlList == null ? 0 : methodImageUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View parentView = layoutInflater.inflate(R.layout.page_method_image, null);
        IBitmapDisplay imgView = (IBitmapDisplay) parentView.findViewById(R.id.img_view);
        String url = methodImageUrlList.get(position);
        if (url != null) {
            Log.v(TAG, "@@ReferenceImageFilePath: " + url);
            this.imageLoader.displayImage(url, imgView);
//            Bitmap bmpAnalysis = CommonUtils.decodeFile(new File(imageFilePath));
//            imgReferenceAnalysis.setImageBitmap(bmpAnalysis);
        }
        container.addView(parentView, 0);
        return parentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

}
