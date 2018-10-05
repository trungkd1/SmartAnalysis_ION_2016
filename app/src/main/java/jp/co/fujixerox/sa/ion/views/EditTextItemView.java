package jp.co.fujixerox.sa.ion.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.activities.ReportDetailScreenActivity;
import jp.co.fujixerox.sa.ion.activities.ScanBarCodeActivity2;
import jp.co.fujixerox.sa.ion.activities.StartScreenActivity;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.imageloader.Utils;
import jp.co.fujixerox.sa.ion.utils.PartialRegexInputFilter;
import jp.co.fujixerox.sa.ion.utils.Utility;
import okhttp3.internal.Util;

/**
 * Created by TrungKD
 */
public class EditTextItemView extends AbstractItemView {
    private static final String TAG = EditTextItemView.class.getSimpleName();
    /**
     * Editable or selectable view
     */
    protected EditText editableView;

    public EditTextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextItemView(Context context) {
        super(context, null);
    }


    @Override
    public void initView() {
        tvTitle = (TextView) findViewById( R.id.textViewItemLabel);
        editableView = (EditText) findViewById(R.id.editTextItem);
        ivValidateIcon = (ImageView) findViewById(R.id.ivTextValidChecked);
        childrenLayout = (LinearLayout) findViewById(R.id.childLayout);
        editableView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });
    }

    @Override
    public void setItem(final Item item) {
        super.setItem(item);
        tvTitle.setText(item.getLabelForView());
        String placeholder = item.getPlaceholder();
        if (placeholder != null) {
            editableView.setHint(placeholder);
        }
        if (Utility.NUMBER.equals(item.getInputtype())) {
            editableView.setInputType(InputType.TYPE_CLASS_NUMBER);
            if(Utility.IS_BARCODE) {
                findViewById(R.id.layout_btn_barcode).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_btn_barcode).setOnClickListener(onClickListener);
            }else {
                findViewById(R.id.layout_btn_barcode).setVisibility(View.GONE);
            }
        }

        String inputPattern = item.getPattern();
        if (inputPattern != null) {
            if (Utility.INPUT_PATTERN.TEXT.name().equals(inputPattern)) {
                editableView.setSingleLine(false);
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(1000);
                editableView.setFilters(filters);
            } else {
                PartialRegexInputFilter.OnInputMatched callback = null;
                if (isRequired()) {
                    callback = new PartialRegexInputFilter.OnInputMatched() {
                        boolean isValid = false;

                        @Override
                        public void onValid() {
                            isValid = true;
                            setValidated(true);
                        }

                        @Override
                        public void onInValid() {
                            isValid = false;
                            setValidated(false);
                        }

                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (editableView.getText().length() > 0) {
                                if (!hasFocus && !isValid) {
                                    setValidated(false);
                                } else if (isValid) {
                                    setValidated(true);
                                }
                            } else {
                                if (isRequired()) {
                                    setValidated(false);
                                }
                            }
                        }
                    };
                }
                InputFilter[] filters = new InputFilter[2];
                filters[0] = new InputFilter.LengthFilter(100);
                filters[1] = new PartialRegexInputFilter(inputPattern, callback);
                editableView.setFilters(filters);
                // tvInputItemView.setOnFocusChangeListener(callback);
            }
        }

        editableView.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (isValidated()) {
//todo                    itemView.clearHighlightInvalid();
//todo                    ivInnerValidChecked.setVisibility(View.VISIBLE);
                } else {
//todo                    ivInnerValidChecked.setVisibility(View.GONE);
                }
                //TODO handle check all item valid
//                if (checkedAllItemValidListener != null) {
//                    checkedAllItemValidListener.OnAllItemValid(mInputItemView.checkAllInputValidate());
//                }
                onValueChangedListener.onValueChanged(item.getFormid(), new Value(editableView.getText().toString(), null));
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });


    }

    @Override
    public AudioFormData createAudioFormData() {
        AudioFormData data = new AudioFormData();
        data.setValue(editableView.getText().toString());
        data.setFormid(item.getFormid());
        data.setText(editableView.getText().toString());
        return data;
    }

    @Override
    public void setValue(String value) {
        editableView.setText(value);
    }


   private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Activity host = (Activity) view.getContext();
            int orientation = getResources().getConfiguration().orientation;
           view.isFocused();
            Intent intent =new Intent(host, ScanBarCodeActivity2.class);
            intent.putExtra(Utility.EXTRA_INTENT.ORIENTATION, orientation);
            host.startActivityForResult(intent,Utility.EXTRA_INTENT.REQUEST_CODE_SCAN_BARCODE);
        }
    };

}
