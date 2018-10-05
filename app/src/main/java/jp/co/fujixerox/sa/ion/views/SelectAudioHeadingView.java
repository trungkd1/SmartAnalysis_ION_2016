package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.co.fujixerox.sa.ion.R;

/**
 * 録音ファイル選択ヘッダービュー
 *
 * Created by TrungKD
 */
public class SelectAudioHeadingView extends RelativeLayout {
    private static final int INDEX_OF_ORDER_VIEW = 1;
    protected final String TAG = SelectAudioHeadingView.class.getSimpleName();
    private boolean orderByAscendant = true;
    private TextView tvOrder = null;

    public SelectAudioHeadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * Change icon Up/Down upon sort order
     */
    public void changeIconOrder() {
        if (getVisibility() == View.INVISIBLE) {
            orderByAscendant = true;
        } else {
            orderByAscendant = !orderByAscendant;
        }
        Log.v(TAG, "ORDER BY ASCENDANT: " + orderByAscendant);
        tvOrder.setVisibility(View.VISIBLE);
        if (orderByAscendant) {
            tvOrder.setText(this.getResources().getString(R.string.down));
        } else {
            tvOrder.setText(this.getResources().getString(R.string.up));
        }

    }

    public boolean isActive() {
        return  (getVisibility() == View.VISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        if (tvOrder == null) {
            tvOrder = (TextView) getChildAt(INDEX_OF_ORDER_VIEW);
            if (this.tvOrder == null) {
                Log.e(TAG, "order view is not found");
                return;
            }
        }
        tvOrder.setVisibility(visibility);
    }

    public boolean isOrderByAscendant() {
        return orderByAscendant;
    }

}
