package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 */
public class AreaCellsItemView extends AbstractItemView {
    private static final String TAG = AreaCellsItemView.class.getSimpleName();
    /**
     * Editable or selectable view
     */
    protected GridLayout mGridLayout;
    private ImageView ivPageBackground;
    private static final int MAX_PAGE = 4;
    private static final int MAX_CELL = 6;
    private int currentPage = 0; //[0~3]
    private View selectedCell;
    private List<Value> values;

    public AreaCellsItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AreaCellsItemView(Context context) {
        super(context, null);
    }


    @Override
    public void initView() {
        tvTitle = (TextView) findViewById( R.id.textViewItemLabel);
        ivValidateIcon = (ImageView) findViewById(R.id.ivValidChecked);
        mGridLayout = (GridLayout) findViewById(R.id.gridCell);
        ivPageBackground = (ImageView) findViewById(R.id.img_place);
        View btnNext = findViewById(R.id.img_next_place);
        View btnPrev = findViewById(R.id.img_previous_place);
        btnNext.setOnClickListener(onClickListener);
        btnPrev.setOnClickListener(onClickListener);
        for (int i = 0; i < MAX_CELL; i++) {
            mGridLayout.getChildAt(i).setOnClickListener(onClickListener);
        }
    }

    @Override
    public void setItem(Item item) {
        super.setItem(item);
        tvTitle.setText(item.getLabelForView());
        values = item.getListvalue();
        updateDataView(-1);
    }

    @Override
    public AudioFormData createAudioFormData() {
        if (selectedCell == null) {
            return null;
        } else {
            AudioFormData data = new AudioFormData();
            Value value = (Value)selectedCell.getTag();
            data.setFormid(item.getFormid());
            data.setValue(value.getValue());
            data.setText(value.getText());
            return data;
        }
    }

    @Override
    public void setValue(String value) {
        if (value == null || value.isEmpty()) {
            Log.e(TAG, "Cell value is empty or null");
            return;
        }
        //find page and cell index from value
        int page = 0;
        int cell = -1;

        outerloop:
        for (; page < MAX_PAGE; page++) {
            cell = 0;
            for (int offset = MAX_CELL * page; offset < MAX_CELL * (page + 1); offset++, cell++) {
                if (values.get(offset).getValue().equalsIgnoreCase(value)) {
                    break outerloop;
                }
            }
        }
        currentPage = page;
        updateDataView(cell);
        onValueChangedListener.onValueChanged(item.getFormid(), new Value(value, null));

    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedCell != null) {
                selectedCell.setBackgroundResource(R.drawable.cell_item_bg);
                ivValidateIcon.setVisibility(INVISIBLE);
            }
            int id = v.getId();
            if (id == R.id.img_next_place) {
                showNextPlace();
                onValueChangedListener.onValueChanged(ICloudParams.areacode, null);
            } else if (id == R.id.img_previous_place) {
                showPrevPlace();
                onValueChangedListener.onValueChanged(ICloudParams.areacode, null);
            } else { //cell selected
                selectedCell = v;
                selectedCell.setBackgroundResource(R.drawable.cell_selected);
                ivValidateIcon.setVisibility(VISIBLE);
                Value value = (Value)selectedCell.getTag();
                onValueChangedListener.onValueChanged(ICloudParams.areacode, value);
                //Tracking Event
                /* DefaultApplication.getInstance().trackEvent(getContext().getString(R.string.category_ui_event),
                        getContext().getString(R.string.action_select_record_area), value.getValue()); 160407 mit */
            }
        }
    };

    private void showNextPlace() {
        currentPage = (currentPage+1) < MAX_PAGE ? currentPage+1 : 0;
        Log.v(TAG, "currentPage="+currentPage);
        updateDataView(-1);
    }

    private void showPrevPlace() {
        currentPage = (currentPage-1) >= 0 ? currentPage-1 : MAX_PAGE-1;
        Log.v(TAG, "currentPage="+currentPage);
        updateDataView(-1);
    }

    private void setDataset() {
        int offset = currentPage*MAX_CELL;
        for (int i = 0; i < MAX_CELL; i++) {
            TextView textView = (TextView) mGridLayout.getChildAt(i);
            Value value = values.get(offset+i);
//            textView.setText(value.getText()); //DEBUG
            textView.setTag(value);
        }
    }

    private void updateDataView(int selectedCell) {
        Bitmap bmpPlace = CommonUtils.getBitmapFromAsset(
                Utility.PHOTO_ASSET_PATH,
                Utility.PHOTO_ASSET_FILE_NAME[currentPage],
                getResources().getAssets());
        ivPageBackground.setImageBitmap(bmpPlace);
        setDataset();
        if (selectedCell >= 0 && selectedCell < MAX_CELL) {
            onClickListener.onClick(mGridLayout.getChildAt(selectedCell));
        }
    }

}
