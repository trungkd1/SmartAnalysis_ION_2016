package jp.co.fujixerox.sa.ion.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;

/**
 * Created by TrungKD
 */
public class CompareCatalogGuidePagerAdapter extends PagerAdapter {
    public static final String TAG = CompareCatalogGuidePagerAdapter.class.getSimpleName();
    private static String CATALOG_URL_PATTERN = "file:///android_asset/compare_guide/html/%s";
    private Context context;
    private List<String> catalogGuideHtmls;

    public CompareCatalogGuidePagerAdapter(Context _context, List<String> catalogGuideHtmls) {
        context = _context;
        this.catalogGuideHtmls = catalogGuideHtmls;
    }

    public int getCount() {
        return catalogGuideHtmls == null ? 0 : catalogGuideHtmls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View parentView = layoutInflater.inflate(R.layout.page_recording_guide, null);
        WebView webView = (WebView) parentView.findViewById(R.id.wv_catalog_guide);
        String url = catalogGuideHtmls.get(position);
        //機種対応 160406 mit
        url = Build.MANUFACTURER+"/"+ Build.MODEL+"/" + url;
        loadWebView(webView, url);
        container.addView(parentView, 0);
        return parentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /**
     * load html in webview
     * @param webView: WebView
     * @param htmlName: html file name
     */
    private void loadWebView(WebView webView, String htmlName) {
        String url = String.format(CATALOG_URL_PATTERN, htmlName);
        Log.v(TAG, "@@Catalog Guide Url: " + url);
        webView.setPadding(0, 0, 0, 0);
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webView.loadUrl(url);
    }

}
