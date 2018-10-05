package jp.co.fujixerox.sa.ion.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.imageloader.ImageLoader;
import jp.co.fujixerox.sa.ion.views.AxisImageView;

/**
 * 画像切替アダプター
 * @author TrungKD
 */
public class SwipeImageAdapter extends PagerAdapter {

    private Context context;
    private List<Catalog> catalogs;
    private ImageLoader imageLoader;
    private HashMap<Integer, AxisImageView> pagePreferenceMap;
    public SwipeImageAdapter(Context context, List<Catalog> catalogs, ImageLoader imageLoader) {
        this.context = context;
        this.catalogs = catalogs;
        this.imageLoader = imageLoader;
        this.pagePreferenceMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return catalogs.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AxisImageView axisImageView = (AxisImageView) inflater.inflate(R.layout.axis_image_view, null);
        String imagePath = catalogs.get(position).getSample_picture();
        /*Bitmap bmp = CommonUtils.getBitmapFromImageFilePath(catalogFilePaths.get(position));
        axisImageView.setImageBitmap(bmp);*/
        if (TextUtils.isEmpty(imagePath)) {
            axisImageView.setText(context.getString(R.string.no_image));
        } else {
            imageLoader.displayImage(imagePath, axisImageView);
        }
        container.addView(axisImageView, 0);
        pagePreferenceMap.put(position, axisImageView);
        return axisImageView;
    }

    public AxisImageView getCurrentItem(int position) {
        return pagePreferenceMap.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
        pagePreferenceMap.remove(position);
    }
}
