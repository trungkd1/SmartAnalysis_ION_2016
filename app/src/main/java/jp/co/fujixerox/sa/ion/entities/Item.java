package jp.co.fujixerox.sa.ion.entities;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.util.List;

import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Json record input form of item
 */
public class Item {
    private final String TAG = Item.class.getSimpleName();

    private String label = "";
    private String formid;
    private String pattern;
    private String inputtype;
    private String placeholder;
    private boolean required;
    private String autocomplete;
    private int srceenType = 0; //0: Report, 1: Catalogy
    private List<Value> listvalue;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFormid() {
        return formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getInputtype() {
        return inputtype;
    }

    public void setInputtype(String inputtype) {
        this.inputtype = inputtype;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }

    public List<Value> getListvalue() {
        return listvalue;
    }

    public void setListvalue(List<Value> listvalue) {
        this.listvalue = listvalue;
    }

    public String toString() {
        return formid;
    }

    /**
     * FUNCTIONS
     */
    public String getTextFromValue(String valueStr) {
        String text = "";
        if (listvalue == null) {
            Log.e(TAG, "listvalue is null");
        } else {
            for (Value valueObj : listvalue) {
                if (valueObj.getValue().equals(valueStr)) {
                    text = valueObj.getText();
                    break;
                }
            }
        }

        return text;
    }

    public Spanned getLabelForView() {
        StringBuilder result = new StringBuilder(getLabel());
        if (isRequired()) {
            result.append(Utility.REQUIRED_MARK);
        }
        Spanned html = Html.fromHtml(result.toString());
        return html;
    }

    public int getSrceenType() {
        return srceenType;
    }

    public void setSrceenType(int srceenType) {
        this.srceenType = srceenType;
    }
}
