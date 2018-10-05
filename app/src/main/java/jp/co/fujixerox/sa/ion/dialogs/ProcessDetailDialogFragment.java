package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.ImagePagerAdapter;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.ViewPagerIndicator;

/**
 * 処置方法詳細表示、確認方法詳細表示のダイアログ
 * Created by TrungKD on 7/6/2016.
 */

public class ProcessDetailDialogFragment extends DialogFragment {
    public static final String TAG = ProcessDetailDialogFragment.class.getSimpleName();
    private String textContent;
    private List<String> urls;
    private String title;

    public ProcessDetailDialogFragment() {

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_process_detail_layout, null);
        initGUI(rootView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(rootView);
        builder.setTitle(title);
        return builder.create();
    }

    private void initGUI(View rootView) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            Catalog catalog;
            if (bundle.containsKey(Utility.EXTRA_INTENT.CATALOG_METHOD_CONFIRM)) {
                this.title = getString(R.string.title_method_confirm);
                catalog = bundle.getParcelable(Utility.EXTRA_INTENT.CATALOG_METHOD_CONFIRM);
                if (catalog != null) {
                    this.urls = JsonParser.gson.fromJson(catalog.getConfirm_image(), JsonParser.LIST_TYPE);
                    this.textContent = catalog.getConfirm_detail();
                }
            } else if (bundle.containsKey(Utility.EXTRA_INTENT.CATALOG_METHOD_DETAIL)) {
                this.title = getString(R.string.title_method_detail);
                catalog = bundle.getParcelable(Utility.EXTRA_INTENT.CATALOG_METHOD_DETAIL);
                if (catalog != null) {
                    this.urls = JsonParser.gson.fromJson(catalog.getMethod_image(), JsonParser.LIST_TYPE);
                    this.textContent = catalog.getMethod_detail();
                }
            }
//            dummy();
        }
        TextView tvContent = (TextView) rootView.findViewById(R.id.tvContent);
        tvContent.setText(textContent);
        if (urls != null) {
            LinearLayout layoutIndicator = (LinearLayout) rootView.findViewById(R.id.layoutIndicator);
            ViewPagerIndicator vipSlider = (ViewPagerIndicator) rootView.findViewById(R.id.vpiSlider);
            vipSlider.setAdapter(new ImagePagerAdapter(getContext(), getUrls()));
            vipSlider.setPagerIndicator(layoutIndicator, urls.size(), new ViewPagerIndicator.OnPageIndicatorChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    private List<String> getUrls() {
        return urls;
    }

    private void dummy() {
        //dummy test
        urls = new ArrayList<>();
        urls.add("https://fxionmirror.appspot.com/api/download/L2Z4aW9ubWlycm9yLmFwcHNwb3QuY29tL2lvbi9jYXRhbG9nL01hcmJsZV9zYW1wbGUxMS5wbmdfTy03azkxOERqQm9SVHN0TXJoVWN3VG1rMTh1MW10dUc1OVkzUV9uWUd5MD0=/");
        urls.add("https://fxionmirror.appspot.com/api/download/L2Z4aW9ubWlycm9yLmFwcHNwb3QuY29tL2lvbi9jYXRhbG9nL01hcmJsZV9zYW1wbGUxOC5wbmdfalhDSGdScVR5a2txMkxDNHlIWmRFZ2xkQUItQU1iRlZXTUVzRjdHdU5lUT0=/");
        urls.add("https://fxionmirror.appspot.com/api/download/L2Z4aW9ubWlycm9yLmFwcHNwb3QuY29tL2lvbi9jYXRhbG9nL01hcmJsZV9zYW1wbGUxMi5wbmdfWG90dVE5Y1lqQTBCenI3WXhsZW8xU1h1TTJzUTdoa2N3bDBtNDZieGFGST0=/");
        urls.add("https://fxionmirror.appspot.com/api/download/L2Z4aW9ubWlycm9yLmFwcHNwb3QuY29tL2lvbi9jYXRhbG9nL01hcmJsZV9zYW1wbGUwMy5wbmdfVThzZVNpQXRUR3FWU2J5RFE0S0xydkZDWUtNb01adDB0TFdLTWF5RVJHTT0=/");
        textContent = "tes\naa\nbbb\naaa\neeee\nffff\nhhhh\naaa\nmmmm";
    }
}
