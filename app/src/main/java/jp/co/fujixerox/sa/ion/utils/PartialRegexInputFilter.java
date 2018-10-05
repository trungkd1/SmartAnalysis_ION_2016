package jp.co.fujixerox.sa.ion.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.View.OnFocusChangeListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartialRegexInputFilter implements InputFilter {
    private Pattern mPattern;
    private OnInputMatched mCallback;

    public PartialRegexInputFilter(String pattern, OnInputMatched callback) {
        this.mPattern = Pattern.compile(pattern);
        this.mCallback = callback;
    }

    /**
     * Check a string is validate with pattern
     *
     * @param patternString
     * @param textToCheck
     * @return
     */
    public static boolean isValidate(String patternString, String textToCheck) {
        boolean result = false;
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(textToCheck);
        result = matcher.matches();
        return result;
    }

    @Override
    public CharSequence filter(CharSequence source, int sourceStart,
                               int sourceEnd, Spanned destination, int destinationStart,
                               int destinationEnd) {
        String textToCheck = destination.subSequence(0, destinationStart)
                .toString()
                + source.subSequence(sourceStart, sourceEnd)
                + destination.subSequence(destinationEnd, destination.length())
                .toString();
        Matcher matcher = mPattern.matcher(textToCheck);
        // Entered text does not match the pattern
        if (!matcher.matches()) {
            // It does not match partially too
            if (!matcher.hitEnd()) {
                return "";
            } else {
                if (mCallback != null)
                    this.mCallback.onInValid();
            }
        } else {
            if (mCallback != null)
                this.mCallback.onValid();
        }
        return null;
    }

    public interface OnInputMatched extends OnFocusChangeListener {

        boolean isValid = false;

        void onValid();

        void onInValid();

    }
}
